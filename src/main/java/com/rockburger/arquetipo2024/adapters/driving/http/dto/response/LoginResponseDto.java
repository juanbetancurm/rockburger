package com.rockburger.arquetipo2024.adapters.driving.http.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDto {
    private String token;
    private String type;
    private Long userId;
    private String email;
    private String role;
}
