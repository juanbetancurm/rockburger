# Rock & Burger - Main Service (Central API Gateway)

The central orchestration service of the Rock & Burger restaurant management system. Built with Spring Boot and hexagonal architecture, this service provides authentication, product management, order processing, and microservice coordination with comprehensive security and resilience patterns.

The Main Service acts as the central API gateway and business logic orchestrator for the Rock & Burger ecosystem. It manages core business entities, provides authentication services, orchestrates microservice communications, and ensures data consistency across the distributed system.

### Core Responsibilities
- **Authentication & Authorization**: JWT-based security for all services
- **Product Catalog Management**: Articles, categories, and brands
- **Order Processing**: Complete order lifecycle from cart to completion
- **Inventory Management**: Stock tracking and supply chain operations  
- **User Management**: Staff and customer account administration
- **Microservice Orchestration**: Coordinate with Cart Service and other microservices
- **Business Logic Enforcement**: Domain rules and validation
- **API Gateway**: Single entry point for frontend applications

## Architecture

### Hexagonal Architecture (Ports & Adapters)

```
┌─────────────────────────────────────────────────────────────────────┐
│                          Infrastructure Layer                       │
├─────────────────┬─────────────────┬─────────────────┬───────────────┤
│   HTTP Layer    │ Feign Clients   │ Database Layer  │ Security Layer│
│                 │                 │                 │               │
│ Rest Controllers│ Cart Service    │ JPA Repositories│ JWT Filters   │
│ Request/Response│ Circuit Breakers│ MySQL Entities  │ Auth Providers│
│ DTOs & Mappers  │ Service Adapters│ Entity Mappers  │ CORS Config   │
└─────────────────┼─────────────────┼─────────────────┼───────────────┤
│                              Application Layer                      │
├─────────────────────────────────────────────────────────────────────┤
│                            Use Cases                                │
│  AuthenticationUseCase │ ArticleUseCase │ OrderUseCase │ etc.        │
└─────────────────────────────────────────────────────────────────────┤
│                              Domain Layer                           │
├─────────────────────────────────────────────────────────────────────┤
│               Domain Models & Business Rules                        │
│  UserModel │ ArticleModel │ OrderModel │ CategoryModel │ etc.        │
│                    Service Ports & Persistence Ports                │
└─────────────────────────────────────────────────────────────────────┘
```

### Key Architectural Patterns

**Domain-Driven Design (DDD)**
- Rich domain models with business logic
- Bounded contexts for different business areas
- Domain services for complex business operations

**Microservice Coordination**
- Feign clients for service-to-service communication
- JWT token propagation across services
- Circuit breakers for fault tolerance

**CQRS Elements**
- Separate read/write operations for complex queries
- Optimized data access patterns
- Event-driven updates

## Features

### Core Business Functions

**Product Management**
- Complete product catalog (articles) with categories and brands
- Inventory tracking with real-time stock updates
- Supply chain management and restocking
- Price management and product lifecycle

**Order Processing**  
- End-to-end order management from cart to completion
- Inventory validation and automatic stock updates
- Order confirmation and receipt generation
- Sales analytics and reporting

**User Management**
- Multi-role user system (admin, auxiliar, client)
- JWT-based authentication and session management
- User profile management and permissions
- Secure password handling with encryption

### Advanced Features

**Microservice Orchestration**
- Seamless integration with Cart Service via Feign
- Distributed transaction coordination
- Service discovery and load balancing
- Cross-service data consistency

**Resilience & Fault Tolerance**
- Circuit breakers for external service calls
- Retry mechanisms with exponential backoff
- Graceful degradation and fallback responses
- Health monitoring and service status tracking

**Security**
- Comprehensive JWT authentication system
- Role-based access control (RBAC)
- CORS configuration for web clients
- API security with Spring Security

## Technology Stack

### Core Framework
- **Spring Boot 3.x** - Application framework and auto-configuration
- **Spring Web MVC** - RESTful web services and API development
- **Spring Security** - Authentication, authorization, and security
- **Spring Data JPA** - Database abstraction and ORM

### Microservice Communication
- **Spring Cloud OpenFeign** - Declarative HTTP clients for service communication
- **Resilience4j** - Circuit breakers, retry, and fault tolerance patterns
- **Spring Cloud Gateway** (optional) - API gateway and routing

