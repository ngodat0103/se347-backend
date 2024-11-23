# User Service (user-svc)

## Overview
The User Service is a part of the se347-backend project, built using Spring Boot. It provides RESTful APIs for user registration, authentication, and account management. This service is designed to be reactive and leverages Spring Data MongoDB for data persistence and Spring Security for authentication and authorization.

## Technologies Used
- **Java**: 17
- **Spring Boot**: 3.3.5
- **Spring Data MongoDB Reactive**: 3.3.5
- **Spring Kafka**: 3.3.5
- **Spring Security **: 1.3.3
- **Lombok**: For reducing boilerplate code
- **MapStruct**: For mapping between DTOs and domain objects
- **Swagger**: For API documentation

## Project Structure
```
user-svc
├── .mvn
│   └── wrapper - Maven wrapper scripts for managing dependencies and building the project.
├── compose.yaml - Docker Compose configuration for the user service.
├── mvnw.cmd - Windows script for running Maven commands.
├── pom.xml - Maven configuration file for managing project dependencies.
├── readme.md - This file, providing an overview of the user service.
└── src
    ├── main
    │   ├── resources - Configuration files and other resources.
    │   │   ├── application.yaml - Main application configuration.
    │   │   ├── dev.yaml - Development-specific configuration.
    │   │   └── META-INF - Metadata files for the application.
    │   └── java - Source code for the application.
    │       └── com
    │           └── github
    │               └── ngodat0103
    │                   └── usersvc
    │                       ├── UserServiceApplication.java - Main application class.
    │                       ├── controller - Controllers for handling requests.
    │                       ├── dto - Data Transfer Objects (DTOs).
    │                       ├── exception - Custom exception classes.
    │                       ├── security - Security configuration classes.
    │                       ├── service - Business logic services.
    │                       ├── swagger - Swagger configuration.
    │                       └── persistence - Data persistence classes.
    └── test - Unit and integration tests for the application.
```

## API Endpoints
The User Service supports the following API endpoints:

| Endpoint                     | HTTP Method | Description                                           |
|------------------------------|-------------|-------------------------------------------------------|
| `/version`                   | GET         | Retrieves the current version of the application.     |
| `/api/v1/auth/jwk`           | GET         | Retrieves the JSON Web Key (JWK) used for signing JWTs. |
| `/api/v1/auth/login`         | POST        | Authenticates a user and returns an access token.    |
| `/api/v1/auth/verify-email`  | GET         | Verifies the user's email using a verification code.  |
| `/api/v1/auth/resend-email`  | GET         | Resends the email verification link to the user (requires authentication). |
| `/api/v1/users`              | POST        | Creates a new user account.                           |
| `/api/v1/users/me`           | GET         | Retrieves the current user's account information (requires authentication). |
| `/api/v1/users/api-docs`     | GET         | Retrieves Swagger API documentation in JSON format.   |
| `/api/v1/users/ui-docs`      | GET         | Retrieves Swagger UI for interactive API documentation.|
| `/actuator/health`           | GET         | Checks the health of the application.                 |

## Supported APIs
This service currently supports a total of **10 API endpoints** for user management and authentication.

## How to Run the Project

### Prerequisites
- Java 17
- Docker
- Docker Compose

### Running the Application
   ```bash
   docker compose --profile all up -d
   ```
### Stopping the Project
To stop the project, run:
```bash
docker compose --profile all stop
```
### Removing the Project
To remove the project, run:
```bash
docker compose --profile all down
```
## Database Credentials
If you use the default values in Docker Compose, you can access the MongoDB database using the following credentials:
- **Username**: dev
- **Password**: dev
- **Database**: user-svc

For root credentials, use:
- **Username**: root
- **Password**: root
- **Authentication Database**: admin
- **Security**: SCRAM-SHA-256