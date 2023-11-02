# Java
FROM maven:3.8-openjdk-18 as builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests=true

FROM openjdk:18-jdk-slim
WORKDIR /app
COPY --from=builder /app/target/katalistpaymentservice-0.0.1*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]