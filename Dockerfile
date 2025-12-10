# Stage 1: Build Angular Frontend
FROM node:18-alpine AS frontend-build
WORKDIR /app/frontend
COPY RevHub/package*.json ./
RUN npm ci --legacy-peer-deps --quiet
COPY RevHub/ ./
RUN npm run build -- --configuration=production

# Stage 2: Build Spring Boot Backend
FROM maven:3.8.6-eclipse-temurin-17 AS backend-build
WORKDIR /app/backend
COPY revHubBack/pom.xml ./
RUN mkdir -p /root/.m2 && chmod -R 777 /root/.m2
RUN mvn dependency:go-offline -B
COPY revHubBack/src ./src
RUN mvn -B -DskipTests clean package

# Stage 3: Unified runtime image (nginx + java)
FROM eclipse-temurin:17-jre-jammy
ENV TZ=Asia/Kolkata

RUN apt-get update && apt-get install -y --no-install-recommends \
  nginx curl tzdata \
  && rm -rf /var/lib/apt/lists/*

WORKDIR /app

RUN rm -f /etc/nginx/nginx.conf
COPY nginx.conf /etc/nginx/nginx.conf

COPY --from=frontend-build /app/frontend/dist/rev-hub/browser/. /usr/share/nginx/html/
COPY --from=backend-build /app/backend/target/*.jar /app/app.jar

RUN printf '%s\n' '#!/bin/bash' \
  'set -e' \
  '' \
  'java -jar /app/app.jar --spring.profiles.active=docker > /var/log/backend.log 2>&1 &' \
  'BACKEND_PID=$!' \
  'echo "Backend started PID: $BACKEND_PID"' \
  '' \
  'nginx -g "daemon off;" &' \
  'NGINX_PID=$!' \
  'echo "Nginx started PID: $NGINX_PID"' \
  '' \
  '_term() {' \
  '  kill -TERM "$NGINX_PID" 2>/dev/null || true' \
  '  kill -TERM "$BACKEND_PID" 2>/dev/null || true' \
  '  wait "$NGINX_PID" 2>/dev/null || true' \
  '  wait "$BACKEND_PID" 2>/dev/null || true' \
  '  exit 0' \
  '}' \
  '' \
  'trap _term SIGTERM SIGINT' \
  '' \
  'wait -n $NGINX_PID $BACKEND_PID' \
  > /app/start.sh \
  && chmod +x /app/start.sh

EXPOSE 80 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD curl -f http://localhost/api/health || curl -f http://localhost:8080/actuator/health || exit 1

CMD ["/app/start.sh"]






