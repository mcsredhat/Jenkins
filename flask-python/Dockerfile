# Select the image python (docker pull python:3.9-slim)
FROM python:3.9-slim

# Create metadata image (docker build --label maintainer="farajassulai@gmail.com" .)
LABEL maintainer="farajassulai@gmail.com" \
      version="1.0" \
      description="create python flask application with secure user" \
      app-name="flask/python" \
      app-version="1.0.0"

# Create variables environment ARG (docker build --build-arg USERID=1000 .)
ARG PYTHON_VERSION=3.9
ARG USERID=1000
ARG USERNAME=appuser
ARG GROUPID=1000
ARG GROUPNAME=appgroup
ARG APP_PORT=5000
ARG APP_VER=production

# Create environment variables (docker run -d --env PYTHONBUFFERED=1 "image")
ENV PYTHONUNBUFFERED=1 \
    PYTHONDONTWRITEBYTECODE=1 \
    ENVIRONMENT=${APP_VER} \
    PORT=${APP_PORT}

# Create working directory on image (docker run -it -w /app "image" sh)
WORKDIR /app

# Copy requirements file first for better caching
COPY requirements.txt .

# Install the requirements package, update, create directory, user&group, chown directory
RUN pip install --no-cache-dir -r requirements.txt && \
    apt-get update && \
    apt-get install -y curl && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/* && \
    mkdir -p /app/data /app/logs && \
    groupadd -g ${GROUPID} ${GROUPNAME} && \
    useradd -m -u ${USERID} -g ${GROUPID} -s /bin/bash ${USERNAME} && \
    chown -R ${USERID}:${GROUPID} /app /app/data /app/logs

# Copy the directory project code (docker cp src "container-id":/app/src)
COPY src/ ./src/

# VOLUME create mount point (docker run --volume=/app/data --volume=/app/logs "image")
VOLUME ["/app/data", "/app/logs"]

# Switch to non-root user (docker run -d --user appuser "container-id" sh)
USER ${USERID}

# Expose port (docker run -d --publish 5000:5000 "image")
EXPOSE ${APP_PORT}

# SHELL (docker exec -it "image" /bin/bash -o pipefail -c)
SHELL ["/bin/bash", "-o", "pipefail", "-c"]

# HEALTHCHECK (docker run --health-cmd="curl -f http://localhost:5000/health || exit 1")
HEALTHCHECK --interval=30s --timeout=30s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:${APP_PORT}/health || exit 1

# STOPSIGNAL (docker stop --signal=SIGTERM "image")
STOPSIGNAL SIGTERM

# CMD (docker exec -it "python" python ./src/app.py)
CMD ["python", "src/app.py"]
