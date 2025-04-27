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
        // First try to get token from thread-local storage
        String token = jwtContextHolder.getToken();

        // If not found, try to get from current thread's SecurityContext
        if (token == null) {
            token = getTokenFromCurrentSecurityContext();
        }

        // If still not found, try from the stored request context
        if (token == null) {
            token = getTokenFromStoredSecurityContext();
        }

        if (token != null && !token.isEmpty()) {
            logger.debug("Adding JWT token to Feign request to: {}", requestTemplate.url());
            requestTemplate.header(AUTHORIZATION_HEADER, BEARER_PREFIX + token);

            // Store for future use
            jwtContextHolder.setToken(token);
        } else {
            logger.warn("No JWT token found for Feign request to: {}", requestTemplate.url());
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