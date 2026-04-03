FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

RUN addgroup -S appuser && adduser -S appuser -G appuser -h /app -s /sbin/nologin && \
    mkdir -p /data/attachments && \
    chown -R appuser:appuser /app /data/attachments

COPY build/libs/mail-service.jar app.jar
RUN chown appuser:appuser app.jar

USER appuser
EXPOSE 8081

ENTRYPOINT ["java", "--enable-native-access=ALL-UNNAMED", "-jar", "app.jar"]
