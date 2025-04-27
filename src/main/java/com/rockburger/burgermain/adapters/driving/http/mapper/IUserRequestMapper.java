package com.rockburger.burgermain.adapters.driving.http.mapper;

import com.rockburger.burgermain.adapters.driving.http.dto.request.AddUserRequest;
import com.rockburger.burgermain.domain.model.UserModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IUserRequestMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    UserModel toModel(AddUserRequest addUserRequest);
}
