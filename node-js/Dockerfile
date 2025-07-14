# Base image
FROM node:18-slim

# Metadata
LABEL maintainer="farajassulai@gmail.com" \
    version="1.0" \
    app_name="node_java" \
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
ARG APP_HOST=localhost

# Environment variables
ENV NODE_VERSION=${NODEVERSION} \
    USERNAME=${USERNAME} \
    USERID=${USERID} \
    GROUPNAME=${GROUPNAME} \
    GROUPID=${GROUPID} \
    APP_PORT=${APP_PORT} \
    SHELL=${SHELL} \
    APP_HOST=${APP_HOST} \
    NODE_ENV=production

# Set working directory
WORKDIR /app

# Install system dependencies and create user
RUN apt-get update && \
    apt-get install -y curl && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/* && \
    groupadd -g ${GROUPID} ${GROUPNAME} && \
    useradd -m -u ${USERID} -g ${GROUPID} -s ${SHELL} ${USERNAME} && \
    mkdir -p /app/data /app/logs && \
    chown -R ${USERNAME}:${GROUPNAME} /app && \
    chmod 755 /app

# Copy package files and install dependencies before copying full source
COPY ./app/package*.json ./
RUN npm install --production

# Copy application code
COPY ./app ./

# Define volumes
VOLUME ["/app/data", "/app/logs"]

# Switch to non-root user
USER ${USERNAME}

# Set shell
SHELL ["/bin/bash", "-euxo", "pipefail", "-c"]

# Expose application port
EXPOSE ${APP_PORT}

# Health check endpoint (ensure your app has /health)
HEALTHCHECK --interval=30s --timeout=30s --start-period=5s --retries=3 \
    CMD curl -f http://${APP_HOST}:${APP_PORT}/health || exit 1

# Stop signal
STOPSIGNAL SIGTERM

# Default command (make sure server.js exists or change to npm start)
CMD ["node", "server.js"]
