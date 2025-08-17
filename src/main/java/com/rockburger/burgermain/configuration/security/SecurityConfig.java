package com.rockburger.burgermain.configuration.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsConfigurationSource corsConfigurationSource;
    private final ObjectMapper objectMapper;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          CorsConfigurationSource corsConfigurationSource) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.corsConfigurationSource = corsConfigurationSource;
        this.objectMapper = new ObjectMapper();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf().disable()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()

                // Configure exception handling
                .exceptionHandling()
                .authenticationEntryPoint(customAuthenticationEntryPoint())
                .accessDeniedHandler(customAccessDeniedHandler())
                .and()

                // Configure authorization rules
                .authorizeRequests()

                // Public endpoints
                .antMatchers("/error").permitAll()
                .antMatchers("/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**").permitAll()
                .antMatchers("/api/auth/**").permitAll()
                .antMatchers("/actuator/health").permitAll()

                // Admin/Auxiliar endpoints
                .antMatchers("/purchase/**")
                .hasAnyRole("auxiliar", "admin")
                .antMatchers("/supply/**")
                .hasAnyRole("auxiliar", "admin")
                .antMatchers("/category/**", "/brand/**")
                .hasAnyRole("admin", "auxiliar")

                // Article endpoints - more granular control
                .antMatchers("GET", "/article/**")
                .hasAnyRole("admin", "auxiliar", "client")
                .antMatchers("POST", "/article/**")
                .hasAnyRole("admin", "auxiliar")
                .antMatchers("PUT", "/article/**")
                .hasAnyRole("admin", "auxiliar")
                .antMatchers("DELETE", "/article/**")
                .hasAnyRole("admin")

                // Cart endpoints - available to all authenticated users
                .antMatchers("/cart/**")
                .hasAnyRole("auxiliar", "admin", "client")

                // All other requests require authentication
                .anyRequest().authenticated()
                .and()

                // Add JWT filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Custom authentication entry point for handling authentication failures
     */
    @Bean
    public AuthenticationEntryPoint customAuthenticationEntryPoint() {
        return new CustomAuthenticationEntryPoint();
    }

    /**
     * Custom access denied handler for handling authorization failures
     */
    @Bean
    public AccessDeniedHandler customAccessDeniedHandler() {
        return new CustomAccessDeniedHandler();
    }

    /**
     * Custom AuthenticationEntryPoint implementation
     */
    private class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
        @Override
        public void commence(HttpServletRequest request, HttpServletResponse response,
                             AuthenticationException authException) throws IOException, ServletException {

            String requestURI = request.getRequestURI();
            String method = request.getMethod();

            logger.warn("Authentication failed for {} {} from IP {}: {}",
                    method, requestURI, getClientIP(request), authException.getMessage());

            // Determine the type of authentication failure
            String errorMessage = determineAuthenticationErrorMessage(authException, request);
            String errorCode = determineAuthenticationErrorCode(authException, request);

            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, errorMessage, errorCode, requestURI);
        }

        private String determineAuthenticationErrorMessage(AuthenticationException authException,
                                                           HttpServletRequest request) {
            String message = authException.getMessage();

            if (message != null) {
                if (message.contains("expired")) {
                    return "Your session has expired. Please log in again.";
                } else if (message.contains("invalid")) {
                    return "Invalid authentication credentials.";
                } else if (message.contains("malformed")) {
                    return "Malformed authentication token.";
                }
            }

            // Check if no token was provided
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || authHeader.trim().isEmpty()) {
                return "Authentication required. Please provide valid credentials.";
            }

            return "Authentication failed. Please check your credentials.";
        }

        private String determineAuthenticationErrorCode(AuthenticationException authException,
                                                        HttpServletRequest request) {
            String message = authException.getMessage();

            if (message != null) {
                if (message.contains("expired")) {
                    return "TOKEN_EXPIRED";
                } else if (message.contains("invalid")) {
                    return "INVALID_TOKEN";
                } else if (message.contains("malformed")) {
                    return "MALFORMED_TOKEN";
                }
            }

            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || authHeader.trim().isEmpty()) {
                return "TOKEN_REQUIRED";
            }

            return "AUTHENTICATION_FAILED";
        }
    }

    /**
     * Custom AccessDeniedHandler implementation
     */
    private class CustomAccessDeniedHandler implements AccessDeniedHandler {
        @Override
        public void handle(HttpServletRequest request, HttpServletResponse response,
                           org.springframework.security.access.AccessDeniedException accessDeniedException)
                throws IOException, ServletException {

            String requestURI = request.getRequestURI();
            String method = request.getMethod();
            String userPrincipal = request.getUserPrincipal() != null ?
                    request.getUserPrincipal().getName() : "anonymous";

            logger.warn("Access denied for user {} to {} {} from IP {}: {}",
                    userPrincipal, method, requestURI, getClientIP(request),
                    accessDeniedException.getMessage());

            String errorMessage = "Access denied. You don't have permission to access this resource.";
            String errorCode = "ACCESS_DENIED";

            // Provide more specific error messages based on the endpoint
            if (requestURI.startsWith("/admin/")) {
                errorMessage = "Administrator privileges required for this operation.";
                errorCode = "ADMIN_REQUIRED";
            } else if (requestURI.startsWith("/supply/") || requestURI.startsWith("/purchase/")) {
                errorMessage = "Staff privileges required for this operation.";
                errorCode = "STAFF_REQUIRED";
            }

            sendErrorResponse(response, HttpStatus.FORBIDDEN, errorMessage, errorCode, requestURI);
        }
    }

    /**
     * Send standardized error response
     */
    private void sendErrorResponse(HttpServletResponse response, HttpStatus status,
                                   String message, String errorCode, String path) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("message", message);
        errorResponse.put("errorCode", errorCode);
        errorResponse.put("path", path);
        errorResponse.put("service", "burger-main");

        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);

        logger.debug("Sent security error response: {}", jsonResponse);
    }

    /**
     * Get client IP address from request
     */
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }

        return request.getRemoteAddr();
    }
}