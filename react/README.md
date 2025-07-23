# React Docker Application Deployment Guide

A comprehensive guide for deploying React applications using Docker with multi-stage builds and Nginx serving for production environments.

## 🚀 Project Structure

```
react-project/
├── Dockerfile
├── nginx.conf
├── package.json
├── package-lock.json
├── .dockerignore
├── src/
│   ├── index.js
│   ├── App.js
│   ├── components/
│   └── assets/
├── public/
│   ├── index.html
│   └── favicon.ico
├── build/ (created during build)
└── node_modules/ (installed dependencies)
```

## 📋 Prerequisites

Before you begin, ensure you have the following installed:

- **Docker** (latest version)
- **Node.js** (v20.x - installed via NodeSource)
- **npm** (comes with Node.js)

## 🛠️ Environment Setup

### Step 1: Install Node.js 20.x

```bash
# Install Node.js 20.x from NodeSource repository
curl -fsSL https://rpm.nodesource.com/setup_20.x | sudo bash -
sudo dnf install nodejs
```

### Step 2: Verify Installation

```bash
# Verify Node.js and npm versions
node --version    # Should show v20.x.x
npm --version     # Should show compatible npm version
```

**Expected Output:**
- Node.js: v20.x.x
- npm: 10.x.x or higher

## 🏗️ Project Setup and Build

### Step 1: Clean Previous Dependencies

```bash
# Remove old dependencies and lock file
rm -rf node_modules package-lock.json
```

**Purpose**: Ensures clean installation and resolves potential version conflicts.

### Step 2: Install Dependencies

```bash
# Reinstall dependencies with compatible versions
npm install
```

**Purpose**: Installs all required packages as specified in package.json.

### Step 3: Build the Application

```bash
# Build the React application for production
npm run build
```

**Purpose**: Creates optimized production build in the `/build` directory.

### Step 4: Security Audit and Fixes

```bash
# Check for security vulnerabilities
npm audit

# Automatically fix vulnerabilities
npm audit fix

# Force fixes for remaining issues (use with caution)
npm audit fix --force
```

**Purpose**: Identifies and resolves security vulnerabilities in dependencies.

## 🐳 Docker Deployment

### Step 1: Build Docker Image

```bash
# Build the multi-stage Docker image targeting production
docker build --target production -t reactapp:prod .
```

**Purpose**: Creates an optimized production Docker image with multi-stage build process.

### Step 2: Run the Container

```bash
# Deploy the container with custom nginx configuration
docker run -p 3000:80 --name react-project --entrypoint="" reactapp:prod nginx -g "daemon off;" -c /etc/nginx/nginx.conf
```

**Command Breakdown:**
- `-p 3000:80`: Maps host port 3000 to container port 80
- `--name react-project`: Assigns container name for easy management
- `--entrypoint=""`: Overrides default entrypoint
- `nginx -g "daemon off;"`: Runs nginx in foreground mode
- `-c /etc/nginx/nginx.conf`: Specifies nginx configuration file

### Step 3: Verify Deployment

```bash
# Test application accessibility via curl
curl http://localhost:3000

# Alternative: Open in browser
# Navigate to http://localhost:3000
# Or http://your-server-ip:3000
```

**Purpose**: Confirms the React application is successfully deployed and accessible.

## 🔧 Container Management

### Basic Container Operations

```bash
# Check running containers
docker ps

# View all containers (including stopped)
docker ps -a

# Stop the container
docker stop react-project

# Remove the container
docker rm react-project

# View container logs
docker logs react-project

# Execute commands in running container
docker exec -it react-project /bin/sh
```

### Image Management

```bash
# List Docker images
docker images

# Remove the image
docker rmi reactapp:prod

# Build without cache
docker build --no-cache --target production -t reactapp:prod .
```

## 🔍 Troubleshooting

### Common Issues and Solutions

#### Issue: Container fails to start
**Solution:**
```bash
# Check container logs
docker logs react-project

# Verify nginx configuration
docker exec react-project nginx -t
```

#### Issue: Port already in use
**Solution:**
```bash
# Use different port
docker run -p 3001:80 --name react-project --entrypoint="" reactapp:prod nginx -g "daemon off;" -c /etc/nginx/nginx.conf

# Or stop conflicting service
sudo lsof -i :3000
sudo kill -9 <PID>
```

#### Issue: Build fails due to memory issues
**Solution:**
```bash
# Increase Node.js memory limit
export NODE_OPTIONS="--max-old-space-size=4096"
npm run build
```

#### Issue: npm audit shows vulnerabilities
**Solution:**
```bash
# Review vulnerabilities
npm audit

# Fix automatically
npm audit fix

# Manual review for high-severity issues
npm audit fix --force
```

### Development vs Production

#### Development Mode
```bash
# Start development server
npm start

# Access at http://localhost:3000
```

#### Production Mode (Docker)
```bash
# Build and run production container
docker build --target production -t reactapp:prod .
docker run -p 3000:80 --name react-project --entrypoint="" reactapp:prod nginx -g "daemon off;" -c /etc/nginx/nginx.conf
```

## 📝 Essential Files

### Dockerfile Requirements

Your Dockerfile should include:
- Multi-stage build (development and production stages)
- Node.js base image for building
- Nginx base image for serving
- Proper file copying and permissions

### nginx.conf Configuration

Essential nginx configuration for React SPA:
- Serve static files from `/build` directory
- Handle client-side routing with `try_files`
- Proper MIME types for assets
- Gzip compression for performance

### .dockerignore

Exclude unnecessary files:
```
node_modules
npm-debug.log
.git
.gitignore
README.md
.env
.nyc_output
coverage
.coverage
```

## 🌐 Accessing the Application

### Local Development
- **Development server**: `http://localhost:3000`
- **Production container**: `http://localhost:3000`

### Remote Server
- **Production container**: `http://your-server-ip:3000`

## 📊 Performance Optimization

### Build Optimization
```bash
# Analyze bundle size
npm run build
npx webpack-bundle-analyzer build/static/js/*.js
```

### Container Optimization
```bash
# Multi-stage build reduces image size
docker images | grep reactapp

# Use .dockerignore to exclude unnecessary files
# Minimize layers in Dockerfile
```

## 🔐 Security Best Practices

1. **Regular Updates**: Keep Node.js and dependencies updated
2. **Vulnerability Scanning**: Run `npm audit` regularly
3. **Minimal Base Images**: Use specific version tags
4. **Non-root User**: Run containers with non-root user when possible
5. **Environment Variables**: Use environment variables for configuration

## 📚 Additional Resources

- [React Documentation](https://reactjs.org/)
- [Docker Documentation](https://docs.docker.com/)
- [Nginx Documentation](https://nginx.org/en/docs/)
- [Node.js Documentation](https://nodejs.org/en/docs/)

