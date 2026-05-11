# syntax=docker/dockerfile:1.6

# ---------- Build stage ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Cache dependencies before copying source so that source-only changes
# don't bust the Maven layer.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline -DskipTests

COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy the fat jar produced by spring-boot-maven-plugin
COPY --from=build /workspace/target/*.jar /app/app.jar

# Render injects PORT at runtime; Spring Boot reads it via server.port=${PORT:10000}.
# EXPOSE here is documentation only — Render uses $PORT for the actual mapping.
EXPOSE 10000

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
