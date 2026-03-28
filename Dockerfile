FROM amazoncorretto:25.0.2-alpine
WORKDIR /app
COPY build/libs/mail-service.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]