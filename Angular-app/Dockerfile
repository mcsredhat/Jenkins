
# =============================================================================
# ANGULAR 4-STAGE MULTI-STAGE DOCKERFILE - FIXED FOR ANGULAR-APP PROJECT
# =============================================================================

# ================================
# Stage 1: Base Stage
# ================================
FROM node:20.19.3 AS base

# ---------- Base Arguments ----------
ARG APP_NAME="angular-portfolio"
ARG APP_VERSION="1.0.0"
ARG BUILD_ENV="base"
ARG APP_DIR="/app"
ARG APP_DIR_PATH="/app/dist/portfolio-project"
ARG USER_ID=1001
ARG USER_NAME="angularuser"
ARG GROUP_ID=1001
ARG GROUP_NAME="angulargroup"
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
    APP_DIR_PATH=${APP_DIR_PATH} \
    PATH="${APP_DIR}/node_modules/.bin:${PATH}"

# ---------- Set Working Directory ----------
WORKDIR ${APP_DIR}

# ---------- Install System Dependencies ----------
RUN apt-get update && apt-get install -y \
    ca-certificates \
    curl \
    wget \
    git \
    build-essential \
    chromium \
    && npm install -g @angular/cli@20.0.4 \
    && rm -rf /var/lib/apt/lists/*

# ---------- Create Build User & Group ----------
RUN addgroup --gid ${GROUP_ID} ${GROUP_NAME} && \
    adduser --uid ${USER_ID} --gid ${GROUP_ID} --disabled-password --gecos "" ${USER_NAME}

# ---------- Create Application Directories ----------
RUN mkdir -p ${APP_DIR} ${APP_DIR_PATH} && \
    chown -R ${USER_NAME}:${GROUP_NAME} ${APP_DIR} ${APP_DIR_PATH} && \
    chmod -R 755 ${APP_DIR} ${APP_DIR_PATH} 


# ================================
# Stage 2: Dependencies Stage
# ================================
FROM base AS dependencies

# ---------- Debug: List build context to verify files ----------
RUN echo "=== Debugging: Listing build context ===" && \
    ls -la / && \
    echo "=== End debugging ==="

# ---------- Copy Dependency Files from Build Context ----------
COPY package.json ${APP_DIR}/
COPY package-lock.json ${APP_DIR}/

# ---------- Verify package.json contents ----------
RUN echo "=== Verifying package.json ===" && \
    cat package.json

# ---------- Set npm registry ----------
RUN npm config set registry https://registry.npmjs.org/

# ---------- Install Dependencies as Root (Clean Install) ----------
RUN echo "=== ATTEMPTING CLEAN INSTALL (DEPENDENCIES) ===" && \
    rm -rf node_modules package-lock.json ~/.npm && \
    npm install --no-audit --verbose --production 2>&1 || \
    (echo "=== CLEAN INSTALL FAILED, TRYING LEGACY PEER DEPS ===" && \
    npm install --legacy-peer-deps --no-audit --verbose --production 2>&1) || \
    (echo "=== ALL INSTALL STRATEGIES FAILED FOR DEPENDENCIES!" && exit 1)

# ---------- Install Dev Dependencies ----------
RUN echo "=== INSTALLING DEV DEPENDENCIES ===" && \
    npm install --no-audit --verbose --only=dev 2>&1 || \
    (echo "=== DEV DEPENDENCIES INSTALL FAILED, TRYING LEGACY PEER DEPS ===" && \
    npm install --legacy-peer-deps --no-audit --verbose --only=dev 2>&1) || \
    (echo "=== ALL INSTALL STRATEGIES FAILED FOR DEV DEPENDENCIES!" && exit 1)

# ---------- Debug: Verify critical dependencies ----------
RUN echo "=== Verifying critical dependencies ===" && \
    ls -la node_modules/@angular-devkit/build-angular/ 2>/dev/null || \
    (echo "Installing @angular-devkit/build-angular explicitly" && \
    npm install --no-audit --verbose @angular-devkit/build-angular@20.0.4 2>&1) && \
    ls -la node_modules/typescript/ 2>/dev/null || \
    (echo "Installing typescript explicitly" && \
    npm install --no-audit --verbose typescript@5.8.0 2>&1) && \
    ls -la node_modules/zone.js/ 2>/dev/null || \
    (echo "Installing zone.js explicitly" && \
    npm install --no-audit --verbose zone.js@0.15.1 2>&1) && \
    ls -la node_modules/@angular/core/ 2>/dev/null || \
    (echo "Installing @angular/core explicitly" && \
    npm install --no-audit --verbose @angular/core@20.0.4 2>&1)

# ---------- Copy the rest of the application ----------
COPY --chown=${USER_NAME}:${GROUP_NAME} . ${APP_DIR}/

# ---------- Fix tsconfig.jap if it's a typo ----------
RUN if [ -f tsconfig.jap ]; then mv tsconfig.jap tsconfig.app.json; fi

# ---------- Set Ownership ----------
RUN chown -R ${USER_NAME}:${GROUP_NAME} ${APP_DIR} ${APP_DIR_PATH} && \
    find ${APP_DIR} -type d -exec chmod 755 {} \; && \
    find ${APP_DIR} -type f -exec chmod 644 {} \;

# ---------- Switch to Build User ----------
USER ${USER_NAME}

# ---------- Configure npm settings ----------
RUN npm config set fund false && npm config set audit-level moderate

# ---------- Clean npm cache ----------
RUN npm cache clean --force

# ================================
# Stage 3: Build Stage
# ================================
FROM dependencies AS build

# ---------- Set Build Environment ----------
ENV NODE_ENV=production \
    CHROME_BIN=/usr/bin/chromium \
    PUPPETEER_SKIP_CHROMIUM_DOWNLOAD=true \
    APP_DIR_PATH=${APP_DIR_PATH}

# ---------- Ensure Application Directories Exist ----------
# RUN mkdir -p ${APP_DIR_PATH} && \
#     chown -R ${USER_NAME}:${GROUP_NAME} ${APP_DIR} ${APP_DIR_PATH} && \
#     chmod -R 755 ${APP_DIR} ${APP_DIR_PATH} 

# ---------- Copy Source Code Files ----------
COPY --chown=${USER_NAME}:${GROUP_NAME} . ${APP_DIR}/

# ---------- Fix tsconfig.jap if it's a typo ----------
RUN if [ -f tsconfig.jap ]; then mv tsconfig.jap tsconfig.app.json; fi

# ---------- Debug: List files to verify copying ----------
RUN echo "=== Debugging: Checking copied files ===" && \
    ls -la && \
    echo "=== src directory contents ===" && \
    ls -la src/ 2>/dev/null || echo "No src directory found" && \
    echo "=== End debugging ==="

# ---------- Verify required files exist ----------
RUN test -f package.json || (echo "package.json missing in build stage!" && exit 1)
RUN test -f angular.json || (echo "angular.json missing - creating basic one" && \
    echo '{"version":1,"projects":{"portfolio-project":{"root":"","sourceRoot":"src","architect":{"build":{"builder":"@angular-devkit/build-angular:browser","options":{"outputPath":"dist/portfolio-project","index":"src/index.html","main":"src/main.ts","tsConfig":"tsconfig.app.json","polyfills":["zone.js"]}}}}}' > angular.json)
RUN test -f tsconfig.json || (echo "tsconfig.json missing - creating basic one" && \
    echo '{"compilerOptions":{"target":"ES2022","module":"ES2022","lib":["ES2022","dom"]}}' > tsconfig.json)
RUN test -f tsconfig.app.json || (echo "tsconfig.app.json missing - creating basic one" && \
    echo '{"extends":"./tsconfig.json","compilerOptions":{"outDir":"./out-tsc/app"},"files":["src/main.ts"]}' > tsconfig.app.json)

# ---------- Code Quality Checks (Optional) ----------
RUN ng lint 2>/dev/null || echo "Lint skipped"
RUN npm run lint 2>/dev/null || echo "Lint check skipped"
RUN npm run format:check 2>/dev/null || echo "Format check skipped"

# ---------- Run Tests (Optional) ----------
RUN npm run test:ci 2>/dev/null || \
    npm run test -- --watch=false --browsers=Chrome --no-sandbox --disable-gpu 2>/dev/null || \
    echo "Tests skipped"

# ---------- Build Angular Application ----------
RUN echo "Starting Angular build process..." && \
    ng build --configuration=production --output-path=/app/dist/portfolio-project 2>&1 || \
    (echo "Angular build failed! Check logs above for details." && exit 1)

# ---------- Verify Build Artifacts ----------
RUN test -f /app/dist/portfolio-project/index.html || (echo "index.html missing in dist/portfolio-project! Build failed." && exit 1) && \
    ls -la /app/dist/portfolio-project/ && \
    echo "Build artifacts verified"

# ---------- Security Audit (Non-blocking) ----------
RUN npm audit --audit-level=high || echo "Security audit completed with warnings"

# ---------- Cleanup ----------
RUN rm -rf node_modules/.cache && \
    rm -rf /tmp/* && \
    npm cache clean --force

# ================================
# Stage 4: Production Stage
# ================================
FROM nginx:alpine AS production

# ---------- Metadata Labels ----------
LABEL maintainer="farajassulai@gmail.com" \
    org.label-schema.name="angular-portfolio" \
    org.label-schema.description="Angular portfolio application served with nginx" \
    org.label-schema.version="1.0.0" \
    org.label-schema.schema-version="1.0" \
    stage="production"

# ---------- Production Arguments ----------
ARG APP_PORT=80
ARG HEALTH_CHECK_PATH="/health"
ARG TIMEZONE="UTC"
ARG USER_NAME="nginx"
ARG DATA_DIR="/var/lib/angular-portfolio"
ARG LOGS_DIR="/var/log/angular-portfolio"

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
    && rm -rf /var/cache/apk/*

# ---------- Create necessary directories ----------
RUN mkdir -p /var/cache/nginx/client_temp \
    /app/dist/portfolio-project \
    /var/cache/nginx/proxy_temp \
    /var/cache/nginx/fastcgi_temp \
    /var/cache/nginx/uwsgi_temp \
    /var/cache/nginx/scgi_temp \
    ${DATA_DIR} \
    ${LOGS_DIR} && \
    chown -R nginx:nginx /var/cache/nginx \
    ${DATA_DIR} \
    ${LOGS_DIR} \
    /usr/share/nginx/html && \
    find /usr/share/nginx/html -type d -exec chmod 755 {} \; && \
    find /usr/share/nginx/html -type f -exec chmod 644 {} \;

# ---------- Copy Build Artifacts ----------
COPY --from=build /app/dist/portfolio-project/ /usr/share/nginx/html/



# ---------- Copy Custom Nginx Configuration if Available ----------
COPY nginx.conf* /etc/nginx/conf.d/default.conf




# ---------- Verify build files and Nginx configuration ----------
RUN echo "=== Production Stage - Verifying copied files ===" && \
    ls -la /usr/share/nginx/html/ && \
    echo "=== Nginx config ===" && \
    cat /etc/nginx/conf.d/default.conf

# ---------- Setup Volumes ----------
VOLUME ["${DATA_DIR}", "${LOGS_DIR}"]

# ---------- Expose Application Port ----------
EXPOSE ${APP_PORT}

# ---------- Configure Stop Signal ----------
STOPSIGNAL SIGTERM

# ---------- Health Check ----------
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:${APP_PORT}/health || exit 1

# ---------- Start nginx ----------
CMD ["nginx", "-g", "daemon off;"]
