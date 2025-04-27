package com.rockburger.burgermain.adapters.driving.http.mapper;

import com.rockburger.burgermain.adapters.driving.http.dto.response.ClientResponse;
import com.rockburger.burgermain.domain.model.ClientModel;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IClientResponseMapper {
    ClientResponse toResponse(ClientModel clientModel);
}