### Database & Persistence
- **MySQL 8.x** - Primary relational database
- **Hibernate ORM** - Object-relational mapping framework
- **HikariCP** - High-performance connection pooling
- **Spring Data JPA** - Repository pattern implementation

### Security & Authentication
- **JWT (JSON Web Tokens)** - Stateless authentication mechanism
- **Spring Security** - Comprehensive security framework
- **BCrypt** - Secure password hashing algorithm

### Documentation & Testing
- **OpenAPI 3.0 (Swagger)** - API documentation and testing UI
- **JUnit 5** - Unit and integration testing framework
- **TestContainers** - Integration testing with real databases
- **Mockito** - Mocking framework for unit tests

### Build & Operations
- **Maven** - Dependency management and build automation
- **Spring Boot Actuator** - Production monitoring and management
- **SLF4J + Logback** - Structured logging framework
- **Docker** - Containerization support

## Getting Started

### Prerequisites
- **Java 17 or higher**
- **Maven 3.8+**
- **MySQL 8.0+**
- **Cart Service** running on port 8091
- **Docker** (optional, for containerized deployment)

### Database Setup

1. **Create Main Database**
   ```sql
   CREATE DATABASE rockburgerapp CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   CREATE USER 'burger_user'@'localhost' IDENTIFIED BY 'secure_password';
   GRANT ALL PRIVILEGES ON rockburgerapp.* TO 'burger_user'@'localhost';
   FLUSH PRIVILEGES;
   ```

2. **Database Configuration**
   Update `application.yml` with your database credentials:
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/rockburgerapp
       username: burger_user
       password: secure_password
   ```

### Installation & Startup

1. **Clone and Build**
   ```bash
   git clone <repository-url>
   cd main-service
   mvn clean install
   ```

2. **Start Dependencies**
   ```bash
   # Start MySQL
   docker run --name mysql-main \
     -e MYSQL_ROOT_PASSWORD=12345 \
     -e MYSQL_DATABASE=rockburgerapp \
     -p 3306:3306 -d mysql:8.0

   # Start Cart Service (in separate terminal)
   cd ../cart-service
   mvn spring-boot:run
   ```

3. **Run Main Service**
   ```bash
   mvn spring-boot:run
   
   # Or run the JAR
   java -jar target/main-service-1.0.0.jar
   ```

4. **Verify Startup**
   ```bash
   curl http://localhost:8090/actuator/health
   ```

### Initial Setup

1. **Access Swagger UI**
   Open `http://localhost:8090/swagger-ui.html` for interactive API documentation

2. **Create Admin User** (via API or direct database insert)
   ```bash
   POST /api/auth/register
   {
     "email": "admin@rockburger.com",
     "password": "admin123",
     "role": "admin",
     "firstName": "Admin",
     "lastName": "User"
   }
   ```

## API Documentation

### Authentication Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| `POST` | `/api/auth/login` | User authentication | No |
| `POST` | `/api/auth/register` | User registration | No |
| `POST` | `/api/auth/refresh` | Refresh JWT token | Yes |
| `POST` | `/api/auth/logout` | Invalidate user session | Yes |

### Product Management

| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| `GET` | `/article/articles?page={page}&size={size}` | List all products | auxiliar, admin |
| `GET` | `/article/article/{id}` | Get product by ID | auxiliar, admin |
| `POST` | `/article/articlenew` | Create new product | admin |
| `PUT` | `/article/article/{id}` | Update product | admin |
| `DELETE` | `/article/article/{id}` | Delete product | admin |

### Category & Brand Management

| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| `GET` | `/category/categories` | List all categories | auxiliar, admin |
| `POST` | `/category/categorynew` | Create category | admin |
| `GET` | `/brand/brands` | List all brands | auxiliar, admin |
| `POST` | `/brand/brandnew` | Create brand | admin |

### Shopping Cart (Proxy to Cart Service)

| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| `GET` | `/shopping-cart` | Get active cart | auxiliar, client |
| `POST` | `/shopping-cart/items/{articleId}?quantity={qty}` | Add to cart | auxiliar, client |
| `PUT` | `/shopping-cart/items/{articleId}?quantity={qty}` | Update cart item | auxiliar, client |
| `DELETE` | `/shopping-cart/items/{articleId}` | Remove from cart | auxiliar, client |
| `DELETE` | `/shopping-cart` | Clear cart | auxiliar, client |

