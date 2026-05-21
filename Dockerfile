# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-26-alpine AS build

WORKDIR /app

# Copy only the POM first to cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the rest of the source code
COPY src ./src

# Build the project (skip tests for faster builds)
RUN mvn clean package -DskipTests

RUN mvn clean package -DskipTests && \
    echo "=== TARGET CONTENTS ===" && \
    ls -la /app/target/ 2>/dev/null || echo "TARGET DIR MISSING" && \
    echo "=== ALL JARS IN CONTAINER ===" && \
    find / -name "*.jar" -path "*/target/*" 2>/dev/null && \
    echo "=== POM PACKAGING TYPE ===" && \
    grep -i "<packaging>" /app/pom.xml || echo "No packaging tag found - defaults to jar"


# Stage 2: Create the final runtime image
FROM eclipse-temurin:26-alpine

WORKDIR /app

# Copy the Spring Boot jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Spring Boot application environment variables
ENV PORT=8080
ENV SPRING_APPLICATION_NAME=personal-finance-manager
ENV SPRING_JACKSON_SERIALIZATION_WRITE_DATES_AS_TIMESTAMPS=false
ENV SPRING_JACKSON_DATE_FORMAT=yyyy-MM-dd'T'HH:mm:ss
ENV SPRING_JACKSON_MAPPER_ACCEPT_CASE_INSENSITIVE_ENUMS=true
ENV SPRING_JPA_HIBERNATE_DDL_AUTO=update
ENV SPRING_JPA_PROPERTIES_HIBERNATE_FORMAT_SQL=true
ENV SPRING_JPA_PROPERTIES_HIBERNATE_JDBC_BATCH_SIZE=20
ENV SPRING_JPA_SHOW_SQL=false
ENV SERVER_SERVLET_CONTEXT_PATH=/
ENV SERVER_ERROR_INCLUDE_MESSAGE=always
ENV SERVER_ERROR_INCLUDE_BINDING_ERRORS=always
ENV JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
ENV JWT_EXPIRATION=86400000
ENV ML_SERVICE_URL=http://localhost:5000
ENV ML_API_KEY=
ENV SPRINGDOC_API_DOCS_PATH=/v3/api-docs
ENV SPRINGDOC_SWAGGER_UI_PATH=/swagger-ui.html
ENV SPRINGDOC_SWAGGER_UI_OPERATIONS_SORTER=method
ENV LOGGING_LEVEL_COM_FINANCEAPP=DEBUG
ENV LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_SECURITY=DEBUG
ENV LOGGING_LEVEL_ORG_HIBERNATE_SQL=WARN
ENV LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_WEB=DEBUG
ENV MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics
ENV MANAGEMENT_ENDPOINT_HEALTH_SHOW_DETAILS=when-authorized
ENV SPRING_PROFILES_ACTIVE=default

# Spring Boot runs on 8080 by default
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
