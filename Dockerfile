# Java
FROM openjdk:18-jdk-alpine3.14
WORKDIR /app
COPY target/katalistpaymentservice*.jar .
COPY start.sh ./start.sh
#COPY .env ./.env

EXPOSE 8080

CMD sh start.sh