### Order Processing

| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| `POST` | `/purchase/checkout` | Process cart checkout | auxiliar |
| `POST` | `/purchase/complete` | Complete manual order | auxiliar |
| `GET` | `/purchase/availability` | Check product availability | auxiliar |
| `GET` | `/purchase/sales/daily?date={date}` | Daily sales summary | auxiliar |

### Supply Management

| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| `POST` | `/supply/add` | Add inventory supply | auxiliar, admin |
| `GET` | `/supply/history/{articleId}` | Supply history | auxiliar, admin |

### Request/Response Examples

**User Authentication**
```bash
POST /api/auth/login
Content-Type: application/json

{
  "email": "staff@rockburger.com",
  "password": "password123"
}

Response:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "email": "staff@rockburger.com",
  "role": "auxiliar",
  "expiresIn": 3600000
}
```

**Add Product to Cart**
```bash
POST /shopping-cart/items/123?quantity=2
Authorization: Bearer <jwt-token>

Response:
{
  "id": 1,
  "userId": "staff@rockburger.com",
  "items": [
    {
      "articleId": 123,
      "articleName": "Deluxe Burger",
      "quantity": 2,
      "unitPrice": 12.99,
      "subtotal": 25.98
    }
  ],
  "total": 25.98,
  "status": "ACTIVE"
}
```

**Process Checkout**
```bash
POST /purchase/checkout
Authorization: Bearer <jwt-token>

Response:
{
  "id": 456,
  "userId": 1,
  "items": [...],
  "total": 25.98,
  "orderDate": "2025-08-25T10:30:00Z",
  "status": "COMPLETED"
}
```

## Microservice Integration

### Feign Client Configuration

The Main Service integrates with other microservices using Spring Cloud OpenFeign:

```java
@FeignClient(
    name = "cart-service",
    url = "${cart.service.url}",
    configuration = FeignConfiguration.class
)
public interface CartFeignClient {
    @GetMapping("/carts")
    ResponseEntity<CartResponse> getActiveCart();
    
    @PostMapping("/carts/items/{articleId}")
    ResponseEntity<CartResponse> addItemToCart(
        @PathVariable Long articleId, 
        @RequestParam int quantity
    );
}
```

### Circuit Breaker Integration

Resilience4j circuit breakers protect against service failures:

```yaml
resilience4j:
  circuitbreaker:
    configs:
      default:
        slidingWindowSize: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 10s
    instances:
      getActiveCart:
        baseConfig: default
```

### JWT Token Propagation

JWT tokens are automatically propagated to downstream services:

```java
@Component
public class FeignJwtInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getCredentials() instanceof String) {
            String token = (String) auth.getCredentials();
            template.header("Authorization", "Bearer " + token);
        }
    }
}
```

## Security & Authentication

### JWT Authentication Flow

1. **User Login**: Credentials sent to `/api/auth/login`
2. **Token Generation**: JWT token created with user claims
3. **Token Storage**: Frontend stores token securely
4. **Request Authentication**: Token included in `Authorization` header
5. **Token Validation**: Each request validates token and extracts user info
6. **Service Propagation**: Token forwarded to downstream services

### JWT Token Structure

```json
{
  "sub": "user@rockburger.com",
  "userId": 1,
  "role": "auxiliar",
  "iat": 1692960000,
  "exp": 1692963600
}
```

### Role-Based Access Control

```java
@PreAuthorize("hasRole('admin')")
@PostMapping("/article/articlenew")
public ResponseEntity<ArticleResponse> createArticle(...) {
    // Only admin users can create articles
}

@PreAuthorize("hasAnyRole('auxiliar', 'admin')")
@PostMapping("/purchase/checkout")
public ResponseEntity<OrderResponse> checkout(...) {
    // Staff and admin can process orders
}
```

