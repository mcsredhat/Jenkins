# Vue.js Docker Application Deployment Guide

A comprehensive guide for deploying Vue.js applications using Docker with multi-stage builds and custom entrypoint scripts for production environments.

## 🚀 Project Structure

```
your-vue-project/
├── Dockerfile
├── nginx.conf
├── vue-entrypoint-nonroot.sh
├── package.json
├── package-lock.json (or yarn.lock/pnpm-lock.yaml)
├── .dockerignore 
├── src/
│   ├── main.js (or main.ts)
│   ├── App.vue
│   ├── components/
│   │   └── FeatureCard.vue
│   └── utils/
│       └── index.js
├── public/
│   ├── index.html
│   ├── vite.config.js
│   ├── vue.config.js
│   └── favicon.ico
├── vite.config.js (or vue.config.js)
└── dist/ (created during build)
```

## 📋 Prerequisites

Before you begin, ensure you have the following installed:

- **Docker** (latest version)
- **Node.js** (v16+ recommended)
- **npm/yarn/pnpm** (package manager)

## 🛠️ Required Files

### Essential Docker Files

1. **Dockerfile** - Multi-stage build configuration
2. **nginx.conf** - Nginx configuration for serving static files
3. **vue-entrypoint-nonroot.sh** - Custom entrypoint script
4. **.dockerignore** - Exclude unnecessary files from Docker context

### Vue.js Configuration

- **vite.config.js** or **vue.config.js** - Build configuration
- **package.json** - Dependencies and scripts

## 🏗️ Deployment Process

### Phase 1: Initial Build and Deployment

#### Step 1: Build the Production Image

```bash
# Build the multi-stage Docker image targeting the production stage
docker build --target production -t vueapp:prod .
```

**Purpose**: Creates an optimized production build of your Vue.js application using multi-stage Docker build.

#### Step 2: Run the Container

```bash
# Deploy the container with custom entrypoint script
docker run -d --name vue-app-pro -p 8080:80 --entrypoint /usr/local/bin/vue-entrypoint-nonroot.sh vueapp:prod
```

**Purpose**: Starts the container in detached mode, maps port 8080 to internal port 80, and uses a custom non-root entrypoint script.

#### Step 3: Verify Container Status

```bash
# Check if the container is running successfully
docker ps -a | grep vue-app-pro
```

**Purpose**: Confirms the container status and helps identify any startup issues.

#### Step 4: Check Application Logs

```bash
# View container logs for debugging
docker logs vue-app-pro
```

**Purpose**: Reviews startup logs and error messages to troubleshoot deployment issues.

#### Step 5: Test Application Access

```bash
# Test if the application is responding
curl http://localhost:8080
```

**Purpose**: Verifies that the Vue.js application is accessible and responding to requests.

### Phase 2: Troubleshooting and Debugging

#### When Container Fails to Start Properly

##### Step 1: Verify Container Status

```bash
# Check detailed container status
docker ps -a | grep vue-app-pro
```

**Purpose**: Determine if the container exited immediately or is running but not responding.

##### Step 2: Inspect Entrypoint Script

```bash
# Verify the entrypoint script exists and has correct permissions
docker exec vue-app-pro ls -la /usr/local/bin/vue-entrypoint-nonroot.sh
```

**Purpose**: Ensures the custom entrypoint script is present and executable.

### Phase 3: Advanced Troubleshooting (When Entrypoint Issues Occur)

#### Step 1: Clean Slate Approach

```bash
# Stop and remove the problematic container
docker stop vue-app-pro
docker rm vue-app-pro
```

**Purpose**: Removes the failed container to start fresh with debugging approach.

#### Step 2: Start Container with Temporary Entrypoint

```bash
# Start container with tail command to keep it running for debugging
docker run -d --name vue-app-pro --entrypoint tail vueapp:prod -f /dev/null
```

**Purpose**: Keeps container alive without executing the problematic entrypoint script, allowing for debugging.

