package com.rockburger.arquetipo2024.adapters.driving.http.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String idDocument;
    private String phoneNumber;
    private LocalDate birthDate;
    private String email;
    private String role;
}