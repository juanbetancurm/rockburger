package com.rockburger.arquetipo2024.adaptertest.driving.httptest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rockburger.arquetipo2024.adapters.driving.http.controller.BrandRestController;
import com.rockburger.arquetipo2024.adapters.driving.http.dto.request.AddBrandRequest;
import com.rockburger.arquetipo2024.adapters.driving.http.dto.response.BrandResponse;
import com.rockburger.arquetipo2024.adapters.driving.http.mapper.IBrandRequestMapper;
import com.rockburger.arquetipo2024.adapters.driving.http.mapper.IBrandResponseMapper;
import com.rockburger.arquetipo2024.configuration.exceptionhandler.ControllerAdvisor;
import com.rockburger.arquetipo2024.domain.api.IBrandServicePort;
import com.rockburger.arquetipo2024.domain.exception.InvalidParameterException;
import com.rockburger.arquetipo2024.domain.exception.NameAlreadyExistsExceptionD;
import com.rockburger.arquetipo2024.domain.model.BrandModel;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(ControllerAdvisor.class)
@WebMvcTest(BrandRestController.class)
class BrandRestControllerTest {
    @MockBean
    private IBrandServicePort brandServicePort;

    @MockBean
    private IBrandRequestMapper brandRequestMapper;

    @MockBean
    private IBrandResponseMapper brandResponseMapper;

    @Autowired
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();


    @Test
    void testCreateBrand_Success() throws Exception {
        // Arrange
        AddBrandRequest request = new AddBrandRequest("Adidas", "Shoes");
        BrandModel brandModel = new BrandModel(null, "Adidas", "Shoes");
        BrandResponse response = new BrandResponse(1L, "Adidas", "Shoes");

        when(brandRequestMapper.addRequestToBrandModel(any(AddBrandRequest.class))).thenReturn(brandModel);
        when(brandServicePort.createBrand(any(BrandModel.class))).thenReturn(brandModel);
        when(brandResponseMapper.toResponse(any(BrandModel.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/brand/brandnew")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Adidas"))
                .andExpect(jsonPath("$.description").value("Shoes"));
    }

    @Test
    void testCreateBrand_AlreadyExists() throws Exception {

        AddBrandRequest request = new AddBrandRequest("Adidas", "Shoes");

        when(brandRequestMapper.addRequestToBrandModel(any(AddBrandRequest.class)))
                .thenReturn(new BrandModel(null, "Adidas", "Shoes"));
        when(brandServicePort.createBrand(any(BrandModel.class)))
                .thenThrow(new NameAlreadyExistsExceptionD("Adidas"));


        mockMvc.perform(post("/brand/brandnew")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$['This name already exists']").value("Name 'Adidas' already exists"));

    }

    @Test
    void testCreateBrand_InvalidName() throws Exception {
        // Arrange
        AddBrandRequest request = new AddBrandRequest("", "Shoes");

        // Act & Assert
        mockMvc.perform(post("/brand/brandnew")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Name is required"));
    }

    @Test
    void testCreateBrand_InvalidDescription() throws Exception {

        String invalidDescription = "";
        String errorMessage = "Description is required";

        AddBrandRequest request = new AddBrandRequest("Adidas", invalidDescription);

        // Act & Assert
        mockMvc.perform(post("/brand/brandnew")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value(errorMessage));
    }

    //getBrandsWithPagination() method.

    @Test
    void testGetBrandsWithPagination_Success() throws Exception {
        // Arrange
        BrandModel brandModel1 = new BrandModel(1L, "Adidas", "Shoes");
        BrandModel brandModel2 = new BrandModel(2L, "Rolex", "Watches");

        List<BrandModel> brandModels = List.of(brandModel1, brandModel2);
        List<BrandResponse> brandResponses = List.of(
                new BrandResponse(1L, "Adidas", "Shoes"),
                new BrandResponse(2L, "Rolex", "Watches")
        );

        when(brandServicePort.getBrandsWithPagination(anyInt(), anyInt(), any(String.class), any(Boolean.class)))
                .thenReturn(brandModels);
        when(brandResponseMapper.toBrandResponseList(any(List.class))).thenReturn(brandResponses);

        // Act & Assert
        mockMvc.perform(get("/brand/brandspage")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "name")
                        .param("asc", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Adidas"))
                .andExpect(jsonPath("$[1].name").value("Rolex"));
    }
    @Test
    void testGetBrandsWithPagination_InvalidPageParameter() throws Exception {
        when(brandServicePort.getBrandsWithPagination(-1, 5, "name", true))
                .thenThrow(new InvalidParameterException("Page number cannot be negative."));

        // Perform the request
        mockMvc.perform(get("/brand/brandspage?page=-1&size=5"))
                .andDo(print()) // Print the actual response for debugging
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.['Invalid page parameter']").value("Page number cannot be negative."));
    }

    @Test
    void testGetBrandsWithPagination_InvalidSizeParameter() throws Exception {
        String invalidSize = "0";
        String errorMessage = "Page size must be greater than zero";

        // Mocking service behavior
        when(brandServicePort.getBrandsWithPagination(0, 0, "name", true))
                .thenThrow(new InvalidParameterException(errorMessage));

        // Act & Assert
        MvcResult result = mockMvc.perform(get("/brand/brandspage")
                        .param("page", "0")
                        .param("size", invalidSize)
                        .param("sortBy", "name")
                        .param("asc", "true"))
                .andDo(print())  // This will print the response details to the console for easier debugging
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.['Invalid page parameter']").value(errorMessage))
                .andReturn();

        // Log the actual response body
        String actualResponse = result.getResponse().getContentAsString();
        System.out.println("Actual response: " + actualResponse);

        // Log expected and actual values for debugging
        Logger logger = LoggerFactory.getLogger(this.getClass());
        logger.info("Expected error message: {}", errorMessage);
        logger.info("Actual response: {}", actualResponse);
    }


}