#### Step 3: Fix Entrypoint Script Issues

```bash
# Copy corrected entrypoint script into the running container
docker cp vue-entrypoint-fixed.sh vue-app-pro:/usr/local/bin/vue-entrypoint-nonroot.sh
```

**Purpose**: Replaces the problematic entrypoint script with a corrected version (likely fixing line ending issues).

#### Step 4: Verify and Test Script

```bash
# Confirm the file exists and test execution
docker exec vue-app-pro ls -la /usr/local/bin/vue-entrypoint-nonroot.sh
docker exec vue-app-pro /usr/local/bin/vue-entrypoint-nonroot.sh
```

**Purpose**: Validates that the corrected script is properly installed and executable.

#### Step 5: Create Fixed Image

```bash
# Commit the container with fixes to a new image
docker commit vue-app-pro vueapp:prod-fixed
```

**Purpose**: Saves the container state with fixes as a new Docker image for future use.

### Phase 4: Final Deployment with Fixed Image

#### Step 1: Deploy with Fixed Image

```bash
# Stop and remove the debugging container
docker stop vue-app-pro
docker rm vue-app-pro

# Run the container with the fixed image and proper entrypoint
docker run -d --name vue-app-pro -p 8080:80 --entrypoint /usr/local/bin/vue-entrypoint-nonroot.sh vueapp:prod-fixed
```

**Purpose**: Deploys the application using the corrected image with working entrypoint script.

#### Step 2: Comprehensive Verification

```bash
# Check container status
docker ps

# Review startup logs
docker logs vue-app-pro

# Verify running processes
docker exec vue-app-pro ps aux

# Test application connectivity
curl http://localhost:8080
```

**Purpose**: Comprehensive health check to ensure the application is running correctly.

#### Step 3: Update Original Image (Optional)

```bash
# Tag the fixed image as the main production image
docker tag vueapp:prod-fixed vueapp:prod
```

**Purpose**: Makes the fixed version the default production image for future deployments.

## 🔧 Common Issues and Solutions

### Issue: Container exits immediately
**Solution**: Check logs with `docker logs vue-app-pro` and verify entrypoint script permissions

### Issue: Entrypoint script has wrong line endings (Windows CRLF vs Unix LF)
**Solution**: Use the troubleshooting phase to copy a corrected script with proper Unix line endings

### Issue: Port not accessible
**Solution**: Verify port mapping (-p 8080:80) and check if the application is binding to the correct internal port

### Issue: Permission denied on entrypoint script
**Solution**: Ensure the script has execute permissions (`chmod +x`) in the Docker image

## 🌐 Accessing the Application

Once deployed successfully, your Vue.js application will be available at:
- **Local development**: `http://localhost:8080`

## 📝 Docker Management Commands

### Container Management
```bash
# View all containers
docker ps -a

# Stop container
docker stop vue-app-pro

# Remove container
docker rm vue-app-pro

# View logs
docker logs vue-app-pro

# Execute commands in container
docker exec -it vue-app-pro /bin/sh
```

### Image Management
```bash
# List images
docker images

# Remove image
docker rmi vueapp:prod

# Build without cache
docker build --no-cache --target production -t vueapp:prod .
```

## 🔍 Debugging Tips

1. **Check container logs first**: `docker logs vue-app-pro`
2. **Verify file permissions**: Ensure entrypoint script has execute permissions
3. **Test script directly**: Use `docker exec` to test the entrypoint script manually
4. **Use temporary entrypoint**: Start with `tail -f /dev/null` to keep container running for debugging
5. **Check port bindings**: Verify both internal and external port configurations

## 📚 Additional Resources

- [Docker Documentation](https://docs.docker.com/)
- [Vue.js Documentation](https://vuejs.org/)
- [Nginx Documentation](https://nginx.org/en/docs/)
- [Docker Multi-stage Builds](https://docs.docker.com/develop/dev-best-practices/multistage-build/)

