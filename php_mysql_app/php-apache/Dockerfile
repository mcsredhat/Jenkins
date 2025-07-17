ARG PHPVER=8.1-apache
FROM php:${PHPVER}
ARG PHP_USER_ID=1100
ARG PHP_USER_NAME=phpapacheuser
ARG PHP_GROUP_ID=1100
ARG PHP_GROUP_NAME=phpapachegrp
ARG DIR_PATH=/var/www/html/
ARG APP_PORT=8989
ARG MOUNT1=/php/data
ARG MOUNT2=/php/logs
ARG SHELL=/bin/bash
ARG PHP_ENV=production
ARG VAR_RUN_PATH=/var/run/apache2
ARG CP_PHP_SRC=/php-apache/src
ARG CP_PHP_CFG=/php-apache/php-config
ARG CP_APACHE_CFG=/php-apache/apache-config
ARG VAR_LOGS=/var/log/apache2

RUN groupadd -g ${PHP_GROUP_ID} ${PHP_GROUP_NAME} && \
    useradd -u ${PHP_USER_ID} -g ${PHP_GROUP_NAME} -s ${SHELL} ${PHP_USER_NAME} && \
    mkdir -p ${DIR_PATH} ${MOUNT1} ${MOUNT2} ${VAR_RUN_PATH} ${VAR_LOGS} && \
    chown -R ${PHP_USER_NAME}:${PHP_GROUP_NAME} ${DIR_PATH} ${MOUNT1} ${MOUNT2} ${VAR_LOGS}
COPY . ${DIR_PATH}
USER ${PHP_USER_NAME}
WORKDIR ${DIR_PATH}
EXPOSE ${APP_PORT}
CMD ["apache2-foreground"]
