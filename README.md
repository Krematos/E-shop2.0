# E-Shop Backend - Spring Boot REST API

Kompletní REST API backend pro e-commerce aplikaci postavený na Spring Boot 3.5.3 s PostgreSQL databází, JWT autentizací a pokročilými funkcemi jako caching a asynchronní zpracování.

## 📋 Obsah

- [Technologie](#technologie)
- [Struktura projektu](#struktura-projektu)
- [Požadavky](#požadavky)
- [Instalace a spuštění](#instalace-a-spuštění)
- [Konfigurace](#konfigurace)
- [API Dokumentace](#api-dokumentace)
- [Autentizace a autorizace](#autentizace-a-autorizace)
- [Datové modely](#datové-modely)
- [Bezpečnost](#bezpečnost)
- [Caching](#caching)
- [Testování](#testování)

## 🛠 Technologie

### Hlavní frameworky a knihovny
- **Spring Boot 3.5.7** - Hlavní framework
- **Spring Security** - Autentizace a autorizace
- **Spring Data JPA** - Práce s databází
- **PostgreSQL 42.7.8** - Relační databáze
- **JWT (jjwt 0.13.0)** - Token-based autentizace
- **MapStruct 1.6.0** - Mapování mezi entitami a DTO
- **Lombok 1.18.34** - Redukce boilerplate kódu
- **Caffeine** - In-memory caching
- **Thymeleaf** - Šablony pro e-maily

### Frontend
- **React 19** - UI knihovna
- **Vite** - Build tool a dev server
- **TailwindCSS** - Styling
- **Axios** - HTTP klient
- **React Router** - Routování

### Deployment
- **Docker** - Kontejnerizace
- **Docker Compose** - Orchestrace kontejnerů
- **Nginx** - Web server pro frontend

### Java verze
- **Java 21**

## 📁 Struktura projektu

```
src/main/java/org/example/
├── Main.java
├── DataInitializer.java
├── config/
│   ├── SecurityConfig.java
│   ├── WebConfig.java
│   └── AsyncConfig.java
├── controller/
│   ├── AuthController.java
│   ├── ProductController.java
│   ├── OrderController.java
│   └── UserController.java
├── dto/
│   ├── ForgotPasswordRequest.java
│   ├── LoginDto.java
│   ├── OrderDto.java
│   ├── OrderItemDto.java
│   ├── ProductDto.java
│   ├── RegisterDto.java
│   ├── ResetPasswordRequest.java
│   ├── UserDto.java
│   └── UserUpdateDto.java
├── mapper/
│   ├── OrderItemMapper.java
│   ├── OrderMapper.java
│   ├── ProductMapper.java
│   └── UserMapper.java
├── model/
│   ├── Order.java
│   ├── OrderItem.java
│   ├── PasswordResetToken.java
│   ├── Product.java
│   └── User.java
├── repository/
│   ├── OrderItemRepository.java
│   ├── OrderRepository.java
│   ├── ProductRepository.java
│   └── UserRepository.java
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

## 📦 Požadavky

- **Java 21** nebo vyšší
- **Maven 3.6+**
- **PostgreSQL 12+**
- **Node.js 20+** (pro frontend vývoj)
- **Docker & Docker Compose** (pro deployment)
- **IDE** (IntelliJ IDEA, Eclipse, VS Code)

## 🚀 Instalace a spuštění

### 1. Klonování a příprava

```bash
# Klonujte repozitář (pokud je v GIT)
git clone <repository-url>
cd E-stop
```

### 2. Databáze

Vytvořte PostgreSQL databázi:

```sql
CREATE DATABASE "E-stop";
```

Nebo použijte existující databázi a upravte `application.properties`.

### 3. Konfigurace

Upravte `src/main/resources/application.properties`:

```properties
# Databáze
spring.datasource.url=jdbc:postgresql://localhost:5433/E-stop
spring.datasource.username=postgres
spring.datasource.password=vaše-heslo

# JWT secret (změňte na bezpečný klíč!)
jwt.secret=vaše-bezpečný-secret-klíč

# CORS (upravte podle vašeho frontendu)
app.cors.allowed-origins=http://localhost:5173
```

### 4. Sestavení a spuštění

```bash
# Sestavení projektu
mvn clean install

# Spuštění aplikace
mvn spring-boot:run
```

Aplikace poběží na `http://localhost:8080`

### 5. Spuštění Frontendu (Vývoj)

```bash
cd frontend
npm install
npm run dev
```

Frontend poběží na `http://localhost:5173`

### 6. Docker Deployment (Produkce)

Pro spuštění celé aplikace (databáze, backend, frontend) pomocí Docker Compose:

```bash
cd deploy
docker-compose up -d --build
```

- Frontend: `http://localhost:80`
- Backend API: `http://localhost:8080`
- Databáze: port `5432`

### 7. Ověření

Otevřete prohlížeč a navštivte:
- Frontend: `http://localhost:5173` (dev) nebo `http://localhost:80` (docker)
- Health check: `http://localhost:8080/actuator/health`
- API base: `http://localhost:8080/api`

## ⚙️ Konfigurace

### Databáze (application.properties)

```properties
spring.datasource.url=jdbc:postgresql://localhost:5433/E-stop
spring.datasource.username=postgres
spring.datasource.password=heslo
spring.jpa.hibernate.ddl-auto=update  # update, create, validate, none
spring.jpa.show-sql=true
```

### JWT

```properties
jwt.secret=!NjMcLFCUT0W@fnznEoz!9kRcMhihoFM
jwt.access-token-expiration-ms=3600000    # 1 hodina
jwt.refresh-token-expiration-ms=86400000  # 24 hodin
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

### E-mail (volitelné)

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=email@gmail.com
spring.mail.password=aplikacni-heslo
spring.mail.password=aplikacni-heslo
app.mail.from=noreply@eshop.cz
```

### Frontend (.env)

Vytvořte soubor `frontend/.env` pro konfiguraci prostředí:

```properties
VITE_API_BASE_URL=http://localhost:8080/api
```

## 📡 API Dokumentace

Base URL: `http://localhost:8080/api`

### Autentizace (`/api/auth`)

#### Registrace
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "jan_novak",
  "email": "jan@example.com",
  "password": "heslo123"
}
```

**Odpověď:**
```json
{
  "message": "Registrace proběhla úspěšně",
  "username": "jan_novak",
  "email": "jan@example.com"
}
```

#### Přihlášení
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "jan_novak",
  "password": "heslo123"
}
```

**Odpověď:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "jan_novak",
  "roles": [
    {
      "authority": "ROLE_USER"
    }
  ]
}
```

#### Validace tokenu
```http
GET /api/auth/validate
Authorization: Bearer <token>
```

### Produkty (`/api/products`)

#### Získání všech produktů
```http
GET /api/products
```
**Veřejný endpoint** - nevyžaduje autentizaci

**Odpověď:**
```json
[
  {
    "id": 1,
    "name": "Notebook",
    "description": "Výkonný notebook",
    "price": 25000.00,
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  }
]
```

#### Získání produktu podle ID
```http
GET /api/products/{id}
```
**Veřejný endpoint**

#### Vytvoření produktu (ADMIN)
```http
POST /api/products
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Nový produkt",
  "description": "Popis produktu",
  "price": 1500.00
}
```

#### Aktualizace produktu (ADMIN)
```http
PUT /api/products/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Upravený produkt",
  "description": "Nový popis",
  "price": 1800.00
}
```

#### Smazání produktu (ADMIN)
```http
DELETE /api/products/{id}
Authorization: Bearer <token>
```

### Objednávky (`/api/orders`)

#### Vytvoření objednávky
```http
POST /api/orders
Authorization: Bearer <token>
Content-Type: application/json

{
  "productName": "Notebook",
  "quantity": 2,
  "Price": 25000.00
}
```

**Poznámka:** Pole `Price` musí mít velké P pro kompatibilitu s frontendem.

#### Získání objednávek uživatele
```http
GET /api/orders
Authorization: Bearer <token>
```

#### Získání všech objednávek (ADMIN)
```http
GET /api/orders/all
Authorization: Bearer <token>
```

#### Získání objednávky podle ID
```http
GET /api/orders/{orderId}
Authorization: Bearer <token>
```

### Uživatelé (`/api/user`)

#### Získání informací o přihlášeném uživateli
```http
GET /api/user/me
Authorization: Bearer <token>
```

**Odpověď:**
```json
{
  "id": 1,
  "username": "jan_novak",
  "email": "jan@example.com",
  "roles": "[ROLE_USER]"
}
```

#### Získání uživatele podle ID
```http
GET /api/user/{userId}
Authorization: Bearer <token>
```

#### Aktualizace uživatele
```http
PUT /api/user/{userId}
Authorization: Bearer <token>
Content-Type: application/json

{
  "username": "novy_username",
  "email": "novy@email.com"
}
```

#### Získání všech uživatelů (ADMIN)
```http
GET /api/user
Authorization: Bearer <token>
```

#### Smazání uživatele (ADMIN)
```http
DELETE /api/user/{userId}
Authorization: Bearer <token>
```

## 🔐 Autentizace a autorizace

### JWT Token

Aplikace používá JWT (JSON Web Tokens) pro autentizaci. Po úspěšném přihlášení obdržíte token, který musíte posílat v hlavičce každého požadavku:

```http
Authorization: Bearer <token>
```

### Role

Aplikace podporuje dvě role:
- **ROLE_USER** - Běžný uživatel (může vytvářet objednávky, zobrazit své objednávky)
- **ROLE_ADMIN** - Administrátor (plný přístup, správa produktů, zobrazení všech objednávek)

### Veřejné endpointy

Tyto endpointy nevyžadují autentizaci:
- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/validate`
- `GET /api/products`
- `GET /api/products/{id}`

### Chráněné endpointy

Všechny ostatní endpointy vyžadují platný JWT token.

## 📊 Datové modely

### User (Uživatel)
```java
- id: Long
- username: String (unique, 3-30 znaků)
- email: String (unique, 5-50 znaků)
- password: String (hashovaný, 6-100 znaků)
- roles: Set<Role> (ROLE_USER, ROLE_ADMIN)
```

### Product (Produkt)
```java
- id: Long
- name: String (unique, max 100 znaků)
- description: String (max 1000 znaků)
- price: BigDecimal (min 0.01, precision 10, scale 2)
- createdAt: LocalDateTime
- updatedAt: LocalDateTime
```

### Order (Objednávka)
```java
- id: Long
- user: User (ManyToOne)
- product: Product (ManyToOne)
- orderItems: List<OrderItem> (OneToMany)
- quantity: int
- totalPrice: BigDecimal
- orderDate: LocalDateTime
```

### OrderItem (Položka objednávky)
```java
- id: Long
- order: Order (ManyToOne)
- productId: Long
- productName: String
- quantity: int
- price: BigDecimal (cena za jednotku)
- totalPrice: BigDecimal (celková cena)
```

## 🔒 Bezpečnost

### Implementované bezpečnostní opatření

1. **JWT Autentizace**
    - Token-based autentizace
    - Automatické ověřování tokenu v každém požadavku
    - Expirace tokenu po 1 hodině

2. **Password Encoding**
    - Hesla jsou hashována pomocí BCrypt
    - Nikdy nejsou ukládána v plain textu

3. **CORS Protection**
    - Konfigurováno pro specifické originy
    - Povolené metody: GET, POST, PUT, DELETE, OPTIONS

4. **CSRF Protection**
    - Zakázáno pro REST API (stateless)

5. **Role-based Access Control**
    - Kontrola oprávnění na úrovni metod pomocí `@PreAuthorize`

### Doporučení pro produkci

1. **Změňte JWT secret** na silný, náhodný klíč
2. **Použijte HTTPS** pro všechny komunikace
3. **Nastavte správné CORS** pro produkční domény
4. **Změňte databázové heslo** na silné heslo
5. **Zakázat SQL logging** v produkci (`spring.jpa.show-sql=false`)
6. **Použijte environment variables** místo hardcoded hodnot

## 💾 Caching

Aplikace používá **Caffeine** pro in-memory caching:

- **Produkty:** Cache pro všechny produkty a produkty podle ID
- **Objednávky:** Cache pro objednávky podle uživatele a všechny objednávky
- **Uživatelé:** Cache pro uživatele podle username a ID

**Konfigurace:**
- Maximální velikost: 100 položek
- Expirace: 10 minut po zápisu

Cache je automaticky invalidována při změnách (create, update, delete).

## 🧪 Testování

### Spuštění testů

```bash
mvn test
```

### Testovací třídy

- `OrderControllerTest.java` - Testy REST kontroleru objednávek
- `OrderServiceImplTest.java` - Testy business logiky objednávek
- `ProductServiceImplTest.java` - Testy business logiky produktů
- `LoginDtoTest.java` - Testy DTO validace
- `RegisterDtoTest.java` - Testy DTO validace

### Testovací databáze

Pro testování doporučujeme použít H2 in-memory databázi nebo samostatnou PostgreSQL testovací databázi.

## 📝 Příklady použití

### cURL příklady

#### Registrace
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test_user",
    "email": "test@example.com",
    "password": "password123"
  }'
```

#### Přihlášení
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test_user",
    "password": "password123"
  }'
```

#### Získání produktů
```bash
curl http://localhost:8080/api/products
```

#### Vytvoření objednávky
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{
    "productName": "Notebook",
    "quantity": 1,
    "Price": 25000.00
  }'
```

## 🐛 Řešení problémů

### Databáze se nepřipojí
- Zkontrolujte, zda PostgreSQL běží
- Ověřte údaje v `application.properties`
- Zkontrolujte, zda databáze existuje

### 401 Unauthorized
- Ověřte, zda token není vypršený
- Zkontrolujte formát hlavičky: `Authorization: Bearer <token>`
- Zkontrolujte, zda token je platný

### 403 Forbidden
- Ověřte, zda máte správnou roli (ADMIN pro admin endpointy)
- Zkontrolujte, zda se pokoušíte přistupovat k cizím datům

### CORS chyby
- Upravte `app.cors.allowed-origins` v `application.properties`
- Zkontrolujte, zda frontend běží na povolené URL

## 📚 Další zdroje

- [Spring Boot Dokumentace](https://spring.io/projects/spring-boot)
- [Spring Security Dokumentace](https://spring.io/projects/spring-security)
- [JWT.io](https://jwt.io/) - Informace o JWT
- [PostgreSQL Dokumentace](https://www.postgresql.org/docs/)

## 👥 Autor

Vytvořeno jako ukázka moderní webové aplikace s Java a Spring Boot.

## 📄 Licence

Tento projekt je vytvořen pro vzdělávací účely.

---

**Verze:** 1.0-SNAPSHOT  
**Spring Boot:** 3.5.7
**Java:** 21