# Suraksha 🔐

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue)
![Redis](https://img.shields.io/badge/Redis-latest-red)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3-orange)
![License](https://img.shields.io/badge/License-MIT-yellow)

A production-ready, **multi-tenant authentication service** built with Spring Boot. Suraksha provides comprehensive authentication solutions including email/password login, passwordless magic URLs, two-factor authentication (2FA), and secure token management. Built with Redis for caching, RabbitMQ for asynchronous email delivery, and PostgreSQL for persistence, it offers enterprise-grade security with RS256 JWT signing, refresh token rotation, and organization-level isolation.

---

## Table of Contents

- [Features](#features)
- [Architecture](#architecture)
  - [System Overview](#system-overview)
  - [Deployment Architecture](#deployment-architecture)
  - [Component Interaction](#component-interaction)
- [Authentication Flows](#authentication-flows)
  - [Email/Password Flow](#emailpassword-flow)
  - [Magic URL Flow](#magic-url-flow)
  - [Two-Factor Authentication Flow](#two-factor-authentication-flow)
  - [Token Refresh Flow](#token-refresh-flow)
  - [Password Reset Flow](#password-reset-flow)
- [Database Schema](#database-schema)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [API Endpoints](#api-endpoints)
- [JWT Token Claims](#jwt-token-claims)
- [Email System](#email-system)
- [Getting Started](#getting-started)
- [Security Features](#security-features)
- [Development](#development)
- [Troubleshooting](#troubleshooting)

---

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
- **API Keys** - HMAC-SHA256 hashed API keys for organization access
- **User Isolation** - Same email can exist across different organizations

### Password Management

- **Forgot Password** - Secure token-based password reset flow
- **Password Reset Tokens** - Base64-encoded 64-byte random tokens with Redis storage

### Infrastructure

- **PostgreSQL** - Persistent storage with JPA/Hibernate
- **Redis** - Token caching, OTP storage, and session management
- **RabbitMQ** - Message queue for asynchronous email delivery
- **Flyway** - Database migrations

---

## Architecture

### System Overview

```mermaid
graph TB
    subgraph "Client Layer"
        Client[Client Application]
    end

    subgraph "API Gateway Layer"
        API[Spring Boot API<br/>Port 8080]
        JWKS[JWKS Endpoint<br/>/.well-known/jwks.json]
    end

    subgraph "Service Layer"
        AuthSvc[Authentication Services]
        TokenSvc[Token Services]
        EmailSvc[Email Services]
        OrgSvc[Organization Services]
    end

    subgraph "Security Layer"
        JWT[JWT Service<br/>RS256 Signing]
        Hash[Hashing Service<br/>SHA256/Bcrypt]
        Valid[Validation Service<br/>Redirect URLs]
    end

    subgraph "Data Layer"
        PG[(PostgreSQL<br/>User Data)]
        Redis[(Redis<br/>Tokens & Cache)]
        RMQ[RabbitMQ<br/>Email Queue]
    end

    subgraph "External Services"
        SMTP[SMTP Server]
    end

    Client -->|HTTPS| API
    Client -->|Verify JWT| JWKS

    API --> AuthSvc
    API --> TokenSvc
    API --> OrgSvc

    AuthSvc --> EmailSvc
    AuthSvc --> JWT
    AuthSvc --> Hash
    AuthSvc --> Valid

    TokenSvc --> JWT
    TokenSvc --> Hash

    EmailSvc -->|Enqueue| RMQ
    RMQ -->|Consume| SMTP

    AuthSvc --> PG
    AuthSvc --> Redis
    TokenSvc --> Redis
    OrgSvc --> PG

    style Client fill:#e1f5ff
    style API fill:#fff9c4
    style PG fill:#c8e6c9
    style Redis fill:#ffcdd2
    style RMQ fill:#ffe0b2
    style JWT fill:#f8bbd0
```

### Deployment Architecture

```mermaid
graph LR
    subgraph "Docker Environment"
        subgraph "Application Container"
            App[Suraksha Spring Boot<br/>Port 8080]
        end

        subgraph "Data Containers"
            DB[PostgreSQL<br/>Port 5433]
            Cache[Redis<br/>Port 6379]
            Queue[RabbitMQ<br/>Ports 5672, 15672]
        end
    end

    subgraph "External"
        Client[Client Apps]
        SMTP[Email Provider]
    end

    Client -->|HTTP/HTTPS| App
    App -->|JDBC| DB
    App -->|Redis Protocol| Cache
    App -->|AMQP| Queue
    Queue -->|SMTP| SMTP

    style App fill:#4caf50
    style DB fill:#2196f3
    style Cache fill:#f44336
    style Queue fill:#ff9800
    style Client fill:#9c27b0
    style SMTP fill:#607d8b
```

### Component Interaction

```mermaid
graph TB
    subgraph "Controllers"
        UC[UserController]
        JC[JwksController]
    end

    subgraph "Core Services"
        LS[LoginService]
        RS[RegistrationService]
        MUS[MagicUrlService]
        TFS[TwoFactorService]
        FPS[ForgotPasswordService]
        RTS[RefreshTokenService]
        LOS[LogoutService]
    end

    subgraph "Support Services"
        JS[JwtService]
        OS[OtpService]
        HS[HashingService]
        HMS[HmacService]
        VRS[ValidRedirectService]
        ETS[EmailTemplateService]
        MSS[MailSenderService]
    end

    subgraph "Data Access"
        UR[UserRepository]
        OR[OrganisationsRepository]
        RTR[RefreshTokenRepository]
    end

    UC --> LS
    UC --> RS
    UC --> MUS
    UC --> TFS
    UC --> FPS
    UC --> RTS
    UC --> LOS
    JC --> JS

    LS --> JS
    LS --> OS
    LS --> HS
    LS --> VRS

    RS --> JS
    RS --> HS

    MUS --> HS
    MUS --> ETS
    MUS --> MSS

    TFS --> OS
    TFS --> ETS
    TFS --> MSS

    FPS --> HS
    FPS --> ETS
    FPS --> MSS

    RTS --> JS
    RTS --> HS

    LS --> UR
    RS --> UR
    MUS --> UR
    FPS --> UR

    RS --> OR
    RTS --> RTR

    style UC fill:#64b5f6
    style JC fill:#64b5f6
    style LS fill:#81c784
    style RS fill:#81c784
    style JS fill:#ffb74d
    style HS fill:#ffb74d
```

---

## Authentication Flows

### Email/Password Flow

```mermaid
sequenceDiagram
    participant C as Client
    participant API as Spring Boot API
    participant Auth as LoginService
    participant Hash as HashingService
    participant JWT as JwtService
    participant DB as PostgreSQL
    participant Redis as Redis

    C->>API: POST /api/v1/auth/login<br/>{email, password, orgId}
    API->>Auth: authenticate()
    Auth->>DB: findUser(email, orgId)
    DB-->>Auth: UserEntity
    Auth->>Hash: verifyPassword()
    Hash-->>Auth: valid

    alt 2FA Enabled
        Auth->>Redis: storeOTP(email, code)
        Auth->>C: {twoFaRequired: true}
    else 2FA Disabled
        Auth->>JWT: generateToken(user)
        JWT-->>Auth: JWT Token
        Auth->>Hash: hashRefreshToken()
        Auth->>Redis: storeRefreshToken()
        Auth->>C: Set Cookies<br/>{jwt, refresh_token}
        Auth-->>C: {status: true}
    end
```

### Magic URL Flow

```mermaid
sequenceDiagram
    participant C as Client
    participant API as Spring Boot API
    participant Magic as MagicUrlService
    participant Hash as HashingService
    participant Email as EmailService
    participant Redis as Redis
    participant RMQ as RabbitMQ
    participant JWT as JwtService

    Note over C,RMQ: Phase 1: Request Magic URL
    C->>API: POST /api/v1/magic-url<br/>{email, orgId}
    API->>Magic: sendMagicUrl()
    Magic->>Hash: generateSecureToken()
    Hash-->>Magic: token
    Magic->>Hash: hashToken(token)
    Hash-->>Magic: hashedToken
    Magic->>Redis: store(hashedToken, 10min)
    Magic->>Email: generateMagicUrlEmail()
    Email->>RMQ: enqueue(email)
    Magic-->>C: {status: true, message: "sent"}

    Note over C,JWT: Phase 2: Verify Magic URL
    C->>C: User clicks link in email
    C->>API: GET /api/v1/verify-magic-url?token=xxx
    API->>Magic: verifyMagicUrl(token)
    Magic->>Hash: hashToken(token)
    Magic->>Redis: getAndDelete(hashedToken)
    Redis-->>Magic: userData
    Magic->>JWT: generateToken(user)
    JWT-->>Magic: JWT Token
    Magic->>API: tokens
    API->>C: Set Cookies & Redirect
```

### Two-Factor Authentication Flow

```mermaid
sequenceDiagram
    participant C as Client
    participant API as Spring Boot API
    participant TFA as TwoFactorService
    participant OTP as OtpService
    participant Email as EmailService
    participant Redis as Redis
    participant JWT as JwtService

    Note over C,Redis: Phase 1: Login triggers OTP
    C->>API: POST /api/v1/auth/login<br/>(2FA enabled user)
    API->>OTP: generateOTP()
    OTP->>OTP: random(1000-9999)
    OTP->>Redis: store(email:otp, 2-5min)
    OTP->>Email: sendOtpEmail(email, code)
    API-->>C: {twoFaRequired: true}

    Note over C,JWT: Phase 2: Verify OTP
    C->>API: POST /api/v1/auth/2fa/otp<br/>{email, otp, orgId}
    API->>TFA: verifyOtp()
    TFA->>Redis: get(email:otp)
    Redis-->>TFA: storedOtp
    TFA->>TFA: compare(otp, storedOtp)
    TFA->>Redis: delete(email:otp)
    TFA->>JWT: generateToken(user)
    JWT-->>TFA: JWT Token
    TFA->>API: tokens
    API->>C: Set Cookies<br/>{jwt, refresh_token}
```

### Token Refresh Flow

```mermaid
sequenceDiagram
    participant C as Client
    participant API as Spring Boot API
    participant RT as RefreshTokenService
    participant Hash as HashingService
    participant JWT as JwtService
    participant Redis as Redis
    participant DB as PostgreSQL

    C->>API: POST /api/v1/auth/refresh<br/>Cookie: refresh_token, refresh_token_id
    API->>RT: refreshToken()
    RT->>Hash: hashToken(refresh_token)
    Hash-->>RT: hashedToken
    RT->>Redis: get(refresh_token_id)
    Redis-->>RT: storedToken
    RT->>RT: compare(hashedToken, storedToken)

    alt Token Valid
        RT->>Redis: delete(old_token_id)
        RT->>DB: getUser(userId)
        RT->>JWT: generateNewToken(user)
        JWT-->>RT: new JWT
        RT->>Hash: hashNewRefreshToken()
        RT->>Redis: store(new_token, 30min)
        RT->>DB: save(new_refresh_token)
        RT->>API: new tokens
        API->>C: Set New Cookies<br/>{jwt, refresh_token}
    else Token Invalid/Expired
        RT-->>API: error
        API-->>C: 401 Unauthorized
    end
```

### Password Reset Flow

```mermaid
sequenceDiagram
    participant C as Client
    participant API as Spring Boot API
    participant FP as ForgotPasswordService
    participant Hash as HashingService
    participant Email as EmailService
    participant Redis as Redis
    participant DB as PostgreSQL

    Note over C,Redis: Phase 1: Request Reset
    C->>API: POST /api/v1/forgot-password<br/>{email}
    API->>FP: generateResetToken()
    FP->>Hash: generateSecureToken(64 bytes)
    Hash-->>FP: token
    FP->>Hash: encodeBase64(token)
    Hash-->>FP: resetToken
    FP->>Redis: store(email:resetToken, TTL)
    FP->>Email: sendResetEmail(email, token)
    FP-->>C: {resetToken, message}

    Note over C,DB: Phase 2: Reset Password
    C->>API: POST /api/v1/reset-password<br/>{token, newPassword}
    API->>FP: resetPassword()
    FP->>Redis: get(token)
    Redis-->>FP: email
    FP->>Hash: bcrypt(newPassword)
    FP->>DB: updatePassword(email, hashedPassword)
    FP->>Redis: delete(token)
    FP-->>C: {status: true}
```

---

## Database Schema

```mermaid
erDiagram
    ORGANISATIONS ||--o{ USERS : "has"
    ORGANISATIONS ||--o{ VALID_REDIRECTS : "configures"
    USERS ||--o{ REFRESH_TOKENS : "owns"

    ORGANISATIONS {
        uuid id PK
        string name
        string organisation_id UK "org_xxxxx"
        string api_key_hash "HMAC-SHA256"
        timestamp created_at
        timestamp updated_at
    }

    USERS {
        uuid id PK
        string mail_id
        string password_hash "Bcrypt"
        uuid organisation_id FK
        boolean two_fa_enabled
        timestamp created_at
        timestamp updated_at
    }

    REFRESH_TOKENS {
        uuid id PK
        uuid user_id FK
        string token_hash "SHA256"
        string client_ip
        string user_agent
        timestamp created_at
        timestamp expires_at
    }

    VALID_REDIRECTS {
        uuid id PK
        uuid organisation_id FK
        string redirect_url
        timestamp created_at
    }

    REDIS_KEYS {
        string key PK
        string value
        int ttl "Time To Live"
    }

    REDIS_KEYS ||--o{ OTP_STORAGE : "stores"
    REDIS_KEYS ||--o{ MAGIC_URL_TOKENS : "stores"
    REDIS_KEYS ||--o{ PASSWORD_RESET_TOKENS : "stores"
    REDIS_KEYS ||--o{ REFRESH_TOKEN_CACHE : "caches"

    OTP_STORAGE {
        string key "email:otp"
        string otp "4-digit code"
        int ttl "2-5 minutes"
    }

    MAGIC_URL_TOKENS {
        string key "magic:hashedToken"
        json value "user data"
        int ttl "10 minutes"
    }

    PASSWORD_RESET_TOKENS {
        string key "reset:email"
        string token
        int ttl "configurable"
    }

    REFRESH_TOKEN_CACHE {
        string key "refresh:tokenId"
        string hashedToken
        int ttl "30 minutes"
    }
```

---

## Tech Stack

| Component        | Technology         | Purpose                            |
| ---------------- | ------------------ | ---------------------------------- |
| Framework        | Spring Boot 4.0    | Application framework              |
| Language         | Java 21            | Programming language               |
| Database         | PostgreSQL 15      | Persistent data storage            |
| Cache            | Redis              | Token storage & session management |
| Message Queue    | RabbitMQ           | Asynchronous email delivery        |
| Migrations       | Flyway             | Database version control           |
| JWT Library      | jjwt 0.12.5        | JSON Web Token handling            |
| Security         | Spring Security    | Authentication & authorization     |
| ORM              | Hibernate/JPA      | Object-relational mapping          |
| Build Tool       | Maven              | Dependency management & builds     |
| Containerization | Docker Compose     | Development environment            |
| Email            | Spring Mail + SMTP | Email delivery                     |
| Validation       | Jakarta Validation | DTO validation                     |

---

## Project Structure

```
suraksha/
├── docker-compose.yml              # PostgreSQL container configuration
├── suraksha/                       # Spring Boot application root
│   ├── pom.xml                     # Maven dependencies
│   └── src/main/
│       ├── java/com/aneesh/suraksha/
│       │   ├── SurakshaApplication.java         # Application entry point
│       │   ├── SecurityConfig.java              # Spring Security configuration
│       │   │
│       │   ├── config/                          # Application configuration
│       │   │   ├── AppSecretConfig.java         # JWT & secret keys
│       │   │   └── RabbitMQConfig.java          # Message queue setup
│       │   │
│       │   ├── redis/                           # Redis configuration
│       │   │   └── configuration/
│       │   │       └── RedisConfig.java         # Redis connection setup
│       │   │
│       │   ├── dto/                             # Shared DTOs
│       │   │   └── MailDto.java                 # Email data transfer object
│       │   │
│       │   └── users/                           # User domain module
│       │       │
│       │       ├── component/                   # Utility components
│       │       │   ├── ClientIPAddress.java     # IP address extraction
│       │       │   ├── OrganisationIdGenerator.java  # Org ID generation
│       │       │   └── RefreshTokenGenerator.java    # Token generation
│       │       │
│       │       ├── configuration/
│       │       │   └── PasswordEncoderConfig.java    # Bcrypt configuration
│       │       │
│       │       ├── controller/                  # REST API endpoints
│       │       │   ├── JwksController.java      # Public key endpoint
│       │       │   ├── TestController.java      # Development endpoints
│       │       │   └── UserController.java      # Auth API routes
│       │       │
│       │       ├── dto/                         # Data transfer objects
│       │       │   ├── LoginRequest.java
│       │       │   ├── LoginResponse.java
│       │       │   ├── RegisterRequest.java
│       │       │   ├── RegisterResponse.java
│       │       │   ├── MagicLinkRequest.java
│       │       │   ├── MagicLinkResponse.java
│       │       │   ├── OTPRequest.java
│       │       │   ├── OTPResponse.java
│       │       │   ├── RefreshTokenResponse.java
│       │       │   ├── CreateOrganizationRequest.java
│       │       │   ├── CreateOrganizationResponse.java
│       │       │   └── ...
│       │       │
│       │       ├── model/                       # JPA entities & repositories
│       │       │   ├── UserEntity.java          # User entity
│       │       │   ├── UserRepository.java      # User data access
│       │       │   ├── Organisations.java       # Organization entity
│       │       │   ├── OrganisationsRepository.java  # Org data access
│       │       │   ├── RefreshToken.java        # Refresh token entity
│       │       │   └── RefreshTokenRepository.java   # Token data access
│       │       │
│       │       └── service/                     # Business logic layer
│       │           ├── LoginService.java        # Login authentication
│       │           ├── RegistrationService.java # User registration
│       │           ├── MagicUrlService.java     # Passwordless auth
│       │           ├── TwofactorService.java    # 2FA logic
│       │           ├── OtpService.java          # OTP generation/validation
│       │           ├── ForgotPasswordService.java    # Password reset
│       │           ├── EmailTemplateService.java     # HTML templates
│       │           ├── MailSenderService.java        # Email queue producer
│       │           ├── EmailProducer.java            # RabbitMQ producer
│       │           ├── JwtService.java               # JWT operations
│       │           ├── RefreshTokenService.java      # Token refresh
│       │           ├── RefreshCheck.java             # Token validation
│       │           ├── LogoutService.java            # Session termination
│       │           ├── ValidRedirectService.java     # URL validation
│       │           ├── HashingService.java           # SHA256/Bcrypt
│       │           ├── HmacService.java              # HMAC operations
│       │           └── OrganisationOnboard.java      # Org creation
│       │
│       └── resources/
│           ├── application.properties          # Application configuration
│           └── db/migration/                   # Flyway SQL migrations
│               ├── V1__initial_schema.sql
│               ├── V2__add_organisations.sql
│               └── ...
```

---

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
  "redirect": "https://app.example.com/dashboard"
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

Request a magic sign-in link:

```http
POST /api/v1/magic-url
Content-Type: application/json

{
  "mailId": "user@example.com",
  "organisationId": "org_xxxxx",
  "redirect": "https://app.example.com/dashboard"
}
```

Verify the magic URL (user clicks link in email):

```http
GET /api/v1/verify-magic-url?token=<magic_token>&redirect=<redirect_url>
```

#### Two-Factor Authentication (OTP)

```http
POST /api/v1/auth/2fa/otp
Content-Type: application/json

{
  "mailId": "user@example.com",
  "otp": "1234",
  "organisationId": "org_xxxxx",
  "redirect": "https://app.example.com/dashboard"
}
```

#### Token Refresh

```http
POST /api/v1/auth/refresh
Cookie: refresh_token=<token>; refresh_token_id=<token_id>
```

#### Logout

```http
POST /api/v1/auth/logout
Cookie: refresh_token_id=<token_id>
```

#### Forgot Password

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

> **Note:** In production, the reset token should be sent via email, not returned in the response.

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

> **Important:** The API key is only returned once during creation. Store it securely.

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

---

## JWT Token Claims

Tokens issued by Suraksha include the following claims:

| Claim            | Description          | Example                                |
| ---------------- | -------------------- | -------------------------------------- |
| `sub`            | User's email address | "user@example.com"                     |
| `userId`         | User's UUID          | "123e4567-e89b-12d3-a456-426614174000" |
| `mailId`         | User's email address | "user@example.com"                     |
| `organisationId` | Organization ID      | "org_abc123xyz"                        |
| `iat`            | Issued at timestamp  | 1679097600                             |
| `exp`            | Expiration timestamp | 1679098500 (15 min)                    |

---

## Email System

### Email Architecture

```mermaid
graph LR
    subgraph "Application Layer"
        Service[Auth/Magic/OTP Service]
        Template[EmailTemplateService]
        Producer[MailSenderService]
    end

    subgraph "Message Queue"
        Exchange[EMAIL_EXCHANGE]
        Queue[Email Queue]
    end

    subgraph "Delivery Layer"
        Consumer[Email Consumer]
        SMTP[SMTP Server]
    end

    subgraph "User"
        Inbox[User Inbox]
    end

    Service -->|Generate| Template
    Template -->|HTML Email| Producer
    Producer -->|Enqueue| Exchange
    Exchange -->|Route| Queue
    Queue -->|Consume| Consumer
    Consumer -->|Send| SMTP
    SMTP -->|Deliver| Inbox

    style Service fill:#4caf50
    style Template fill:#2196f3
    style Exchange fill:#ff9800
    style Queue fill:#ff9800
    style Consumer fill:#9c27b0
    style SMTP fill:#607d8b
    style Inbox fill:#f44336
```

### Email Templates

Professional HTML email templates with responsive design:

- **OTP Emails** - 4-digit OTP codes with monospace formatting
- **Magic URL Emails** - Secure sign-in links with button and raw link
- **Password Reset Emails** - Token-based reset links
- **Dark Mode Support** - Automatic color scheme detection
- **Responsive Design** - Mobile-friendly layouts
- **Branded Design** - Suraksha branding with GitHub logo

### Asynchronous Email Delivery

- **RabbitMQ Integration** - Non-blocking email sending
- **Email Queue** - `EMAIL_EXCHANGE` and `EMAIL_ROUTING_KEY`
- **Reliability** - Message persistence ensures delivery
- **Scalability** - Multiple consumers for high volume

### Email Types & Expiration

| Email Type     | Expiration | Storage | Purpose                   |
| -------------- | ---------- | ------- | ------------------------- |
| OTP            | 2-5 min    | Redis   | Two-factor authentication |
| Magic URL      | 10 min     | Redis   | Passwordless login        |
| Password Reset | Custom     | Redis   | Password recovery         |

---

## Getting Started

### Prerequisites

- Java 21+
- Docker & Docker Compose
- Maven
- Redis (via Docker or local)
- RabbitMQ (via Docker or local)

### Quick Start

#### 1. Start Required Services

```bash
# Start PostgreSQL (port 5433)
docker-compose up -d

# Start Redis (port 6379)
docker run -d --name suraksha-redis -p 6379:6379 redis:latest

# Start RabbitMQ (ports 5672, 15672)
docker run -d --name suraksha-rabbitmq \
  -p 5672:5672 -p 15672:15672 \
  rabbitmq:3-management
```

**Service Ports:**

- PostgreSQL: `5433`
- Redis: `6379`
- RabbitMQ: `5672` (AMQP), `15672` (Management UI)

#### 2. Configure Application

Create `suraksha/src/main/resources/application.properties`:

```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5433/my_database
spring.datasource.username=admin
spring.datasource.password=securepassword

# JPA Configuration
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Flyway Migration
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration

# Redis Configuration
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.timeout=60000

# RabbitMQ Configuration
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest

# Email Queue Configuration
rabbitmq.exchange.name=EMAIL_EXCHANGE
rabbitmq.routing.key=EMAIL_ROUTING_KEY

# RSA Keys for JWT (generate using openssl)
rsa.private-key=-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----
rsa.public-key=-----BEGIN PUBLIC KEY-----\n...\n-----END PUBLIC KEY-----

# Application Secrets (base64 encoded)
app.secret.key=<base64-secret-for-jwt>
app.secret_refresh.key=<base64-secret-for-refresh-tokens>
app.secret_api.key=<base64-secret-for-api-keys>

# Server Configuration
server.port=8080
```

#### 3. Generate RSA Keys

```bash
# Generate private key
openssl genrsa -out private.key 2048

# Convert to PKCS#8 format (required by Java)
openssl pkcs8 -topk8 -inform PEM -outform PEM \
  -nocrypt -in private.key -out private_pkcs8.key

# Generate public key
openssl rsa -in private.key -pubout -out public.key

# View keys (copy to application.properties)
cat private_pkcs8.key
cat public.key
```

#### 4. Generate Secret Keys

```bash
# Generate JWT secret
openssl rand -base64 32

# Generate refresh token secret
openssl rand -base64 32

# Generate API key secret
openssl rand -base64 32
```

#### 5. Run the Application

```bash
cd suraksha
./mvnw spring-boot:run
```

The server starts on `http://localhost:8080`.

#### 6. Verify Installation

```bash
# Check JWKS endpoint
curl http://localhost:8080/.well-known/jwks.json

# Check RabbitMQ Management UI
open http://localhost:15672  # Login: guest/guest

# Check Redis connection
redis-cli -h localhost -p 6379 ping
```

---

## Security Features

### Token Security Architecture

```mermaid
graph TB
    subgraph "Token Generation"
        Gen[Generate Token]
        Sign[RS256 Signature]
        Store[Store in Redis]
    end

    subgraph "Token Storage"
        Cookie[HttpOnly Cookie]
        Redis[(Redis Cache)]
        DB[(PostgreSQL)]
    end

    subgraph "Token Validation"
        Extract[Extract from Cookie]
        Verify[Verify Signature]
        Check[Check Expiration]
        Lookup[Lookup in Redis]
    end

    subgraph "Security Measures"
        Rotate[Token Rotation]
        Revoke[Revocation Support]
        Expire[Auto Expiration]
    end

    Gen --> Sign
    Sign --> Store
    Store --> Cookie
    Store --> Redis
    Store --> DB

    Extract --> Verify
    Verify --> Check
    Check --> Lookup

    Lookup --> Rotate
    Lookup --> Revoke
    Lookup --> Expire

    style Sign fill:#f44336
    style Cookie fill:#4caf50
    style Redis fill:#ff9800
    style Verify fill:#2196f3
    style Rotate fill:#9c27b0
```

### Security Features Overview

#### Token Security

- **RS256 Signing** - Asymmetric cryptography with public key verification
- **Short-lived JWTs** - 15-minute expiration minimizes token theft impact
- **Refresh Token Rotation** - New refresh token issued on each use
- **HttpOnly Cookies** - Prevents XSS attacks by blocking JavaScript access
- **Secure & SameSite Flags** - All cookies have `Secure` and `SameSite=Strict`
- **Token Revocation** - Individual token invalidation via logout

#### Password Security

- **Bcrypt Hashing** - Industry-standard with automatic salting
- **Configurable Work Factor** - Adjustable computational cost
- **No Plain Text Storage** - Passwords never stored or logged
- **Password Reset Tokens** - Cryptographically secure random generation

#### API Key Security

- **HMAC-SHA256 Hashing** - API keys hashed before storage
- **One-time Display** - Keys only shown at creation time
- **Organization Scoping** - Keys tied to specific organizations

#### Refresh Token Security

- **SHA256 Hashing** - Tokens hashed before Redis storage
- **Metadata Tracking** - IP address and User-Agent recorded
- **30-minute Expiration** - Configurable TTL
- **Redis-backed Storage** - Fast, distributed validation
- **Single-use Tokens** - Rotation on each refresh prevents replay attacks

#### Magic URL & OTP Security

- **Time-limited Tokens** - Magic URLs (10 min), OTPs (2-5 min)
- **SHA256 Hashing** - Magic URL tokens hashed in Redis
- **Single-use Tokens** - Deleted after successful verification
- **Cryptographically Secure** - `SecureRandom` for generation
- **Redis TTL** - Automatic expiration via time-to-live

#### Redirect Validation

- **Whitelist-based** - Only organization-scoped URLs allowed
- **URL Validation** - Prevents open redirect vulnerabilities
- **Organization Isolation** - Each org maintains own whitelist

#### Request Metadata Tracking

- **Client IP Tracking** - All requests log IP addresses
- **User-Agent Logging** - Browser and device information
- **Audit Trail** - Metadata stored with refresh tokens

---

## Development

### Build Project

```bash
cd suraksha
./mvnw clean package
```

### Run Tests

```bash
./mvnw test
```

### Run with Profile

```bash
# Development profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Production profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

### Database Migrations

```bash
# Create new migration
# Add file: src/main/resources/db/migration/V{version}__description.sql

# View migration status
./mvnw flyway:info

# Migrate database
./mvnw flyway:migrate

# Rollback (use with caution)
./mvnw flyway:undo
```

### Monitoring Endpoints

Spring Boot Actuator endpoints (if enabled):

```bash
# Health check
curl http://localhost:8080/actuator/health

# Application info
curl http://localhost:8080/actuator/info

# Metrics
curl http://localhost:8080/actuator/metrics
```

---

## Troubleshooting

### Database Connection Refused

**Issue:** Cannot connect to PostgreSQL

**Solutions:**

```bash
# Check if PostgreSQL container is running
docker ps | grep postgres

# Check port availability
lsof -i :5433

# View container logs
docker logs <postgres-container-id>

# Restart container
docker-compose restart

# Verify credentials in application.properties
```

### Redis Connection Issues

**Issue:** Redis connection failures

**Solutions:**

```bash
# Check Redis container
docker ps | grep redis

# Test Redis connection
redis-cli -h localhost -p 6379 ping
# Expected: PONG

# Check port availability
lsof -i :6379

# View Redis logs
docker logs suraksha-redis

# Clear Redis cache
redis-cli -h localhost -p 6379 FLUSHALL
```

### RabbitMQ Connection Issues

**Issue:** Email queue not working

**Solutions:**

```bash
# Check RabbitMQ container
docker ps | grep rabbitmq

# Access Management UI
open http://localhost:15672
# Login: guest/guest

# Check port availability
lsof -i :5672
lsof -i :15672

# View RabbitMQ logs
docker logs suraksha-rabbitmq

# Restart RabbitMQ
docker restart suraksha-rabbitmq
```

### Email Not Sending

**Issue:** Emails not being delivered

**Diagnosis:**

```bash
# Check RabbitMQ queue
# Visit: http://localhost:15672/#/queues
# Look for messages in EMAIL_EXCHANGE queue

# Check application logs
tail -f suraksha/logs/spring.log

# Verify SMTP configuration
# Ensure email service consumer is running
```

**Solutions:**

- Verify RabbitMQ connection in `application.properties`
- Check SMTP credentials and server availability
- Ensure email consumer service is running
- Check firewall rules for SMTP port (usually 587 or 465)

### Magic URL or OTP Not Working

**Issue:** Tokens not validating

**Diagnosis:**

```bash
# Check Redis for stored tokens
redis-cli -h localhost -p 6379 KEYS "*"

# Check specific OTP
redis-cli -h localhost -p 6379 GET "otp:user@example.com"

# Check token expiration
redis-cli -h localhost -p 6379 TTL "magic:hashedtoken"
```

**Solutions:**

- Ensure Redis is running and accessible
- Verify token hasn't expired (10 min for magic URLs, 2-5 min for OTPs)
- Check application logs for token generation errors
- Ensure clock synchronization (tokens are time-sensitive)

### Key Generation Errors

**Issue:** RSA key format errors

**Solutions:**

```bash
# Ensure OpenSSL is installed
openssl version

# Use PKCS#8 format (required by Java)
openssl pkcs8 -topk8 -inform PEM -outform PEM \
  -nocrypt -in private.key -out private_pkcs8.key

# Verify key format
openssl rsa -in private_pkcs8.key -check

# Test key pair match
openssl rsa -in private.key -pubout | diff - public.key
```

### Token Validation Failures

**Issue:** JWT verification failing

**Diagnosis:**

- Check JWT structure: `jwt.io`
- Verify signature algorithm (should be RS256)
- Check token expiration

**Solutions:**

- Ensure public key matches private key used for signing
- Verify JWT hasn't expired (15-minute default)
- Check refresh token exists in Redis
- Ensure system clocks are synchronized
- Verify `rsa.public-key` in `application.properties` is correct

### Memory or Performance Issues

**Issue:** High memory usage or slow responses

**Solutions:**

```bash
# Check Java heap size
java -XX:+PrintFlagsFinal -version | grep HeapSize

# Increase heap size
export MAVEN_OPTS="-Xmx2048m -Xms512m"
./mvnw spring-boot:run

# Monitor JVM
jstat -gc <pid> 1000

# Check database connections
# Adjust in application.properties:
spring.datasource.hikari.maximum-pool-size=10

# Monitor Redis memory
redis-cli -h localhost -p 6379 INFO memory
```

---

## Contributing

Contributions are welcome! Please follow these guidelines:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## License

This project is licensed under the MIT License.

---

## Author

**Aneesh Patne**

For questions, issues, or feature requests, please open an issue on GitHub.

---

## Roadmap

- [ ] OAuth2/OIDC integration
- [ ] Social login (Google, GitHub, etc.)
- [ ] Rate limiting and brute force protection
- [ ] Session management UI
- [ ] Admin dashboard
- [ ] Audit log system
- [ ] Webhook support for authentication events
- [ ] Multi-region deployment guide
- [ ] Kubernetes deployment manifests
- [ ] Prometheus metrics integration

---

**Built with ❤️ using Spring Boot**
