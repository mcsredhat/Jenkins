# Container Management Project

## Overview

This project demonstrates comprehensive container management techniques using Docker, including deployment, monitoring, troubleshooting, and automation strategies for containerized applications. It provides a practical guide for developers and operations teams working with containerized environments.

# Features

- Web application with built-in health checks
- Container monitoring and diagnostic tools
- Crash recovery mechanisms
- Resource usage tracking
- Automated container management utilities

# Project Structure & Documentation 
```
container-demo/
├── app/
│   └── server.js        # Node.js web server with health endpoints
├── Dockerfile           # Container configuration
└── README.md            # This documentation
```

## Prerequisites
- Docker Engine (version 19.03 or later)
- Node.js (for local development)
- curl (for testing endpoints)

# Getting Started

### Building the Container
```
docker build -t container-monitor-demo:latest .
```

### Running the Container
```
docker run -d -p 3000:3000 --name web-monitor container-monitor-demo:latest
```

### Testing the Application
- Main page: `http://localhost:3000`
- Health check: `http://localhost:3000/health`
- Crash trigger (for testing): `http://localhost:3000/crash`

## Monitoring Commands
### Basic Container Status
```
docker ps --format "table {{.ID}}\t{{.Names}}\t{{.Status}}\t{{.Ports}}"
```

### Log Monitoring
# Stream logs in real-time
```
docker logs -f web-monitor
```

# View recent logs
```
docker logs --tail 5 web-monitor
```

### Container Health
# Check container health status
```
docker inspect --format "{{.State.Health.Status}}" web-monitor
```
# List container processes
```
docker top web-monitor
```

### Resource Monitoring
# Monitor resource usage
```
docker stats
```
# Format output for specific metrics
```
docker stats --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.NetIO}}"
```

## Troubleshooting
### Diagnosing Container Issues
# Check container exit code
```
docker inspect --format "{{.State.ExitCode}}" container-name
```

# Extract network information
```
docker inspect --format "{{range \$k, \$v := .NetworkSettings.Ports}}{{printf \"%s -> %s\" \$k (\$v | printf \"%s\" . | printf \"%s\" .)}}{{end}}" container-name
```

### Container Auditing
# Check container creation time
```
docker inspect --format "{{.Created}}" container-name
```

# List container mounts
```
docker inspect --format "{{range .Mounts}}{{.Source}} -> {{.Destination}} ({{.Mode}}){{println}}{{end}}" container-name
```
# Export container filesystem for analysis
```
docker export container-name > container-filesystem.tar
```
## Automated Management
The project includes utility scripts for automatic container management:
- Restarting unhealthy containers based on health checks
- Logging and cleanup of failed containers
- Resource monitoring and alerting

## Cleanup
# Remove the container
```
docker rm web-monitor
```
# Remove the image
```
docker rmi container-monitor-demo:latest
```
