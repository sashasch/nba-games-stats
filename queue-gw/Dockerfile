# ---------- Build stage ----------
FROM gradle:8.4.0-jdk17 AS builder

WORKDIR /app

# Copy only required files to leverage Docker cache
COPY settings.gradle settings.gradle
COPY build.gradle build.gradle
COPY queue-gw/build.gradle queue-gw/build.gradle
COPY gradle gradle
COPY queue-gw/src queue-gw/src

# Pre-download dependencies and build the app
RUN gradle :queue-gw:bootJar --no-daemon

# ---------- Runtime stage ----------
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy built JAR from build container
COPY --from=builder /app/queue-gw/build/libs/queue-gw-*.jar app.jar

# Expose health endpoint port
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
