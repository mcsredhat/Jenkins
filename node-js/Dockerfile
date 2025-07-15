# Base image
FROM node:18-slim

# Metadata
LABEL maintainer="farajassulai@gmail.com" \
    version="1.0" \
    app_name="node_app" \
    app_version="1.0.0" \
    description="Node.js app using non-root user"

# Arguments
ARG NODEVERSION=18
ARG USERNAME=nodeuser
ARG USERID=1001
ARG GROUPNAME=nodegroup
ARG GROUPID=1001
ARG APP_PORT=3000
ARG SHELL=/bin/bash

# Environment variables
ENV NODE_VERSION=${NODEVERSION} \
    USERNAME=${USERNAME} \
    USERID=${USERID} \
    GROUPNAME=${GROUPNAME} \
    GROUPID=${GROUPID} \
    APP_PORT=${APP_PORT} \
    SHELL=${SHELL} \
    NODE_ENV=production

# Set working directory
WORKDIR /app

# Install system dependencies and create user
RUN apt-get update && \
    apt-get install -y curl && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/* && \
    groupadd -g ${GROUPID} ${GROUPNAME} && \
    useradd -m -u ${USERID} -g ${GROUPID} -s ${SHELL} ${USERNAME}

# Copy package files and install dependencies
COPY package*.json ./
RUN npm install --omit=dev && \
    npm cache clean --force

# Copy application code (excluding files in .dockerignore)
COPY server.js ./

# Create directories and set permissions
RUN mkdir -p /app/data /app/logs && \
    chown -R ${USERNAME}:${GROUPNAME} /app && \
    chmod 755 /app

# Define volumes
VOLUME ["/app/data", "/app/logs"]

# Switch to non-root user
USER ${USERNAME}

# Expose application port
EXPOSE ${APP_PORT}

# Health check endpoint (uses 0.0.0.0 for container networking)
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD curl -f http://0.0.0.0:${APP_PORT}/health || exit 1

# Stop signal
STOPSIGNAL SIGTERM

# Default command
CMD ["node", "server.js"]
