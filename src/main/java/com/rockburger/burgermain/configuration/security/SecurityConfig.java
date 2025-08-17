package com.rockburger.burgermain.configuration.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          CorsConfigurationSource corsConfigurationSource) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        logger.info("Configuring security filter chain");

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf().disable()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                // Public endpoints
                .antMatchers("/error").permitAll()
                .antMatchers("/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**",
                        "/api/auth/**").permitAll()
                .antMatchers("/actuator/health").permitAll()

                // Purchase endpoints - auxiliar and admin can access
                .antMatchers("/purchase/**")
                .hasAnyRole("auxiliar", "admin")

                // Supply endpoints - auxiliar and admin can access
                .antMatchers("/supply/**")
                .hasAnyRole("auxiliar", "admin")

                // Category, brand, article endpoints - auxiliar and admin can read, admin can modify
                // Note: Specific method-level security is handled by @PreAuthorize annotations
                .antMatchers("/category/**", "/brand/**")
                .hasAnyRole("admin", "auxiliar")
                .antMatchers("/article/**")
                .hasAnyRole("admin", "auxiliar")

                // Cart and shopping cart endpoints - client, auxiliar, and admin can access
                .antMatchers("/cart/**", "/shopping-cart/**")
                .hasAnyRole("auxiliar", "admin", "client")

                // Default: all other requests require authentication
                .anyRequest().authenticated()
                .and()

                // Custom access denied handler
                .exceptionHandling()
                .accessDeniedHandler(accessDeniedHandler())
                .and()

                // Add JWT filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return new CustomAccessDeniedHandler();
    }

    /**
     * Custom Access Denied Handler to provide better error messages and logging
     */
    public static class CustomAccessDeniedHandler implements AccessDeniedHandler {
        private static final Logger logger = LoggerFactory.getLogger(CustomAccessDeniedHandler.class);

        @Override
        public void handle(HttpServletRequest request, HttpServletResponse response,
                           AccessDeniedException accessDeniedException) throws IOException {

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth != null ? auth.getName() : "anonymous";
            String authorities = auth != null ? auth.getAuthorities().toString() : "none";
            String clientIp = getClientIpAddress(request);

            logger.warn("Access denied for user {} to {} from IP {}: {}. User authorities: {}",
                    username,
                    request.getMethod() + " " + request.getRequestURI(),
                    clientIp,
                    accessDeniedException.getMessage(),
                    authorities);

            // Set response headers
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            // Create error response
            String errorResponse = String.format(
                    "{" +
                            "\"path\":\"%s\"," +
                            "\"service\":\"burger-main\"," +
                            "\"errorCode\":\"ACCESS_DENIED\"," +
                            "\"error\":\"Forbidden\"," +
                            "\"message\":\"Access denied. You don't have permission to access this resource.\"," +
                            "\"timestamp\":\"%s\"," +
                            "\"status\":403" +
                            "}",
                    request.getRequestURI(),
                    java.time.LocalDateTime.now().toString()
            );

            logger.debug("Sent security error response: {}", errorResponse);
            response.getWriter().write(errorResponse);
        }

        private String getClientIpAddress(HttpServletRequest request) {
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }

            String xRealIp = request.getHeader("X-Real-IP");
            if (xRealIp != null && !xRealIp.isEmpty()) {
                return xRealIp;
            }

            return request.getRemoteAddr();
        }
    }
}