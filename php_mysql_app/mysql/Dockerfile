# Base image - using official MySQL image
FROM mysql:8.0

# Build-time variables - matching .env values exactly
ARG MYSQL_VERSION=8.0
ARG MYSQL_USER_ID=1010
ARG MYSQL_USER_NAME=sqluser
ARG MYSQL_GROUP_ID=1010
ARG MYSQL_GROUP_NAME=sqlgroup
ARG MYSQL_APP_PORT=3306
ARG MYSQL_APP_ENV=production
ARG MYSQL_BASH_SHELL=/bin/bash
ARG MYSQL_APP_DIR=/sqldb
ARG MYSQL_MOUNT1=/sqldb/data
ARG MYSQL_MOUNT2=/sqldb/logs
ARG MYSQL_ROOT_PASSWORD=rootpass123!@#
ARG MYSQL_DATABASE=myappdb
ARG MYSQL_PASSWORD=MyAppUserPass456!@#
ARG MYSQL_USER=sqluser

# Set runtime environment variables from ARGs
ENV MYSQL_VERSION=${MYSQL_VERSION} \
    MYSQL_USER_ID=${MYSQL_USER_ID} \
    MYSQL_USER_NAME=${MYSQL_USER_NAME} \
    MYSQL_GROUP_ID=${MYSQL_GROUP_ID} \
    MYSQL_GROUP_NAME=${MYSQL_GROUP_NAME} \
    MYSQL_APP_PORT=${MYSQL_APP_PORT} \
    MYSQL_APP_ENV=${MYSQL_APP_ENV} \
    MYSQL_BASH_SHELL=${MYSQL_BASH_SHELL} \
    MYSQL_APP_DIR=${MYSQL_APP_DIR} \
    MYSQL_MOUNT1=${MYSQL_MOUNT1} \
    MYSQL_MOUNT2=${MYSQL_MOUNT2} \
    MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD} \
    MYSQL_DATABASE=${MYSQL_DATABASE} \
    MYSQL_PASSWORD=${MYSQL_PASSWORD} \
    MYSQL_USER=${MYSQL_USER} \
    TZ=Europe/Paris

# Labels
LABEL maintainer="farajassulai@gmail.com"
LABEL environment=${MYSQL_APP_ENV}
LABEL description="MySQL database server with custom sqluser and configuration"
LABEL date-created="2025-07-20"
LABEL version=${MYSQL_VERSION}

# Create necessary directories with proper permissions
# Note: Keep MySQL's default data directory structure
RUN mkdir -p ${MYSQL_APP_DIR} ${MYSQL_MOUNT2} /var/log/mysql && \
    chown -R mysql:mysql ${MYSQL_APP_DIR} ${MYSQL_MOUNT2} /var/log/mysql && \
    chmod 755 ${MYSQL_APP_DIR} ${MYSQL_MOUNT2} && \
    chmod 755 /var/log/mysql

# Copy configuration files from mysql/src directory
COPY mysql/src/my.cnf /etc/mysql/conf.d/custom.cnf
COPY mysql/src/init.sql /docker-entrypoint-initdb.d/

# Set proper permissions for config files
RUN chown mysql:mysql /etc/mysql/conf.d/custom.cnf && \
    chmod 644 /etc/mysql/conf.d/custom.cnf && \
    chown mysql:mysql /docker-entrypoint-initdb.d/init.sql && \
    chmod 644 /docker-entrypoint-initdb.d/init.sql

# Create volume mount points (use MySQL's default data directory)
VOLUME ["${MYSQL_MOUNT2}", "/var/lib/mysql"]

# Set shell
SHELL ["/bin/bash", "-o", "pipefail", "-c"]

# Set working directory
WORKDIR ${MYSQL_APP_DIR}

# STOPSIGNAL
STOPSIGNAL SIGTERM

# Healthcheck
HEALTHCHECK --interval=30s \
    --timeout=15s \
    --start-period=100s \
    --retries=3 \
    CMD mysqladmin ping -h localhost -u root --password="$MYSQL_ROOT_PASSWORD" || exit 1

# Expose port
EXPOSE ${MYSQL_APP_PORT}

# Default command - use the official MySQL entrypoint
CMD ["mysqld"]
