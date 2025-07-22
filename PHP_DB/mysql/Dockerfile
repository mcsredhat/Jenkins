# Base image - using official MySQL image
FROM mysql:8.0

# Build-time variables
ARG MYSQL_VERSION=8.0
ARG MYSQL_USER_ID=1010
ARG MYSQL_USER_NAME=sqluser
ARG MYSQL_GROUP_ID=1010
ARG MYSQL_GROUP_NAME=sqlgroup
ARG MYSQL_APP_PORT=3306
ARG MYSQL_APP_ENV=production
ARG MYSQL_BASH_SHELL=/bin/bash
ARG MYSQL_APP_DIR=/var/lib/mysql
ARG MYSQL_MOUNT1=/var/lib/mysql
ARG MYSQL_MOUNT2=/var/log/mysql
ARG MYSQL_ROOT_PASSWORD=rootpass123!@#
ARG MYSQL_DATABASE=myappdb
ARG MYSQL_PASSWORD=MyAppUserPass456!@#
ARG MYSQL_USER=appuser
ARG CP_SQL_CFG=mysql/config
ARG TZ=Europe/Paris
ARG MYSQL_CHARSET=utf8mb4
ARG MYSQL_COLLATION=utf8mb4_unicode_ci
ARG MYSQL_LOWER_CASE_TABLE_NAMES=1
ARG MYSQL_MAX_CONNECTIONS=151
ARG MYSQL_INNODB_BUFFER_POOL_SIZE=256M

# Set runtime environment variables from ARGs
ENV MYSQL_VERSION=${MYSQL_VERSION} \
    USER_ID=${MYSQL_USER_ID} \
    USER_NAME=${MYSQL_USER_NAME} \
    GROUP_ID=${MYSQL_GROUP_ID} \
    GROUP_NAME=${MYSQL_GROUP_NAME} \
    APP_PORT=${MYSQL_APP_PORT} \
    APP_ENV=${MYSQL_APP_ENV} \
    BASH_SHELL=${MYSQL_BASH_SHELL} \
    APP_DIR=${MYSQL_APP_DIR} \
    MOUNT1=${MYSQL_MOUNT1} \
    MOUNT2=${MYSQL_MOUNT2} \
    MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD} \
    MYSQL_DATABASE=${MYSQL_DATABASE} \
    MYSQL_PASSWORD=${MYSQL_PASSWORD} \
    MYSQL_USER=${MYSQL_USER} \
    CP_SQL_CFG=${CP_SQL_CFG} \
    TZ=${TZ} \
    MYSQL_CHARSET=${MYSQL_CHARSET} \
    MYSQL_COLLATION=${MYSQL_COLLATION} \
    MYSQL_LOWER_CASE_TABLE_NAMES=${MYSQL_LOWER_CASE_TABLE_NAMES} \
    MYSQL_MAX_CONNECTIONS=${MYSQL_MAX_CONNECTIONS} \
    MYSQL_INNODB_BUFFER_POOL_SIZE=${MYSQL_INNODB_BUFFER_POOL_SIZE}

# Labels
LABEL maintainer="farajassulai@gmail.com"
LABEL environment=${MYSQL_APP_ENV}
LABEL description="MySQL database server with custom configuration"
LABEL date-created="2025-07-18"
LABEL version=${MYSQL_VERSION}

# Create additional user and group (but MySQL will still run as mysql user)
RUN groupadd -g ${GROUP_ID} ${GROUP_NAME} || true && \
    useradd -u ${USER_ID} -g ${GROUP_NAME} -m -s /bin/bash ${USER_NAME} || true && \
    mkdir -p /var/log/mysql && \
    chown -R mysql:mysql /var/log/mysql && \
    chmod 755 /var/log/mysql

# Copy MySQL configuration files
COPY mysql/config/my.cnf /etc/mysql/conf.d/custom.cnf
COPY mysql/config/init.sql /docker-entrypoint-initdb.d/

# Set proper permissions for config files
RUN chown mysql:mysql /etc/mysql/conf.d/custom.cnf && \
    chmod 644 /etc/mysql/conf.d/custom.cnf && \
    chown mysql:mysql /docker-entrypoint-initdb.d/init.sql && \
    chmod 644 /docker-entrypoint-initdb.d/init.sql

# Create volume mount points
VOLUME ["/var/lib/mysql", "/var/log/mysql"]

# Set shell
SHELL ["/bin/bash", "-o", "pipefail", "-c"]

# MySQL must run as mysql user - don't change this
# USER ${USER_NAME} # Commented out - MySQL needs to run as mysql user

# STOPSIGNAL (docker stop --signal=SIGTERM "container_id")
STOPSIGNAL SIGTERM

# Healthcheck
HEALTHCHECK --interval=30s \
    --timeout=15s \
    --start-period=60s \
    --retries=3 \
    CMD mysqladmin ping -h localhost -u root --password="$MYSQL_ROOT_PASSWORD" || exit 1

# Expose port
EXPOSE ${APP_PORT}

# Default command - use the official MySQL entrypoint
CMD ["mysqld"]
