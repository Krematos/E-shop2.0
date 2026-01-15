# E-Shop Backend - Spring Boot REST API

Complete REST API backend for an e-commerce application built on Spring Boot 3.5.7 with PostgreSQL database, JWT authentication, and advanced features like caching and asynchronous processing.

## 📋 Table of Contents

- [Technologies](#technologies)
- [Project Structure](#project-structure)
- [Requirements](#requirements)
- [Installation and Running](#installation-and-running)
- [Configuration](#configuration)
- [API Documentation](#api-documentation)
- [Authentication and Authorization](#authentication-and-authorization)
- [Data Models](#data-models)
- [Security](#security)
- [Caching](#caching)
- [Testing](#testing)

## 🛠 Technologies

### Main Frameworks and Libraries
- **Spring Boot 3.5.7** - Main framework
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Database operations
- **PostgreSQL 42.7.8** - Relational database
- **JWT (jjwt 0.13.0)** - Token-based authentication
- **MapStruct 1.6.3** - Entity-DTO mapping
- **Lombok 1.18.42** - Boilerplate code reduction
- **Caffeine** - In-memory caching
- **Thymeleaf** - Email templates

### Frontend
- **React 19** - UI library
- **Vite** - Build tool and dev server
- **TailwindCSS** - Styling
- **Axios** - HTTP client
- **React Router** - Routing

### Deployment
- **Docker** - Containerization
- **Docker Compose** - Container orchestration
- **Nginx** - Web server for frontend

### Java Version
- **Java 21**

## 📁 Project Structure

```
src/main/java/org/example/
├── Main.java
├── DataInitializer.java
├── config/
│   ├── SecurityConfig.java
│   ├── WebConfig.java
│   ├── AsyncConfig.java
│   ├── GlobalExceptionHandler.java
│   └── OpenApiConfig.java
├── controller/
│   ├── AuthController.java
│   ├── ProductController.java
│   ├── OrderController.java
│   ├── UserController.java
│   ├── ImageController.java
│   └── PasswordResetController.java
├── dto/
│   ├── CreateOrderRequest.java
│   ├── ForgotPasswordRequest.java
│   ├── JwtResponse.java
│   ├── LoginRequest.java
│   ├── MessageResponse.java
│   ├── OrderItemRequest.java
│   ├── OrderItemResponse.java
│   ├── OrderResponse.java
│   ├── ProductRequest.java
│   ├── ProductResponse.java
│   ├── ResetPasswordRequest.java
│   ├── UserRegistrationRequest.java
│   ├── UserResponse.java
│   └── UserUpdateResponse.java
├── exception/
│   ├── UserAlreadyExistException.java
│   └── UserNotFoundException.java
├── mapper/
│   ├── OrderMapper.java
│   ├── ProductMapper.java
│   └── UserMapper.java
├── model/
│   ├── Order.java
│   ├── OrderItem.java
│   ├── PasswordResetToken.java
│   ├── BlacklistedToken.java
│   ├── Product.java
│   ├── User.java
│   └── enums/
│       ├── Permission.java
│       └── Role.java
├── repository/
│   ├── OrderItemRepository.java
│   ├── OrderRepository.java
│   ├── ProductRepository.java
│   ├── UserRepository.java
│   ├── PasswordResetRepository.java
│   └── BlacklistedTokenRepository.java
├── security/
│   ├── JwtAuthenticationFilter.java
│   └── JwtUtil.java
└── service/
    ├── email/
    │   └── EmailService.java
    ├── impl/
    │   ├── OrderServiceImpl.java
    │   ├── ProductServiceImpl.java
    │   ├── UserDetailsImpl.java
    │   └── UserDetailsServiceImpl.java
    ├── order/
    │   └── OrderService.java
    ├── user/
    │   └── UserService.java
    ├── JwtService.java
    ├── PasswordResetService.java
    └── ProductService.java

frontend/
├── src/
│   ├── assets/
│   ├── components/
│   ├── context/
│   ├── pages/
│   ├── services/
│   └── utils/
├── public/
├── index.html
├── package.json
└── vite.config.js

deploy/
├── backend.Dockerfile
├── frontend.Dockerfile
├── docker-compose.yml
└── nginx/
    └── default.conf
```

## 📦 Requirements

- **Java 21** or higher
- **Maven 3.6+**
- **PostgreSQL 12+**
- **Node.js 20+** (for frontend development)
- **Docker & Docker Compose** (for deployment)
- **IDE** (IntelliJ IDEA, Eclipse, VS Code)

## 🚀 Installation and Running

### 1. Cloning and Setup

```bash
# Clone the repository (if in GIT)
git clone <repository-url>
cd E-stop
```

### 2. Database

Create a PostgreSQL database:

```sql
CREATE DATABASE "E-stop";
```

Or use an existing database and modify `application.properties`.

### 3. Configuration

Edit `src/main/resources/application.properties`:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5433/E-stop
spring.datasource.username=postgres
spring.datasource.password=your-password

# JWT secret (change to a secure key!)
jwt.secret=your-secure-secret-key

# CORS (adjust according to your frontend)
app.cors.allowed-origins=http://localhost:5173
```

### 4. Build and Run

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will run on `http://localhost:8080`

### 5. Running Frontend (Development)

```bash
cd frontend
npm install
npm run dev
```

Frontend will run on `http://localhost:5173`

### 6. Docker Deployment (Production)

To run the entire application (database, backend, frontend) using Docker Compose:

```bash
cd deploy
docker-compose up -d --build
```

- Frontend: `http://localhost:80`
- Backend API: `http://localhost:8080`
- Database: port `5432`

### 7. Verification

Open your browser and visit:
- Frontend: `http://localhost:5173` (dev) or `http://localhost:80` (docker)
- Health check: `http://localhost:8080/actuator/health`
- API base: `http://localhost:8080/api`

## ⚙️ Configuration

### Database (application.properties)

```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/E-stop
spring.datasource.username=postgres
spring.datasource.password=password
spring.jpa.hibernate.ddl-auto=update  # update, create, validate, none
spring.jpa.show-sql=true
```

### JWT

```properties
jwt.secret=!NjMcLFCUT0W@fnznEoz!9kRcMhihoFM
jwt.access-token-expiration-ms=3600000    # 1 hour
jwt.refresh-token-expiration-ms=86400000  # 24 hours
```

### Caching (Caffeine)

```properties
spring.cache.type=caffeine
spring.cache.caffeine.spec=maximumSize=100,expireAfterWrite=10m
```

### CORS

```properties
app.cors.allowed-origins=http://localhost:5173,http://localhost:5174
```

### Email (optional)

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=email@gmail.com
spring.mail.password=app-password
app.mail.from=noreply@eshop.cz
```

### Frontend (.env)

Create a `frontend/.env` file for environment configuration:

```properties
VITE_API_BASE_URL=http://localhost:8080/api
```

## 📡 API Documentation

Base URL: `http://localhost:8080/api`

### Authentication (`/api/auth`)

#### Registration
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "message": "Registration successful",
  "username": "john_doe",
  "email": "john@example.com"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "john_doe",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "john_doe",
  "roles": [
    {
      "authority": "ROLE_USER"
    }
  ]
}
```

#### Token Validation
```http
GET /api/auth/validate
Authorization: Bearer <token>
```

### Products (`/api/products`)

#### Get All Products
```http
GET /api/products
```
**Public endpoint** - no authentication required

**Response:**
```json
[
  {
    "id": 1,
    "name": "Laptop",
    "description": "Powerful laptop",
    "price": 25000.00,
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  }
]
```

#### Get Product by ID
```http
GET /api/products/{id}
```
**Public endpoint**

#### Create Product (ADMIN)
```http
POST /api/products
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "New product",
  "description": "Product description",
  "price": 1500.00
}
```

#### Update Product (ADMIN)
```http
PUT /api/products/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Updated product",
  "description": "New description",
  "price": 1800.00
}
```

#### Delete Product (ADMIN)
```http
DELETE /api/products/{id}
Authorization: Bearer <token>
```

### Orders (`/api/orders`)

#### Create Order
```http
POST /api/orders
Authorization: Bearer <token>
Content-Type: application/json

{
  "productName": "Laptop",
  "quantity": 2,
  "Price": 25000.00
}
```

**Note:** The `Price` field must have a capital P for frontend compatibility.

#### Get User Orders
```http
GET /api/orders
Authorization: Bearer <token>
```

#### Get All Orders (ADMIN)
```http
GET /api/orders/all
Authorization: Bearer <token>
```

#### Get Order by ID
```http
GET /api/orders/{orderId}
Authorization: Bearer <token>
```

### Users (`/api/user`)

#### Get Logged-in User Information
```http
GET /api/user/me
Authorization: Bearer <token>
```

**Response:**
```json
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "roles": "[ROLE_USER]"
}
```

#### Get User by ID
```http
GET /api/user/{userId}
Authorization: Bearer <token>
```

#### Update User
```http
PUT /api/user/{userId}
Authorization: Bearer <token>
Content-Type: application/json

{
  "username": "new_username",
  "email": "new@email.com"
}
```

#### Get All Users (ADMIN)
```http
GET /api/user
Authorization: Bearer <token>
```

#### Delete User (ADMIN)
```http
DELETE /api/user/{userId}
Authorization: Bearer <token>
```

## 🔐 Authentication and Authorization

### JWT Token

The application uses JWT (JSON Web Tokens) for authentication. After successful login, you receive a token that must be sent in the header of every request:

```http
Authorization: Bearer <token>
```

### Roles

The application supports two roles:
- **ROLE_USER** - Regular user (can create orders, view their own orders)
- **ROLE_ADMIN** - Administrator (full access, product management, view all orders)

### Public Endpoints

These endpoints do not require authentication:
- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/validate`
- `GET /api/products`
- `GET /api/products/{id}`

### Protected Endpoints

All other endpoints require a valid JWT token.

## 📊 Data Models

### User
```java
- id: Long
- username: String (unique, 3-30 characters)
- email: String (unique, 5-50 characters)
- password: String (hashed, 6-100 characters)
- roles: Set<Role> (ROLE_USER, ROLE_ADMIN)
```

### Product
```java
- id: Long
- name: String (unique, max 100 characters)
- description: String (max 1000 characters)
- price: BigDecimal (min 0.01, precision 10, scale 2)
- createdAt: LocalDateTime
- updatedAt: LocalDateTime
```

### Order
```java
- id: Long
- user: User (ManyToOne)
- product: Product (ManyToOne)
- orderItems: List<OrderItem> (OneToMany)
- quantity: int
- totalPrice: BigDecimal
- orderDate: LocalDateTime
```

### OrderItem
```java
- id: Long
- order: Order (ManyToOne)
- productId: Long
- productName: String
- quantity: int
- price: BigDecimal (unit price)
- totalPrice: BigDecimal (total price)
```

## 🔒 Security

### Implemented Security Measures

1. **JWT Authentication**
    - Token-based authentication
    - Automatic token verification in every request
    - Token expires after 1 hour

2. **Password Encoding**
    - Passwords are hashed using BCrypt
    - Never stored in plain text

3. **CORS Protection**
    - Configured for specific origins
    - Allowed methods: GET, POST, PUT, DELETE, OPTIONS

4. **CSRF Protection**
    - Disabled for REST API (stateless)

5. **Role-based Access Control**
    - Permission checks at method level using `@PreAuthorize`

### Production Recommendations

1. **Change JWT secret** to a strong, random key
2. **Use HTTPS** for all communications
3. **Set proper CORS** for production domains
4. **Change database password** to a strong password
5. **Disable SQL logging** in production (`spring.jpa.show-sql=false`)
6. **Use environment variables** instead of hardcoded values

## 💾 Caching

The application uses **Caffeine** for in-memory caching:

- **Products:** Cache for all products and products by ID
- **Orders:** Cache for orders by user and all orders
- **Users:** Cache for users by username and ID

**Configuration:**
- Maximum size: 100 items
- Expiration: 10 minutes after write

Cache is automatically invalidated on changes (create, update, delete).

## 🧪 Testing

### Running Tests

```bash
mvn test
```

### Test Classes

- `OrderControllerTest.java` - Order controller REST tests
- `OrderServiceImplTest.java` - Order business logic tests
- `ProductServiceImplTest.java` - Product business logic tests
- `LoginDtoTest.java` - DTO validation tests
- `RegisterDtoTest.java` - DTO validation tests

### Test Database

For testing, we recommend using H2 in-memory database or a separate PostgreSQL test database.

## 📝 Usage Examples

### cURL Examples

#### Registration
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test_user",
    "email": "test@example.com",
    "password": "password123"
  }'
```

#### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test_user",
    "password": "password123"
  }'
```

#### Get Products
```bash
curl http://localhost:8080/api/products
```

#### Create Order
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "productName": "Laptop",
    "quantity": 1,
    "Price": 25000.00
  }'
```

## 🐛 Troubleshooting

### Database Connection Failed
- Check if PostgreSQL is running
- Verify credentials in `application.properties`
- Check if the database exists

### 401 Unauthorized
- Verify that the token hasn't expired
- Check the header format: `Authorization: Bearer <token>`
- Check if the token is valid

### 403 Forbidden
- Verify that you have the correct role (ADMIN for admin endpoints)
- Check if you're trying to access someone else's data

### CORS Errors
- Edit `app.cors.allowed-origins` in `application.properties`
- Check if frontend is running on the allowed URL

## 📚 Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security Documentation](https://spring.io/projects/spring-security)
- [JWT.io](https://jwt.io/) - Information about JWT
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)

## 👥 Author

Created as a demonstration of a modern web application with Java and Spring Boot.

## 📄 License

This project is created for educational purposes.

---

**Version:** 1.0-SNAPSHOT  
**Spring Boot:** 3.5.7
**Java:** 21
