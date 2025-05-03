# ShopWave Backend

ShopWave is a modern e-commerce platform built with microservices architecture, providing a robust and scalable solution for online shopping.

## 🚀 Technologies

- **Core Framework**: Spring Boot 3.x
- **Language**: Java 17+
- **Database**: PostgreSQL
- **Authentication & Notifications**: Firebase
- **Containerization**: Docker
- **API Documentation**: Swagger/OpenAPI
- **Monitoring**: Grafana, Prometheus
- **Payment Integration**: Iyzico
- **Internationalization**: i18n
- **Testing**: JUnit, Mockito
- **CI/CD**: GitHub Actions

## 🏗️ Architecture

The application follows a microservices architecture with the following services:

1. **API Gateway**
   - Request routing
   - Load balancing
   - Authentication
   - Rate limiting

2. **User Service**
   - User management
   - Authentication
   - Profile management
   - Firebase integration

3. **Product Service**
   - Product catalog
   - Category management
   - Inventory management
   - Search functionality

4. **Order Service**
   - Order processing
   - Payment integration (Iyzico)
   - Order tracking
   - Transaction management

5. **Review Service**
   - Product reviews
   - Rating system
   - Review moderation

6. **Promotion Service**
   - Discount management
   - Coupon system
   - Campaign management

7. **Notification Service**
   - Email notifications
   - Push notifications (Firebase)
   - Notification preferences

8. **Analytics Service**
   - Sales analytics
   - User behavior tracking
   - Reporting

## 📦 Prerequisites

- Java 17 or higher
- Docker and Docker Compose
- PostgreSQL
- Firebase account
- Iyzico merchant account
- Maven

## 🛠️ Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/shopwave.git
   cd shopwave
   ```

2. Configure environment variables:
   ```bash
   cp .env.example .env
   # Edit .env with your configuration
   ```

3. Start the development environment:
   ```bash
   docker-compose up -d
   ```

4. Build and run the application:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

## 📚 API Documentation

API documentation is available at `http://localhost:8080/swagger-ui.html` when running the application.

## 🔍 Monitoring

Access Grafana dashboard at `http://localhost:3000` to monitor:
- Service health
- Performance metrics
- Error rates
- User activity
- Sales analytics

## 🌐 Internationalization

The application supports multiple languages with automatic detection based on browser settings. Language files are stored in the `resources/i18n` directory.

## 🔒 Security

- JWT-based authentication
- Role-based access control
- API key management
- Rate limiting
- Input validation
- SQL injection prevention

## 🧪 Testing

Run tests with:
```bash
mvn test
```

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Support

For support, please open an issue in the GitHub repository or contact the maintainers.

## Payment Integration

ShopWave uses Iyzico for payment processing. To set up the payment integration:

1. Sign up for an Iyzico account at [https://www.iyzico.com](https://www.iyzico.com)
2. Get your API key and secret key from the Iyzico dashboard
3. Update the following properties in `application.properties`:
   ```properties
   iyzico.api-key=your-api-key
   iyzico.secret-key=your-secret-key
   iyzico.base-url=https://sandbox-api.iyzipay.com
   ```
   Note: Use `https://api.iyzipay.com` for production

### Payment Flow

1. User adds items to cart and proceeds to checkout
2. System creates an order and initializes payment with Iyzico
3. User enters payment details
4. System processes payment and updates order status
5. User receives confirmation

### Payment Features

- Secure payment processing
- Multiple payment methods support
- Refund capability
- Payment status tracking
- Transaction history 