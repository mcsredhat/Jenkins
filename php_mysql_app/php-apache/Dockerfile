# Base image
FROM php:8.1-apache

# Build-time variables
ARG PHPVER=8.1-apache
ARG USER_ID=1100
ARG USER_NAME=phpapacheuser
ARG GROUP_ID=1100
ARG GROUP_NAME=phpapachegrp
ARG DIR_PATH=/var/www/html
ARG APP_PORT=8989
ARG MOUNT1=/php/data
ARG MOUNT2=/php/logs
ARG PHP_ENV=production

# Set runtime environment variables
ENV USER_ID=${USER_ID} \
    USER_NAME=${USER_NAME} \
    GROUP_ID=${GROUP_ID} \
    GROUP_NAME=${GROUP_NAME} \
    DIR_PATH=${DIR_PATH} \
    APP_PORT=${APP_PORT} \
    MOUNT1=${MOUNT1} \
    MOUNT2=${MOUNT2} \
    TZ=Europe/Paris

# Labels
LABEL maintainer="farajassulai@gmail.com" \
      environment=${PHP_ENV} \
      description="PHP-Apache application with custom configuration" \
      date-created="2025-06-20" \
      version=${PHPVER} \
      app="backend" \
      tier="application"

# Set working directory
WORKDIR ${DIR_PATH}

# Update packages, install dependencies, create directories, and set up user
RUN apt-get update && \
    apt-get install -y curl vim && \
    docker-php-ext-install pdo_mysql && \
    groupadd -g ${GROUP_ID} ${GROUP_NAME} && \
    useradd -u ${USER_ID} -g ${GROUP_NAME} -m -s /bin/bash ${USER_NAME} && \
    mkdir -p ${DIR_PATH} ${MOUNT1} ${MOUNT2} /var/log/apache2 /var/run/apache2 && \
    touch /var/log/apache2/php_errors.log && \
    chown -R ${USER_NAME}:${GROUP_NAME} ${DIR_PATH} ${MOUNT1} ${MOUNT2} /var/log/apache2 /var/run/apache2 && \
    chmod -R 755 ${DIR_PATH} ${MOUNT1} ${MOUNT2} && \
    chmod 644 /var/log/apache2/php_errors.log && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Copy configuration and source files
COPY --chown=${USER_NAME}:${GROUP_NAME} php-apache/config/ports.conf /etc/apache2/ports.conf
COPY --chown=${USER_NAME}:${GROUP_NAME} php-apache/config/000-default.conf /etc/apache2/sites-available/000-default.conf
COPY --chown=${USER_NAME}:${GROUP_NAME} php-apache/config/php.ini /usr/local/etc/php/php.ini
COPY --chown=${USER_NAME}:${GROUP_NAME} php-apache/src/ ${DIR_PATH}/

# Set permissions for configuration files
RUN chmod 644 /etc/apache2/ports.conf /etc/apache2/sites-available/000-default.conf /usr/local/etc/php/php.ini

# Enable Apache modules and site
RUN a2enmod rewrite headers expires deflate && \
    a2ensite 000-default

# Create volume mount points
VOLUME ["${MOUNT1}", "${MOUNT2}"]

# Set shell
SHELL ["/bin/bash", "-o", "pipefail", "-c"]

# Healthcheck
HEALTHCHECK --interval=30s \
            --timeout=15s \
            --start-period=60s \
            --retries=3 \
            CMD curl -f http://localhost:${APP_PORT}/health || exit 1

# Expose port
EXPOSE ${APP_PORT}

# Switch to non-root user
USER ${USER_NAME}

# Default command
CMD ["apache2-foreground"]
