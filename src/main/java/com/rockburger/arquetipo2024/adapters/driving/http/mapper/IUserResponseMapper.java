package com.rockburger.arquetipo2024.adapters.driving.http.mapper;

import com.rockburger.arquetipo2024.adapters.driving.http.dto.response.UserResponse;
import com.rockburger.arquetipo2024.domain.model.UserModel;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IUserResponseMapper {
    UserResponse toResponse(UserModel userModel);
}