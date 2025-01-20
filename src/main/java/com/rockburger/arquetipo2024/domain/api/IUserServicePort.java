package com.rockburger.arquetipo2024.domain.api;

import com.rockburger.arquetipo2024.domain.model.UserModel;

public interface IUserServicePort {
    UserModel createUser(UserModel userModel);
    boolean validateCredentials(String email, String password);
}