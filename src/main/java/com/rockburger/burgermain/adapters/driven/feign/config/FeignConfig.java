package com.rockburger.burgermain.adapters.driven.feign.config;

import com.rockburger.burgermain.configuration.security.JwtContextHolder;
import com.rockburger.burgermain.configuration.security.SecurityContextCopyingRequestInterceptor;
import feign.Logger;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    /**
     * Configure JWT token interceptor for all Feign clients
     */
    @Bean
    public RequestInterceptor jwtTokenInterceptor(SecurityContextCopyingRequestInterceptor securityInterceptor,
                                                  JwtContextHolder jwtContextHolder) {
        return new JwtTokenInterceptor(securityInterceptor, jwtContextHolder);
    }

    /**
     * Configure Feign logging level for debugging
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL; // Log all request and response details
    }

    /**
     * Ensure SecurityContextCopyingRequestInterceptor is available as a bean
     */
    @Bean
    public SecurityContextCopyingRequestInterceptor securityContextCopyingRequestInterceptor() {
        return new SecurityContextCopyingRequestInterceptor();
    }
}
