# Nginx Docker Project

A secure, production-ready Nginx web server running in a Docker container with Alpine Linux base image, configured with non-root user, security hardening, and comprehensive monitoring.

## 🚀 Features

- **Security Hardened**: Non-root user execution, minimal capabilities, no new privileges
- **Performance Optimized**: Gzip compression, static file caching, worker process tuning
- **Production Ready**: Health checks, logging, resource limits, restart policies
- **Monitoring**: Built-in health endpoint, comprehensive logging
- **Alpine Based**: Lightweight container with minimal attack surface

## 📋 Prerequisites

- Docker Engine 20.10+
- Docker Compose 2.0+
- curl (for testing)

## 🏗️ Project Structure

```
nginx-docker-project/
├── docker-compose.yaml    # Container orchestration
├── Dockerfile            # Container build instructions
├── nginx.conf           # Main Nginx configuration
├── conf/
│   └── server.conf      # Server block configuration
└── README.md           # This file
```

## 🔧 Configuration

### Container Details
- **Base Image**: `nginx:alpine`
- **User**: `nginxusr` (UID: 1111, GID: 1111)
- **Port**: 8080
- **Working Directory**: `/nginx`

### Security Features
- Non-privileged user execution
- Dropped ALL capabilities, only essential ones added
- No new privileges security option
- Tmpfs mounts for temporary directories
- Security headers enabled

### Resource Limits
- **Memory**: 512M limit, 256M reservation
- **CPU**: 0.25 cores limit
- **File descriptors**: 1024 soft, 4096 hard limit
- **Processes**: 512 soft, 1024 hard limit

## 🚀 Quick Start

### Step 1: Create Project Structure

```
# Create project directory
mkdir nginx-docker-project && cd nginx-docker-project

# Create configuration directory
mkdir -p conf logs data 
```

### Step 2: Create Configuration Files

Create the configuration files with the content provided in this repository:
- `nginx.conf` - Main Nginx configuration
- `conf/server.conf` - Server block configuration
- `Dockerfile` - Container build instructions
- `docker-compose.yaml` - Container orchestration

### Step 3: Build and Deploy

```
# Build the container (no cache for clean build)
docker compose build --no-cache

# Start the service
docker compose up -d

# Verify container is running
docker ps -a
```

### Step 4: Verify Deployment

```
# Check health endpoint
curl http://localhost:8080/health

# Check main page
curl http://localhost:8080

# View logs
docker compose logs nginx-webserver

# Test Nginx configuration
docker compose exec nginx-webserver nginx -t
```

## 🔍 Monitoring and Debugging

### Health Check
The container includes a built-in health check accessible at:
```
http://localhost:8080/health
```

### View Logs
```
# View container logs
docker compose logs nginx-webserver

# Follow logs in real-time
docker compose logs -f nginx-webserver
```

### Container Inspection
```
# Check user inside container
docker compose exec nginx-webserver /bin/bash -c "id"

# View Nginx configuration
docker compose exec nginx-webserver cat /etc/nginx/nginx.conf

# Access container shell
docker compose exec nginx-webserver /bin/bash
```

### Test Configuration
```
# Test Nginx configuration syntax
docker compose exec nginx-webserver nginx -t

# Reload Nginx configuration
docker compose exec nginx-webserver nginx -s reload
```

## 📊 Container Resources

### Volumes
- **nginx_data**: Persistent data storage (`/nginx/data`)
- **nginx_logs**: Log file storage (`/nginx/logs`)

### Networks
- **nginx_frontend_project-network**: Bridge network (172.20.0.0/24)

### Ports
- **8080**: HTTP traffic (host:container)

## 🛠️ Common Operations

### Start/Stop Services
```
# Start services
docker compose up -d

# Stop services
docker compose down

# Restart services
docker compose restart
```

### Update Configuration
```
# After modifying configuration files
docker compose down
docker compose up -d
```

### Clean Rebuild
```
# Complete rebuild with no cache
docker compose down
docker compose build --no-cache
docker compose up -d
```

### View Resource Usage
```
# Container resource usage
docker stats nginx_webserver_project

# Volume usage
docker system df
```

## 🔐 Security Configuration

### Security Headers
- X-Frame-Options: SAMEORIGIN
- X-Content-Type-Options: nosniff  
- X-XSS-Protection: 1; mode=block
- Referrer-Policy: no-referrer-when-downgrade
- Content-Security-Policy: default-src 'self'

### Rate Limiting
- API endpoints: 10 requests/second
- Login endpoints: 1 request/second

### SSL/TLS Ready
Pre-configured for TLS 1.2 and 1.3 support when certificates are added.

## 📝 Environment Variables

The container supports the following environment variables:

| Variable | Default | Description |
|----------|---------|-------------|
| NGINX_VER | alpine | Nginx version |
| USER_ID | 1111 | Container user ID |
| USER_NAME | nginxusr | Container username |
| GROUP_ID | 1111 | Container group ID |
| GROUP_NAME | nginxusr | Container group name |
| APP_PORT | 8080 | Application port |
| TZ | GMT+2 | Timezone |

## 🐛 Troubleshooting

### Common Issues

**Container won't start:**
```
# Check logs for errors
docker compose logs nginx-webserver

# Verify configuration syntax
docker compose run --rm nginx-webserver nginx -t
```

**Health check failing:**
```
# Test health endpoint manually
curl -v http://localhost:8080/health

# Check if port is accessible
netstat -tulpn | grep 8080
```

**Permission issues:**
```
# Verify user permissions
docker compose exec nginx-webserver ls -la /nginx/
```

**Configuration errors:**
```
# Validate Nginx config
docker compose exec nginx-webserver nginx -t

# Check configuration files
docker compose exec nginx-webserver cat /etc/nginx/nginx.conf
```

## 📚 Additional Resources

- [Nginx Documentation](https://nginx.org/en/docs/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Alpine Linux Documentation](https://wiki.alpinelinux.org/)

## 👥 Maintenance

### Regular Tasks
- Monitor logs for errors
- Update base image regularly
- Review security headers
- Check resource usage
- Backup configuration files

### Updates
```
# Update base image
docker compose pull
docker compose up -d --force-recreate
```

## 📄 License

This project is provided as-is for educational and production use.

## 📧 Support

For issues and questions, contact: farajassulai@gmail.com

---

**Maintainer**: farajassulai@gmail.com  
**Version**: 1.0.0  
**Created**: June 16, 2025
