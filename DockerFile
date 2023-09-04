# Maven
FROM maven:3.8.6-eclipse-temurin-18 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn -e -B dependency:resolve
COPY src ./src
RUN mvn clean -e -B package

# Java
FROM openjdk:18-jdk-alpine3.14
WORKDIR /app
COPY --from=builder /app/target/katalistpaymentservice*.jar .
COPY start.sh ./start.sh

EXPOSE 8080

CMD sh start.sh

