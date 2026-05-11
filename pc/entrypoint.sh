#!/bin/sh
# Generates /app/config/application.properties at container start using env vars
# injected by Render, then execs the Spring Boot jar.
#
# Why startup-generated rather than baked-in: application.properties is
# gitignored to keep secrets out of source control, so the Docker build context
# on Render does NOT contain it. Spring Boot looks for ./config/application.properties
# relative to the working directory (here, /app), so writing to /app/config/
# means Spring picks it up automatically with no extra --spring.config.location flag.
set -eu

mkdir -p /app/config

cat > /app/config/application.properties <<EOF
# ===============================
# APP CONFIGURATION
# ===============================
spring.application.name=pharmaconnect
server.port=${PORT:-10000}

# ===============================
# DATABASE CONFIGURATION (MySQL)
# ===============================
spring.datasource.url=${MYSQL_URL:-}
spring.datasource.username=${MYSQL_USERNAME:-}
spring.datasource.password=${MYSQL_PASSWORD:-}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# ===============================
# JPA / HIBERNATE CONFIGURATION
# ===============================
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# ===============================
# CORS
# ===============================
app.cors.origins=${CORS_ORIGINS:-http://localhost:4200}

# ===============================
# THIRD-PARTY SERVICES
# ===============================
google.client.id=${GOOGLE_CLIENT_ID:-}
google.client.secret=${GOOGLE_CLIENT_SECRET:-}
brevo.api.key=${BREVO_API_KEY:-}

# Groq (chatbot)
groq.api.key=${GROQ_API_KEY:-}
groq.model=${GROQ_MODEL:-llama-3.3-70b-versatile}

# ===============================
# FILE UPLOADS
# ===============================
spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=5MB
EOF

exec java -jar /app/app.jar
