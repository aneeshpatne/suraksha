# Suraksha 🛡️

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue)
![License](https://img.shields.io/badge/License-MIT-yellow)

A production-ready, multi-tenant authentication service built with Spring Boot. Suraksha provides secure JWT-based authentication with RS256 signing, refresh token rotation, organization management, and JWKS endpoint for token verification.

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [API Endpoints](#api-endpoints)
- [JWT Token Claims](#jwt-token-claims)
- [Getting Started](#getting-started)
- [Security Features](#security-features)
- [Database Schema](#database-schema)
- [Development](#development)
- [Troubleshooting](#troubleshooting)

## Features

- **🔐 JWT Authentication** - RS256-signed JWTs with 15-minute expiration
- **🔄 Refresh Tokens** - Secure refresh token rotation with HttpOnly cookies
- **🏢 Multi-Tenant Organizations** - Isolated user namespaces per organization
- **🔑 API Keys** - HMAC-SHA256 hashed API keys for organization access
- **📜 JWKS Endpoint** - Standard `.well-known/jwks.json` for public key distribution
- **🗄️ PostgreSQL** - Persistent storage with Flyway migrations
- **🔒 Bcrypt Hashing** - Secure password storage

## Tech Stack

| Component        | Technology      |
| ---------------- | --------------- |
| Framework        | Spring Boot 4.0 |
| Language         | Java 21         |
| Database         | PostgreSQL 15   |
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
│       │   └── AppSecretConfig.java
│       └── users/
│           ├── component/      # Generators (API Key, Refresh Token, etc.)
│           ├── configuration/  # Password encoder config
│           ├── controller/     # REST endpoints
│           ├── dto/            # Data transfer objects
│           ├── model/          # JPA entities & repositories
│           └── service/        # Business logic
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

- Sets `jwt` cookie (HttpOnly, 1 hour)
- Sets `refresh_token` cookie (HttpOnly, 7 days)
- Sets `refresh_token_id` cookie (HttpOnly, 7 days)

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
  "organisationId": "org_xxxxx"
}
```

**Response:**

```json
{
  "status": true,
  "message": "Success"
}
```

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

## Getting Started

### Prerequisites

- Java 21+
- Docker & Docker Compose
- Maven

### 1. Start PostgreSQL

```bash
docker-compose up -d
```

This starts a PostgreSQL 15 instance on port `5433`.

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

### Password Security

- **Bcrypt Hashing**: Industry-standard password hashing with salt
- **No Plain Text**: Passwords are never stored or logged

### API Key Security

- **HMAC-SHA256 Hashing**: API keys are hashed before storage
- **One-time Display**: API keys are only shown at creation time

### Refresh Token Security

- **HMAC-SHA256 Hashing**: Refresh tokens are hashed before storage
- **IP & User-Agent Tracking**: Each refresh token records client metadata
- **Revocation Support**: Tokens can be individually revoked
- **7-day Expiration**: Configurable expiration period

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

- Ensure the Docker container is running: `docker ps`
- Check if port `5433` is available.
- Verify credentials in `application.properties`.

### Key Generation Errors

- Ensure `openssl` is installed.
- Verify the keys are correctly copied to `application.properties` (remove newlines if necessary, though Spring Boot handles them).

## Author

Aneesh Patne
