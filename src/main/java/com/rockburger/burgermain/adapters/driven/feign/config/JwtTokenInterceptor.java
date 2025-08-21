package com.rockburger.burgermain.adapters.driven.feign.config;

import com.rockburger.burgermain.configuration.security.JwtContextHolder;
import com.rockburger.burgermain.configuration.security.SecurityContextCopyingRequestInterceptor;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Intercepts Feign client requests to add the JWT token from the stored SecurityContext.
 */
public class JwtTokenInterceptor implements RequestInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenInterceptor.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final SecurityContextCopyingRequestInterceptor securityContextInterceptor;
    private final JwtContextHolder jwtContextHolder;

    public JwtTokenInterceptor(SecurityContextCopyingRequestInterceptor securityContextInterceptor,
                               JwtContextHolder jwtContextHolder) {
        this.securityContextInterceptor = securityContextInterceptor;
        this.jwtContextHolder = jwtContextHolder;
    }

    @Override
    public void apply(RequestTemplate requestTemplate) {
        String token = null;
        
        // 1. Try ThreadLocal first (highest priority)
        token = jwtContextHolder.getToken();
        if (token != null) {
            logger.debug("Token retrieved from ThreadLocal for: {}", requestTemplate.url());
        }
        
        // 2. Try current SecurityContext
        if (token == null) {
            token = getTokenFromCurrentSecurityContext();
            if (token != null) {
                logger.debug("Token retrieved from SecurityContext for: {}", requestTemplate.url());
                // Store for future use in ThreadLocal
                jwtContextHolder.setToken(token);
            }
        }
        
        // 3. Try stored request context as fallback
        if (token == null) {
            token = getTokenFromStoredSecurityContext();
            if (token != null) {
                logger.debug("Token retrieved from stored context for: {}", requestTemplate.url());
                // Store for future use in ThreadLocal
                jwtContextHolder.setToken(token);
            }
        }
        
        // 4. Apply token to request
        if (token != null && !token.isEmpty()) {
            requestTemplate.header(AUTHORIZATION_HEADER, BEARER_PREFIX + token);
            logger.debug("JWT token added to Feign request to: {}", requestTemplate.url());
        } else {
            logger.error("No JWT token available for Feign request to: {} - This will likely result in 401 Unauthorized", requestTemplate.url());
        }
    }

    private String getTokenFromCurrentSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getCredentials() instanceof String) {
            return (String) authentication.getCredentials();
        }
        return null;
    }

    private String getTokenFromStoredSecurityContext() {
        SecurityContext storedContext = securityContextInterceptor.getSecurityContextForCurrentRequest();
        if (storedContext != null && storedContext.getAuthentication() != null &&
                storedContext.getAuthentication().getCredentials() instanceof String) {
            return (String) storedContext.getAuthentication().getCredentials();
        }
        return null;
    }
}