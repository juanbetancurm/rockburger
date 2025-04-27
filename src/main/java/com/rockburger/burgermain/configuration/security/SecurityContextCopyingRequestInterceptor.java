package com.rockburger.burgermain.configuration.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SecurityContextCopyingRequestInterceptor implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(SecurityContextCopyingRequestInterceptor.class);

    private final ConcurrentHashMap<String, SecurityContext> requestSecurityContexts = new ConcurrentHashMap<>();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("Initializing SecurityContextCopyingRequestInterceptor");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String requestId = generateRequestId((HttpServletRequest)request);

        try {
            SecurityContext context = SecurityContextHolder.getContext();
            Authentication auth = context.getAuthentication();

            if (auth != null) {
                logger.debug("Storing SecurityContext for request: {}", requestId);
                requestSecurityContexts.put(requestId, context);
                RequestContextHolder.setRequestId(requestId);
            }

            chain.doFilter(request, response);
        } finally {
            requestSecurityContexts.remove(requestId);
            RequestContextHolder.clear();
        }
    }

    @Override
    public void destroy() {
        logger.info("Destroying SecurityContextCopyingRequestInterceptor");
        requestSecurityContexts.clear();
    }

    public SecurityContext getSecurityContextForCurrentRequest() {
        String requestId = RequestContextHolder.getRequestId();
        if (requestId != null) {
            SecurityContext context = requestSecurityContexts.get(requestId);
            if (context != null) {
                logger.debug("Retrieved SecurityContext for request: {}", requestId);
                return context;
            }
        }
        return null;
    }

    private String generateRequestId(HttpServletRequest request) {
        return request.getSession().getId() + "-" + System.nanoTime();
    }
}