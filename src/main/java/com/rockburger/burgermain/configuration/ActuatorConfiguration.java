package com.rockburger.burgermain.configuration;

import com.rockburger.burgermain.adapters.driven.feign.CartFeignClient;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;

@Configuration
public class ActuatorConfiguration {

    @Bean
    public HealthIndicator cartServiceHealth(CartFeignClient cartFeignClient) {
        return new AbstractHealthIndicator() {
            @Override
            protected void doHealthCheck(Health.Builder builder) {
                try {
                    // Call the cart service health endpoint
                    ResponseEntity<String> response = cartFeignClient.healthCheck();
                    if (response.getStatusCode().is2xxSuccessful()) {
                        builder.up().withDetail("message", "Cart service is up and running");
                    } else {
                        builder.down().withDetail("message", "Cart service responded with non-200 status");
                    }
                } catch (Exception e) {
                    builder.down()
                            .withDetail("message", "Cart service is unreachable")
                            .withException(e);
                }
            }
        };
    }
}