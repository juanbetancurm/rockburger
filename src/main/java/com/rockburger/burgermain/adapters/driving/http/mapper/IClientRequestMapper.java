package com.rockburger.burgermain.adapters.driving.http.mapper;

import com.rockburger.burgermain.adapters.driving.http.dto.request.AddClientRequest;
import com.rockburger.burgermain.domain.model.ClientModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IClientRequestMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "role", ignore = true)
    ClientModel toModel(AddClientRequest addClientRequest);
}
