# ShopWave Backend MVP - Complete Implementation

## Goal

Build a complete MVP backend for a shopping web application using Java 24, Spring Boot 3.5.3, and Maven. The system must provide secure authentication, user management, product catalog with reviews, payment processing, statistics, and notifications through a RESTful API architecture.

## Why

- **Business Value**: Enable e-commerce operations with complete customer lifecycle management
- **User Impact**: Provide seamless shopping experience from authentication to purchase completion
- **Integration**: Foundational backend that supports future frontend integrations (React, mobile apps)
- **Scalability**: Modular architecture allows independent scaling of services

## What

Build six core microservices within a single Spring Boot application:

1. **Login Service**: Email/Google OAuth2 authentication with JWT tokens
2. **User Service**: Role-based access control (Admin/Customer) with user management
3. **Product Service**: CRUD operations with reviews and ratings system
4. **Payment Service**: Provider-agnostic payment processing with Iyzılink integration
5. **Statistics Service**: Analytics for payments and product trends
6. **Notifications Service**: Email and push notification system

### Success Criteria

- [ ] Complete authentication flow with JWT tokens
- [ ] Role-based authorization working for all endpoints
- [ ] Full product CRUD with reviews and ratings
- [ ] Payment processing with Iyzılink provider
- [ ] Statistics endpoints returning aggregated data
- [ ] Notification system sending emails
- [ ] 80%+ test coverage across all services
- [ ] OpenAPI documentation complete for all endpoints
- [ ] All SonarQube quality gates passing

## All Needed Context

### Documentation & References

```yaml
# MUST READ - Include these in your context window
- url: https://docs.spring.io/spring-boot/reference/
  why: Spring Boot 3.5.3 configuration patterns, auto-configuration, and best practices
  
- url: https://docs.spring.io/spring-security/reference/
  why: OAuth2, JWT, and role-based security implementation patterns

- url: https://docs.spring.io/spring-data/jpa/reference/
  why: Repository patterns, query methods, and entity relationships

- url: https://docs.iyzico.com/en/products/iyzilink/iyzilink-api
  why: Payment provider integration, webhook handling, and error codes

- url: https://openjdk.org/projects/jdk/24/
  why: Java 24 features like virtual threads, pattern matching, and records

- url: https://springdoc.org/
  why: OpenAPI documentation generation with Spring Boot integration

- file: /Users/acbiler/dev/projects/shopwave/shopwave/CLAUDE.md
  why: Project-specific coding standards, validation rules, and architecture patterns
```

### Current Codebase Structure

```bash
shopwave/
├── CLAUDE.md
├── INITIAL.md
├── PRPs/
│   └── templates/
│       └── prp_base.md
```

### Desired Codebase Structure

