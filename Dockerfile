# Use an official OpenJDK runtime as the base image
FROM openjdk:17-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the Spring Boot JAR file into the container (adjust the JAR name as necessary)
COPY target/crypto-recommendation-service-0.0.1-SNAPSHOT.jar app.jar

# Expose the port the app runs on (match the port configured in your Spring Boot app)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
