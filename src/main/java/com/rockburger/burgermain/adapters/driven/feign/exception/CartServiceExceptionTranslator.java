package com.rockburger.burgermain.adapters.driven.feign.exception;

import com.rockburger.burgermain.domain.exception.*;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * Translates exceptions from the cart service to domain exceptions in the main application.
 * This ensures consistent error handling across microservices.
 */
@Component
public class CartServiceExceptionTranslator {
    private static final Logger logger = LoggerFactory.getLogger(CartServiceExceptionTranslator.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Translates a FeignException to an appropriate domain exception.
     */
    public RuntimeException translate(FeignException exception) {
        int status = exception.status();
        String responseBody = exception.contentUTF8();

        logger.debug("Translating FeignException with status {} and body: {}", status, responseBody);

        try {
            // Parse error response
            Map<String, Object> errorResponse = objectMapper.readValue(responseBody, Map.class);
            String errorMessage = (String) errorResponse.getOrDefault("message", "Unknown cart service error");

            // Map HTTP status codes to appropriate domain exceptions
            switch (status) {
                case 400:
                    return new InvalidParameterException(errorMessage);
                case 404:
                    return new ResourceNotFoundException(errorMessage);
                case 409:
                    if (errorMessage.contains("duplicate")) {
                        return new DuplicateArticleException(errorMessage);
                    } else if (errorMessage.contains("concurrent")) {
                        return new ConcurrentModificationException(errorMessage);
                    }
                    return new InvalidCartOperationException(errorMessage);
                case 401:
                    return new InvalidCredentialsException(errorMessage);
                case 403:
                    return new AccessDeniedException(errorMessage);
                default:
                    return new RuntimeException("Cart service error: " + errorMessage);
            }
        } catch (Exception e) {
            logger.error("Error parsing cart service error response", e);
            return new RuntimeException("Unknown cart service error: " + exception.getMessage());
        }
    }

    /**
     * Domain specific exception class for cart operations
     */
    public static class InvalidCartOperationException extends RuntimeException {
        public InvalidCartOperationException(String message) {
            super(message);
        }
    }

    /**
     * Domain specific exception class for duplicate articles
     */
    public static class DuplicateArticleException extends RuntimeException {
        public DuplicateArticleException(String message) {
            super(message);
        }
    }

    /**
     * Domain specific exception for access denial
     */
    public static class AccessDeniedException extends RuntimeException {
        public AccessDeniedException(String message) {
            super(message);
        }
    }
}