### Security Headers Configuration

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .cors().and()
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(STATELESS)
            .authorizeRequests()
                .antMatchers("/api/auth/**").permitAll()
                .antMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .anyRequest().authenticated()
            .and()
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
```

## Database Schema

### Core Entities

**Users Table**
```sql
CREATE TABLE users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  email VARCHAR(255) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  first_name VARCHAR(100),
  last_name VARCHAR(100),
  birth_date DATE,
  role ENUM('admin', 'auxiliar', 'client') DEFAULT 'client',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  active BOOLEAN DEFAULT TRUE,
  INDEX idx_email (email),
  INDEX idx_role (role)
);
```

**Articles (Products) Table**
```sql
CREATE TABLE articles (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  price DECIMAL(10,2) NOT NULL,
  quantity INT DEFAULT 0,
  category_id BIGINT NOT NULL,
  brand_id BIGINT NOT NULL,
  image_url VARCHAR(500),
  active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (category_id) REFERENCES categories(id),
  FOREIGN KEY (brand_id) REFERENCES brands(id),
  INDEX idx_category (category_id),
  INDEX idx_brand (brand_id),
  INDEX idx_active (active)
);
```

**Orders Table**
```sql
CREATE TABLE orders (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  total_amount DECIMAL(10,2) NOT NULL,
  order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  status ENUM('PENDING', 'COMPLETED', 'CANCELLED') DEFAULT 'PENDING',
  customer_name VARCHAR(255),
  customer_email VARCHAR(255),
  FOREIGN KEY (user_id) REFERENCES users(id),
  INDEX idx_user_id (user_id),
  INDEX idx_order_date (order_date),
  INDEX idx_status (status)
);

CREATE TABLE order_items (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_id BIGINT NOT NULL,
  article_id BIGINT NOT NULL,
  article_name VARCHAR(255),
  quantity INT NOT NULL,
  unit_price DECIMAL(10,2) NOT NULL,
  subtotal DECIMAL(10,2) NOT NULL,
  FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
  FOREIGN KEY (article_id) REFERENCES articles(id),
  INDEX idx_order_id (order_id)
);
```

**Categories & Brands**
```sql
CREATE TABLE categories (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) UNIQUE NOT NULL,
  description VARCHAR(90),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE brands (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) UNIQUE NOT NULL,
  description VARCHAR(90),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Supply Management**
```sql
CREATE TABLE supplies (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  article_id BIGINT NOT NULL,
  supplier_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  supply_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  notes TEXT,
  FOREIGN KEY (article_id) REFERENCES articles(id),
  FOREIGN KEY (supplier_id) REFERENCES users(id),
  INDEX idx_article_id (article_id),
  INDEX idx_supply_date (supply_date)
);
```

## Configuration

### Application Properties

```yaml
# Server Configuration
server:
  port: 8090

# Database Configuration  
spring:
  profiles:
    active: dev
  datasource:
    url: jdbc:mysql://localhost:3306/rockburgerapp
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:12345}
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: true

# JWT Configuration
jwt:
  secret: ${JWT_SECRET:your-very-long-secret-key}
  expiration: 3600000  # 1 hour in milliseconds

# Microservice URLs
cart:
  service:
    url: ${CART_SERVICE_URL:http://localhost:8091}

# Feign Configuration
feign:
  client:
    config:
      default:
        connectTimeout: 5000
        readTimeout: 5000

# Logging Configuration
logging:
  level:
    com.rockburger.burgermain: INFO
    org.springframework.security: WARN
    feign: INFO
    io.github.resilience4j: INFO
  
# Actuator Configuration
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,circuitbreakers
  endpoint:
    health:
      show-details: always
```

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SERVER_PORT` | Application server port | 8090 |
| `DB_HOST` | Database host | localhost |
| `DB_PORT` | Database port | 3306 |
| `DB_NAME` | Database name | rockburgerapp |
| `DB_USERNAME` | Database username | root |
| `DB_PASSWORD` | Database password | 12345 |
| `JWT_SECRET` | JWT signing secret | (required) |
| `JWT_EXPIRATION` | Token expiration (ms) | 3600000 |
| `CART_SERVICE_URL` | Cart Service URL | http://localhost:8091 |

### Profile-Specific Configuration

**Development Profile (`application-dev.yml`)**
```yaml
spring:
  jpa:
    show-sql: true
  
logging:
  level:
    com.rockburger.burgermain: DEBUG
    org.springframework.security: DEBUG
```

**Production Profile (`application-prod.yml`)**
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      
logging:
  level:
    root: INFO
    com.rockburger.burgermain: INFO
```

