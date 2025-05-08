# E-Commerce Application

A robust and secure e-commerce platform built with Spring Boot, featuring user authentication, product management, and order processing capabilities.

## 🚀 Technologies Used

- Java 17
- Spring Boot 3.3.5
- Spring Security with JWT
- Spring Data JPA
- MySQL Database
- Maven
- Lombok
- ModelMapper
- JWT for Authentication

## 📋 Prerequisites

- JDK 17 or higher
- Maven
- MySQL Server
- IDE (IntelliJ IDEA recommended)

## 🛠️ Project Structure

```
src/main/java/com/ecommerce/project/
├── config/         # Configuration classes
├── controller/     # REST controllers
├── exceptions/     # Custom exception handlers
├── model/         # Entity classes
├── payload/       # DTOs and request/response objects
├── repositories/  # JPA repositories
├── security/      # Security configuration and JWT
├── service/       # Business logic
└── utils/         # Utility classes
```

## 🔧 Setup and Installation

1. Clone the repository:
   ```bash
   git clone [repository-url]
   ```

2. Configure MySQL database:
   - Create a new database
   - Update `application.properties` with your database credentials

3. Build the project:
   ```bash
   mvn clean install
   ```

4. Run the application:
   ```bash
   mvn spring-boot:run
   ```

## 🔐 Security

The application uses Spring Security with JWT (JSON Web Token) for authentication and authorization. The security features include:

- User registration and login
- JWT token-based authentication
- Role-based access control
- Password encryption

## 📦 Features

- User Authentication and Authorization
- Product Management
- Order Processing
- Shopping Cart
- User Profile Management
- Admin Dashboard

## 🧪 Testing

Run the test suite using:
```bash
mvn test
```

## 📝 API Documentation

The API documentation will be available at:
```
http://localhost:8080/swagger-ui.html
```

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

