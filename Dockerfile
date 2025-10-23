FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY ..
RUN mvn clean package

CMD ["java", "-jar", "target/etl-dataflow-1.1.0-SNAPSHOT.jar"]