# Base image - Use official MySQL image
FROM mysql:8.0

# Build-time variables
ARG SQLVER=8.0
ARG USER_ID=1010
ARG USER_NAME=sqluser
ARG GROUP_ID=1010
ARG GROUP_NAME=sqlgroup
ARG APP_PORT=3306
ARG APP_DIR=/sqldb
ARG MOUNT1=/sqldb/data
ARG MOUNT2=/sqldb/logs

# Set runtime environment variables
ENV MYSQL_USER=${MYSQL_USER} \
    MYSQL_PASSWORD=${MYSQL_PASSWORD} \
    MYSQL_DATABASE=${MYSQL_DATABASE} \
    MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD} \
    TZ=Europe/Paris

# Labels
LABEL maintainer="farajassulai@gmail.com" \
      environment="production" \
      description="MySQL database server with custom sqluser and configuration" \
      date-created="2025-06-19" \
      version=${SQLVER}

# Set working directory
WORKDIR ${APP_DIR}

# Create necessary directories, user, group, and set permissions
RUN groupadd -g ${GROUP_ID} ${GROUP_NAME} && \
    useradd -u ${USER_ID} -g ${GROUP_NAME} -m -s /bin/bash ${USER_NAME} && \
    mkdir -p ${APP_DIR} ${MOUNT1} ${MOUNT2} /etc/mysql/conf.d && \
    chown -R ${USER_NAME}:${GROUP_NAME} ${APP_DIR} ${MOUNT1} ${MOUNT2} /etc/mysql/conf.d && \
    chmod 755 ${APP_DIR} ${MOUNT1} ${MOUNT2} && \
    chmod 644 /etc/mysql/conf.d

# Copy MySQL configuration files (validate existence)
COPY --chown=${USER_NAME}:${GROUP_NAME} mysql/config/my.cnf /etc/mysql/conf.d/custom.cnf
COPY --chown=${USER_NAME}:${GROUP_NAME} mysql/config/init.sql /docker-entrypoint-initdb.d/

# Set proper permissions for config files
RUN chmod 644 /etc/mysql/conf.d/custom.cnf /docker-entrypoint-initdb.d/init.sql

# Create volume mount points
VOLUME ["${MOUNT1}", "${MOUNT2}"]

# Set shell
SHELL ["/bin/bash", "-o", "pipefail", "-c"]

# Switch to non-root user
USER ${USER_NAME}

# Healthcheck using MYSQL_USER for consistency
HEALTHCHECK --interval=30s \
            --timeout=15s \
            --start-period=30s \
            --retries=3 \
            CMD mysqladmin ping -h localhost -u ${MYSQL_USER} --password="${MYSQL_PASSWORD}" || exit 1

# Expose port
EXPOSE ${APP_PORT}

# Default command
CMD ["mysqld"]
