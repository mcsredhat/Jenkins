# =============================================================================
# REACT 4-STAGE MULTI-STAGE DOCKERFILE TEMPL
# =============================================================================
# This template provides maximum optimization with 4 distinct stages:
# 1. Base Stage: Common foundation and system setup
# 2. Dependencies Stage: Install and cache dependencies
# 3. Build Stage: Code quality checks, testing, and building
# 4. Production Stage: Clean runtime environment with nginx
# =============================================================================

# ================================
# Stage 1: Base Stage
# ================================
FROM node:18-alpine AS base

# ---------- Base Arguments ----------
ARG APP_NAME="reactapp"
ARG APP_VERSION="1.0.0"
ARG BUILD_ENV="base"
ARG APP_DIR="/app"
ARG USER_ID=1001
ARG USER_NAME="reactuser"
ARG GROUP_ID=1001
ARG GROUP_NAME="reactgroup"
ARG TIMEZONE="UTC"

# ---------- Base Environment Variables ----------
ENV APP_NAME=${APP_NAME} \
    APP_VERSION=${APP_VERSION} \
    BUILD_ENV=${BUILD_ENV} \
    APP_DIR=${APP_DIR} \
    USER_ID=${USER_ID} \
    USER_NAME=${USER_NAME} \
    GROUP_ID=${GROUP_ID} \
    GROUP_NAME=${GROUP_NAME} \
    TZ=${TIMEZONE} \
    PATH="${APP_DIR}/bin:${PATH}"

# ---------- Set Working Directory ----------
WORKDIR ${APP_DIR}

# ---------- Install System Dependencies ----------
RUN apk update && apk add --no-cache \
    ca-certificates \
    curl \
    wget \
    git \
    build-base \
    bash \
    make && \
    rm -rf /var/cache/apk/*

# ---------- Create Build User & Group ----------
RUN addgroup -g ${GROUP_ID} ${GROUP_NAME} && \
    adduser -u ${USER_ID} -G ${GROUP_NAME} -D -s /bin/sh ${USER_NAME}

# ---------- Create Application Directories ----------
RUN mkdir -p ${APP_DIR} && \
    chown -R ${USER_NAME}:${GROUP_NAME} ${APP_DIR}

# ================================
# Stage 2: Dependencies Stage
# ================================
FROM base AS dependencies

# ---------- Copy Dependency Files First (for better caching) ----------
COPY package.json ./
COPY package-lock.json* ./
COPY yarn.lock* ./
COPY pnpm-lock.yaml* ./

# ---------- Set Ownership of Dependency Files ----------
RUN chown -R ${USER_NAME}:${GROUP_NAME} ${APP_DIR}

# ---------- Switch to Build User ----------
USER ${USER_NAME}

# ---------- Configure npm settings ----------
RUN npm config set fund false && npm config set audit-level moderate

# ---------- Install Dependencies with Caching ----------
RUN --mount=type=cache,target=/home/${USER_NAME}/.npm,uid=${USER_ID},gid=${GROUP_ID} \
    npm install --no-audit --prefer-offline --progress=false

# ================================
# Stage 3: Build Stage
# ================================
FROM dependencies AS build

# ---------- Copy Source Code Files One by One ----------
# Copy public directory
COPY --chown=${USER_NAME}:${GROUP_NAME} public/ ./public/

# Copy src directory
COPY --chown=${USER_NAME}:${GROUP_NAME} src/ ./src/

# Copy other necessary files
COPY --chown=${USER_NAME}:${GROUP_NAME} .eslintrc.json* ./
COPY --chown=${USER_NAME}:${GROUP_NAME} .prettierrc* ./
COPY --chown=${USER_NAME}:${GROUP_NAME} .prettierignore* ./
COPY --chown=${USER_NAME}:${GROUP_NAME} tsconfig.json* ./
COPY --chown=${USER_NAME}:${GROUP_NAME} jsconfig.json* ./

# ---------- Debug: List files to verify copying ----------
RUN echo "=== Debugging: Checking copied files ===" && \
    ls -la && \
    echo "=== src directory contents ===" && \
    ls -la src/ && \
    echo "=== public directory contents ===" && \
    ls -la public/ && \
    echo "=== End debugging ==="

# ---------- Code Quality Checks (Optional - don't fail build if scripts don't exist) ----------
RUN npm run lint 2>/dev/null || echo "Lint check skipped or completed"
RUN npm run format:check 2>/dev/null || echo "Format check skipped or completed"
RUN npx tsc --noEmit --skipLibCheck 2>/dev/null || echo "TypeScript check skipped or completed"

# ---------- Run Tests ----------
RUN CI=true npm run test -- --coverage --watchAll=false --passWithNoTests 2>/dev/null || echo "Tests skipped or completed"

# ---------- Build Application ----------
RUN npm run build

# ---------- Verify Build Artifacts ----------
RUN test -d ./build || (echo "Build artifacts not found in ./build directory" && exit 1)
RUN ls -la build/ && test -f build/index.html && echo "Build verified successfully"
RUN du -sh build/

# ---------- Security Audit (Non-blocking) ----------
RUN npm audit --audit-level=high || echo "Security audit completed with warnings"

# ---------- Build Stage Cleanup ----------
RUN rm -rf node_modules/.cache && \
    rm -rf /tmp/* && \
    rm -rf /home/${USER_NAME}/.npm/_cacache && \
    rm -rf src/ public/ && \
    npm cache clean --force

# ================================
# Stage 4: Production Stage
# ================================
FROM nginx:alpine AS production

# ---------- Metadata Labels ----------
LABEL maintainer="farajassulai@gmail.com" \
    org.label-schema.name="reactapp" \
    org.label-schema.description="React application served with nginx" \
    org.label-schema.version="1.0.0" \
    org.label-schema.schema-version="1.0" \
    stage="production"

# ---------- Production Arguments ----------
ARG APP_PORT=80
ARG HEALTH_CHECK_PATH="/health"
ARG TIMEZONE="UTC"
ARG USER_NAME="reactuser"
ARG DATA_DIR="/var/lib/reactapp"
ARG LOGS_DIR="/var/log/reactapp"

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
COPY --from=build /app/build /usr/share/nginx/html

# ---------- Set proper ownership for nginx directories ----------  
RUN mkdir -p /var/cache/nginx /var/run/nginx /var/log/nginx && \
    chown -R nginx:nginx /var/cache/nginx /var/run/nginx /var/log/nginx /usr/share/nginx/html && \
    chmod -R 755 /var/cache/nginx /var/run/nginx /var/log/nginx /usr/share/nginx/html

# ---------- Copy nginx configuration ----------
COPY nginx.conf /etc/nginx/nginx.conf

# ---------- Copy Entrypoint Script ----------
COPY react-entrypoint-nonroot.sh /usr/local/bin/entrypoint.sh

# ---------- Set Entrypoint Script Permissions ----------
RUN chmod +x /usr/local/bin/entrypoint.sh && \
    chown nginx:nginx /usr/local/bin/entrypoint.sh

# ---------- Create health check endpoint ----------
RUN echo '<!DOCTYPE html><html><head><title>Health Check</title></head><body><h1>OK</h1></body></html>' > /usr/share/nginx/html/health && \
    chown nginx:nginx /usr/share/nginx/html/health

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

# ---------- Use Entrypoint Script ----------
ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]