```bash
shopwave/
├── pom.xml                              # Maven configuration with all dependencies
├── README.md                            # Project documentation and setup guide
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/acanbiler/shopwave/
│   │   │       ├── ShopWaveApplication.java         # Main Spring Boot application
│   │   │       ├── config/
│   │   │       │   ├── SecurityConfig.java         # Security configuration
│   │   │       │   ├── PaymentConfig.java          # Payment provider configuration
│   │   │       │   └── OpenApiConfig.java          # API documentation config
│   │   │       ├── controller/
│   │   │       │   ├── AuthController.java         # Login/logout endpoints
│   │   │       │   ├── UserController.java         # User management endpoints
│   │   │       │   ├── ProductController.java      # Product CRUD endpoints
│   │   │       │   ├── ReviewController.java       # Product review endpoints
│   │   │       │   ├── PaymentController.java      # Payment processing endpoints
│   │   │       │   ├── StatisticsController.java   # Analytics endpoints
│   │   │       │   └── NotificationController.java # Notification endpoints
│   │   │       ├── service/
│   │   │       │   ├── AuthService.java            # Authentication logic
│   │   │       │   ├── UserService.java            # User management logic
│   │   │       │   ├── ProductService.java         # Product business logic
│   │   │       │   ├── ReviewService.java          # Review business logic
│   │   │       │   ├── PaymentService.java         # Payment processing logic
│   │   │       │   ├── StatisticsService.java      # Analytics logic
│   │   │       │   └── NotificationService.java    # Notification logic
│   │   │       ├── repository/
│   │   │       │   ├── UserRepository.java         # User data access
│   │   │       │   ├── ProductRepository.java      # Product data access
│   │   │       │   ├── ReviewRepository.java       # Review data access
│   │   │       │   ├── PaymentRepository.java      # Payment data access
│   │   │       │   └── NotificationRepository.java # Notification data access
│   │   │       ├── entity/
│   │   │       │   ├── User.java                   # User entity with roles
│   │   │       │   ├── Product.java                # Product entity
│   │   │       │   ├── Review.java                 # Review entity
│   │   │       │   ├── Payment.java                # Payment entity
│   │   │       │   └── Notification.java           # Notification entity
│   │   │       ├── dto/
│   │   │       │   ├── request/                    # Request DTOs
│   │   │       │   │   ├── LoginRequest.java
│   │   │       │   │   ├── UserCreateRequest.java
│   │   │       │   │   ├── ProductCreateRequest.java
│   │   │       │   │   ├── ReviewCreateRequest.java
│   │   │       │   │   ├── PaymentRequest.java
│   │   │       │   │   └── NotificationRequest.java
│   │   │       │   └── response/                   # Response DTOs
│   │   │       │       ├── LoginResponse.java
│   │   │       │       ├── UserResponse.java
│   │   │       │       ├── ProductResponse.java
│   │   │       │       ├── ReviewResponse.java
│   │   │       │       ├── PaymentResponse.java
│   │   │       │       ├── StatisticsResponse.java
│   │   │       │       └── NotificationResponse.java
│   │   │       ├── exception/
│   │   │       │   ├── GlobalExceptionHandler.java # Global error handling
│   │   │       │   ├── AuthenticationException.java
│   │   │       │   ├── AuthorizationException.java
│   │   │       │   ├── PaymentException.java
│   │   │       │   └── ValidationException.java
│   │   │       └── util/
│   │   │           ├── JwtUtil.java                # JWT token utilities
│   │   │           ├── PasswordUtil.java           # Password hashing utilities
│   │   │           └── ValidationUtil.java         # Input validation utilities
│   │   └── resources/
│   │       ├── application.yml                     # Main configuration
│   │       ├── application-dev.yml                 # Development configuration
│   │       ├── application-prod.yml                # Production configuration
│   │       └── data.sql                            # Initial data setup
│   └── test/
│       ├── java/
│       │   └── com/acanbiler/shopwave/
│       │       ├── ShopWaveApplicationTests.java   # Application context tests
│       │       ├── controller/                     # Controller integration tests
│       │       ├── service/                        # Service unit tests
│       │       └── repository/                     # Repository tests
│       └── resources/
│           └── application-test.yml                # Test configuration
```

### Known Gotchas & Library Quirks

```java
// CRITICAL: Spring Boot 3.5.3 with Java 24 requires specific Maven configuration
// Virtual threads enabled by default - avoid synchronized blocks in service layer
// Use ReentrantLock instead of synchronized for better virtual thread performance

// CRITICAL: Spring Security 6.x with OAuth2 requires specific JWT configuration
// JWT tokens must include user roles in claims for @PreAuthorize to work
// OAuth2 login requires specific redirect URIs and client registration

// CRITICAL: JPA with PostgreSQL requires specific dialect configuration
// Use @Entity with @Table for explicit table naming
// Avoid bi-directional relationships without proper fetch strategies

// CRITICAL: Iyzılink payment provider requires specific webhook verification
// All webhook requests must be verified using HMAC signature
// Payment status updates are asynchronous - implement proper status tracking

// CRITICAL: Bean Validation with groups for different validation scenarios
// Use validation groups for create vs update operations
// @Valid cascades validation to nested objects automatically
```

## Implementation Blueprint

### Data Models and Entity Relationships

```java
// Core entity structure with proper JPA relationships
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    private UserRole role; // ADMIN, CUSTOMER
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Review> reviews;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Payment> payments;
}

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<Review> reviews;
    
    @Formula("(SELECT AVG(r.rating) FROM Review r WHERE r.product_id = id)")
    private Double averageRating;
}

// Payment entity with provider abstraction
@Entity
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    private PaymentProvider provider; // IYZILINK, STRIPE, etc.
    
    @Enumerated(EnumType.STRING)
    private PaymentStatus status; // PENDING, COMPLETED, FAILED
    
    private String providerPaymentId; // External payment ID
    private String webhookData; // Store webhook payload for audit
}
```

### Task Implementation Order

