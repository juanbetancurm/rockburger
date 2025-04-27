package com.rockburger.burgermain.configuration.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Configuration class for JWT secret management.
 * Handles environment variable overrides and ensures secret is properly configured.
 */
@Configuration
public class JwtSecretConfig {
    private static final Logger logger = LoggerFactory.getLogger(JwtSecretConfig.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    private final Environment environment;

    public JwtSecretConfig(Environment environment) {
        this.environment = environment;
    }

    @Bean
    public String jwtSecretKey() {
        // Check for environment-specific override
        String envSecret = environment.getProperty("JWT_SECRET");
        if (envSecret != null && !envSecret.isEmpty()) {
            logger.debug("Using JWT secret from environment variable");
            return envSecret;
        }

        // Fall back to configured value
        if (jwtSecret == null || jwtSecret.isEmpty() || jwtSecret.startsWith("${")) {
            logger.error("JWT secret not properly configured!");
            throw new IllegalStateException(
                    "JWT secret not properly configured! Please set jwt.secret in config or JWT_SECRET env variable."
            );
        }

        logger.debug("Using JWT secret from application configuration");
        return jwtSecret;
    }
}