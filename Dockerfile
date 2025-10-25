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
FROM gcr.io/dataflow-templates-base/java17-template-launcher-base AS base

WORKDIR /app

# Copy the built JAR file from the 'builder' stage into the final image.
COPY --from=builder /app/target/etl-dataflow-shaded.jar etl-dataflow.jar

# Specify the main class of your pipeline.
ENV FLEX_TEMPLATE_JAVA_MAIN_CLASS="com.gcp.labs.etl.dataflow.Dataflow"

# Specify the classpath to the JAR file.
ENV FLEX_TEMPLATE_JAVA_CLASSPATH="/app/etl-dataflow.jar"

# Define the entrypoint to run the JAR
ENTRYPOINT [/opt/google/dataflow/java_template_launcher"]
