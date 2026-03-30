# syntax=docker/dockerfile:1
# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml ./
COPY libs/repo ./libs/repo
# Download all other dependencies
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn package -q -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/homeutils-1.0.0.jar app.jar
EXPOSE 8730
ENTRYPOINT ["java", "-jar", "app.jar"]
