package com.rockburger.burgermain.configuration.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Enable credentials for JWT authentication
        configuration.setAllowCredentials(true);

        // Specify exact frontend origins instead of wildcard "*"
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",  // Vite development server
                "http://localhost:3000",  // Alternative React dev server
                "http://localhost:8080"   // Alternative frontend port
                // Add production URLs here:
                // "https://your-production-domain.com"
        ));

        // Allow all headers
        configuration.addAllowedHeader("*");

        // Allow specific HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));

        // Configure how long preflight requests can be cached
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public CorsFilter corsFilter() {
        return new CorsFilter(corsConfigurationSource());
    }
}