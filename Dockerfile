# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-25-alpine AS build

WORKDIR /app

# Copy only the POM first to cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the rest of the source code
COPY src ./src

# Build the project (skip tests for faster builds)
RUN mvn clean package -DskipTests

# Stage 2: Create the final runtime image
FROM eclipse-temurin:25-alpine

WORKDIR /app

# Copy the Spring Boot jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Spring Boot runs on 8080 by default
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]