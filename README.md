# ShopWave - E-Commerce Backend API

A comprehensive, production-ready e-commerce backend built with Java 21, Spring Boot 3.5.3, and modern development practices. ShopWave provides a complete foundation for building scalable e-commerce applications with advanced features like multi-provider payment processing, real-time notifications, and comprehensive analytics.

## 🚀 Features

### Core Functionality
- **User Management** - Registration, authentication, profile management with OAuth2 Google integration
- **Product Catalog** - Product management, categories, search, filtering, and inventory tracking
- **Review System** - Customer reviews and ratings with aggregation
- **Payment Processing** - Multi-provider payment gateway with IyziLink integration
- **Order Management** - Complete order lifecycle management
- **Notifications** - Real-time notifications with email integration
- **Analytics** - Comprehensive business analytics and reporting

### Technical Features
- **Security** - JWT authentication, OAuth2, role-based authorization (RBAC)
- **Performance** - Virtual threads (Java 21), connection pooling, caching
- **Documentation** - Complete OpenAPI/Swagger documentation
- **Payment Security** - Webhook verification, HMAC signatures, fraud prevention
- **Database** - PostgreSQL with optimized queries and indexing
- **Architecture** - Clean architecture, dependency injection, service layers

## 🛠️ Technology Stack

- **Java 21** - Latest LTS with virtual threads and modern language features
- **Spring Boot 3.5.3** - Enterprise-grade framework with auto-configuration
- **Spring Security** - Authentication, authorization, and security features
- **Spring Data JPA** - Data persistence with Hibernate ORM
- **PostgreSQL** - Production-ready relational database
- **Maven** - Dependency management and build automation
- **OpenAPI 3** - API documentation and testing interface
- **Jackson** - JSON processing and serialization
- **Lombok** - Code generation and boilerplate reduction

## 📋 Prerequisites

- **Java 21** or higher
- **Maven 3.8+**
- **PostgreSQL 13+**
- **Git**

## 🚀 Quick Start

### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/shopwave.git
cd shopwave
```

### 2. Database Setup
```sql
-- Create database
CREATE DATABASE shopwave;
CREATE USER shopwave_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE shopwave TO shopwave_user;
```

### 3. Environment Configuration
Create a `.env` file or set environment variables:

```bash
# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=shopwave
DB_USERNAME=shopwave_user
DB_PASSWORD=your_password

# JWT Configuration
JWT_SECRET=your-256-bit-secret-key-here
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000

# OAuth2 Google
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret

# Payment Provider (IyziLink)
IYZICO_API_KEY=your-iyzico-api-key
IYZICO_SECRET_KEY=your-iyzico-secret-key
IYZICO_API_URL=https://sandbox-api.iyzipay.com
IYZICO_WEBHOOK_SECRET=your-webhook-secret

# Email Configuration
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
```

### 4. Build and Run
```bash
# Build the project
mvn clean compile

# Run tests
mvn test

# Start the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## 📚 API Documentation

### Interactive Documentation
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

### API Endpoints Overview

#### Authentication
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `POST /api/auth/refresh` - Refresh JWT token
- `GET /api/auth/oauth2/google` - Google OAuth2 login

#### User Management
- `GET /api/users/profile` - Get user profile
- `PUT /api/users/profile` - Update user profile
- `GET /api/users` - List all users (Admin)

#### Product Catalog
- `GET /api/products` - List products with filtering
- `GET /api/products/search` - Search products
- `POST /api/products` - Create product (Admin)
- `PUT /api/products/{id}` - Update product (Admin)

#### Reviews
- `GET /api/products/{id}/reviews` - Get product reviews
- `POST /api/products/{id}/reviews` - Add review
- `PUT /api/reviews/{id}` - Update review

#### Payments
- `POST /api/payments` - Process payment
- `GET /api/payments/user/{userId}` - Get user payments
- `POST /api/payments/webhook/{provider}` - Payment webhook
- `POST /api/payments/{id}/refund` - Refund payment (Admin)

#### Notifications
- `GET /api/notifications/user/{userId}` - Get user notifications
- `POST /api/notifications` - Send notification (Admin)
- `PATCH /api/notifications/{id}/read` - Mark as read

#### Analytics (Admin)
- `GET /api/statistics/dashboard` - Dashboard statistics
- `GET /api/statistics/payments` - Payment analytics
- `GET /api/statistics/revenue` - Revenue statistics

