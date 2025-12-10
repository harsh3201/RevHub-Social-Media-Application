# Stage 1: Build Angular Frontend
FROM node:18-alpine AS frontend-build
WORKDIR /app/frontend
# Copy package files and install dependencies
COPY RevHub/package*.json ./
RUN npm ci --silent
# Copy source code and build
COPY RevHub/ ./
RUN npm run build -- --configuration=production

# Stage 2: Build Spring Boot Backend
FROM maven:3.8.6-eclipse-temurin-17 AS backend-build
WORKDIR /app/backend
# Copy pom.xml and dependency download
COPY revHubBack/pom.xml ./
RUN mvn dependency:go-offline -B
# Copy source code and build
COPY revHubBack/src ./src
RUN mvn -B -DskipTests clean package

# Stage 3: Runtime Image (Unified Frontend + Backend in one container)
FROM eclipse-temurin:17-jre-jammy
ENV TZ=Asia/Kolkata

# Install Nginx and required tools
RUN apt-get update && apt-get install -y --no-install-recommends \
  nginx curl tzdata \
  && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# remove default nginx config
RUN rm -rf /etc/nginx/nginx.conf

# Copy Custom Nginx Config
COPY nginx.conf /etc/nginx/nginx.conf

# Copy frontend build output to nginx document root
COPY --from=frontend-build /app/frontend/dist/rev-hub/browser/. /usr/share/nginx/html/

# Copy backend jar
COPY --from=backend-build /app/backend/target/*.jar /app/app.jar

# Setup startup script
# We use a script that traps signals to cleanly shutdown both services
RUN echo '#!/bin/bash\n\
  set -e\n\
  \n\
  # Start Spring Boot Backend in background\n\
  java -jar /app/app.jar --spring.profiles.active=docker > /var/log/backend.log 2>&1 &\n\
  BACKEND_PID=$!\n\
  echo "Backend started with PID $BACKEND_PID"\n\
  \n\
  # Start Nginx in background\n\
  nginx -g "daemon off;" &\n\
  NGINX_PID=$!\n\
  echo "Nginx started with PID $NGINX_PID"\n\
  \n\
  # Handle termination signals\n\
  _term() {\n\
  echo "Caught signal! Stopping..."\n\
  kill -TERM "$NGINX_PID" 2>/dev/null\n\
  kill -TERM "$BACKEND_PID" 2>/dev/null\n\
  wait "$NGINX_PID"\n\
  wait "$BACKEND_PID"\n\
  exit 0\n\
  }\n\
  \n\
  trap _term SIGTERM SIGINT\n\
  \n\
  # Wait for processes to exit\n\
  wait -n $NGINX_PID $BACKEND_PID\n\
  ' > /app/start.sh && chmod +x /app/start.sh

EXPOSE 80 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:80/api/health || curl -f http://localhost:8080/actuator/health || exit 1

CMD ["/app/start.sh"]
