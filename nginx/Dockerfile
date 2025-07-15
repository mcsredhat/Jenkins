# Docker images (docker pull nginx:alpine)
FROM nginx:alpine

# Add metadata images   
LABEL maintainer="farajassulai@gmail.com" \
      environment="production" \
      description="Create webserver using nginx and secure by using non-root user" \
      created_date="2025-6-16" \
      APP_name="webserver-nginx" \
      APP_VER="1.0.0" 

# Add the arguments variables 
ARG NGINXVER=alpine
ARG USER_ID=1111
ARG USER_NAME=nginxusr
ARG GROUP_ID=1111
ARG GROUP_NAME=nginxusr
ARG APP_PORT=8888
ARG HOST=localhost
ARG APP_DIR=/etc/nginx 
ARG MOUNT1=/nginx/data 
ARG MOUNT2=/nginx/logs 
ARG LOCAL_URL=http://localhost:8888/health 
ARG SHELL=/bin/bash 
ARG VAR_log=/var/log/nginx
ARG VAR_cache=/var/cache/nginx
ARG dir_tmp=/tmp
ARG dir_run=/run 

# The environment variables for the image 
ENV APP_version=${NGINXVER} \
      port=${APP_PORT} \
      USER_ID=${USER_ID} \
      USER_NAME=${USER_NAME} \
      GROUP_ID=${GROUP_ID} \
      GROUP_NAME=${GROUP_NAME} \
      APP_PORT=${APP_PORT} \
      HOST=${HOST} \
      APP_DIR=${APP_DIR} \
      MOUNT1=${MOUNT1} \
      MOUNT2=${MOUNT2} \
      LOCAL_URL=${LOCAL_URL} \
      log_dir=${VAR_log} \
      cache_dir=${VAR_cache} \
      dir_tmp=${dir_tmp} \
      dir_run=${dir_run} 

# Create working directory 
WORKDIR ${APP_DIR} 

# Install updates & packages, create user and group, create directories, set permissions
RUN apk update && \
      apk add --no-cache curl bash && \
      mkdir -p ${APP_DIR} ${MOUNT1} ${MOUNT2} ${VAR_log} ${VAR_cache} ${dir_tmp} ${dir_run} \
      ${VAR_cache}/client_temp ${VAR_cache}/proxy_temp ${VAR_cache}/fastcgi_temp \
      ${VAR_cache}/uwsgi_temp ${VAR_cache}/scgi_temp && \
      touch ${VAR_log}/access.log ${VAR_log}/error.log && \
      addgroup -g ${GROUP_ID} ${GROUP_NAME} && \
      adduser -u ${USER_ID} -D -h ${APP_DIR} -s ${SHELL} -G ${GROUP_NAME} ${USER_NAME} && \
      chown -R ${USER_NAME}:${GROUP_NAME} ${APP_DIR} ${MOUNT1} ${MOUNT2} ${VAR_log} ${VAR_cache} ${dir_tmp} ${dir_run} && \
      chmod 755 ${APP_DIR} ${MOUNT1} ${MOUNT2} ${VAR_log} ${VAR_cache} ${dir_tmp} ${dir_run} \
      ${VAR_cache}/client_temp ${VAR_cache}/proxy_temp ${VAR_cache}/fastcgi_temp \
      ${VAR_cache}/uwsgi_temp ${VAR_cache}/scgi_temp && \
      chmod 644 ${VAR_log}/*.log && \
      rm -rf /var/cache/apk/*

# Create a simple index.html file
RUN echo '<html><head><title>Nginx Web Server</title></head><body><h1>Welcome to Nginx!</h1><p>This is a containerized nginx web server.</p><p>Health check: <a href="/health">/health</a></p></body></html>' > /usr/share/nginx/html/index.html

# Copy the project files 
COPY conf/server.conf /etc/nginx/conf.d/server.conf

# Remove default nginx config to avoid conflicts
RUN rm /etc/nginx/conf.d/default.conf

# Add the mount point volumes
VOLUME ["${MOUNT1}", "${MOUNT2}"]

# Identify the shell to be used in image 
SHELL ["/bin/bash", "-o", "pipefail", "-c"]

# Port to be used internally in the image 
EXPOSE ${APP_PORT} 

# Switch to non-root user
USER ${USER_ID}

# Signal stop for the image 
STOPSIGNAL SIGTERM 

# Healthcheck for the image 
HEALTHCHECK --interval=30s \
      --timeout=30s \
      --start-period=5s \
      --retries=3 \
      CMD curl -f ${LOCAL_URL} || exit 1

# Run the command 
CMD ["nginx", "-g", "daemon off;"]
