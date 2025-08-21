package com.rockburger.burgermain.domain.api;

import com.rockburger.burgermain.domain.model.JwtModel;
import com.rockburger.burgermain.domain.model.UserModel;

public interface IAuthenticationServicePort {

    /**
     * Existing method for backward compatibility
     * Authenticates user credentials and returns JWT model
     */
    JwtModel authenticate(String email, String password);

    /**
     * Enhanced method that returns authenticated user model
     * This allows access to user details for enhanced token generation
     */
    default UserModel authenticateAndGetUser(String email, String password) {
        // Default implementation for backward compatibility
        // Subclasses should override this method for enhanced functionality
        JwtModel jwtModel = authenticate(email, password);

        // Create a basic UserModel from JWT data
        UserModel userModel = new UserModel();
        userModel.setId(jwtModel.getUserId());
        userModel.setEmail(jwtModel.getEmail());
        userModel.setRole(jwtModel.getRole());

        return userModel;
    }

    /**
     * Method to validate user credentials without generating token
     */
    default boolean validateCredentials(String email, String password) {
        try {
            authenticate(email, password);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Method to get user by email (for token refresh scenarios)
     */
    default UserModel getUserByEmail(String email) {
        throw new UnsupportedOperationException("getUserByEmail method not implemented");
    }
}