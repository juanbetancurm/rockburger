package com.rockburger.burgermain.configuration.security;

import com.rockburger.burgermain.domain.api.IJwtServicePort;
import com.rockburger.burgermain.domain.model.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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

@Component
@Order(1) // Add this annotation with a low value to ensure it runs early
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final IJwtServicePort jwtServicePort;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(IJwtServicePort jwtServicePort,
                                   UserDetailsService userDetailsService) {
        this.jwtServicePort = jwtServicePort;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String authHeader = request.getHeader("Authorization");

        // ADD THIS LOGGING BLOCK
        logger.info("=== JWT FILTER DEBUG - REQUEST: {} ===", requestURI);
        logger.info("Authorization header present: {}", authHeader != null);
        if (authHeader != null) {
            logger.info("Authorization header length: {}", authHeader.length());
            logger.info("Authorization header starts with 'Bearer ': {}", authHeader.startsWith("Bearer "));
        }

        try {
            String jwt = extractJwtFromRequest(request);
            logger.debug("Processing request to '{}' with JWT: {}", request.getRequestURI(),
                    jwt != null ? "present" : "not present");

            if (jwt != null) {
                // ADD THIS LOG
                logger.info("Extracted JWT token length: {}", jwt.length());
                logger.info("JWT starts with 'eyJ': {}", jwt.startsWith("eyJ"));

                // Validate JWT token and get user information
                UserModel user = jwtServicePort.validateAndGetUserFromToken(jwt);
                logger.debug("User authenticated with role: {}", user.getRole());

                // ADD THIS LOG
                logger.info("Successfully validated JWT and extracted user: {}", user.getEmail());

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

                // ADD THIS VERIFICATION LOG
                Authentication savedAuth = SecurityContextHolder.getContext().getAuthentication();
                logger.info("✅ Authentication saved with credentials type: {}",
                        savedAuth.getCredentials() != null ? savedAuth.getCredentials().getClass().getSimpleName() : "null");
                logger.info("✅ Credentials is String: {}", savedAuth.getCredentials() instanceof String);
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e.getMessage());
        }
        logger.info("=== END JWT FILTER DEBUG ===");
        filterChain.doFilter(request, response);
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}