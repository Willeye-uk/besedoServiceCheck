FROM openjdk:21-slim

WORKDIR /app

COPY target/BesedoServiceCheck-1.0-SNAPSHOT-jar-with-dependencies.jar app.jar

ENV BSC_SEND_FROM="my_value"
ENV BSC_SEND_PASSWORD="my_value"
ENV BSC_SEND_TO="my_value"
ENV BSC_SEND_SERVER="my_value"
ENV BSC_SEND_SERVER_PORT="587"

CMD ["java", "-jar", "app.jar"]

