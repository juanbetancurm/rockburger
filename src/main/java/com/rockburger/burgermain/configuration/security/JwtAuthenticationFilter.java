package com.rockburger.burgermain.configuration.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rockburger.burgermain.domain.api.IJwtServicePort;
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
@Order(1) // Ensure it runs early in the filter chain
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final IJwtServicePort jwtServicePort;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;

    // Paths that don't require authentication
    private static final String[] PUBLIC_PATHS = {
            "/api/auth/",
            "/swagger-ui/",
            "/v3/api-docs/",
            "/swagger-resources/",
            "/webjars/",
            "/error"
    };

    public JwtAuthenticationFilter(IJwtServicePort jwtServicePort,
                                   UserDetailsService userDetailsService) {
        this.jwtServicePort = jwtServicePort;
        this.userDetailsService = userDetailsService;
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
                // Validate JWT token and get user information
                ValidationResult validationResult = validateToken(jwt);

                if (validationResult.isValid()) {
                    UserModel user = validationResult.getUser();
                    logger.debug("User authenticated with role: {}", user.getRole());

                    UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
                    logger.debug("UserDetails loaded with authorities: {}", userDetails.getAuthorities());

                    // Store the JWT token in Authentication credentials
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    jwt, // Store JWT token here for later retrieval
                                    userDetails.getAuthorities()
                            );

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.debug("Set authentication in SecurityContext with token");
                } else {
                    // Handle invalid token
                    handleInvalidToken(request, response, validationResult);
                    return;
                }
            } else {
                // No token provided - let Spring Security handle this
                logger.debug("No JWT token provided for protected resource: {}", requestURI);
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication for request to {}: {}", requestURI, e.getMessage());

            // For API endpoints, return JSON error response
            if (isApiEndpoint(requestURI)) {
                handleAuthenticationError(response, "Authentication processing failed",
                        HttpStatus.INTERNAL_SERVER_ERROR);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Validate JWT token and return validation result
     */
    private ValidationResult validateToken(String jwt) {
        try {
            UserModel user = jwtServicePort.validateAndGetUserFromToken(jwt);
            return new ValidationResult(true, user, null);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            logger.warn("JWT token expired: {}", e.getMessage());
            return new ValidationResult(false, null, "Token has expired");
        } catch (io.jsonwebtoken.JwtException e) {
            logger.warn("JWT token validation failed: {}", e.getMessage());
            return new ValidationResult(false, null, "Invalid token");
        } catch (Exception e) {
            logger.error("Unexpected error during token validation: {}", e.getMessage());
            return new ValidationResult(false, null, "Token validation failed");
        }
    }

    /**
     * Handle invalid token by returning appropriate error response
     */
    private void handleInvalidToken(HttpServletRequest request, HttpServletResponse response,
                                    ValidationResult validationResult) throws IOException {
        String requestURI = request.getRequestURI();

        logger.warn("Invalid token for request to {}: {}", requestURI, validationResult.getErrorMessage());

        // For API endpoints, return JSON error response
        if (isApiEndpoint(requestURI)) {
            HttpStatus status = validationResult.getErrorMessage().contains("expired") ?
                    HttpStatus.UNAUTHORIZED : HttpStatus.UNAUTHORIZED;

            handleAuthenticationError(response, validationResult.getErrorMessage(), status);
        }
        // For non-API endpoints, let Spring Security handle the response
    }

    /**
     * Handle authentication errors with JSON response
     */
    private void handleAuthenticationError(HttpServletResponse response, String message,
                                           HttpStatus status) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("message", message);
        errorResponse.put("path", "authentication");

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
     * Check if the request is for an API endpoint
     */
    private boolean isApiEndpoint(String requestURI) {
        return requestURI.startsWith("/api/") ||
                requestURI.startsWith("/cart/") ||
                requestURI.startsWith("/article/") ||
                requestURI.startsWith("/category/") ||
                requestURI.startsWith("/brand/") ||
                requestURI.startsWith("/purchase/") ||
                requestURI.startsWith("/supply/");
    }

    /**
     * Inner class to hold token validation results
     */
    private static class ValidationResult {
        private final boolean valid;
        private final UserModel user;
        private final String errorMessage;

        public ValidationResult(boolean valid, UserModel user, String errorMessage) {
            this.valid = valid;
            this.user = user;
            this.errorMessage = errorMessage;
        }

        public boolean isValid() { return valid; }
        public UserModel getUser() { return user; }
        public String getErrorMessage() { return errorMessage; }
    }
}