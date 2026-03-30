# syntax=docker/dockerfile:1
# Stage 1: Build
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
COPY gradle ./gradle
COPY gradlew ./
COPY build.gradle.kts settings.gradle.kts ./
COPY libs ./libs
RUN ./gradlew dependencies --no-daemon -q
COPY src ./src
RUN ./gradlew bootJar --no-daemon -q

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/HomeUtils-1.0.0.jar app.jar
EXPOSE 8730
ENTRYPOINT ["java", "-jar", "app.jar"]
