package com.rockburger.burgermain.configuration;

import com.rockburger.burgermain.adapters.driven.feign.dto.CartResponse;
import com.rockburger.burgermain.adapters.driven.jpa.mysql.adapter.security.JwtAdapter;
import com.rockburger.burgermain.adapters.driven.jpa.mysql.adapter.*;
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
import com.rockburger.burgermain.domain.spi.IPasswordPEncryptionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.rockburger.burgermain.domain.api.IJwtServicePort;

import java.math.BigDecimal;
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
            IPasswordPEncryptionPort passwordEncryptionPort) {
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
             IPasswordPEncryptionPort passwordEncryptionPort) {
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
            IPasswordPEncryptionPort passwordEncryptionPort) {
        return new ClientUseCase(clientPersistencePort, passwordEncryptionPort);
    }

    @Bean
    public IClientPersistencePort clientPersistencePort(
            IClientRepository clientRepository,
            IClientEntityMapper clientEntityMapper) {
        return new ClientAdapter(clientRepository, clientEntityMapper);
    }




    /*Order Classes*/


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
    public ICartToOrderMapper cartToOrderMapper() {
        return new ICartToOrderMapper() {
            @Override
            public OrderModel toOrderModel(CartResponse cartResponse, Long userId) {
                if (cartResponse == null) {
                    throw new IllegalArgumentException("Cart response cannot be null");
                }
                if (cartResponse.getItems() == null || cartResponse.getItems().isEmpty()) {
                    throw new IllegalArgumentException("Cart items cannot be null or empty");
                }
                if (userId == null) {
                    throw new IllegalArgumentException("User ID cannot be null");
                }

                OrderModel orderModel = new OrderModel();
                orderModel.setUserId(userId);

                // Convert cart items to order items
                List<OrderItemModel> orderItems = cartResponse.getItems().stream()
                        .map(cartItem -> {
                            // Convert double price from cart to BigDecimal for order
                            BigDecimal unitPrice = BigDecimal.valueOf(cartItem.getPrice());

                            return new OrderItemModel(
                                    cartItem.getArticleId(),
                                    cartItem.getArticleName(),
                                    cartItem.getQuantity(),
                                    unitPrice
                            );
                        })
                        .toList();

                orderModel.setItems(orderItems);

                // Convert cart total (double) to order total (BigDecimal)
                orderModel.setTotalAmount(BigDecimal.valueOf(cartResponse.getTotal()));

                return orderModel;
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


}
