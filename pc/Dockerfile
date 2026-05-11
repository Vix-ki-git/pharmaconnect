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

# Entrypoint generates /app/config/application.properties at container start
# from env vars (Render-injected). Spring Boot auto-loads files under ./config/
# relative to WORKDIR, so no --spring.config.location flag is needed.
COPY entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh

# Render injects PORT at runtime; entrypoint.sh defaults it to 10000.
# EXPOSE is documentation only — Render uses $PORT for the actual mapping.
EXPOSE 10000

ENTRYPOINT ["/app/entrypoint.sh"]
