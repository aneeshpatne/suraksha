# Suraksha Authentication Service

A Spring Boot application designed to handle secure user authentication using JWT (JSON Web Tokens). This service manages user registration, login, and token lifecycle management including access and refresh token rotation.

## Features

- **Unified Authentication Endpoint**: A single route handles both user registration and login processes.
- **JWT Implementation**: Secure generation and validation of Access and Refresh tokens.
- **Token Rotation**: Automatic issuance of new Access Tokens when they expire, provided a valid Refresh Token is presented.

## API Documentation

### Authentication

**Endpoint:** `POST /auth`

This endpoint serves as the entry point for both registering a new user and logging in an existing user.

**Request Body:**

```json
{
  "username": "user@example.com",
  "password": "securePassword123"
}
```

**Successful Response (200 OK):**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4...",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

### Token Refresh

**Endpoint:** `POST /auth/refresh` (Internal or implicit flow depending on implementation)

_Note: The system is designed to "upgrade" or refresh access tokens when they are expired._

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven or Gradle

### Running the Application

```bash
./mvnw spring-boot:run
```

The server will start on `http://localhost:8080`.