## 🏗️ Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/acanbiler/shopwave/
│   │       ├── ShopWaveApplication.java          # Main application class
│   │       ├── config/                           # Configuration classes
│   │       │   ├── SecurityConfig.java           # Security configuration
│   │       │   └── PaymentConfig.java            # Payment provider config
│   │       ├── controller/                       # REST controllers
│   │       │   ├── AuthController.java           # Authentication endpoints
│   │       │   ├── UserController.java           # User management
│   │       │   ├── ProductController.java        # Product catalog
│   │       │   ├── PaymentController.java        # Payment processing
│   │       │   ├── NotificationController.java   # Notifications
│   │       │   └── StatisticsController.java     # Analytics
│   │       ├── entity/                           # JPA entities
│   │       │   ├── User.java                     # User entity
│   │       │   ├── Product.java                  # Product entity
│   │       │   ├── Review.java                   # Review entity
│   │       │   ├── Payment.java                  # Payment entity
│   │       │   └── Notification.java             # Notification entity
│   │       ├── repository/                       # Data repositories
│   │       │   ├── UserRepository.java           # User data access
│   │       │   ├── ProductRepository.java        # Product data access
│   │       │   ├── ReviewRepository.java         # Review data access
│   │       │   ├── PaymentRepository.java        # Payment data access
│   │       │   └── NotificationRepository.java   # Notification data access
│   │       ├── service/                          # Business logic
│   │       │   ├── AuthService.java              # Authentication service
│   │       │   ├── UserService.java              # User business logic
│   │       │   ├── ProductService.java           # Product business logic
│   │       │   ├── PaymentService.java           # Payment business logic
│   │       │   ├── NotificationService.java      # Notification service
│   │       │   ├── StatisticsService.java        # Analytics service
│   │       │   └── payment/                      # Payment providers
│   │       │       ├── PaymentProvider.java      # Provider interface
│   │       │       ├── IyziLinkPaymentProvider.java # IyziLink implementation
│   │       │       └── WebhookVerificationService.java # Webhook security
│   │       └── util/                             # Utility classes
│   │           └── JwtUtil.java                  # JWT token utilities
│   └── resources/
│       ├── application.yml                       # Application configuration
│       └── logback-spring.xml                    # Logging configuration
└── test/                                         # Test classes
    └── java/
        └── com/acanbiler/shopwave/
            └── ShopWaveApplicationTests.java
```

## 🔒 Security Features

### Authentication & Authorization
- **JWT Tokens** - Stateless authentication with refresh tokens
- **OAuth2 Integration** - Google social login
- **Role-Based Access Control** - ADMIN and CUSTOMER roles
- **Password Security** - BCrypt hashing

### Payment Security
- **Webhook Verification** - HMAC signature validation
- **Constant-Time Comparison** - Prevents timing attacks
- **Request Timeout Protection** - Prevents replay attacks
- **Provider Abstraction** - Secure multi-provider support

### API Security
- **CORS Configuration** - Cross-origin request handling
- **Input Validation** - Jakarta Bean Validation
- **SQL Injection Prevention** - Parameterized queries
- **XSS Protection** - JSON encoding and validation

## 🚀 Deployment

### Production Configuration

1. **Environment Variables**
```bash
# Set production database
DB_HOST=your-production-db-host
DB_PASSWORD=secure-production-password

# Use production OAuth2 credentials
GOOGLE_CLIENT_ID=production-client-id
GOOGLE_CLIENT_SECRET=production-client-secret

# Configure production payment provider
IYZICO_API_URL=https://api.iyzipay.com
IYZICO_TEST_MODE=false
```

2. **Build Production JAR**
```bash
mvn clean package -Pprod
```

3. **Run with Production Profile**
```bash
java -jar target/shopwave-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### Docker Deployment

```dockerfile
FROM openjdk:21-jre-slim
COPY target/shopwave-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 🧪 Testing

### Run Tests
```bash
# Unit tests
mvn test

# Integration tests
mvn verify

# Test with coverage
mvn clean test jacoco:report
```

### Test Coverage
- **Minimum Coverage**: 80% line coverage required
- **Critical Business Logic**: 90%+ coverage
- **Report Location**: `target/site/jacoco/index.html`

## 📊 Monitoring & Observability

### Health Checks
- **Application Health**: `/actuator/health`
- **Database Health**: Automatic connection monitoring
- **Payment Provider Status**: Provider-specific health checks

### Metrics
- **Payment Processing**: Success rates, failure reasons
- **User Activity**: Registration, login, engagement
- **Performance**: Response times, throughput
- **Business KPIs**: Revenue, orders, customer retention

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Follow the coding standards in `CLAUDE.md`
4. Commit your changes (`git commit -m 'Add amazing feature'`)
5. Push to the branch (`git push origin feature/amazing-feature`)
6. Open a Pull Request

### Code Quality Standards
- **Follow Google Java Style Guide**
- **Maintain 80%+ test coverage**
- **Document all public APIs with OpenAPI annotations**
- **Use meaningful commit messages**
- **Run `mvn checkstyle:check` before committing**

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

### Getting Help
- **Documentation**: [Swagger UI](http://localhost:8080/swagger-ui.html)
- **Issues**: [GitHub Issues](https://github.com/yourusername/shopwave/issues)
- **Discussions**: [GitHub Discussions](https://github.com/yourusername/shopwave/discussions)

### Troubleshooting

#### Common Issues

**Database Connection Issues**
```bash
# Check PostgreSQL status
sudo systemctl status postgresql

# Verify database exists
psql -U shopwave_user -d shopwave -c "\dt"
```

**Payment Provider Issues**
```bash
# Verify IyziLink configuration
curl -X POST https://sandbox-api.iyzipay.com/payment/test \
  -H "Authorization: Bearer your-token"
```

**Authentication Issues**
```bash
# Verify JWT secret length (must be 256-bit)
echo "your-jwt-secret" | wc -c  # Should be 32+ characters
```

## 🚧 Roadmap

### Phase 1 (Current)
- [x] Core API implementation
- [x] Payment processing
- [x] User authentication
- [x] Product catalog

### Phase 2 (Upcoming)
- [ ] Advanced search with Elasticsearch
- [ ] Caching with Redis
- [ ] Message queuing with RabbitMQ
- [ ] Microservices architecture

### Phase 3 (Future)
- [ ] Machine learning recommendations
- [ ] Advanced analytics dashboard
- [ ] Multi-tenant support
- [ ] International payment providers

---

**Built with ❤️ using Java 21, Spring Boot 3.5.3, and modern development practices.**

For more detailed information, check the [API Documentation](http://localhost:8080/swagger-ui.html) or explore the codebase!