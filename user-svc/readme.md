# **User Service**

## **Project Overview**
The **User Service** is a scalable backend service designed to manage user accounts and authentication for a distributed application. It provides capabilities for user registration, email verification, login, and workspace management. The service leverages a reactive programming model using **Spring WebFlux**, ensuring high performance and scalability for I/O-intensive operations.

---

## **Project Details**
### **Features**
- **User Management**: Create, update, and delete user accounts.
- **Authentication**: Supports login/logout using JWT-based OAuth2 authentication.
- **Email Integration**:
    - Sends email verification for new users.
    - Resends verification emails on request.
- **Workspace Management**: Allows users to manage associated workspaces.
- **Event Streaming**:
    - Integrates with **Kafka** to produce user-related events for downstream systems.
    - Implements CDC (Change Data Capture) to publish changes to user accounts.
- **Asynchronous Processing**: Uses Redis for task offloading, such as email verification.

### **Architecture**
The service follows a **modular architecture**:
- Organized into separate layers (controllers, services, and repositories).
- Adheres to interface-based design for better extensibility and testing.

---

## **Description**
This service is part of a larger microservices-based system and is designed to handle user-related workflows. It leverages modern practices such as:
- **Reactive Programming**: Ensures high concurrency and non-blocking I/O.
- **Event-Driven Architecture**: Publishes user events to Kafka for external systems to consume.
- **Cloud-Native Design**: Configured for deployment in containerized environments with minimal overhead.

### **Modules**
1. **Auth**:
    - Handles login/logout functionality.
    - Uses JWT for authentication.
2. **Email**:
    - Sends email verification using a Kafka-backed producer.
    - Stores verification codes in Redis for validation.
3. **Kafka Integration**:
    - Produces events for topics like `user-email` and `user-cdc`.
    - Custom Kafka service (`DefaultKafkaService`) ensures type safety and validation.
4. **Workspace**:
    - Provides API endpoints for workspace-related actions.

---

## **Technology Stack**
### **Core Frameworks and Libraries**
- **Java 17**: Core programming language.
- **Spring WebFlux**: Reactive framework for building asynchronous and non-blocking REST APIs.
- **Spring Data MongoDB**: Manages user and workspace data in MongoDB.
- **Spring Security**: Implements authentication and authorization using OAuth2 and JWT.
- **Reactor**: Core library for reactive programming.

### **Infrastructure and Tools**
- **Kafka**: Event streaming platform for publishing and consuming user events.
- **Redis**: In-memory data store for email verification and token blacklisting.
- **MinIO**: Cloud-native storage for workspace-related files (if applicable).
- **Swagger/OpenAPI**: API documentation for easier client integration.

### **Development Tools**
- **Maven**: Build and dependency management.
- **IntelliJ IDEA**: Recommended IDE for Java development.
- **JUnit 5**: Unit testing framework.
- **Mocking**: Uses Mockito for unit test isolation.

---

## **Getting Started**
### **Prerequisites**
- **Java 17** or higher.
- **Docker** for containerized deployment.
- Kafka and Redis running locally or in a cloud environment.

### **Run the Application**
1. Clone the repository:
   ```bash
   git clone <repo-url>
   cd user-svc
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