```yaml
Task 1: Project Setup and Configuration
CREATE pom.xml:
  - INCLUDE Java 24 compiler configuration
  - ADD Spring Boot 3.5.3 parent
  - ADD dependencies: web, security, data-jpa, validation, oauth2-client
  - ADD OpenAPI (springdoc-openapi-starter-webmvc-ui)
  - ADD PostgreSQL driver, H2 for testing

CREATE src/main/resources/application.yml:
  - CONFIGURE database connection
  - CONFIGURE JWT secret and expiration
  - CONFIGURE OAuth2 client credentials
  - CONFIGURE Iyzılink payment provider settings

CREATE src/main/java/com/acanbiler/shopwave/ShopWaveApplication.java:
  - ENABLE virtual threads: @EnableAsync with virtual thread executor
  - ENABLE JPA repositories
  - ENABLE scheduling for background tasks

Task 2: Security Infrastructure
CREATE src/main/java/com/acanbiler/shopwave/config/SecurityConfig.java:
  - CONFIGURE JWT authentication filter
  - CONFIGURE OAuth2 login with Google
  - CONFIGURE role-based authorization
  - DISABLE CSRF for API endpoints
  - CONFIGURE CORS for frontend integration

CREATE src/main/java/com/acanbiler/shopwave/util/JwtUtil.java:
  - IMPLEMENT JWT token generation with user roles
  - IMPLEMENT JWT token validation and parsing
  - INCLUDE refresh token mechanism

Task 3: Core Entities and Repositories
CREATE entity classes:
  - User.java with role-based security
  - Product.java with review relationship
  - Review.java with rating validation
  - Payment.java with provider abstraction
  - Notification.java with type enumeration

CREATE repository interfaces:
  - EXTEND JpaRepository for all entities
  - ADD custom query methods for complex operations
  - INCLUDE statistics queries with @Query annotation

Task 4: Service Layer Implementation
CREATE service classes:
  - AuthService.java with JWT and OAuth2 integration
  - UserService.java with role-based operations
  - ProductService.java with review aggregation
  - PaymentService.java with provider abstraction
  - StatisticsService.java with aggregation queries
  - NotificationService.java with email integration

Task 5: Controller Layer with OpenAPI Documentation
CREATE controller classes:
  - ADD comprehensive @Operation annotations
  - IMPLEMENT proper @ApiResponse documentation
  - ADD @Parameter documentation with examples
  - INCLUDE validation with @Valid and custom groups
  - IMPLEMENT global exception handling

Task 6: Payment Provider Integration
CREATE PaymentConfig.java:
  - CONFIGURE Iyzılink client with credentials
  - IMPLEMENT webhook signature verification
  - ADD retry mechanism for failed payments

CREATE PaymentService.java:
  - IMPLEMENT payment processing with Iyzılink
  - ADD webhook handling for payment status updates
  - INCLUDE payment status reconciliation

Task 7: Statistics and Analytics
CREATE StatisticsService.java:
  - IMPLEMENT payment trend analytics
  - ADD product performance metrics
  - INCLUDE user engagement statistics
  - USE native queries for complex aggregations

Task 8: Notification System
CREATE NotificationService.java:
  - IMPLEMENT email notifications using JavaMailSender
  - ADD notification templates
  - INCLUDE notification history tracking
  - IMPLEMENT async notification processing

Task 9: Testing Infrastructure
CREATE test classes:
  - Unit tests for all service methods
  - Integration tests for all controller endpoints
  - Repository tests with @DataJpaTest
  - Security tests with @WithMockUser

Task 10: Documentation and README
CREATE README.md:
  - INCLUDE project setup instructions
  - ADD API documentation links
  - INCLUDE development environment setup
  - ADD deployment instructions
```

### Critical Implementation Details

```java
// Task 1: JWT Authentication Filter
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // PATTERN: Extract JWT from Authorization header
    // VALIDATE: Token signature and expiration
    // POPULATE: SecurityContext with user authentication
    // HANDLE: Invalid tokens with proper error response
}

// Task 2: Role-Based Authorization
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/admin/users")
public ResponseEntity<List<UserResponse>> getAllUsers() {
    // CRITICAL: Roles must be in JWT token claims
    // PATTERN: Use @PreAuthorize for method-level security
}

// Task 3: Payment Provider Abstraction
public interface PaymentProvider {
    PaymentResponse processPayment(PaymentRequest request);
    boolean verifyWebhook(String signature, String payload);
}

@Service
public class IyziLinkPaymentProvider implements PaymentProvider {
    // IMPLEMENT: Iyzılink-specific payment processing
    // HANDLE: Provider-specific error codes
    // VERIFY: Webhook signatures using HMAC
}

// Task 4: Statistics Aggregation
@Query("SELECT new com.acanbiler.shopwave.dto.response.PaymentStatistics(" +
       "FUNCTION('DATE_TRUNC', 'month', p.createdAt), " +
       "SUM(p.amount), COUNT(p)) " +
       "FROM Payment p " +
       "WHERE p.status = :status " +
       "GROUP BY FUNCTION('DATE_TRUNC', 'month', p.createdAt)")
List<PaymentStatistics> findMonthlyPaymentStatistics(@Param("status") PaymentStatus status);
```

