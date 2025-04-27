package com.rockburger.burgermain.adapters.driving.http.controller;

import com.rockburger.burgermain.adapters.driving.http.dto.request.LoginRequestDto;
import com.rockburger.burgermain.adapters.driving.http.dto.response.LoginResponseDto;
import com.rockburger.burgermain.adapters.driving.http.mapper.IAuthenticationResponseMapper;
import com.rockburger.burgermain.domain.api.IAuthenticationServicePort;
import com.rockburger.burgermain.domain.model.JwtModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthenticationController {
    private final IAuthenticationServicePort authenticationServicePort;
    private final IAuthenticationResponseMapper authenticationResponseMapper;

    public AuthenticationController(
            IAuthenticationServicePort authenticationServicePort,
            IAuthenticationResponseMapper authenticationResponseMapper) {
        this.authenticationServicePort = authenticationServicePort;
        this.authenticationResponseMapper = authenticationResponseMapper;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequest) {
        log.debug("Received login request for email: {}", loginRequest.getEmail());
        try {
            JwtModel jwtModel = authenticationServicePort.authenticate(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
            );
            LoginResponseDto response = authenticationResponseMapper.toDto(jwtModel);
            log.info("Authentication successful for user: {}", loginRequest.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Authentication failed for user: {}", loginRequest.getEmail(), e);
            throw e;
        }
    }
}

