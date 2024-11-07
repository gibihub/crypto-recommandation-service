
# Crypto Recommendation Service

This project is a cryptocurrency recommendation and statistics service that provides a set of RESTful APIs for managing cryptocurrency data, fetching statistics, and loading data from CSV files. It uses **Spring Boot**, **Spring Security**, **Swagger/OpenAPI** for API documentation, and a rate-limiting filter for better control of requests.

## Table of Contents

1. [Features](#features)
2. [Technologies](#technologies)
3. [Installation](#installation)
4. [Configuration](#configuration)
5. [Endpoints](#endpoints)
6. [Authentication](#authentication)
7. [Running the Application](#running-the-application)
8. [Docker Setup](#docker-setup)
9. [Kubernetes Setup](#kubernetes-setup)
10. [Testing](#testing)
11. [Contributing](#contributing)
12. [License](#license)

## Features

- **API Endpoints** for fetching cryptocurrency statistics, sorted lists, and recommendations.
- **Load Crypto Data** from CSV files and store them into a database.
- **Role-based Access Control (RBAC)** for admin access to certain features.
- **Rate Limiting** to prevent abuse of the service.
- **Swagger UI** for API documentation and testing.
- **Spring Security** for user authentication and authorization.

## Technologies

- **Java 17**
- **Spring Boot**
- **Spring Security** (for role-based access)
- **Swagger/OpenAPI** (for API documentation)
- **Docker** (for containerization)
- **Kubernetes** (for orchestration)
- **H2 Database** (for demo purposes)
- **JUnit & Mockito** (for testing)

## Installation

Follow these steps to get the application up and running:

### 1. Clone the repository

```bash
git clone https://github.com/your-username/crypto-recommendation-service.git
cd crypto-recommendation-service
```

### 2. Build the project using Maven or Gradle

If you're using Maven:

```bash
mvn clean install
```

If you're using Gradle:

```bash
gradle build
```

### 3. Set up your environment

- **Database**: By default, H2 is used for demonstration purposes. You can configure a different database in `application.properties` if needed.
- **CSV Files**: Ensure that the required CSV files for cryptocurrency symbols are placed in the appropriate folder or path as expected by the service.

### 4. Configuration

Edit the following configuration in `application.properties`:

```properties
# Swagger and OpenAPI settings
springfox.documentation.enabled=true

# Database configuration (for H2)
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect

# Security configuration
spring.security.user.name=admin
spring.security.user.password=adminPass
spring.security.user.roles=ADMIN

# Server settings
server.port=8080
```

## Endpoints

### Public Endpoints (No Authentication Required)

- `GET /cryptos/{symbol}/stats`: Retrieves statistical data for a specific cryptocurrency.
- `GET /cryptos/sorted-by-range`: Retrieves a sorted list of cryptocurrencies by normalized range.
- `GET /cryptos/highest-range?date={date}`: Retrieves the cryptocurrency with the highest range for a specific day.

### Admin-Only Endpoints (Requires Authentication)

- `POST /cryptos/load-data/{symbol}`: Loads cryptocurrency data for the given symbol from a CSV file.

**Example Response:**

- For `GET /cryptos/{symbol}/stats`:

```json
{
  "volume": 1000000.0,
  "price": 150.0,
  "range": 50.0
}
```

- For `POST /cryptos/load-data/{symbol}`:

```json
{
  "message": "Crypto data for symbol XRP loaded successfully!"
}
```

## Authentication

Authentication is required for all **POST** endpoints and any action marked with `@PreAuthorize("hasRole('ADMIN')")`.

- **Username**: `admin`
- **Password**: `adminPass`
- **Role**: `ADMIN`

The authentication is done via **Basic Authentication**. Make sure to provide the correct credentials in your API client (Postman, curl, etc.).

## Running the Application

### 1. Run locally using Maven

```bash
mvn spring-boot:run
```

### 2. Run locally using Gradle

```bash
gradle bootRun
```

### 3. Access the API

After starting the application, access the API by navigating to:

- `http://localhost:8080/cryptos` (API base path)
- `http://localhost:8080/swagger-ui.html` (Swagger UI for API documentation)

## Docker Setup

You can build and run the application in a Docker container.

### 1. Create a `Dockerfile`

```Dockerfile
# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-alpine

# Set the working directory in the container
WORKDIR /app

# Copy the JAR file into the container at /app
COPY target/crypto-recommendation-service.jar /app/crypto-recommendation-service.jar

# Make port 8080 available to the world outside this container
EXPOSE 8080

# Run the JAR file
ENTRYPOINT ["java", "-jar", "crypto-recommendation-service.jar"]
```

### 2. Build the Docker image

```bash
docker build -t crypto-recommendation-service .
```

### 3. Run the Docker container

```bash
docker run -p 8080:8080 crypto-recommendation-service
```

The application will be accessible on `http://localhost:8080`.

## Kubernetes Setup

### 1. Create a Kubernetes deployment YAML

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: crypto-recommendation-service
spec:
  replicas: 1
  selector:
    matchLabels:
      app: crypto-recommendation-service
  template:
    metadata:
      labels:
        app: crypto-recommendation-service
    spec:
      containers:
        - name: crypto-recommendation-service
          image: crypto-recommendation-service:latest
          ports:
            - containerPort: 8080
---
apiVersion: v1
kind: Service
metadata:
  name: crypto-recommendation-service
spec:
  selector:
    app: crypto-recommendation-service
  ports:
    - protocol: TCP
      port: 8080
      targetPort: 8080
  type: LoadBalancer
```

### 2. Apply the Kubernetes configuration

```bash
kubectl apply -f k8s-deployment.yaml
```

### 3. Check the status of the deployment

```bash
kubectl get pods
kubectl get services
```

The application will be deployed and accessible through a Kubernetes service.

## Testing

### Unit Tests

To run the unit tests, execute:

```bash
mvn test
```

or

```bash
gradle test
```

Ensure that tests for the following components are available and pass:

- **CryptoController**: Test API endpoints and error handling.
- **CryptoService**: Test the business logic for handling cryptocurrency data.
- **SecurityConfig**: Test authentication and authorization rules.

## Contributing

We welcome contributions to this project. To contribute:

1. Fork the repository.
2. Create a new branch (`git checkout -b feature-xyz`).
3. Commit your changes (`git commit -am 'Add feature xyz'`).
4. Push to the branch (`git push origin feature-xyz`).
5. Create a new pull request.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
