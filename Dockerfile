# Stage 1: Build the Java application using a Maven image
FROM maven:3.9.6-eclipse-temurin-17 AS builder

# Set the working directory inside the container
WORKDIR /app

# Copy the project files into the image
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package

# Stage 2: Create a minimal image with the compiled JAR
FROM openjdk:17-slim

# Set the working directory
WORKDIR /app

# Copy the built JAR file from the 'builder' stage
COPY --from=builder /app/target/*.jar etl-dataflow.jar

# Define the entrypoint to run the JAR
ENTRYPOINT ["java", "-jar", "etl-dataflow.jar"]