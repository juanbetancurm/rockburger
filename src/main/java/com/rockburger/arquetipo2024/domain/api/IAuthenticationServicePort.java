package com.rockburger.arquetipo2024.domain.api;

import com.rockburger.arquetipo2024.domain.model.AuthenticationModel;
import com.rockburger.arquetipo2024.domain.model.JwtModel;

public interface IAuthenticationServicePort {
    JwtModel authenticate(String email, String password);
}
