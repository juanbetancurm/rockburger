package com.rockburger.burgermain.configuration.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rockburger.burgermain.domain.api.IJwtServicePort;
import com.rockburger.burgermain.domain.exception.InvalidTokenException;
import com.rockburger.burgermain.domain.model.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Component
@Order(1)
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final IJwtServicePort jwtServicePort;
    private final UserDetailsService userDetailsService;
    private final JwtContextHolder jwtContextHolder;
    private final ObjectMapper objectMapper;

    // Paths that don't require authentication
    private static final String[] PUBLIC_PATHS = {
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/refresh",
            "/swagger-ui/",
            "/v3/api-docs/",
            "/swagger-resources/",
            "/webjars/",
            "/error",
            "/actuator/health"
    };

    public JwtAuthenticationFilter(IJwtServicePort jwtServicePort,
                                   UserDetailsService userDetailsService,
                                   JwtContextHolder jwtContextHolder) {
        this.jwtServicePort = jwtServicePort;
        this.userDetailsService = userDetailsService;
        this.jwtContextHolder = jwtContextHolder;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        logger.debug("Processing {} request to '{}'", method, requestURI);

        // Skip authentication for public paths
        if (isPublicPath(requestURI)) {
            logger.debug("Skipping authentication for public path: {}", requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = extractJwtFromRequest(request);
            logger.debug("Processing request to '{}' with JWT: {}", requestURI,
                    jwt != null ? "present" : "not present");

            if (jwt != null) {
                TokenValidationResult validationResult = validateToken(jwt);

                if (validationResult.isValid()) {
                    UserModel user = validationResult.getUser();
                    setupAuthentication(request, user, jwt);

                    // Store token in context holder for Feign clients
                    jwtContextHolder.setToken(jwt);

                    logger.debug("User authenticated with role: {}", user.getRole());
                } else {
                    handleInvalidToken(request, response, validationResult);
                    return;
                }
            } else {
                // No token provided for protected resource
                logger.warn("No JWT token provided for protected resource: {}", requestURI);
                handleMissingToken(response, requestURI);
                return;
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication for {}: {}", requestURI, e.getMessage(), e);
            handleAuthenticationError(response, "Authentication processing failed", HttpStatus.INTERNAL_SERVER_ERROR);
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Validate JWT token and return validation result
     */
    private TokenValidationResult validateToken(String jwt) {
        try {
            // Check if token is expired
            if (jwtServicePort.isTokenExpired(jwt)) {
                logger.warn("Token is expired");
                return new TokenValidationResult(false, null, "TOKEN_EXPIRED", "Access token has expired");
            }

            // Validate token and get user
            UserModel user = jwtServicePort.validateAndGetUserFromToken(jwt);

            // Check if token is expiring soon (within 5 minutes)
            if (jwtServicePort.isTokenExpiringSoon(jwt)) {
                logger.info("Token is expiring soon for user: {}", user.getEmail());
                return new TokenValidationResult(true, user, "TOKEN_EXPIRING_SOON", "Token is expiring soon");
            }

            return new TokenValidationResult(true, user, null, null);

        } catch (InvalidTokenException e) {
            logger.warn("Token validation failed: {}", e.getMessage());
            return new TokenValidationResult(false, null, "INVALID_TOKEN", e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during token validation: {}", e.getMessage(), e);
            return new TokenValidationResult(false, null, "VALIDATION_ERROR", "Token validation failed");
        }
    }

    /**
     * Setup Spring Security authentication
     */
    private void setupAuthentication(HttpServletRequest request, UserModel user, String jwt) {
        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
            logger.debug("UserDetails loaded with authorities: {}", userDetails.getAuthorities());

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            jwt, // Store JWT token as credentials
                            userDetails.getAuthorities()
                    );

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.debug("Set authentication in SecurityContext");
        } catch (Exception e) {
            logger.error("Error setting up authentication for user {}: {}", user.getEmail(), e.getMessage());
            throw new RuntimeException("Failed to setup authentication", e);
        }
    }

    /**
     * Handle cases where no token is provided
     */
    private void handleMissingToken(HttpServletResponse response, String requestURI) throws IOException {
        logger.debug("No authentication token provided for: {}", requestURI);
        handleAuthenticationError(response, "Authentication token required", HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle invalid token scenarios
     */
    private void handleInvalidToken(HttpServletRequest request, HttpServletResponse response,
                                    TokenValidationResult validationResult) throws IOException {
        String requestURI = request.getRequestURI();
        String errorCode = validationResult.getErrorCode();
        String errorMessage = validationResult.getErrorMessage();

        logger.warn("Invalid token for request to {}: {} - {}", requestURI, errorCode, errorMessage);

        // Determine appropriate HTTP status and response
        if ("TOKEN_EXPIRED".equals(errorCode)) {
            handleAuthenticationError(response, "Access token has expired", HttpStatus.UNAUTHORIZED, errorCode);
        } else if ("TOKEN_EXPIRING_SOON".equals(errorCode)) {
            // This shouldn't reach here as expiring soon tokens are still valid
            logger.warn("Unexpected expiring soon token in invalid token handler");
            handleAuthenticationError(response, "Token validation issue", HttpStatus.UNAUTHORIZED, errorCode);
        } else {
            handleAuthenticationError(response, "Invalid authentication token", HttpStatus.UNAUTHORIZED, errorCode);
        }
    }

    /**
     * Handle authentication errors with structured response
     */
    private void handleAuthenticationError(HttpServletResponse response, String message,
                                           HttpStatus status) throws IOException {
        handleAuthenticationError(response, message, status, null);
    }

    private void handleAuthenticationError(HttpServletResponse response, String message,
                                           HttpStatus status, String errorCode) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", true);
        errorResponse.put("message", message);
        errorResponse.put("status", status.value());
        errorResponse.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        if (errorCode != null) {
            errorResponse.put("errorCode", errorCode);
        }

        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);

        logger.debug("Sent authentication error response: {}", jsonResponse);
    }

    /**
     * Extract JWT token from Authorization header
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * Check if the request path is public (doesn't require authentication)
     */
    private boolean isPublicPath(String requestURI) {
        for (String publicPath : PUBLIC_PATHS) {
            if (requestURI.startsWith(publicPath)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Inner class to hold token validation results
     */
    private static class TokenValidationResult {
        private final boolean valid;
        private final UserModel user;
        private final String errorCode;
        private final String errorMessage;

        public TokenValidationResult(boolean valid, UserModel user, String errorCode, String errorMessage) {
            this.valid = valid;
            this.user = user;
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
        }

        public boolean isValid() {
            return valid;
        }

        public UserModel getUser() {
            return user;
        }

        public String getErrorCode() {
            return errorCode;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}