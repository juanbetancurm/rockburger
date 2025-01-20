package com.rockburger.arquetipo2024.adapters.driving.http.mapper;

import com.rockburger.arquetipo2024.adapters.driving.http.dto.request.AddUserRequest;
import com.rockburger.arquetipo2024.domain.model.UserModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IUserRequestMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    UserModel toModel(AddUserRequest addUserRequest);
}
