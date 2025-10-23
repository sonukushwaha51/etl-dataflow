FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY target/*.jar etl-dataflow.jar
RUN mvn clean package

ENTRYPOINT ["java", "-jar", "target/etl-dataflow.jar"]