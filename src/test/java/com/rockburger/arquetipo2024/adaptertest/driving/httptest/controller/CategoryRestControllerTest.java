package com.rockburger.arquetipo2024.adaptertest.driving.httptest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rockburger.arquetipo2024.adapters.driving.http.controller.CategoryRestController;
import com.rockburger.arquetipo2024.adapters.driving.http.dto.request.AddCategoryRequest;
import com.rockburger.arquetipo2024.adapters.driving.http.dto.response.CategoryResponse;
import com.rockburger.arquetipo2024.adapters.driving.http.mapper.ICategoryRequestMapper;
import com.rockburger.arquetipo2024.adapters.driving.http.mapper.ICategoryResponseMapper;
import com.rockburger.arquetipo2024.configuration.exceptionhandler.ControllerAdvisor;
import com.rockburger.arquetipo2024.domain.api.ICategoryServicePort;
import com.rockburger.arquetipo2024.domain.exception.InvalidParameterException;
import com.rockburger.arquetipo2024.domain.exception.NameAlreadyExistsExceptionD;
import com.rockburger.arquetipo2024.domain.model.CategoryModel;

import org.junit.jupiter.api.Test;


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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(ControllerAdvisor.class)
@WebMvcTest(CategoryRestController.class)
class CategoryRestControllerTest {

    @MockBean
    private ICategoryServicePort categoryServicePort;

    @MockBean
    private ICategoryRequestMapper categoryRequestMapper;

    @MockBean
    private ICategoryResponseMapper categoryResponseMapper;

    @Autowired
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();


    @Test
    void testCreateCategory_Success() throws Exception {
        // Arrange
        AddCategoryRequest request = new AddCategoryRequest("Health", "Products for human health");
        CategoryModel categoryModel = new CategoryModel(null, "Health", "Products for human health");
        CategoryResponse response = new CategoryResponse(1L, "Health", "Products for human health");

        when(categoryRequestMapper.addRequestToCategoryModel(any(AddCategoryRequest.class))).thenReturn(categoryModel);
        when(categoryServicePort.createCategory(any(CategoryModel.class))).thenReturn(categoryModel);
        when(categoryResponseMapper.toResponse(any(CategoryModel.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/category/categorynew")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Health"))
                .andExpect(jsonPath("$.description").value("Products for human health"));
    }

    @Test
    void testCreateCategory_AlreadyExists() throws Exception {

        AddCategoryRequest request = new AddCategoryRequest("Health", "Products for human health");

        when(categoryRequestMapper.addRequestToCategoryModel(any(AddCategoryRequest.class)))
                .thenReturn(new CategoryModel(null, "Health", "Products for human health"));
        when(categoryServicePort.createCategory(any(CategoryModel.class)))
                .thenThrow(new NameAlreadyExistsExceptionD("Health"));


        mockMvc.perform(post("/category/categorynew")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$['This name already exists']").value("Name 'Health' already exists"));

    }

    @Test
    void testCreateCategory_InvalidName() throws Exception {
        // Arrange
        AddCategoryRequest request = new AddCategoryRequest("", "Products for human health");

        // Act & Assert
        mockMvc.perform(post("/category/categorynew")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Name is required"));
    }

    @Test
    void testCreateCategory_InvalidDescription() throws Exception {

        String invalidDescription = "";
        String errorMessage = "Description is required";

        AddCategoryRequest request = new AddCategoryRequest("Health", invalidDescription);

        // Act & Assert
        mockMvc.perform(post("/category/categorynew")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value(errorMessage));
    }


    //getCategoriesWithPagination() method.

    @Test
    void testGetCategoriesWithPagination_Success() throws Exception {
        // Arrange
        CategoryModel categoryModel1 = new CategoryModel(1L, "Health", "Products for human health");
        CategoryModel categoryModel2 = new CategoryModel(2L, "Technology", "Tech gadgets and accessories");

        List<CategoryModel> categoryModels = List.of(categoryModel1, categoryModel2);
        List<CategoryResponse> categoryResponses = List.of(
                new CategoryResponse(1L, "Health", "Products for human health"),
                new CategoryResponse(2L, "Technology", "Tech gadgets and accessories")
        );

        when(categoryServicePort.getCategoriesWithPagination(anyInt(), anyInt(), any(String.class), any(Boolean.class)))
                .thenReturn(categoryModels);
        when(categoryResponseMapper.toCategoryResponseList(any(List.class))).thenReturn(categoryResponses);

        // Act & Assert
        mockMvc.perform(get("/category/categoriespage")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "name")
                        .param("asc", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Health"))
                .andExpect(jsonPath("$[1].name").value("Technology"));
    }
    @Test
    void testGetCategoriesWithPagination_InvalidPageParameter() throws Exception {
        when(categoryServicePort.getCategoriesWithPagination(-1, 5, "name", true))
                .thenThrow(new InvalidParameterException("Page number cannot be negative."));

        // Perform the request
        mockMvc.perform(get("/category/categoriespage?page=-1&size=5"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.['Invalid page parameter']").value("Page number cannot be negative."));
    }

    @Test
    void testGetCategoriesWithPagination_InvalidSizeParameter() throws Exception {
        when(categoryServicePort.getCategoriesWithPagination(-1, 5, "name", true))
                .thenThrow(new InvalidParameterException("Page number cannot be negative."));

        MvcResult result = mockMvc.perform(get("/category/categoriespage?page=-1&size=5"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        System.out.println("Actual response body: " + responseBody);
    }

}
