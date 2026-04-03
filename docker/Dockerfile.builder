FROM eclipse-temurin:25-jdk-alpine

# Set up gradle home so dependencies get cached, making it writable for any host user
ENV GRADLE_USER_HOME=/home/gradle/.gradle
RUN mkdir -p /home/gradle/.gradle && chmod -R 777 /home/gradle

WORKDIR /app

# The build happens when the container runs, so it respects the mounted volumes and host user
CMD ["sh", "-c", "chmod +x ./gradlew && ./gradlew clean build -x test"]
