package com.rockburger.burgermain.configuration;

import com.rockburger.burgermain.adapters.driven.jpa.mysql.adapter.security.JwtAdapter;
import com.rockburger.burgermain.adapters.driven.jpa.mysql.adapter.*;
import com.rockburger.burgermain.adapters.driven.jpa.mysql.adapter.security.BCryptPasswordAdapter;
import com.rockburger.burgermain.adapters.driven.jpa.mysql.mapper.*;
import com.rockburger.burgermain.adapters.driven.jpa.mysql.repository.*;
import com.rockburger.burgermain.adapters.driving.http.dto.request.AddSupplyRequest;
import com.rockburger.burgermain.adapters.driving.http.dto.request.CompleteOrderRequest;
import com.rockburger.burgermain.adapters.driving.http.dto.response.*;
import com.rockburger.burgermain.adapters.driving.http.mapper.*;
import com.rockburger.burgermain.configuration.security.JwtKeyProvider;
import com.rockburger.burgermain.domain.api.*;
import com.rockburger.burgermain.domain.api.usecase.*;
import com.rockburger.burgermain.domain.model.*;
import com.rockburger.burgermain.domain.spi.*;
import com.rockburger.burgermain.domain.spi.IPasswordEncryptionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.rockburger.burgermain.domain.api.IJwtServicePort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.stream.Collectors;


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

    // JWT Configuration Properties
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration:3600000}") // Default: 1 hour
    private int jwtExpiration;

    @Value("${jwt.access-token-expiration:3600000}") // Default: 1 hour (3600000 ms)
    private int jwtAccessTokenExpiration;

    @Value("${jwt.refresh-token-expiration:604800000}") // Default: 7 days (604800000 ms)
    private int jwtRefreshTokenExpiration;

    // Security Beans
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return bCryptPasswordEncoder();
    }

    @Bean
    public JwtKeyProvider jwtKeyProvider() {
        return new JwtKeyProvider();
    }

    // Define the JWT secret key bean with qualifier
    @Bean
    @Qualifier("jwtSecretKey")
    public String jwtSecretKey() {
        return jwtSecret;
    }

    // Article Related Beans
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

    // Category Related Beans
    @Bean
    public ICategoryPersistencePort categoryPersistencePort() {
        return new CategoryAdapter(categoryRepository, categoryEntityMapper, articleRepository);
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

    // Brand Related Beans
    @Bean
    public IBrandPersistencePort brandPersistencePort() {
        return new BrandAdapter(brandRepository, brandEntityMapper, articleRepository);
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

    // User Related Beans
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

    // Security and Authentication Beans
    @Bean
    public IPasswordEncryptionPort passwordEncryptionPort(PasswordEncoder passwordEncoder) {
        return new BCryptPasswordAdapter(passwordEncoder);
    }

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
                jwtAccessTokenExpiration,
                jwtRefreshTokenExpiration
        );
    }

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

    // Authentication Response Mapper Bean
    @Bean
    public IAuthenticationResponseMapper authenticationResponseMapper() {
        return new IAuthenticationResponseMapper() {
            @Override
            public LoginResponseDto toDto(JwtModel jwtModel) {
                if (jwtModel == null) {
                    return null;
                }
                return new LoginResponseDto(
                        jwtModel.getToken(),
                        "Bearer",
                        jwtModel.getUserId(),
                        jwtModel.getEmail(),
                        jwtModel.getRole()
                );
            }
        };
    }

    // Supply Related Beans
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

    // Client Related Beans
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

    // Order Related Beans
    @Bean
    public IOrderServicePort orderServicePort(
            IOrderPersistencePort orderPersistencePort,
            IArticlePersistencePort articlePersistencePort) {
        return new OrderUseCase(orderPersistencePort, articlePersistencePort);
    }

    @Bean
    public IOrderPersistencePort orderPersistencePort(
            IOrderRepository orderRepository,
            IOrderEntityMapper orderEntityMapper) {
        return new OrderAdapter(orderRepository, orderEntityMapper);
    }

    @Bean
    public IOrderRequestMapper orderRequestMapper() {
        return new IOrderRequestMapper() {
            @Override
            public OrderModel toModel(CompleteOrderRequest request, Long userId) {
                if (request == null) {
                    return null;
                }
                OrderModel model = new OrderModel();
                model.setUserId(userId);
                model.setTotalAmount(request.getTotalAmount());

                List<OrderItemModel> items = request.getItems().stream()
                        .map(item -> new OrderItemModel(
                                item.getArticleId(),
                                item.getArticleName(),
                                item.getQuantity(),
                                item.getUnitPrice()
                        ))
                        .toList();
                model.setItems(items);

                return model;
            }
        };
    }

    @Bean
    public IOrderResponseMapper orderResponseMapper() {
        return new IOrderResponseMapper() {
            @Override
            public OrderResponse toResponse(OrderModel orderModel) {
                if (orderModel == null) {
                    return null;
                }
                List<OrderItemResponse> itemResponses = orderModel.getItems().stream()
                        .map(item -> new OrderItemResponse(
                                item.getId(),
                                item.getArticleId(),
                                item.getArticleName(),
                                item.getQuantity(),
                                item.getUnitPrice(),
                                item.getSubtotal()
                        ))
                        .toList();

                return new OrderResponse(
                        orderModel.getId(),
                        orderModel.getUserId(),
                        orderModel.getTotalAmount(),
                        orderModel.getOrderDate(),
                        orderModel.getStatus(),
                        itemResponses
                );
            }

            @Override
            public SalesSummaryResponse toResponse(SalesSummaryModel salesSummaryModel) {
                if (salesSummaryModel == null) {
                    return null;
                }
                return new SalesSummaryResponse(
                        salesSummaryModel.getDate(),
                        salesSummaryModel.getTotalOrders(),
                        salesSummaryModel.getTotalRevenue()
                );
            }

            @Override
            public ProductAvailabilityResponse toAvailabilityResponse(ArticleModel articleModel) {
                if (articleModel == null) {
                    return null;
                }
                return new ProductAvailabilityResponse(
                        articleModel.getId(),
                        articleModel.getName(),
                        articleModel.getQuantity(),
                        articleModel.getPrice()
                );
            }

            @Override
            public List<ProductAvailabilityResponse> toAvailabilityResponseList(List<ArticleModel> articles) {
                if (articles == null) {
                    return List.of();
                }
                return articles.stream()
                        .map(this::toAvailabilityResponse)
                        .toList();
            }
        };
    }

    /**
     * Configuration properties bean for JWT settings
     */
    @Bean
    public JwtConfigurationProperties jwtConfigurationProperties() {
        return new JwtConfigurationProperties(
                jwtSecret,
                jwtAccessTokenExpiration,
                jwtRefreshTokenExpiration
        );
    }

    /**
     * Inner class to hold JWT configuration properties
     */
    public static class JwtConfigurationProperties {
        private final String secret;
        private final int accessTokenExpiration;
        private final int refreshTokenExpiration;

        public JwtConfigurationProperties(String secret, int accessTokenExpiration, int refreshTokenExpiration) {
            this.secret = secret;
            this.accessTokenExpiration = accessTokenExpiration;
            this.refreshTokenExpiration = refreshTokenExpiration;
        }

        public String getSecret() {
            return secret;
        }

        public int getAccessTokenExpiration() {
            return accessTokenExpiration;
        }

        public int getRefreshTokenExpiration() {
            return refreshTokenExpiration;
        }

        public long getAccessTokenExpirationInMillis() {
            return accessTokenExpiration;
        }

        public long getRefreshTokenExpirationInMillis() {
            return refreshTokenExpiration;
        }

        public long getAccessTokenExpirationInSeconds() {
            return accessTokenExpiration / 1000;
        }

        public long getRefreshTokenExpirationInSeconds() {
            return refreshTokenExpiration / 1000;
        }

        public long getAccessTokenExpirationInMinutes() {
            return accessTokenExpiration / (1000 * 60);
        }

        public long getRefreshTokenExpirationInDays() {
            return refreshTokenExpiration / (1000 * 60 * 60 * 24);
        }
    }
}