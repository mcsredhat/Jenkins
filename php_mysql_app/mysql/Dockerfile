# Base image - using official MySQL image
FROM mysql:8.0

# Build-time variables - corrected to match .env values
ARG SQLVER=8.0
ARG USER_ID=1010
ARG USER_NAME=sqluser
ARG GROUP_ID=1010
ARG GROUP_NAME=sqlgroup
ARG APP_PORT=3306
ARG APP_ENV=production
ARG BASH_SHELL=/bin/bash
ARG APP_DIR=/sqldb
ARG MOUNT1=/sqldb/data
ARG MOUNT2=/sqldb/logs
ARG MYSQL_ROOT_PASSWORD=rootpass123!@#
ARG MYSQL_DATABASE=myappdb
ARG MYSQL_PASSWORD=MyAppUserPass456!@#
ARG MYSQL_USER=sqluser

# Set runtime environment variables from ARGs
ENV SQLVER=${SQLVER} \
    USER_ID=${USER_ID} \
    USER_NAME=${USER_NAME} \
    GROUP_ID=${GROUP_ID} \
    GROUP_NAME=${GROUP_NAME} \
    APP_PORT=${APP_PORT} \
    APP_ENV=${APP_ENV} \
    BASH_SHELL=${BASH_SHELL} \
    APP_DIR=${APP_DIR} \
    MOUNT1=${MOUNT1} \
    MOUNT2=${MOUNT2} \
    MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD} \
    MYSQL_DATABASE=${MYSQL_DATABASE} \
    MYSQL_PASSWORD=${MYSQL_PASSWORD} \
    MYSQL_USER=${MYSQL_USER} \
    TZ=Europe/Paris

# Labels
LABEL maintainer="farajassulai@gmail.com"
LABEL environment=${APP_ENV}
LABEL description="MySQL database server with custom sqluser and configuration"
LABEL date-created="2025-06-19"
LABEL version=${SQLVER}

# Set working directory
WORKDIR ${APP_DIR}

# Create necessary directories, user, group and set permissions
RUN groupadd -g ${GROUP_ID} ${GROUP_NAME} && \
    useradd -u ${USER_ID} -g ${GROUP_NAME} -m -s /bin/bash ${USER_NAME} && \
    usermod -a -G 999 ${USER_NAME} && \
    mkdir -p ${APP_DIR} ${MOUNT1} ${MOUNT2} /etc/mysql/conf.d && \
    chown -R mysql:mysql ${APP_DIR} ${MOUNT1} ${MOUNT2} && \
    chmod 755 ${APP_DIR} ${MOUNT1} ${MOUNT2}

# Copy MySQL configuration files (make sure these files exist)
COPY mysql/config/my.cnf /etc/mysql/conf.d/custom.cnf
COPY mysql/config/init.sql /docker-entrypoint-initdb.d/

# Set proper permissions for config files
RUN chown mysql:mysql /etc/mysql/conf.d/custom.cnf && \
    chmod 644 /etc/mysql/conf.d/custom.cnf && \
    chown mysql:mysql /docker-entrypoint-initdb.d/init.sql && \
    chmod 644 /docker-entrypoint-initdb.d/init.sql

# Create volume mount points
VOLUME ["${MOUNT1}", "${MOUNT2}", "/var/lib/mysql"]

# Set shell
SHELL ["/bin/bash", "-o", "pipefail", "-c"]



# IMPORTANT: MySQL containers must run as root user for proper database initialization
# The official MySQL image handles security internally
# USER ${USER_NAME}  # Commented out - MySQL needs root privileges

# STOPSIGNAL (docker stop --signal=SIGTERM "container_id")
STOPSIGNAL SIGTERM

# Healthcheck
HEALTHCHECK --interval=30s \
    --timeout=15s \
    --start-period=30s \
    --retries=3 \
    CMD mysqladmin ping -h localhost -u root --password="$MYSQL_ROOT_PASSWORD" || exit 1

# Expose port
EXPOSE ${APP_PORT}

# Default command - use the official MySQL entrypoint
CMD ["mysqld"]
