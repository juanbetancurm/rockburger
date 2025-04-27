package com.rockburger.burgermain.adapters.driven.feign.config;

import com.rockburger.burgermain.configuration.security.JwtContextHolder;
import com.rockburger.burgermain.configuration.security.SecurityContextCopyingRequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignClientConfig {
    private final SecurityContextCopyingRequestInterceptor securityContextInterceptor;
    private final JwtContextHolder jwtContextHolder;

    public FeignClientConfig(SecurityContextCopyingRequestInterceptor securityContextInterceptor,
                             JwtContextHolder jwtContextHolder) {
        this.securityContextInterceptor = securityContextInterceptor;
        this.jwtContextHolder = jwtContextHolder;
    }

    @Bean
    public JwtTokenInterceptor jwtTokenInterceptor() {
        return new JwtTokenInterceptor(securityContextInterceptor, jwtContextHolder);
    }
}