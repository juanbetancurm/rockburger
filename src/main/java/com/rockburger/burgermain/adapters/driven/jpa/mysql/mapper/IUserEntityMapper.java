package com.rockburger.burgermain.adapters.driven.jpa.mysql.mapper;

import com.rockburger.burgermain.adapters.driven.jpa.mysql.entity.UserEntity;
import com.rockburger.burgermain.domain.model.UserModel;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IUserEntityMapper {
    UserEntity toEntity(UserModel userModel);
    UserModel toModel(UserEntity userEntity);
}