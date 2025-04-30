
# ↻ queue-gw - Spring Boot Microservice

This module handles message processing and stores appropriated record
and its aggregation report in postgres.
exposes actuator health checks.

## ✅ Features

- Spring Boot 3.x
- Kafka consumer integration
- Actuator health endpoint at `/actuator/health`

## ⚒️ Configuration

Uses `application-docker.yml` for Docker environment. Key properties:

```yaml
spring:
kafka:
bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:kafka:9092}
datasource:
url: jdbc:postgresql://${POSTGRES_HOST:postgres-app}:${POSTGRES_PORT:5432}/${POSTGRES_DB:nba-games-stats}
username: ${POSTGRES_USER:stats_user}
password: ${POSTGRES_PASSWORD:stats_pass}
```

## 🐳 Docker Support

Dockerfile located at `queue-gw/Dockerfile`. Built and run via:

```bash
docker-compose up --build
```

## 🔍 Health Check

Accessible at:
```
http://localhost:8000/actuator/health
```