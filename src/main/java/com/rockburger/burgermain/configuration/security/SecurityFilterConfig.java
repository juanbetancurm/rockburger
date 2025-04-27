package com.rockburger.burgermain.configuration.security;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityFilterConfig {

    @Bean
    public FilterRegistrationBean<SecurityContextCopyingRequestInterceptor> securityContextFilterRegistration(
            SecurityContextCopyingRequestInterceptor filter) {
        FilterRegistrationBean<SecurityContextCopyingRequestInterceptor> registrationBean =
                new FilterRegistrationBean<>(filter);
        registrationBean.setOrder(-101); // Set to run before Spring Security filters
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(
            JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registrationBean =
                new FilterRegistrationBean<>(filter);
        registrationBean.setEnabled(false); // Disable duplicate registration, Spring Security will handle it
        return registrationBean;
    }
}