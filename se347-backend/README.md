# Project Documentation for SE347 Backend
## Prerequisites
Before you begin, ensure you have the following installed on your machine:

1. **Java Development Kit (JDK)**: Version 21
2. **Apache Maven**: Version 3.9.9
3. **Docker Desktop**: For containerization
4. **A code editor or IDE**: Such as IntelliJ IDEA or Eclipse

## Getting Started
### Step 1: Clone the Repository
### Step 2: Navigate to Project Directory
Change your working directory to the project root:
```bash
cd se347-backend
```
### Step 3: Configure Environment Variables
Set up the necessary environment variables for the project. You can create a `.env` file in the project root with the following variables:

| Environment Variable                  | Default Value                     |
|---------------------------------------|-----------------------------------|
| `MONGODB_ROOT_PASSWORD`               | `root`                            |
| `MONGODB_ROOT_USER`                   | `root`                            |
| `MONGODB_USERNAME`                    | `dev`                             |
| `MONGODB_PASSWORD`                    | `dev`                             |
| `MONGODB_DATABASE`                    | `se347-backend`                   |
| `MINIO_API_SERVER`                    | `http://minio-svc:9000`          |
| `MINIO_SERVER_ACCESS_KEY`             | `minio-demo`                      |
| `MINIO_SERVER_SECRET_KEY`             | `minio-demo`                      |
| `MINIO_ROOT_USER`                     | `minio-demo`                      |
| `MINIO_ROOT_PASSWORD`                 | `minio-demo`                      |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS`     | `kafka:29092`                     |
| `SPRING_DATA_MONGODB_DATABASE`        | `se347-backend`                   |
| `SPRING_DATA_MONGODB_USERNAME`        | `dev`                             |
| `SPRING_DATA_MONGODB_PASSWORD`        | `dev`                             |


### Step 4 To run all services in the background, you can use the following command:
```bash
docker-compose up -d
```
### Step 5: Ensure you have add the "127.0.0.1 minio-svc" to your hosts file
### Step 6: Access the API Documentation
Once the application is running, you can access the API documentation at:
```
http://localhost:5000/api/v1/users/ui-docs
```