## Circuit Breakers & Resilience

### Resilience4j Configuration

```yaml
resilience4j:
  circuitbreaker:
    configs:
      default:
        slidingWindowSize: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 10s
        permittedNumberOfCallsInHalfOpenState: 5
        automaticTransitionFromOpenToHalfOpenEnabled: true
    instances:
      getActiveCart:
        baseConfig: default
      addItemToCart:
        baseConfig: default

  retry:
    configs:
      default:
        maxAttempts: 3
        waitDuration: 500ms
        enableExponentialBackoff: true
```

### Fallback Mechanisms

```java
@Component
public class CartServiceAdapter {
    
    @CircuitBreaker(name = "getActiveCart", fallbackMethod = "getFallbackCart")
    public CartResponse getActiveCart() {
        return cartFeignClient.getActiveCart().getBody();
    }
    
    public CartResponse getFallbackCart(Exception ex) {
        // Return empty cart when service is unavailable
        return CartResponse.builder()
            .id(null)
            .userId(getCurrentUserId())
            .items(Collections.emptyList())
            .total(BigDecimal.ZERO)
            .status("ACTIVE")
            .build();
    }
}
```

## Health Monitoring

### Health Check Endpoints

```bash
# Overall application health
GET /actuator/health

# Database connectivity
GET /actuator/health/db

# Circuit breaker status
GET /actuator/circuitbreakers

# Application metrics
GET /actuator/metrics

# Application info
GET /actuator/info
```

### Custom Health Indicators

```java
@Component
public class CartServiceHealthIndicator implements HealthIndicator {
    
    @Override
    public Health health() {
        try {
            cartFeignClient.healthCheck();
            return Health.up()
                .withDetail("message", "Cart service is accessible")
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("message", "Cart service is unreachable")
                .withException(e)
                .build();
        }
    }
}
```

### Monitoring Metrics

- **HTTP Request Metrics**: Request count, response times, error rates
- **Database Metrics**: Connection pool status, query performance
- **JVM Metrics**: Memory usage, garbage collection, thread pools
- **Circuit Breaker Metrics**: State changes, failure rates, call counts
- **Custom Business Metrics**: Order processing times, cart conversion rates

## Development Workflow

### Local Development Setup

1. **Start Infrastructure**
   ```bash
   # MySQL Database
   docker run --name mysql-dev \
     -e MYSQL_ROOT_PASSWORD=12345 \
     -e MYSQL_DATABASE=rockburgerapp \
     -p 3306:3306 -d mysql:8.0

   # Cart Service
   cd ../cart-service && mvn spring-boot:run
   ```

2. **Run with Development Profile**
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```

3. **Development Tools**
   - Swagger UI: `http://localhost:8090/swagger-ui.html`
   - H2 Console (if enabled): `http://localhost:8090/h2-console`
   - Actuator Dashboard: `http://localhost:8090/actuator`

### Code Quality Standards

- **Code Coverage**: Minimum 80% line coverage required
- **Static Analysis**: SonarQube integration for code quality metrics
- **Code Style**: Google Java Style Guide with Checkstyle enforcement
- **Documentation**: Comprehensive Javadoc for all public APIs
- **Security**: OWASP dependency scanning and vulnerability assessment

### Git Workflow

```bash
# Feature development
git checkout -b feature/user-management-enhancement
git commit -m "feat: add user profile management endpoints"
git push origin feature/user-management-enhancement

# Hotfix workflow
git checkout -b hotfix/security-vulnerability-fix
git commit -m "fix: resolve JWT token validation issue"
git push origin hotfix/security-vulnerability-fix
```

## Testing Strategy

### Test Pyramid Structure

```
src/test/java/
├── unit/                     # Fast, isolated unit tests
│   ├── domain/              # Domain logic tests
│   ├── controllers/         # Controller unit tests
│   └── services/            # Service layer tests
├── integration/             # Integration tests
│   ├── api/                # API endpoint tests
│   ├── database/           # Database integration tests
│   └── external/           # External service integration tests
└── e2e/                    # End-to-end system tests
```

### Running Tests

