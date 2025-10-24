# Stage 1: Build the Java application with Maven
FROM maven:3.9.6-eclipse-temurin-17 AS builder

# Set the working directory inside the container
WORKDIR /app

# Copy the pom.xml file separately to leverage build cache.
# This layer is only invalidated when pom.xml changes.
COPY pom.xml .

# Download project dependencies (cache layer)
RUN mvn dependency:go-offline -B

# Copy the source code (this layer is invalidated on code changes)
COPY src ./src

# Compile and package the application
RUN mvn clean package -DskipTests

# Stage 2: Create a minimal production image with the compiled JAR
FROM openjdk:17-slim

# Set the working directory
WORKDIR /app

# Copy the built JAR file from the 'builder' stage
COPY --from=builder /app/target/*.jar etl-dataflow.jar

# Define the entrypoint to run the JAR
ENTRYPOINT ["java", "-jar", "etl-dataflow.jar"]
