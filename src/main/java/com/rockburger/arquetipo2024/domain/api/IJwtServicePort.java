package com.rockburger.arquetipo2024.domain.api;

import com.rockburger.arquetipo2024.domain.model.JwtModel;
import com.rockburger.arquetipo2024.domain.model.UserModel;

public interface IJwtServicePort {
    JwtModel generateToken(UserModel userModel);
    UserModel validateAndGetUserFromToken(String token);
}