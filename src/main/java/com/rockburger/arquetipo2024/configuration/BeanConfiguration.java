package com.rockburger.arquetipo2024.configuration;

import com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.adapter.*;
import com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.mapper.*;
import com.rockburger.arquetipo2024.adapters.driven.jpa.mysql.repository.*;
import com.rockburger.arquetipo2024.adapters.driving.http.dto.response.BrandResponse;
import com.rockburger.arquetipo2024.adapters.driving.http.dto.response.CategoryResponse;
import com.rockburger.arquetipo2024.adapters.driving.http.dto.response.UserResponse;
import com.rockburger.arquetipo2024.adapters.driving.http.mapper.IBrandResponseMapper;
import com.rockburger.arquetipo2024.adapters.driving.http.mapper.ICategoryResponseMapper;
import com.rockburger.arquetipo2024.adapters.driving.http.mapper.IUserResponseMapper;
import com.rockburger.arquetipo2024.domain.api.*;
import com.rockburger.arquetipo2024.domain.api.usecase.*;
import com.rockburger.arquetipo2024.domain.model.BrandModel;
import com.rockburger.arquetipo2024.domain.model.CategoryModel;
import com.rockburger.arquetipo2024.domain.model.UserModel;
import com.rockburger.arquetipo2024.domain.spi.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


import java.util.List;


@Configuration
@RequiredArgsConstructor
public class BeanConfiguration {

    private final ICategoryRepository categoryRepository;
    private final ICategoryEntityMapper categoryEntityMapper;
    private final IBrandRepository brandRepository;
    private final IBrandEntityMapper brandEntityMapper;


    @Bean
    public IArticleServicePort articleServicePort(IArticlePersistencePort articlePersistencePort,
                                                  ICategoryServicePort categoryServicePort,
                                                  IBrandServicePort brandServicePort) {

        return new ArticleUseCase(articlePersistencePort, categoryServicePort, brandServicePort);
    }

    @Bean
    public ICategoryPersistencePort categoryPersistencePort(){
        return new CategoryAdapter(categoryRepository, categoryEntityMapper);
    }
    @Bean
    public ICategoryServicePort categoryServicePort(){
        return new CategoryUseCase(categoryPersistencePort());
    }

    @Bean
    public ICategoryResponseMapper categoryResponseMapper(){
        return new ICategoryResponseMapper() {
            @Override
            public CategoryResponse toResponse(CategoryModel categoryModel) {
                return new CategoryResponse(
                        categoryModel.getId(),
                        categoryModel.getName(),
                        categoryModel.getDescription()
                );
            }

            @Override
            public List<CategoryResponse> toCategoryResponseList(List<CategoryModel> categoryModels) {
                if (categoryModels == null){
                    return List.of();
                }

                return categoryModels.stream()
                        .map(this::toResponse)
                        .toList();
            }
        };
    }

    @Bean
    public IBrandPersistencePort brandPersistencePort(){

        return new BrandAdapter(brandRepository, brandEntityMapper);
    }
    @Bean
    public IBrandServicePort brandServicePort(){

        return new BrandUseCase(brandPersistencePort());
    }

    @Bean
    public IBrandResponseMapper brandResponseMapper(){
        return new IBrandResponseMapper() {
            @Override
            public BrandResponse toResponse(BrandModel brandModel) {
                return new BrandResponse(
                        brandModel.getId(),
                        brandModel.getName(),
                        brandModel.getDescription()
                );
            }

            @Override
            public List<BrandResponse> toBrandResponseList(List<BrandModel> brandModels) {
                if (brandModels == null){
                    return List.of();
                }

                return brandModels.stream()
                        .map(this::toResponse)
                        .toList();
            }
        };
    }



    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public IPasswordEncryptionPort passwordEncryptionPort(BCryptPasswordEncoder passwordEncoder) {
        return new BCryptPasswordAdapter(passwordEncoder);
    }

    @Bean
    public IUserServicePort userServicePort(
            IUserPersistencePort userPersistencePort,
            IPasswordEncryptionPort passwordEncryptionPort) {
        return new UserUseCase(userPersistencePort, passwordEncryptionPort);
    }

    @Bean
    public IUserPersistencePort userPersistencePort(
            IUserRepository userRepository,
            IUserEntityMapper userEntityMapper) {
        return new UserAdapter(userRepository, userEntityMapper);
    }
    @Bean
    public IUserResponseMapper userResponseMapper() {
        return new IUserResponseMapper() {
            @Override
            public UserResponse toResponse(UserModel userModel) {
                return new UserResponse(
                        userModel.getId(),
                        userModel.getFirstName(),
                        userModel.getLastName(),
                        userModel.getIdDocument(),
                        userModel.getPhoneNumber(),
                        userModel.getBirthDate(),
                        userModel.getEmail(),
                        userModel.getRole()
                );
            }
        };
    }

}
