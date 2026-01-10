# Suraksha

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue)
![Redis](https://img.shields.io/badge/Redis-latest-red)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3-orange)
![License](https://img.shields.io/badge/License-MIT-yellow)

A production-ready, multi-tenant authentication service built with Spring Boot. Suraksha provides comprehensive authentication solutions including email/password login, passwordless magic URLs, two-factor authentication (2FA), and secure token management. Built with Redis for caching, RabbitMQ for asynchronous email delivery, and PostgreSQL for persistence, it offers enterprise-grade security with RS256 JWT signing, refresh token rotation, and organization-level isolation.

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [API Endpoints](#api-endpoints)
- [JWT Token Claims](#jwt-token-claims)
- [Email Features](#email-features)
- [Getting Started](#getting-started)
- [Security Features](#security-features)
- [Database Schema](#database-schema)
- [Development](#development)
- [Troubleshooting](#troubleshooting)

## Features

### Authentication Methods

- **Email/Password Login** - Traditional authentication with Bcrypt password hashing
- **Magic URL Authentication** - Passwordless login via secure email links (10-minute expiration)
- **Two-Factor Authentication (2FA)** - Optional OTP-based 2FA with 4-digit codes (2-5 minute expiration)
- **Token Refresh** - Secure refresh token rotation with HttpOnly cookies
- **Logout** - Token revocation and session termination

### Security & Tokens

- **JWT Authentication** - RS256-signed JWTs with 15-minute expiration
- **JWKS Endpoint** - Standard `.well-known/jwks.json` for public key distribution
- **Password Security** - Bcrypt hashing with salt
- **Token Security** - SHA256 hashing for refresh tokens and magic URLs
- **Redirect Validation** - Organization-scoped redirect URL whitelisting

### Organization & Multi-Tenancy

- **Multi-Tenant Organizations** - Isolated user namespaces per organization
- **API Keys** - HMAC-SHA256 hashed API keys for organization access (infrastructure in place)
- **User Isolation** - Same email can exist across different organizations

### Email & Communication

- **Email Templates** - Professional HTML templates for OTP and magic URLs with dark mode support
- **RabbitMQ Integration** - Asynchronous email delivery queue
- **Responsive Design** - Mobile-friendly email templates

### Password Management

- **Forgot Password** - Secure token-based password reset flow
- **Password Reset Tokens** - Base64-encoded 64-byte random tokens with Redis storage

### Infrastructure

- **PostgreSQL** - Persistent storage with JPA/Hibernate
- **Redis** - Token caching, OTP storage, and session management
- **RabbitMQ** - Message queue for asynchronous email delivery
- **Flyway** - Database migrations

## Tech Stack

| Component        | Technology      |
| ---------------- | --------------- |
| Framework        | Spring Boot 4.0 |
| Language         | Java 21         |
| Database         | PostgreSQL 15   |
| Cache            | Redis           |
| Message Queue    | RabbitMQ        |
| Migrations       | Flyway          |
| JWT Library      | jjwt 0.12.5     |
| Build Tool       | Maven           |
| Containerization | Docker Compose  |

## Project Structure

```
suraksha/
├── docker-compose.yml          # PostgreSQL container
├── suraksha/                   # Spring Boot application
│   ├── pom.xml
│   └── src/main/java/com/aneesh/suraksha/
│       ├── SurakshaApplication.java
│       ├── SecurityConfig.java
│       ├── config/
│       │   ├── AppSecretConfig.java       # JWT secrets configuration
│       │   └── RabbitMQConfig.java        # RabbitMQ setup
│       ├── redis/
│       │   └── configuration/
│       │       └── RedisConfig.java       # Redis configuration
│       ├── dto/
│       │   └── MailDto.java               # Email DTOs
│       └── users/
│           ├── component/                 # Utility components
│           │   ├── ClientIPAddress.java
│           │   ├── OrganisationIdGenerator.java
│           │   └── RefreshTokenGenerator.java
│           ├── configuration/
│           │   └── PasswordEncoderConfig.java
│           ├── controller/                # REST endpoints
│           │   ├── JwksController.java    # JWKS endpoint
│           │   ├── TestController.java    # Test endpoints
│           │   └── UserController.java    # Auth & user endpoints
│           ├── dto/                       # Data transfer objects
│           │   ├── Auth DTOs (Login, Register, etc.)
│           │   ├── Magic Link DTOs
│           │   ├── OTP DTOs
│           │   ├── Organization DTOs
│           │   └── Token DTOs
│           ├── model/                     # JPA entities & repositories
│           │   ├── UserEntity.java
│           │   ├── UserRepository.java
│           │   ├── Organisations.java
│           │   ├── OrganisationsRepository.java
│           │   ├── RefreshToken.java
│           │   └── RefreshTokenRepository.java
│           └── service/                   # Business logic
│               ├── LoginService.java
│               ├── RegistrationService.java
│               ├── MagicUrlService.java
│               ├── TwofactorService.java
│               ├── OtpService.java
│               ├── ForgotPasswordService.java
│               ├── EmailTemplateService.java
│               ├── MailSenderService.java
│               ├── JwtService.java
│               ├── RefreshTokenService.java
│               ├── RefreshCheck.java
│               ├── LogoutService.java
│               ├── ValidRedirectService.java
│               ├── HashingService.java
│               ├── HmacService.java
│               └── OrganisationOnboard.java
```

## API Endpoints

### Authentication

#### Register User

```http
POST /api/v1/auth/register
Content-Type: application/json

{
  "mailId": "user@example.com",
  "password": "securepassword",
  "organisationId": "org_xxxxx"
}
```

**Response:**

- Sets `jwt` cookie (HttpOnly, 15 minutes)
- Sets `refresh_token` cookie (HttpOnly, 30 minutes)
- Sets `refresh_token_id` cookie (HttpOnly, 30 minutes)

```json
{
  "status": true,
  "message": "User Created Successfully",
  "token": "eyJhbGc..."
}
```

#### Login

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "mailId": "user@example.com",
  "password": "securepassword",
  "organisationId": "org_xxxxx",
  "redirect": "https://app.example.com/dashboard" // Optional
}
```

**Response (without 2FA):**

```json
{
  "status": true,
  "message": "Success"
}
```

**Response (with 2FA enabled):**

```json
{
  "status": true,
  "message": "OTP sent to your email",
  "twoFaRequired": true
}
```

#### Magic URL Authentication

Request a magic sign-in link via email:

```http
POST /api/v1/magic-url
Content-Type: application/json

{
  "mailId": "user@example.com",
  "organisationId": "org_xxxxx",
  "redirect": "https://app.example.com/dashboard" // Optional
}
```

**Response:**

```json
{
  "status": true,
  "message": "Magic URL sent to email"
}
```

Verify the magic URL (user clicks the link in email):

```http
GET /api/v1/verify-magic-url?token=<magic_token>&redirect=<redirect_url>
```

**Response:**

- Sets JWT and refresh token cookies
- Redirects to the specified URL or default

#### Two-Factor Authentication (OTP)

Verify OTP after login:

```http
POST /api/v1/auth/2fa/otp
Content-Type: application/json

{
  "mailId": "user@example.com",
  "otp": "1234",
  "organisationId": "org_xxxxx",
  "redirect": "https://app.example.com/dashboard" // Optional
}
```

**Response:**

```json
{
  "status": true,
  "message": "OTP verified successfully"
}
```

#### Token Refresh

```http
POST /api/v1/auth/refresh
Cookie: refresh_token=<token>; refresh_token_id=<token_id>
```

**Response:**

- Sets new `jwt` cookie (HttpOnly, 15 minutes)
- Sets new `refresh_token` cookie (HttpOnly, 30 minutes)
- Sets new `refresh_token_id` cookie (HttpOnly, 30 minutes)

```json
{
  "status": true,
  "message": "Token refreshed successfully"
}
```

#### Logout

```http
POST /api/v1/auth/logout
Cookie: refresh_token_id=<token_id>
```

**Response:**

```json
{
  "status": true,
  "message": "Logged out successfully"
}
```

#### Forgot Password

Request a password reset token:

```http
POST /api/v1/forgot-password
Content-Type: application/json

{
  "mailId": "user@example.com"
}
```

**Response:**

```json
{
  "resetToken": "base64-encoded-token",
  "message": "Password reset token generated"
}
```

> Note: In production, the reset token should be sent via email, not returned in the response.

### Organizations

#### Register Organization

```http
POST /api/v1/organisations
Content-Type: application/json

{
  "name": "My Company"
}
```

**Response:**

```json
{
  "id": "org_xxxxx",
  "name": "My Company",
  "apiKey": "suraksha_apiKey_xxxxxxxx"
}
```

> ⚠️ **Important:** The API key is only returned once during creation. Store it securely.

#### List Organizations

```http
GET /api/v1/organisations
```

### Users

#### List All Users

```http
GET /api/v1/users
```

### JWKS

#### Get Public Keys

```http
GET /.well-known/jwks.json
```

**Response:**

```json
{
  "keys": [
    {
      "kty": "RSA",
      "use": "sig",
      "alg": "RS256",
      "n": "...",
      "e": "AQAB"
    }
  ]
}
```

## JWT Token Claims

Tokens issued by Suraksha include the following claims:

| Claim            | Description                   |
| ---------------- | ----------------------------- |
| `sub`            | User's email address          |
| `userId`         | User's UUID                   |
| `mailId`         | User's email address          |
| `organisationId` | Organization ID               |
| `iat`            | Issued at timestamp           |
| `exp`            | Expiration timestamp (15 min) |

## Email Features

Suraksha includes a comprehensive email system for authentication workflows:

### Email Templates

Professional HTML email templates with responsive design:

- **OTP Emails** - 4-digit OTP codes with monospace formatting and spacing for clarity
- **Magic URL Emails** - Secure sign-in links with button and raw link display
- **Dark Mode Support** - Automatic color scheme detection for both light and dark preferences
- **Responsive Design** - Mobile-friendly layouts that work across all devices
- **Branded Design** - Includes Suraksha branding with GitHub logo integration

### Asynchronous Email Delivery

- **RabbitMQ Integration** - All emails are sent asynchronously via RabbitMQ message queue
- **Email Queue** - `EMAIL_EXCHANGE` and `EMAIL_ROUTING_KEY` configuration
- **Non-blocking** - Email sending doesn't block API responses
- **Reliability** - Message queue ensures email delivery even during high load

### Email Service Architecture

```
User Request → Controller → Service (Generate Token/OTP)
                               ↓
                           MailSenderService
                               ↓
                           RabbitMQ Queue
                               ↓
                          Email Service
                               ↓
                         SMTP Server → User's Inbox
```

### Email Types

1. **OTP Emails** (2-5 minute expiration)
   - Sent during 2FA login flow
   - 4-digit random code (1000-9999)
   - Clear expiration notice
   - Security messaging

2. **Magic URL Emails** (10-minute expiration)
   - Sent during passwordless authentication
   - Secure, time-limited links
   - Click-to-authenticate button
   - Backup raw link for accessibility

3. **Password Reset Emails** (infrastructure in place)
   - Token-based reset flow
   - Secure reset links

## Getting Started

### Prerequisites

- Java 21+
- Docker & Docker Compose
- Maven
- Redis (via Docker or local installation)
- RabbitMQ (via Docker or local installation)

### 1. Start Required Services

Start PostgreSQL, Redis, and RabbitMQ using Docker:

```bash
# Start PostgreSQL (port 5433)
docker-compose up -d

# Start Redis (port 6379)
docker run -d --name suraksha-redis -p 6379:6379 redis:latest

# Start RabbitMQ (ports 5672, 15672)
docker run -d --name suraksha-rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

**Service Ports:**
- PostgreSQL: `5433`
- Redis: `6379`
- RabbitMQ: `5672` (AMQP), `15672` (Management UI)

### 2. Configure Application

Create `suraksha/src/main/resources/application.properties`:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5433/my_database
spring.datasource.username=admin
spring.datasource.password=securepassword

# JPA
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true

# Flyway
spring.flyway.enabled=true

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379

# RabbitMQ
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest

# Email Queue Configuration
rabbitmq.exchange.name=EMAIL_EXCHANGE
rabbitmq.routing.key=EMAIL_ROUTING_KEY

# RSA Keys (generate using openssl)
rsa.private-key=-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----
rsa.public-key=-----BEGIN PUBLIC KEY-----\n...\n-----END PUBLIC KEY-----

# Secrets (base64 encoded)
app.secret.key=<base64-secret-for-jwt>
app.secret_refresh.key=<base64-secret-for-refresh-tokens>
app.secret_api.key=<base64-secret-for-api-keys>
```

### 3. Generate RSA Keys

```bash
# Generate private key
openssl genrsa -out private.key 2048

# Convert to PKCS#8 format
openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in private.key -out private_pkcs8.key

# Generate public key
openssl rsa -in private.key -pubout -out public.key
```

### 4. Run the Application

```bash
cd suraksha
./mvnw spring-boot:run
```

The server starts on `http://localhost:8080`.

## Security Features

### Token Security

- **RS256 Signing**: Asymmetric cryptography allows public key verification
- **Short-lived JWTs**: 15-minute expiration minimizes token theft impact
- **Refresh Token Rotation**: New refresh token issued on each use
- **HttpOnly Cookies**: Tokens are stored in HttpOnly cookies, preventing XSS attacks
- **Secure & SameSite Flags**: All cookies have Secure and SameSite=Strict attributes

### Password Security

- **Bcrypt Hashing**: Industry-standard password hashing with salt
- **No Plain Text**: Passwords are never stored or logged

### API Key Security

- **HMAC-SHA256 Hashing**: API keys are hashed before storage
- **One-time Display**: API keys are only shown at creation time

### Refresh Token Security

- **SHA256 Hashing**: Refresh tokens are hashed before Redis storage
- **IP & User-Agent Tracking**: Each refresh token records client metadata
- **Revocation Support**: Tokens can be individually revoked via logout
- **30-minute Expiration**: Configurable expiration period
- **Redis-backed Storage**: Fast, distributed token validation

### Magic URL & OTP Security

- **Time-limited Tokens**: Magic URLs expire in 10 minutes, OTPs in 2-5 minutes
- **SHA256 Hashing**: Magic URL tokens are hashed before Redis storage
- **Single-use Tokens**: Tokens are deleted from Redis after successful verification
- **Random Generation**: SecureRandom for cryptographically secure token generation
- **Redis TTL**: Automatic expiration via Redis time-to-live

### Redirect Validation

- **Whitelist-based**: Only organization-scoped redirect URLs are allowed
- **URL Validation**: Prevents open redirect vulnerabilities
- **Organization Isolation**: Each organization maintains its own redirect whitelist

### Password Reset Security

- **Secure Token Generation**: 64-byte Base64-encoded random tokens
- **Redis Storage**: Tokens stored temporarily with TTL
- **One-time Use**: Infrastructure in place for single-use reset tokens

### Request Metadata Tracking

- **Client IP Tracking**: All authenticated requests track client IP addresses
- **User-Agent Logging**: Browser and device information recorded
- **Audit Trail**: Metadata stored with refresh tokens for security monitoring

## Database Schema

### Tables

- `users` - User accounts with unique email per organization
- `organisations` - Multi-tenant organizations with API keys
- `refresh-tokens` - Refresh token storage with metadata

### Indexes

- `refresh-tokens.id` - Primary key index
- `refresh-tokens.token` - Token lookup index

## Development

### Build

```bash
cd suraksha
./mvnw clean package
```

### Run Tests

```bash
./mvnw test
```

## Troubleshooting

### Database Connection Refused

- Ensure the PostgreSQL Docker container is running: `docker ps`
- Check if port `5433` is available: `lsof -i :5433`
- Verify credentials in `application.properties`

### Redis Connection Issues

- Ensure Redis is running: `docker ps | grep redis`
- Test Redis connection: `redis-cli -h localhost -p 6379 ping`
- Check if port `6379` is available: `lsof -i :6379`
- Verify Redis configuration in `application.properties`

### RabbitMQ Connection Issues

- Ensure RabbitMQ is running: `docker ps | grep rabbitmq`
- Check RabbitMQ management UI: `http://localhost:15672` (guest/guest)
- Verify port `5672` is available: `lsof -i :5672`
- Check RabbitMQ logs: `docker logs suraksha-rabbitmq`
- Verify exchange and routing key configuration in `application.properties`

### Email Not Sending

- Verify RabbitMQ is running and connected
- Check RabbitMQ management UI for queue messages
- Ensure email service consumer is running to process the queue
- Verify SMTP configuration (if applicable)

### Magic URL or OTP Not Working

- Ensure Redis is running and accessible
- Check Redis for stored tokens: `redis-cli -h localhost -p 6379 KEYS "*"`
- Verify token expiration times (10 min for magic URLs, 2-5 min for OTPs)
- Check application logs for token generation and validation errors

### Key Generation Errors

- Ensure `openssl` is installed
- Verify the keys are correctly copied to `application.properties` (remove newlines if necessary, though Spring Boot handles them)

### Token Validation Failures

- Verify RSA public key matches the private key used for signing
- Check JWT expiration times (15 minutes for access tokens)
- Ensure refresh token exists in Redis
- Verify client IP and User-Agent match (if strict validation is enabled)

## Author

Aneesh Patne
