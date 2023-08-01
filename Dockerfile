# Use the official Maven image as build stage
FROM maven:3.8.3-openjdk-17-slim AS build

# Copy the source code to the container
COPY pom.xml /app/
COPY src /app/src/

# Set the working directory to the project root
WORKDIR /app

# Build the Spring Boot application (you can adjust the command based on your project setup)
RUN mvn clean package

# Use the official OpenJDK 17 as base image for the application
#FROM --platform=linux/amd64 openjdk:17-alpine
FROM openjdk:17-alpine


# Set the working directory inside the container
WORKDIR /app

# Copy the Spring Boot executable JAR from the build stage to the application container
COPY --from=build /app/target/caixinha-backend-0.0.1-SNAPSHOT.jar /app/caixinha-backend-0.0.1-SNAPSHOT.jar

# Copy the SQLite database file to the container
COPY budgets-db.sqlite /app/budgets-db.sqlite

# Expose the port that your Spring Boot application listens to
EXPOSE 8080

# Run the Spring Boot application when the container starts
CMD ["java", "-jar", "/app/caixinha-backend-0.0.1-SNAPSHOT.jar"]
