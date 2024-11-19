# SE347 Backend Project

## Overview

The SE347 Backend Project is a Spring Boot application designed to provide user registration, authentication, and account management features. It utilizes Spring Data MongoDB Reactive for data persistence and Spring Security for authentication and authorization. The project is structured to facilitate easy development and deployment using Docker.

## Directory Structure

```
D:\code\se347-backend
├── .github
│   └── workflows - Contains GitHub Actions workflows for CI/CD pipelines.
└── user-svc
    ├── .mvn
    │   └── wrapper - Contains Maven wrapper scripts for managing dependencies and building the project.
    └── src
        ├── main
        │   ├── resources - Contains configuration files, templates, and other resources used by the application.
        │   │   └── META-INF - Contains metadata files for the application.
        │   └── java - Contains the source code for the application.
        │       └── com
        │           └── github
        │               └── ngodat0103
        │                   └── usersvc
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

## Project Details

### Technologies Used
- **Java 17**: The programming language used for the application.
- **Spring Boot 3.3.5**: Framework for building the RESTful API.
- **Spring Data MongoDB Reactive**: For interacting with a MongoDB database.
- **Spring Security Jwt**: For authentication and authorization.
- **Docker**: For containerization and deployment.
- **GitHub Actions**: For CI/CD pipelines.
- **Swagger**: For API documentation. 
- **Testcontainers**: For integration testing.