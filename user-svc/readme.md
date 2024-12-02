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
D:\code\projects\se347-backend\user-svc
    src
    ├── main
    │   ├── resources
    │   │   ├── application.yaml
    │   │   ├── dev.yaml
    │   │   ├── IT.yaml
    │   │   ├── minio-default-policy.json
    │   │   ├── staging.yaml
    │   │   └── META-INF
    │   │       └── additional-spring-configuration-metadata.json
    │   └── java
    │       └── com
    │           └── github
    │               └── ngodat0103
    │                   └── usersvc
    │                       ├── UserServiceApplication.java
    │                       ├── controller
    │                       │   ├── AppController.java
    │                       │   ├── AuthController.java
    │                       │   ├── UserController.java
    │                       │   └── WorkspaceController.java
    │                       ├── dto
    │                       │   ├── WorkspaceDto.java
    │                       │   ├── account
    │                       │   │   ├── AccountDto.java
    │                       │   │   ├── CredentialDto.java
    │                       │   │   └── EmailDto.java
    │                       │   ├── mapper
    │                       │   │   ├── UserMapper.java
    │                       │   │   └── WorkspaceMapper.java
    │                       │   └── topic
    │                       │       ├── Action.java
    │                       │       ├── KeyTopic.java
    │                       │       └── TopicRegisteredUser.java
    │                       ├── exception
    │                       │   ├── ConflictException.java
    │                       │   ├── GlobalExceptionHandler.java
    │                       │   ├── InvalidEmailCodeException.java
    │                       │   ├── NotFoundException.java
    │                       │   └── Util.java
    │                       ├── filter
    │                       │   └── BodyRequestSizeLimitFilter.java
    │                       ├── security
    │                       │   └── SecurityConfiguration.java
    │                       ├── service
    │                       │   ├── CdcService.java
    │                       │   ├── MinioService.java
    │                       │   ├── ServiceProducer.java
    │                       │   ├── UserService.java
    │                       │   ├── WorkspaceService.java
    │                       │   └── impl
    │                       │       ├── UserServiceImpl.java
    │                       │       └── WorkspaceServiceImpl.java
    │                       ├── swagger
    │                       │   └── SwaggerConfiguration.java
    │                       ├── config
    │                       │   ├── kafka
    │                       │   │   ├── KafkaConfiguration.java
    │                       │   │   └── KeyTopicSerialize.java
    │                       │   └── minio
    │                       │       ├── MinioAutoconfiguration.java
    │                       │       └── MinioProperties.java
    │                       └── persistence
    │                           ├── document
    │                           │   ├── Account.java
    │                           │   ├── BaseDocument.java
    │                           │   └── workspace
    │                           │       ├── Workspace.java
    │                           │       ├── WorkSpacePermission.java
    │                           │       └── WorkspaceProperty.java
    │                           └── repository
    │                               ├── UserRepository.java
    │                               └── WorkspaceRepository.java
    └── test
        └── java
            └── com
                └── github
                    └── ngodat0103
                        └── usersvc
                            └── controller
                                ├── ContainerConfig.java
                                ├── ControllerIT.java
                                └── MyTestConfiguration.java
```

## API Endpoints
The User Service supports the following API endpoints:

| Endpoint                                   | HTTP Method | Description                                                                 |
|--------------------------------------------|-------------|-----------------------------------------------------------------------------|
| `/version`                                 | GET         | Retrieves the current version of the application.                          |
| `/api/v1/auth/jwk`                        | GET         | Retrieves the JSON Web Key (JWK) used for signing JWTs.                   |
| `/api/v1/auth/login`                      | POST        | Authenticates a user and returns an access token.                          |
| `/api/v1/auth/verify-email`               | GET         | Verifies the user's email using a verification code.                        |
| `/api/v1/auth/resend-email`               | GET         | Resends the email verification link to the user (requires authentication).  |
| `/api/v1/users`                           | POST        | Creates a new user account.                                               |
| `/api/v1/users/me`                        | GET         | Retrieves the current user's account information (requires authentication). |
| `/api/v1/workspaces`                      | POST        | Creates a new workspace.                                                  |
| `/api/v1/workspaces/me`                   | GET         | Retrieves a list of workspaces associated with the current user.          |
| `/api/v1/workspaces/{workspaceId}/picture`| PUT         | Updates the picture of a workspace.                                       |
| `/actuator/health`                        | GET         | Checks the health of the application.                                     |

## Supported APIs
This service currently supports a total of **11 API endpoints** for user management and authentication.

## How to Run the Project

### Prerequisites
- Java 17
- Docker
- Docker Compose
### Need to run some dependent services which have defined in docker-compose file root directory
- Kafka
- redis
- elasticsearch
```shell 
cd ../
docker compose  up -d
```

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
