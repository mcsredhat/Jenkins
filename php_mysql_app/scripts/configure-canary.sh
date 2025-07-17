```bash
#!/bin/bash
# Script to configure a canary deployment by splitting traffic between production and canary services
# Usage: ./configure-canary.sh <environment> <production_port> <canary_port> <canary_percentage>
# Example: ./configure-canary.sh production 8989 8990 10

set -e

# Validate input arguments
if [ $# -ne 4 ]; then
    echo "Error: Usage: $0 <environment> <production_port> <canary_port> <canary_percentage>"
    exit 1
fi

ENVIRONMENT="$1"
PRODUCTION_PORT="$2"
CANARY_PORT="$3"
CANARY_PERCENTAGE="$4"

# Validate percentage is a number between 0 and 100
if ! [[ "$CANARY_PERCENTAGE" =~ ^[0-9]+$ ]] || [ "$CANARY_PERCENTAGE" -lt 0 ] || [ "$CANARY_PERCENTAGE" -gt 100 ]; then
    echo "Error: Canary percentage must be a number between 0 and 100"
    exit 1
fi

# Define variables
COMPOSE_FILE="docker-compose.yml"
APP_DIR="."
NETWORK="web-app-net"
LB_CONTAINER="nginx-lb"
NGINX_CONF="/etc/nginx/conf.d/default.conf"

# Ensure Docker Compose file exists
if [ ! -f "$APP_DIR/$COMPOSE_FILE" ]; then
    echo "Error: $COMPOSE_FILE not found in $APP_DIR"
    exit 1
fi

# Create canary-specific Docker Compose file
CANARY_COMPOSE_FILE="$APP_DIR/docker-compose-canary.yml"
cp "$APP_DIR/$COMPOSE_FILE" "$CANARY_COMPOSE_FILE"

# Modify container names and ports for canary services
sed -i "s/container_name: my_php_apache/container_name: my_php_apache_canary/" "$CANARY_COMPOSE_FILE"
sed -i "s/container_name: mysql-project/container_name: mysql-project-canary/" "$CANARY_COMPOSE_FILE"
sed -i "s/container_name: adminer/container_name: adminer-canary/" "$CANARY_COMPOSE_FILE"
sed -i "s/8989:8989/$CANARY_PORT:8989/" "$CANARY_COMPOSE_FILE"
sed -i "s/8181:8080/8182:8080/" "$CANARY_COMPOSE_FILE"
sed -i "s/3306:3306/3307:3306/" "$CANARY_COMPOSE_FILE"

# Start canary services
echo "Starting canary services for environment: $ENVIRONMENT"
cd "$APP_DIR"
docker-compose -f "$CANARY_COMPOSE_FILE" up -d

# Wait for canary services to be healthy
echo "Waiting for canary services to be healthy..."
for service in my_php_apache_canary mysql-project-canary adminer-canary; do
    timeout 300s bash -c "until docker inspect --format='{{.State.Health.Status}}' $service | grep -q 'healthy'; do sleep 5; echo 'Waiting for $service...'; done"
    if [ $? -ne 0 ]; then
        echo "Error: Canary service $service failed to become healthy"
        docker-compose -f "$CANARY_COMPOSE_FILE" down
        exit 1
    fi
done

# Generate Nginx configuration for traffic splitting
echo "Configuring Nginx load balancer for $CANARY_PERCENTAGE% canary traffic..."
cat << EOF > nginx-canary.conf
upstream backend {
    server my_php_apache:$PRODUCTION_PORT weight=$((100 - CANARY_PERCENTAGE));
    server my_php_apache_canary:$CANARY_PORT weight=$CANARY_PERCENTAGE;
}

server {
    listen 80;
    server_name localhost;

    location / {
        proxy_pass http://backend;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote absolutely;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
    }
}
EOF

# Copy Nginx configuration to load balancer container
docker cp nginx-canary.conf "$LB_CONTAINER:$NGINX_CONF"
docker exec "$LB_CONTAINER" nginx -s reload

echo "Canary deployment configured: $CANARY_PERCENTAGE% traffic to port $CANARY_PORT"
```
