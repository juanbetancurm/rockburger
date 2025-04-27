package com.rockburger.burgermain.domain.api;

import com.rockburger.burgermain.domain.model.AuthenticationModel;
import com.rockburger.burgermain.domain.model.JwtModel;

public interface IAuthenticationServicePort {
    JwtModel authenticate(String email, String password);
}
