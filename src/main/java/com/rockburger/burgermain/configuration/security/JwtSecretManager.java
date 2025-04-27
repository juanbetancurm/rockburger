package com.rockburger.burgermain.configuration.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Configuration to ensure consistent JWT secret management across services.
 */
@Configuration
public class JwtSecretManager {
    private static final Logger logger = LoggerFactory.getLogger(JwtSecretManager.class);

    @Value("${jwt.secret}")
    private String configuredSecret;

    private final Environment environment;

    public JwtSecretManager(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public String jwtSecretKey() {
        // First priority: Environment variable
        String envSecret = environment.getProperty("JWT_SECRET");
        if (envSecret != null && !envSecret.isEmpty()) {
            logger.debug("Using JWT secret from environment variable");
            return envSecret;
        }

        // Second priority: Spring configuration
        if (configuredSecret == null || configuredSecret.isEmpty() || configuredSecret.startsWith("${")) {
            logger.error("JWT secret not properly configured!");
            throw new IllegalStateException(
                    "JWT secret not properly configured! Please set jwt.secret in application.yml or JWT_SECRET env variable."
            );
        }

        logger.debug("Using JWT secret from application configuration");
        return configuredSecret;
    }

    /**
     * Validates that the JWT secret is of sufficient length and complexity
     */
    @Bean
    public void validateJwtSecret() {
        String secret = jwtSecretKey();
        if (secret.length() < 64) {
            logger.warn("JWT secret may be too short for optimal security. Recommended length is at least 64 characters.");
        }
    }
}