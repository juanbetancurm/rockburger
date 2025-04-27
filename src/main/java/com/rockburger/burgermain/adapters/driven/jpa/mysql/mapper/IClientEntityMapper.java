package com.rockburger.burgermain.adapters.driven.jpa.mysql.mapper;

import com.rockburger.burgermain.adapters.driven.jpa.mysql.entity.ClientEntity;
import com.rockburger.burgermain.domain.model.ClientModel;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IClientEntityMapper {
    ClientEntity toEntity(ClientModel clientModel);
    ClientModel toModel(ClientEntity clientEntity);
}
