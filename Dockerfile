# Stage 1: Build Angular Frontend
FROM node:18-alpine AS frontend-build
WORKDIR /app/frontend
COPY RevHub/package*.json ./
RUN npm ci --silent
COPY RevHub/ ./
RUN npm run build -- --configuration=production

# Stage 2: Build Spring Boot Backend
FROM maven:3.8.6-eclipse-temurin-17 AS backend-build
WORKDIR /app/backend
COPY revHubBack/pom.xml ./
RUN mvn dependency:go-offline -B
COPY revHubBack/src ./src
RUN mvn -B -DskipTests clean package

# Stage 3: Runtime Image (nginx + Java backend)
FROM eclipse-temurin:17-jre-jammy

RUN apt-get update && apt-get install -y --no-install-recommends \
    nginx curl tzdata \
  && rm -rf /var/lib/apt/lists/*

ENV TZ=Asia/Kolkata

WORKDIR /app

# Copy frontend build output to nginx
COPY --from=frontend-build /app/frontend/dist/rev-hub /usr/share/nginx/html

# Copy backend jar (use wildcard)
COPY --from=backend-build /app/backend/target/*.jar /app/app.jar

# Startup script to run both backend + nginx safely
RUN printf '#!/bin/sh\n\
set -e\n\
java -jar /app/app.jar -Dspring.profiles.active=docker &\n\
BACKEND_PID=$!\n\
sleep 6\n\
nginx -g "daemon off;" &\n\
NGINX_PID=$!\n\
_term() {\n\
  kill -TERM "$NGINX_PID" 2>/dev/null || true\n\
  kill -TERM "$BACKEND_PID" 2>/dev/null || true\n\
  wait "$NGINX_PID" 2>/dev/null || true\n\
  wait "$BACKEND_PID" 2>/dev/null || true\n\
  exit 0\n\
}\n\
trap _term SIGTERM SIGINT\n\
wait -n\n\
_term\n' > /app/start.sh && chmod +x /app/start.sh

EXPOSE 80 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=3 \
  CMD curl -f http://localhost:8080/api/health || exit 1

CMD ["/app/start.sh"]
