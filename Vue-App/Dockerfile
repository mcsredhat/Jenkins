# ================================
# Stage 4: Production Stage (Updated with Custom Entrypoint)
# ================================
FROM nginx:alpine AS production

# ---------- Metadata Labels ----------
LABEL maintainer="farajassulai@gmail.com" \
    org.label-schema.name="vueapp" \
    org.label-schema.description="Vue.js application served with nginx" \
    org.label-schema.version="1.0.0" \
    org.label-schema.schema-version="1.0" \
    stage="production"

# ---------- Production Arguments ----------
ARG APP_PORT=8088
ARG HEALTH_CHECK_PATH="/health"
ARG TIMEZONE="UTC"
ARG USER_NAME="vueuser"
ARG DATA_DIR="/var/lib/vueapp"
ARG LOGS_DIR="/var/log/vueapp"

# ---------- Production Environment Variables ----------
ENV APP_PORT=${APP_PORT} \
    HEALTH_CHECK_PATH=${HEALTH_CHECK_PATH} \
    TZ=${TIMEZONE} \
    NODE_ENV=production \
    USER_NAME=${USER_NAME} \
    DATA_DIR=${DATA_DIR} \
    LOGS_DIR=${LOGS_DIR}

# ---------- Install Runtime Dependencies ----------
RUN apk update && apk add --no-cache \
    ca-certificates \
    curl \
    wget \
    tzdata \
    shadow && \
    rm -rf /var/cache/apk/*

# ---------- Create non-root user for nginx ----------
RUN addgroup -g 101 -S nginx || true && \
    adduser -S -D -H -u 101 -h /var/cache/nginx -s /sbin/nologin -G nginx -g nginx nginx || true

# ---------- Copy Build Artifacts from Build Stage ----------
COPY --from=build /app/dist /usr/share/nginx/html

# ---------- Copy Custom Nginx Configuration ----------
COPY nginx.conf /etc/nginx/nginx.conf

# ---------- Copy Custom Entrypoint Script ----------
COPY entrypoint.sh /usr/local/bin/entrypoint.sh

# ---------- Make Entrypoint Executable ----------
RUN chmod +x /usr/local/bin/entrypoint.sh

# ---------- Create necessary directories for nginx ----------
RUN mkdir -p /tmp/nginx /var/cache/nginx/client_temp /tmp/client_temp /tmp/proxy_temp_path /tmp/fastcgi_temp /tmp/uwsgi_temp /tmp/scgi_temp && \
    chown -R nginx:nginx /tmp/nginx /var/cache/nginx /tmp/client_temp /tmp/proxy_temp_path /tmp/fastcgi_temp /tmp/uwsgi_temp /tmp/scgi_temp /usr/share/nginx/html

# ---------- Create health check endpoint ----------
RUN echo '<!DOCTYPE html><html><head><title>Health Check</title></head><body><h1>Vue App OK</h1></body></html>' > /usr/share/nginx/html/health.html && \
    chown nginx:nginx /usr/share/nginx/html/health.html

# ---------- Create directories ----------
RUN mkdir -p ${DATA_DIR} ${LOGS_DIR} && \
    chown -R nginx:nginx ${DATA_DIR} ${LOGS_DIR}

# ---------- Switch to non-root user ----------
USER nginx

# ---------- Setup Volumes ----------
VOLUME ["${DATA_DIR}", "${LOGS_DIR}"]

# ---------- Expose Application Port ----------
EXPOSE ${APP_PORT}

# ---------- Configure Stop Signal ----------
STOPSIGNAL SIGTERM

# ---------- Health Check ----------
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:${APP_PORT}${HEALTH_CHECK_PATH} || exit 1

# ---------- Set Custom Entrypoint ----------
ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]

# ---------- Default Command (can be overridden) ----------
CMD ["nginx", "-g", "daemon off;"]
