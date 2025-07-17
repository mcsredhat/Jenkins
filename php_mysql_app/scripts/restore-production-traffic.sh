```bash
#!/bin/bash
# Script to restore 100% traffic to production services and remove canary services
# Usage: ./restore-production-traffic.sh <environment> <production_port>
# Example: ./restore-production-traffic.sh production 8989

set -e

# Validate input arguments
if [ $# -ne 2 ]; then
    echo "Error: Usage: $0 <environment> <production_port>"
    exit 1
fi

ENVIRONMENT="$1"
PRODUCTION_PORT="$2"

# Define variables
COMPOSE_FILE="docker-compose.yml"
CANARY_COMPOSE_FILE="docker-compose-canary.yml"
APP_DIR="."
LB_CONTAINER="nginx-lb"
NGINX_CONF="/etc/nginx/conf.d/default.conf"

# Ensure Docker Compose files exist
if [ ! -f "$APP_DIR/$COMPOSE_FILE" ]; then
    echo "Error: $COMPOSE_FILE not found in $APP_DIR"
    exit 1
fi
if [ ! -f "$APP_DIR/$CANARY_COMPOSE_FILE" ]; then
    echo "Warning: $CANARY_COMPOSE_FILE not found, assuming canary services are already removed"
else
    # Stop and remove canary services
    echo "Stopping and removing canary services for environment: $ENVIRONMENT"
    cd "$APP_DIR"
    docker-compose -f "$CANARY_COMPOSE_FILE" down
    rm -f "$CANARY_COMPOSE_FILE"
fi

# Ensure production services are running
echo "Ensuring production services are running..."
docker-compose -f "$COMPOSE_FILE" up -d

# Wait for production services to be healthy
echo "Waiting for production services to be healthy..."
for service in my_php_apache mysql-project adminer; do
    timeout 300s bash -c "until docker inspect --format='{{.State.Health.Status}}' $service | grep -q 'healthy'; do sleep 5; echo 'Waiting for $service...'; done"
    if [ $? -ne 0 ]; then
        echo "Error: Production service $service failed to become healthy"
        exit 1
    fi
done

# Update Nginx configuration to route 100% traffic to production
echo "Restoring 100% traffic to production services on port $PRODUCTION_PORT..."
cat << EOF > nginx-production.conf
upstream backend {
    server my_php_apache:$PRODUCTION_PORT weight=100;
}

server {
    listen 80;
    server_name localhost;

    location / {
        proxy_pass http://backend;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
    }
}
EOF

# Copy Nginx configuration to load balancer container
docker cp nginx-production.conf "$LB_CONTAINER:$NGINX_CONF"
docker exec "$LB_CONTAINER" nginx -s reload

echo "Production traffic restored: 100% traffic to port $PRODUCTION_PORT"
```
