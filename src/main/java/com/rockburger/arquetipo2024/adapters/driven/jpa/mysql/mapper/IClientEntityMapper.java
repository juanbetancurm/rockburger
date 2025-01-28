package com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.mapper;

import com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.entity.ClientEntity;
import com.rockburger.arquetipo2024.domain.model.ClientModel;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IClientEntityMapper {
    ClientEntity toEntity(ClientModel clientModel);
    ClientModel toModel(ClientEntity clientEntity);
}