### Integration Points

```yaml
DATABASE:
  - schema: "CREATE DATABASE shopwave_db"
  - migration: "Use Flyway for database versioning"
  - indexes: "CREATE INDEX idx_product_category ON products(category)"

EXTERNAL_SERVICES:
  - payment: "Iyzılink webhook endpoint configuration"
  - email: "SMTP configuration for notifications"
  - oauth: "Google OAuth2 client registration"

SECURITY:
  - jwt: "RSA key pair for token signing"
  - cors: "Frontend domain whitelist"
  - rate_limiting: "API rate limiting configuration"

MONITORING:
  - actuator: "Health checks and metrics endpoints"
  - logging: "Structured logging with correlation IDs"
  - metrics: "Custom metrics for business KPIs"
```

## Validation Loop

### Level 1: Syntax & Style

```bash
# Maven compilation and style checks
mvn clean compile                    # Compile all source code
mvn checkstyle:check                 # Code style validation
mvn spotbugs:check                   # Static analysis for bugs
mvn dependency:analyze               # Unused dependency detection

# Expected: BUILD SUCCESS for all commands
```

### Level 2: Unit Tests

```java
// CREATE comprehensive test suite with these patterns:
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class UserServiceTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    
    @Test
    @DisplayName("should create user with valid data")
    void should_CreateUser_When_ValidDataProvided() {
        // PATTERN: Given-When-Then structure
        // VALIDATE: All business rules and constraints
        // ASSERT: Using AssertJ for readable assertions
    }
    
    @Test
    @DisplayName("should throw validation exception for invalid email")
    void should_ThrowValidationException_When_InvalidEmailProvided() {
        // TEST: Bean validation annotations
        // VERIFY: Proper exception handling
    }
}
```

```bash
# Run comprehensive test suite
mvn test                             # Unit tests
mvn verify                           # Integration tests
mvn jacoco:report                    # Coverage report

# Expected: 80%+ line coverage, all tests passing
```

### Level 3: Integration Tests

```bash
# Start the application
mvn spring-boot:run

# Test authentication flow
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "test@example.com", "password": "password"}'

# Expected: JWT token in response
# Test protected endpoints with JWT

# Test OpenAPI documentation
curl http://localhost:8080/swagger-ui.html
# Expected: Complete API documentation with examples
```

### Level 4: Payment Integration Test

```bash
# Test payment processing with test credentials
curl -X POST http://localhost:8080/api/payments/process \
  -H "Authorization: Bearer ${JWT_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 100.00,
    "currency": "TRY",
    "cardNumber": "5528790000000008",
    "expiryMonth": "12",
    "expiryYear": "2030",
    "cvc": "123"
  }'

# Expected: Payment initiated successfully
# Verify webhook handling with test webhook calls
```

## Final Validation Checklist

- [ ] All tests pass: `mvn test`
- [ ] No compilation errors: `mvn clean compile`
- [ ] Code quality gates pass: `mvn verify sonar:sonar`
- [ ] Security tests pass: Authentication and authorization working
- [ ] Payment integration functional: Test payment processing
- [ ] OpenAPI documentation complete: All endpoints documented
- [ ] Database migrations applied: Schema created successfully
- [ ] Email notifications working: Test email sending
- [ ] Statistics endpoints functional: Return proper aggregated data
- [ ] README.md updated: Complete setup instructions

## Anti-Patterns to Avoid

- ❌ Don't use `@Autowired` field injection - use constructor injection
- ❌ Don't create entities without proper JPA annotations
- ❌ Don't skip OpenAPI documentation - every endpoint must be documented
- ❌ Don't hardcode secrets - use Spring profiles and environment variables
- ❌ Don't ignore validation - use Bean Validation annotations
- ❌ Don't create services without interfaces for payment providers
- ❌ Don't forget webhook signature verification for payments
- ❌ Don't use synchronization in service layer - conflicts with virtual threads
- ❌ Don't return null from service methods - use Optional<T>
- ❌ Don't skip integration tests - test the complete request/response cycle

---

**PRP Confidence Score: 9/10**

This PRP provides comprehensive context for one-pass implementation including:
- Complete project structure with file responsibilities
- Detailed task breakdown with implementation order
- Critical gotchas and library-specific requirements
- Executable validation steps with expected outcomes
- External documentation references for all major components
- Anti-patterns to avoid based on Spring Boot best practices

The high confidence score reflects the thorough research, detailed implementation blueprint, and comprehensive validation loops that should enable successful implementation without multiple iterations.