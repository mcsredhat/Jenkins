# Multi-Environment Image Management
## Overview
This project demonstrates Docker best practices for managing container images across multiple environments (development, staging, and production). It showcases techniques for image tagging, versioning, and environment-specific configuration for a Python Flask application.

## Project Structure
```
docker-lesson-project/
├── src/                  # Application source code directory
│   └── app.py            # Flask application
├── requirements.txt      # Python dependencies
├── Dockerfile            # Container configuration
└── README.md            # This documentation
```

## Features
- Multi-environment Docker image management
- Proper versioning and tagging strategies
- Environment variable configuration
- Docker security best practices
- Container image portability

## Prerequisites
- Docker Engine (version 19.03 or newer)
- Basic knowledge of Python and Flask
- Basic understanding of Docker concepts

## Getting Started
### Building the Base Image
```
docker build -t myapp:latest .
```

### Tagging for Different Environments
```
VERSION="1.0.0"
COMMIT_ID=$(date +%s)  # Simulating a git commit hash
```

# Tag for different environments
```
docker tag myapp:latest myapp:$VERSION
```

```
docker tag myapp:latest myapp:$VERSION-dev
```

```
docker tag myapp:latest myapp:$VERSION-staging
```

```
docker tag myapp:latest myapp:$VERSION-$COMMIT_ID
```

### Exporting Images for Transfer

```
docker save -o myapp-dev.tar myapp:$VERSION-dev
```

### Tagging for Docker Hub

```
docker tag myapp:$VERSION username/myapp:$VERSION
```

```
docker tag myapp:$VERSION-staging username/myapp:staging
```

### Running Environment-Specific Containers
# Development environment
```
docker run -d -p 5001:5000 -e ENVIRONMENT=development --name myapp-dev myapp:$VERSION-dev
```

# Staging environment
```
docker run -d -p 5002:5000 -e ENVIRONMENT=staging --name myapp-staging myapp:$VERSION-staging
```

# Production environment
```
docker run -d -p 5003:5000 -e ENVIRONMENT=production --name myapp-prod myapp:$VERSION
```

## Access Points
- Development: http://localhost:5001
- Staging: http://localhost:5002
- Production: http://localhost:5003

## Additional Commands
### Viewing Created Tags
```
docker images --format "{{.Repository}}:{{.Tag}}" | grep myapp
```



