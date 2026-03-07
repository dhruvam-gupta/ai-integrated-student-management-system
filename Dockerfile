# Use a multi-stage build to create a Docker image for a Java application

# Stage 1: Build the application using Maven
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Create a lightweight runtime image
FROM eclipse-temurin:17-jdk-ubi10-minimal
WORKDIR /app
COPY --from=build /app/target/*.jar ai-intg-student-management-service.jar
EXPOSE 8080
CMD ["java", "-jar", "ai-intg-student-management-service.jar"]