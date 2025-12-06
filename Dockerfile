# Multi-stage build for RevHub (Frontend + Backend)

# Stage 1: Build Angular Frontend
FROM node:18-alpine AS frontend-build
WORKDIR /app/frontend
COPY RevHub/package*.json ./
RUN npm ci
COPY RevHub/ ./
RUN npm run build

# Stage 2: Build Spring Boot Backend
FROM maven:3.8.4-openjdk-17-slim AS backend-build
WORKDIR /app/backend
COPY revHubBack/pom.xml ./
RUN mvn dependency:go-offline -B
COPY revHubBack/src ./src
RUN mvn clean package -DskipTests -B

# Stage 3: Production Image with Nginx + Java
FROM nginx:alpine
RUN apk add --no-cache openjdk17-jre-headless curl tzdata
ENV TZ=Asia/Kolkata

# Copy Angular build to Nginx
COPY --from=frontend-build /app/frontend/dist/rev-hub/browser /usr/share/nginx/html

# Copy Spring Boot JAR
COPY --from=backend-build /app/backend/target/revHubBack-0.0.1-SNAPSHOT.jar /app/backend.jar

# Copy Nginx configuration
COPY nginx.conf /etc/nginx/nginx.conf

# Skip MongoDB installation - use external MongoDB
RUN mkdir -p /data/db

# Create startup script with MongoDB + health checks
RUN echo '#!/bin/sh' > /start.sh && \
    echo 'echo "Starting Spring Boot backend..."' >> /start.sh && \
    echo 'java -jar -Dspring.profiles.active=docker -Duser.timezone=Asia/Kolkata -Xmx512m /app/backend.jar &' >> /start.sh && \
    echo 'BACKEND_PID=$!' >> /start.sh && \
    echo 'echo "Waiting for backend to start..."' >> /start.sh && \
    echo 'sleep 15' >> /start.sh && \
    echo 'echo "Starting Nginx..."' >> /start.sh && \
    echo 'nginx -g "daemon off;" &' >> /start.sh && \
    echo 'NGINX_PID=$!' >> /start.sh && \
    echo 'wait $NGINX_PID' >> /start.sh && \
    chmod +x /start.sh

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost/api/health || exit 1

EXPOSE 80 8080 27017

CMD ["/start.sh"]