```bash
# Unit tests only (fast)
mvn test

# Integration tests (requires test containers)
mvn verify -P integration-tests

# All tests with coverage report
mvn clean verify jacoco:report

# Specific test categories
mvn test -Dtest=*UnitTest
mvn test -Dtest=*IntegrationTest
```

### Test Examples

**Unit Test Example**
```java
@ExtendWith(MockitoExtension.class)
class ArticleUseCaseTest {
    
    @Mock
    private IArticlePersistencePort articlePersistencePort;
    
    @InjectMocks
    private ArticleUseCase articleUseCase;
    
    @Test
    void shouldCreateArticleSuccessfully() {
        // Given
        ArticleModel article = ArticleModel.builder()
            .name("Test Burger")
            .price(BigDecimal.valueOf(12.99))
            .build();
            
        when(articlePersistencePort.save(article)).thenReturn(article);
        
        // When
        ArticleModel result = articleUseCase.createArticle(article);
        
        // Then
        assertThat(result.getName()).isEqualTo("Test Burger");
        verify(articlePersistencePort).save(article);
    }
}
```

**Integration Test Example**
```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
@Testcontainers
class OrderIntegrationTest {
    
    @Container
    static MySQL8ServerContainer mysql = new MySQL8ServerContainer("mysql:8.0")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");
        
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void shouldProcessCheckoutSuccessfully() {
        // Test complete checkout flow
    }
}
```

## Deployment

### Docker Configuration

**Dockerfile**
```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/main-service-*.jar app.jar

EXPOSE 8090

HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8090/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Docker Compose**
```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: ${DB_PASSWORD}
      MYSQL_DATABASE: ${DB_NAME}
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql

  cart-service:
    image: rockburger/cart-service:latest
    ports:
      - "8091:8091"
    depends_on:
      - mysql
    environment:
      - DB_HOST=mysql

  main-service:
    image: rockburger/main-service:latest
    ports:
      - "8090:8090"
    depends_on:
      - mysql
      - cart-service
    environment:
      - DB_HOST=mysql
      - CART_SERVICE_URL=http://cart-service:8091

volumes:
  mysql_data:
```

### Production Deployment

1. **Build Production Image**
   ```bash
   mvn clean package -P production
   docker build -t rockburger/main-service:latest .
   ```

2. **Deploy with Docker Compose**
   ```bash
   docker-compose up -d
   ```

3. **Health Check Verification**
   ```bash
   curl http://localhost:8090/actuator/health
   ```

## Troubleshooting

### Common Issues

**Database Connection Problems**
```bash
# Check MySQL connectivity
mysql -h localhost -u root -p rockburgerapp

# Verify JPA configuration
# Check application logs for Hibernate errors
```

**JWT Authentication Failures**  
```bash
# Validate JWT secret configuration
# Check token expiration settings
# Verify user roles and permissions
```

**Microservice Communication Issues**
```bash
# Check Cart Service availability
curl http://localhost:8091/actuator/health

# Verify Feign client configuration
# Check circuit breaker status via /actuator/circuitbreakers
```

**Performance Issues**
```bash
# Monitor JVM metrics
curl http://localhost:8090/actuator/metrics/jvm.memory.used

# Check database connection pool
curl http://localhost:8090/actuator/metrics/hikaricp.connections

# Review slow query logs
```

### Debug Configuration

```yaml
# Enhanced logging for troubleshooting
logging:
  level:
    com.rockburger.burgermain: DEBUG
    org.springframework.security: DEBUG
    org.springframework.transaction: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
    feign: DEBUG
    io.github.resilience4j: DEBUG
```

### Performance Optimization

**Database Optimization**
- Index optimization for frequently queried columns
- Connection pool tuning (HikariCP settings)
- Query optimization and N+1 problem resolution

**Caching Strategy**  
- Redis integration for session storage
- Application-level caching with Spring Cache
- Database query result caching

**JVM Tuning**
```bash
# Production JVM settings
java -Xms512m -Xmx2g -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=100 \
     -jar main-service.jar
```

**Service Details:**
- **Name**: Main Service (Central API Gateway)
- **Port**: 8090  
- **Database**: MySQL (rockburgerapp)
- **Java Version**: 17+
- **Spring Boot Version**: 3.x
- **Architecture**: Hexagonal (Ports & Adapters)
- **Deployment**: Docker & Docker Compose ready
