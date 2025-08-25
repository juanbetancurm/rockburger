package com.rockburger.burgermain.configuration.security;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class FeignJwtInterceptor implements RequestInterceptor {
	private static final Logger logger = LoggerFactory.getLogger(FeignJwtInterceptor.class);

	@Override
	public void apply(RequestTemplate template) {

		logger.info("=== FEIGN INTERCEPTOR DEBUG - URL: {} ===", template.url());
		logger.info("Thread ID: {}", Thread.currentThread().getId());
		logger.info("Thread Name: {}", Thread.currentThread().getName());

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		logger.info("Authentication exists: {}", authentication != null);


		if (authentication != null) {
			logger.info("Authentication type: {}", authentication.getClass().getSimpleName());
			logger.info("Principal type: {}", authentication.getPrincipal() != null ? authentication.getPrincipal().getClass().getSimpleName() : "null");
			logger.info("Credentials exists: {}", authentication.getCredentials() != null);
			logger.info("Credentials is String: {}", authentication.getCredentials() instanceof String);

			if (authentication.getCredentials() instanceof String) {
				String jwtToken = (String) authentication.getCredentials();
				logger.info("JWT token length: {}", jwtToken.length());
				logger.info("JWT starts with 'eyJ': {}", jwtToken.startsWith("eyJ"));

				template.header("Authorization", "Bearer " + jwtToken);
				logger.info("✅ Successfully added JWT token to Feign request");
			} else {
				logger.warn("❌ Credentials is not a String! Actual type: {}",
						authentication.getCredentials() != null ? authentication.getCredentials().getClass().getSimpleName() : "null");
			}
		} else {
			logger.warn("❌ No Authentication found in SecurityContext!");
			logger.warn("This might be due to Resilience4J running in a separate thread context");
		}

		// Log headers being sent
		logger.info("Request headers: {}", template.headers());
		logger.info("=== END FEIGN INTERCEPTOR DEBUG ===");
	}
}
