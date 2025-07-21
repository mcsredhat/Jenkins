FROM php:8.1-apache

# ARG variables (removed sensitive defaults for security)
ARG PHP_VERSION=8.1-apache
ARG PHP_USER_ID=1100
ARG PHP_USER_NAME=phpapacheuser
ARG PHP_GROUP_ID=1100
ARG PHP_GROUP_NAME=phpapachegrp
ARG PHP_DIR_PATH=/var/www/html/
ARG PHP_APP_PORT=8088
ARG PHP_MOUNT1=/php/data
ARG PHP_MOUNT2=/php/logs
ARG PHP_SHELL=/bin/bash 
ARG PHP_HOSTNAME=php-apache-host
ARG PHP_ENV=production
ARG PHP_HEALTH_URL="http://localhost:8088"
ARG PHP_VAR_RUN_PATH=/var/run/apache2
ARG PHP_CP_SRC=/php-apache/src
ARG PHP_CP_PHP_CFG=/php-apache/php-config
ARG PHP_CP_APACHE_CFG=/php-apache/apache-config
ARG PHP_VAR_LOGS=/var/log/apache2
ARG TZ=Europe/Paris

# Create ENV variables
ENV PHP_VERSION=${PHP_VERSION} \
    USER_NAME=${PHP_USER_NAME} \
    USER_ID=${PHP_USER_ID} \
    GROUP_ID=${PHP_GROUP_ID} \
    GROUP_NAME=${PHP_GROUP_NAME} \
    DIR_PATH=${PHP_DIR_PATH} \
    APP_PORT=${PHP_APP_PORT} \
    MOUNT1_PATH=${PHP_MOUNT1} \
    MOUNT2_PATH=${PHP_MOUNT2} \
    SHELL_PATH=${PHP_SHELL} \
    HOSTNAME=${PHP_HOSTNAME} \
    PHP_ENV=${PHP_ENV} \
    HEALTH_URL=${PHP_HEALTH_URL} \
    VAR_RUN=${PHP_VAR_RUN_PATH} \
    VAR_LOGS=${PHP_VAR_LOGS} \
    CP_SRC=${PHP_CP_SRC} \
    CP_PHP_CFG=${PHP_CP_PHP_CFG} \
    CP_APACHE_CFG=${PHP_CP_APACHE_CFG} \
    TZ=${TZ}

# Labels
LABEL name="my_php_apache" \
    hostname="${PHP_HOSTNAME}" \
    app="backend" \
    tier="application" \
    environment="${PHP_ENV}" \
    maintainer="farajassulai@gmail.com" \
    description="php-apache2 application with custom configuration" \
    date-created="2025-07-18" \
    version="${PHP_VERSION}"

# Working directory
WORKDIR ${DIR_PATH}

# Update packages, install dependencies, create directories and user
RUN apt-get update && \
    apt-get install -y curl vim default-mysql-client && \
    docker-php-ext-install pdo_mysql && \
    mkdir -p ${VAR_RUN} ${DIR_PATH} ${MOUNT1_PATH} ${MOUNT2_PATH} ${VAR_LOGS}  && \
    touch ${VAR_LOGS}/php_errors.log && \
    groupadd -g ${GROUP_ID} ${GROUP_NAME} && \
    useradd -u ${USER_ID} -M -g ${GROUP_NAME} -s ${SHELL_PATH} ${USER_NAME} && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*
# Enable required Apache modules
RUN a2enmod rewrite headers expires deflate SSL
RUN docker-php-ext-install mysqli pdo pdo_mysql
# Copy configuration files
COPY ${CP_APACHE_CFG}/ports.conf /etc/apache2/ports.conf
COPY ${CP_APACHE_CFG}/000-default.conf /etc/apache2/sites-available/000-default.conf
COPY ${CP_PHP_CFG}/php.ini /usr/local/etc/php/php.ini
# COPY volumes/php_data ${DIR_PATH}/volumes/php_data
# COPY volumes/php_logs ${DIR_PATH}/volumes/php_logs

# Copy application source files
COPY ${CP_SRC}/index.php ${DIR_PATH}/

# Set proper ownership and permissions
RUN chown -R ${USER_NAME}:${GROUP_NAME} ${DIR_PATH} ${MOUNT1_PATH} ${MOUNT2_PATH} && \
    chmod -R 755 ${DIR_PATH} && \
    chmod 755 ${MOUNT1_PATH} ${MOUNT2_PATH} && \
    chmod 644 /etc/apache2/ports.conf /etc/apache2/sites-available/000-default.conf && \
    chmod 644 /usr/local/etc/php/php.ini && \
    chown -R www-data:www-data ${VAR_LOGS}
RUN chown -R www-data:www-data /var/www/html
RUN chmod -R 755 /var/www/html

# Enable Apache modules
RUN a2enmod rewrite headers expires deflate

# Enable the default site
RUN a2ensite 000-default

# Create mount points
VOLUME ["${MOUNT1_PATH}", "${MOUNT2_PATH}"]

# Set shell
SHELL ["/bin/bash", "-o", "pipefail", "-c"]

# Health check
HEALTHCHECK --interval=30s \
    --timeout=15s \
    --start-period=60s \
    --retries=5 \
    CMD curl -f ${HEALTH_URL} || exit 1

# Expose port
EXPOSE ${APP_PORT}

# Run command (Apache runs as root initially, then drops privileges)
CMD ["apache2-foreground"]
