package com.rockburger.arquetipo2024.adapters.driving.http.mapper;


import com.rockburger.arquetipo2024.adapters.driving.http.dto.response.LoginResponseDto;
import com.rockburger.arquetipo2024.domain.model.JwtModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IAuthenticationResponseMapper {
    @Mapping(target = "token", source = "token")
    @Mapping(target = "type", constant = "Bearer")
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "role", source = "role")
    LoginResponseDto toDto(JwtModel jwtModel);
}