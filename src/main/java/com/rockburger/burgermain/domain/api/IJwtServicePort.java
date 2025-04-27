package com.rockburger.burgermain.domain.api;

import com.rockburger.burgermain.domain.model.JwtModel;
import com.rockburger.burgermain.domain.model.UserModel;

public interface IJwtServicePort {
    JwtModel generateToken(UserModel userModel);
    UserModel validateAndGetUserFromToken(String token);
}