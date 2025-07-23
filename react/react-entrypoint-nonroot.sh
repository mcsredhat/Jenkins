#!/bin/sh
set -e

# Function to handle graceful shutdown
graceful_shutdown() {
    echo "Received shutdown signal, gracefully shutting down..."
    if [ ! -z "$NGINX_PID" ]; then
        echo "Stopping nginx process..."
        kill -TERM "$NGINX_PID" 2>/dev/null || true
        wait "$NGINX_PID" 2>/dev/null || true
    fi
    echo "Graceful shutdown completed"
    exit 0
}

# Trap signals for graceful shutdown
trap graceful_shutdown TERM INT QUIT

# Initialize variables with defaults if not set
APP_NAME=${APP_NAME:-"reactapp"}
APP_VERSION=${APP_VERSION:-"1.0.0"}
NODE_ENV=${NODE_ENV:-"production"}
DATA_DIR=${DATA_DIR:-"/var/lib/reactapp"}
LOGS_DIR=${LOGS_DIR:-"/var/log/reactapp"}
CONFIG_DIR=${CONFIG_DIR:-"/etc/reactapp"}
APP_PORT=${APP_PORT:-"80"}
USER_NAME=${USER_NAME:-"nginx"}

# Pre-flight checks
echo "Starting ${APP_NAME} v${APP_VERSION} in ${NODE_ENV} mode..."
echo "Running as user: $(whoami)"
echo "User ID: $(id -u), Group ID: $(id -g)"

# Check if nginx html directory exists and has content
if [ ! -d "/usr/share/nginx/html" ] || [ -z "$(ls -A /usr/share/nginx/html 2>/dev/null)" ]; then
    echo "Error: Nginx html directory is missing or empty"
    exit 1
fi

# Verify React build files exist
if [ ! -f "/usr/share/nginx/html/index.html" ]; then
    echo "Error: React build files not found (index.html missing)"
    exit 1
fi

echo "React application files verified successfully"

# Test nginx configuration
echo "Testing nginx configuration..."
nginx -t 2>/dev/null || {
    echo "Error: Nginx configuration test failed"
    # Show more details about the error
    nginx -t
    exit 1
}

echo "Nginx configuration test passed"

# Create health check endpoint if it doesn't exist
if [ ! -f "/usr/share/nginx/html/health" ]; then
    echo "Creating health check endpoint..."
    echo '<!DOCTYPE html><html><head><title>Health Check</title></head><body><h1>OK</h1><p>Service is running on port '${APP_PORT}'</p><p>User: '${USER_NAME}'</p><p>Time: '$(date)'</p></body></html>' > /usr/share/nginx/html/health 2>/dev/null || {
        echo "Warning: Could not create health check endpoint"
    }
fi

# Create a simple 404 page if it doesn't exist
if [ ! -f "/usr/share/nginx/html/404.html" ]; then
    echo "Creating 404 error page..."
    cat > /usr/share/nginx/html/404.html 2>/dev/null << 'EOF' || echo "Warning: Could not create 404 page"
<!DOCTYPE html>
<html>
<head>
    <title>404 Not Found</title>
    <style>
        body { font-family: Arial, sans-serif; text-align: center; padding: 50px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; min-height: 100vh; margin: 0; display: flex; flex-direction: column; justify-content: center; }
        .container { background: rgba(255, 255, 255, 0.1); backdrop-filter: blur(10px); padding: 2rem; border-radius: 10px; max-width: 500px; margin: 0 auto; }
        h1 { color: #fff; margin-bottom: 1rem; }
        p { color: #f0f0f0; margin: 1rem 0; }
        a { color: #fff; text-decoration: none; background: rgba(255, 255, 255, 0.2); padding: 0.5rem 1rem; border-radius: 5px; display: inline-block; margin-top: 1rem; }
        a:hover { background: rgba(255, 255, 255, 0.3); }
    </style>
</head>
<body>
    <div class="container">
        <h1>404 - Page Not Found</h1>
        <p>The page you are looking for might have been removed, had its name changed, or is temporarily unavailable.</p>
        <a href="/">Go back to home</a>
    </div>
</body>
</html>
EOF
fi

# Create a simple 50x error page if it doesn't exist
if [ ! -f "/usr/share/nginx/html/50x.html" ]; then
    echo "Creating 50x error page..."
    cat > /usr/share/nginx/html/50x.html 2>/dev/null << 'EOF' || echo "Warning: Could not create 50x page"
<!DOCTYPE html>
<html>
<head>
    <title>Server Error</title>
    <style>
        body { font-family: Arial, sans-serif; text-align: center; padding: 50px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; min-height: 100vh; margin: 0; display: flex; flex-direction: column; justify-content: center; }
        .container { background: rgba(255, 255, 255, 0.1); backdrop-filter: blur(10px); padding: 2rem; border-radius: 10px; max-width: 500px; margin: 0 auto; }
        h1 { color: #d32f2f; margin-bottom: 1rem; }
        p { color: #f0f0f0; margin: 1rem 0; }
    </style>
</head>
<body>
    <div class="container">
        <h1>Server Error</h1>
        <p>The server encountered an internal error and was unable to complete your request.</p>
        <p>Please try again later.</p>
    </div>
</body>
</html>
EOF
fi

# Start nginx in the background
echo "Starting nginx server on port ${APP_PORT}..."
nginx -g "daemon off;" &

NGINX_PID=$!
echo "Nginx started with PID: $NGINX_PID"

# Wait for nginx to be ready
sleep 2

# Verify nginx is running
if ! kill -0 "$NGINX_PID" 2>/dev/null; then
    echo "Error: Nginx failed to start"
    exit 1
fi

# Test if the application is responding (if curl is available)
echo "Testing application response..."
if command -v curl >/dev/null 2>&1; then
    for i in 1 2 3; do
        if curl -f -s "http://localhost:${APP_PORT}/health" >/dev/null; then
            echo "Application health check passed"
            break
        else
            echo "Health check attempt $i failed, retrying..."
            sleep 2
        fi
        if [ $i -eq 3 ]; then
            echo "Warning: Health check failed after 3 attempts"
        fi
    done
else
    echo "curl not available, skipping health check"
fi

echo "Application started successfully and ready to serve requests"
echo "Health check available at: http://localhost:${APP_PORT}/health"
echo "Application available at: http://localhost:${APP_PORT}/"

# Log some useful information
echo "=== Application Information ==="
echo "App Name: ${APP_NAME}"
echo "App Version: ${APP_VERSION}"
echo "Environment: ${NODE_ENV}"
echo "Port: ${APP_PORT}"
echo "User: ${USER_NAME} ($(id -u):$(id -g))"
echo "React Build Files:"
ls -la /usr/share/nginx/html/ | head -10
echo "=== End Information ==="

# Wait for the nginx process
wait "$NGINX_PID"