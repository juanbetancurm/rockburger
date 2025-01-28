package com.rockburger.arquetipo2024.adapters.driving.http.controller;

import com.rockburger.arquetipo2024.adapters.driving.http.dto.request.AddClientRequest;
import com.rockburger.arquetipo2024.adapters.driving.http.dto.response.ClientResponse;
import com.rockburger.arquetipo2024.adapters.driving.http.mapper.IClientRequestMapper;
import com.rockburger.arquetipo2024.adapters.driving.http.mapper.IClientResponseMapper;
import com.rockburger.arquetipo2024.domain.api.IClientServicePort;
import com.rockburger.arquetipo2024.domain.model.ClientModel;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/clients")
public class ClientRestController {
    private static final Logger logger = LoggerFactory.getLogger(ClientRestController.class);

    private final IClientServicePort clientServicePort;
    private final IClientRequestMapper clientRequestMapper;
    private final IClientResponseMapper clientResponseMapper;

    public ClientRestController(IClientServicePort clientServicePort,
                                IClientRequestMapper clientRequestMapper,
                                IClientResponseMapper clientResponseMapper) {
        this.clientServicePort = clientServicePort;
        this.clientRequestMapper = clientRequestMapper;
        this.clientResponseMapper = clientResponseMapper;
    }

    @PostMapping
    @Operation(
            summary = "Create a new client",
            description = "Creates a new client with the provided details. The client must be 18+ years old.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Client successfully created"),
                    @ApiResponse(responseCode = "400", description = "Invalid input data"),
                    @ApiResponse(responseCode = "409", description = "Client already exists")
            }
    )
    public ResponseEntity<ClientResponse> createClient(
            @Valid @RequestBody AddClientRequest addClientRequest) {
        logger.info("Received request to create client with email: {}", addClientRequest.getEmail());

        ClientModel clientModel = clientRequestMapper.toModel(addClientRequest);
        ClientModel createdClient = clientServicePort.createClient(clientModel);
        ClientResponse response = clientResponseMapper.toResponse(createdClient);

        logger.info("Successfully created client with ID: {}", response.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}