package com.rockburger.burgermain.domain.api;

import com.rockburger.burgermain.domain.model.UserModel;

public interface IUserServicePort {
    UserModel createUser(UserModel userModel);
    boolean validateCredentials(String email, String password);
}