FROM mysql:${MYSQL_VERSION:-8.0}
WORKDIR /var/lib/mysql
RUN mkdir -p /var/lib/mysql /var/log/mysql /etc/mysql/conf.d /var/run/mysqld && \
    chown -R mysql:mysql /var/lib/mysql /var/log/mysql /etc/mysql/conf.d /var/run/mysqld && \
    chmod 755 /var/lib/mysql /var/log/mysql /var/run/mysqld
COPY mysql/config/my.cnf /etc/mysql/conf.d/custom.cnf
COPY mysql/config/init.sql /docker-entrypoint-initdb.d/
RUN chown mysql:mysql /etc/mysql/conf.d/custom.cnf && \
    chmod 644 /etc/mysql/conf.d/custom.cnf && \
    chown mysql:mysql /docker-entrypoint-initdb.d/init.sql && \
    chmod 644 /docker-entrypoint-initdb.d/init.sql
VOLUME ["/var/lib/mysql", "/var/log/mysql"]
HEALTHCHECK --interval=30s \
    --timeout=15s \
    --start-period=30s \
    --retries=3 \
    CMD mysqladmin ping -h localhost -u ${MYSQL_HEALTHCHECK_USER:-healthcheck} --password="${MYSQL_HEALTHCHECK_PASSWORD:-SecureHealthPass2024!}" || exit 1
EXPOSE ${MYSQL_APP_PORT:-3306}
STOPSIGNAL SIGTERM
CMD ["mysqld"]
