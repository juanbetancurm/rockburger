package com.rockburger.burgermain.configuration;

import com.rockburger.burgermain.adapters.driven.jpa.mysql.adapter.security.JwtAdapter;
import com.rockburger.burgermain.adapters.driven.jpa.mysql.adapter.*;
import com.rockburger.burgermain.adapters.driven.jpa.mysql.adapter.security.BCryptPasswordAdapter;
import com.rockburger.burgermain.adapters.driven.jpa.mysql.mapper.*;
import com.rockburger.burgermain.adapters.driven.jpa.mysql.repository.*;
import com.rockburger.burgermain.adapters.driving.http.dto.request.AddSupplyRequest;
import com.rockburger.burgermain.adapters.driving.http.dto.response.BrandResponse;
import com.rockburger.burgermain.adapters.driving.http.dto.response.CategoryResponse;
import com.rockburger.burgermain.adapters.driving.http.dto.response.SupplyResponse;
import com.rockburger.burgermain.adapters.driving.http.dto.response.UserResponse;
import com.rockburger.burgermain.adapters.driving.http.mapper.*;
import com.rockburger.burgermain.configuration.security.JwtKeyProvider;
import com.rockburger.burgermain.domain.api.*;
import com.rockburger.burgermain.domain.api.usecase.*;
import com.rockburger.burgermain.domain.model.BrandModel;
import com.rockburger.burgermain.domain.model.CategoryModel;
import com.rockburger.burgermain.domain.model.SupplyModel;
import com.rockburger.burgermain.domain.model.UserModel;
import com.rockburger.burgermain.domain.spi.*;
import com.rockburger.burgermain.domain.spi.IPasswordEncryptionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.rockburger.burgermain.domain.api.IJwtServicePort;

import java.util.List;


@Configuration
@RequiredArgsConstructor
public class BeanConfiguration {

    private final ICategoryRepository categoryRepository;
    private final ICategoryEntityMapper categoryEntityMapper;
    private final IBrandRepository brandRepository;
    private final IBrandEntityMapper brandEntityMapper;
    private final IUserRepository userRepository;
    private final IUserEntityMapper userEntityMapper;
    private final IArticleRepository articleRepository;
    private final IArticleEntityMapper articleEntityMapper;

    @Bean
    public IArticleServicePort articleServicePort(IArticlePersistencePort articlePersistencePort,
                                                  ICategoryServicePort categoryServicePort,
                                                  IBrandServicePort brandServicePort) {

        return new ArticleUseCase(articlePersistencePort, categoryServicePort, brandServicePort);
    }
    @Bean
    public IArticlePersistencePort articlePersistencePort() {
        return new ArticleAdapter(articleRepository, articleEntityMapper, brandRepository);
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

    /* UserCreation */
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



    /* Security */

    /* Authentication*/
     @Bean
     public IAuthenticationServicePort authenticationServicePort(
             IUserPersistencePort userPersistencePort,
             IJwtServicePort jwtServicePort,
             IPasswordEncryptionPort passwordEncryptionPort) {
         return new AuthenticationUseCase(
                    userPersistencePort,
                    jwtServicePort,
                    passwordEncryptionPort
            );
        }


    /* JWT */
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private int jwtExpiration;

    @Bean
    public IJwtPersistencePort jwtPersistencePort(
            JwtKeyProvider jwtKeyProvider,
            @Qualifier("jwtSecretKey") String jwtSecretKey) {
        return new JwtAdapter(jwtKeyProvider, jwtSecretKey);
    }

    @Bean
    public IJwtServicePort jwtServicePort(
            IJwtPersistencePort jwtPersistencePort,
            IUserPersistencePort userPersistencePort) {
        return new JwtUseCase(
                jwtPersistencePort,
                userPersistencePort,
                jwtSecret,
                jwtExpiration
        );
    }

    /* Supply Beans */
    @Bean
    public ISupplyServicePort supplyServicePort(ISupplyPersistencePort supplyPersistencePort) {
        return new SupplyUseCase(supplyPersistencePort);
    }

    @Bean
    public ISupplyPersistencePort supplyPersistencePort(
            ISupplyRepository supplyRepository,
            ISupplyEntityMapper supplyEntityMapper,
            IArticleRepository articleRepository) {
        return new SupplyAdapter(supplyRepository, supplyEntityMapper, articleRepository);
    }

    @Bean
    public ISupplyRequestMapper supplyRequestMapper() {
        return new ISupplyRequestMapper() {
            @Override
            public SupplyModel toModel(AddSupplyRequest request) {
                if (request == null) {
                    return null;
                }
                SupplyModel model = new SupplyModel();
                model.setQuantity(request.getQuantity());
                return model;
            }
        };
    }

    @Bean
    public ISupplyResponseMapper supplyResponseMapper() {
        return new ISupplyResponseMapper() {
            @Override
            public SupplyResponse toResponse(SupplyModel model) {
                if (model == null) {
                    return null;
                }
                return new SupplyResponse(
                        model.getId(),
                        model.getArticle().getId(),
                        model.getArticle().getName(),
                        model.getQuantity(),
                        model.getSupplyDate(),
                        model.getSupplier().getEmail()
                );
            }
        };
    }


    /* Client Classes*/

    @Bean
    public IClientServicePort clientServicePort(
            IClientPersistencePort clientPersistencePort,
            IPasswordEncryptionPort passwordEncryptionPort) {
        return new ClientUseCase(clientPersistencePort, passwordEncryptionPort);
    }

    @Bean
    public IClientPersistencePort clientPersistencePort(
            IClientRepository clientRepository,
            IClientEntityMapper clientEntityMapper) {
        return new ClientAdapter(clientRepository, clientEntityMapper);
    }


}
