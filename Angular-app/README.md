# Angular Portfolio Project

A modern, responsive portfolio website built with Angular showcasing skills, projects, and professional experience.

## 🚀 Project Structure

```
project portoflio/
├── src/
│   ├── app/
│   │   ├── components/
│   │   │   ├── header/
│   │   │   ├── about/
│   │   │   ├── projects/
│   │   │   ├── skills/
│   │   │   └── contact/
│   │   └── assets/
│   │       ├── images/
│   │       ├── icons/
│   │       └── resume/
├── package.json
├── angular.json
├── tsconfig.json
├── package-lock.json
├── nginx.config
└── Dockerfile
```

## 📋 Prerequisites

Before you begin, ensure you have the following installed:

- **Node.js** (v20.x or later)
- **npm** (v11.x or later)
- **Angular CLI** (latest version)
- **Docker** (for containerized deployment)

### Version Verification

```bash
node --version            # Should show v20.x
npm --version             # Should show v11.x
ng version                # Angular CLI and package details
```

## 🛠️ Installation & Setup

### 1. Clone the Repository

```bash
git clone <repository-url>
cd project-portoflio
```

### 2. Clean Dependencies (if needed)

```bash
# Remove old dependencies and cache
rm -rf node_modules package-lock.json
npm cache clean --force
sudo rm -rf dist
```

### 3. Install Dependencies

```bash
npm install
npm audit fix --force
```

### 4. Development Setup

```bash
# Check for linting issues
npm run lint

# Run tests
npm test

# Build the project
npm run build
```

## 🏃‍♂️ Development Server

```bash
ng serve
```

Navigate to `http://localhost:4200/` to view the application. The app will automatically reload when you make changes to the source files.

## 🔧 Build & Test

### Development Build
```bash
ng build
```

### Production Build
```bash
ng build --configuration production
```

### Running Tests
```bash
ng test              # Unit tests
ng lint              # Linting checks
```

## 🐳 Docker Deployment

### Prerequisites for Docker (Fedora/RHEL)

```bash
# Install required packages
sudo dnf install -y https://dl.fedoraproject.org/pub/epel/epel-release-latest-8.noarch.rpm
sudo dnf install -y https://download1.rpmfusion.org/free/el/rpmfusion-free-release-8.noarch.rpm
sudo dnf install -y chromium
sudo find / -name chromium 2>/dev/null
sudo dnf install flatpak
sudo flatpak remote-add --if-not-exists flathub https://flathub.org/repo/flathub.flatpakrepo
sudo flatpak install flathub org.chromium.Chromium
export CHROME_BIN=/usr/bin/chromium
```

### Build and Run with Docker

1. **Build the Docker image:**
```bash
docker build -t angular-portfolio .
```

2. **Run the container:**
```bash
docker run -d --name angular.app -p 8080:80 angular-portfolio
```

3. **Test the deployment:**
```bash
docker ps                       # Check container status
docker logs angular.app         # View logs
curl http://localhost:8080      # Test the application
curl http://localhost:8080/health  # Health check (if implemented)
```

### Docker Management Commands

```bash
# Stop the container
docker stop angular.app

# Remove the container
docker rm angular.app

# Restart after changes
docker run -d --name angular.app -p 8080:80 angular-portfolio

# Debug mode (interactive shell)
docker run -it --name angular.app -p 8080:80 angular-portfolio /bin/sh

# Manual nginx start
docker run -d --name angular.app -p 8080:80 angular-portfolio nginx -g "daemon off;"
```

## 🌐 Accessing the Application

- **Development:** `http://localhost:4200`
- **Docker deployment:** `http://localhost:8080`

## 📁 Components Overview

- **Header:** Navigation and branding
- **About:** Personal introduction and background
- **Projects:** Showcase of completed projects
- **Skills:** Technical skills and competencies
- **Contact:** Contact information and form

## 🔍 Troubleshooting

### Common Issues

1. **Node/npm version conflicts:**
   - Ensure you're using compatible versions
   - Use `nvm` to manage Node.js versions if needed

2. **Build failures:**
   - Clear cache: `npm cache clean --force`
   - Delete `node_modules` and reinstall: `rm -rf node_modules && npm install`

3. **Docker issues:**
   - Ensure the `/dist/portfolio-project/` directory is created before building Docker image
   - Check Docker logs: `docker logs angular.app`

4. **Port conflicts:**
   - Change the port mapping: `docker run -d --name angular.app -p 8081:80 angular-portfolio`

## 📝 Scripts

Available npm scripts:

```bash
npm start           # Start development server
npm run build       # Build for production
npm run test        # Run unit tests
npm run lint        # Run linting
npm run e2e         # Run end-to-end tests
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

