package com.rockburger.arquetipo2024.adapters.driving.http.controller;

import com.rockburger.arquetipo2024.adapters.driving.http.dto.request.AddCategoryRequest;
import com.rockburger.arquetipo2024.adapters.driving.http.dto.request.AddUserRequest;
import com.rockburger.arquetipo2024.adapters.driving.http.dto.response.UserResponse;
import com.rockburger.arquetipo2024.adapters.driving.http.mapper.IUserRequestMapper;
import com.rockburger.arquetipo2024.adapters.driving.http.mapper.IUserResponseMapper;
import com.rockburger.arquetipo2024.domain.api.IUserServicePort;
import com.rockburger.arquetipo2024.domain.model.UserModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/users")
public class UserRestController {
    private static final Logger logger = LoggerFactory.getLogger(UserRestController.class);

    private final IUserServicePort userServicePort;
    private final IUserRequestMapper userRequestMapper;
    private final IUserResponseMapper userResponseMapper;

    public UserRestController(IUserServicePort userServicePort,
                                       IUserRequestMapper userRequestMapper,
                                       IUserResponseMapper userResponseMapper) {
        this.userServicePort = userServicePort;
        this.userRequestMapper = userRequestMapper;
        this.userResponseMapper = userResponseMapper;
    }

    @PostMapping
    @Operation(
            summary = "Create a new  user",
            description = "Creates a new auxiliary  user with the provided details. The user must be 18+ years old.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "User successfully created"),
                    @ApiResponse(responseCode = "400", description = "Invalid input data"),
                    @ApiResponse(responseCode = "409", description = "User already exists")
            }
    )
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody AddUserRequest addUserRequest) {
        logger.info("Received request to create  user with email: {}", addUserRequest.getEmail());

        UserModel userModel = userRequestMapper.toModel(addUserRequest);
        UserModel createdUser = userServicePort.createUser(userModel);
        UserResponse response = userResponseMapper.toResponse(createdUser);

        logger.info("Successfully created  user with ID: {}", response.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
