# User Service (user-svc)

## Project Overview
The User Service is part of the se347-backend project, which is built using Spring Boot and provides RESTful APIs for user registration, authentication, and account management.

## Project Structure

```
user-svc
├── .mvn
│   └── wrapper - Contains Maven wrapper scripts for managing dependencies and building the project.
├── compose.yaml - Docker Compose configuration for the user service.
├── mvnw.cmd - Windows script for running Maven commands.
├── pom.xml - Maven configuration file for managing project dependencies.
├── readme.md - This file, providing an overview of the user service.
└── src
    ├── main
    │   ├── resources - Contains configuration files, templates, and other resources used by the application.
    │   │   ├── application.yaml - Main application configuration file.
    │   │   ├── dev.yaml - Development-specific configuration.
    │   │   └── META-INF - Contains metadata files for the application.
    │   └── java - Contains the source code for the application.
    │       └── com
    │           └── github
    │               └── ngodat0103
    │                   └── usersvc
    │                       ├── UserServiceApplication.java - Main application class.
    │                       ├── controller - Contains controllers responsible for handling incoming requests and returning responses.
    │                       ├── dto - Contains data transfer objects (DTOs) used for data exchange between the application and external services.
    │                       │   └── mapper - Contains mappers for converting between DTOs and domain objects.
    │                       ├── exception - Contains custom exception classes for handling errors.
    │                       ├── security - Contains classes related to security configuration and authentication.
    │                       ├── service - Contains service classes responsible for business logic.
    │                       │   └── impl - Contains implementations of service interfaces.
    │                       ├── swagger - Contains configuration for Swagger documentation.
    │                       └── persistence - Contains classes related to data persistence.
    │                           ├── document - Contains domain objects that represent data stored in the database.
    │                           └── repository - Contains repositories for interacting with the database.
    └── test - Contains unit and integration tests for the application.
        └── java
            └── com
                └── github
                    └── ngodat0103
                        └── usersvc
                            └── controller - Contains integration tests for the controllers.
```

## API Endpoints

The User Service supports the following API endpoints:

| Endpoint                     | HTTP Method | Description                                           |
|------------------------------|-------------|-------------------------------------------------------|
| `/api/v1/users`              | POST        | Creates a new user account.                           |
| `/api/v1/auth/login`         | POST        | Authenticates a user and returns an access token.    |
| `/api/v1/users/me`           | GET         | Retrieves the current user's account information.     |
| `/api/v1/auth/jwk`           | GET         | Retrieves the JSON Web Key (JWK) for verifying JWTs. |
| `/api/v1/users/api-docs`     | GET         | Retrieves Swagger API documentation in JSON format.   |
| `/api/v1/users/ui-docs`      | GET         | Retrieves Swagger UI for interactive API documentation.|
| `/actuator/health`           | GET         | Checks the health of the application.                 |

## Supported APIs
This service currently supports a total of **7 API endpoints** for user management and authentication.
