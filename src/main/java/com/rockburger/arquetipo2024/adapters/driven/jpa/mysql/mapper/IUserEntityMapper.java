package com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.mapper;

import com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.entity.UserEntity;
import com.rockburger.arquetipo2024.domain.model.UserModel;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IUserEntityMapper {
    UserEntity toEntity(UserModel userModel);
    UserModel toModel(UserEntity userEntity);
}