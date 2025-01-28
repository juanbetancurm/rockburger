package com.rockburger.arquetipo2024.adapters.driving.http.mapper;

import com.rockburger.arquetipo2024.adapters.driving.http.dto.response.ClientResponse;
import com.rockburger.arquetipo2024.domain.model.ClientModel;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IClientResponseMapper {
    ClientResponse toResponse(ClientModel clientModel);
}