# Stage 1: Builder
FROM eclipse-temurin:25-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew && \
    ./gradlew clean build -x test

# Stage 2: Runtime
FROM ghcr.io/graalvm/jdk-community:25
WORKDIR /app

# Create non-root user
RUN groupadd -r appuser && useradd -r -g appuser -d /app -s /sbin/nologin appuser

# Copy JAR from builder
COPY --from=builder /app/build/libs/mail-service.jar app.jar
RUN chown appuser:appuser app.jar

# Switch to non-root user
USER appuser

# Expose correct port
EXPOSE 8081

# Entrypoint with native access flag
ENTRYPOINT ["java", "--enable-native-access=ALL-UNNAMED", "-jar", "app.jar"]
