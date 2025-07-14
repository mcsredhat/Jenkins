pipeline {
    agent any
    
    environment {
        DOCKER_REGISTRY = 'your-registry.com'
        DOCKER_REPO = 'flask-app'
        IMAGE_NAME = "${DOCKER_REGISTRY}/${DOCKER_REPO}"
        BUILD_TAG = "${env.BUILD_NUMBER}-${env.GIT_COMMIT}"
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo "Checking out code..."
                checkout scm
            }
        }
        
        stage('Build') {
            steps {
                echo "Building Docker image..."
                script {
                    docker.build("${IMAGE_NAME}:${BUILD_TAG}")
                }
            }
        }
        
        stage('Test') {
            steps {
                echo "Running tests..."
                script {
                    // Start container for testing
                    sh "docker run -d --name test-container -p 5000:5000 ${IMAGE_NAME}:${BUILD_TAG}"
                    
                    // Wait for app to start
                    sleep 10
                    
                    // Run basic health check
                    sh "curl -f http://localhost:5000/health"
                    
                    // Clean up test container
                    sh "docker stop test-container && docker rm test-container"
                }
            }
        }
        
        stage('Deploy') {
            when {
                branch 'main'
            }
            steps {
                echo "Deploying to production..."
                script {
                    // Stop existing container
                    sh "docker stop flask-app || true"
                    sh "docker rm flask-app || true"
                    
                    // Deploy new container
                    sh "docker run -d --name flask-app -p 80:5000 ${IMAGE_NAME}:${BUILD_TAG}"
                    
                    // Verify deployment
                    sleep 10
                    sh "curl -f http://localhost/health"
                }
            }
        }
    }
    
    post {
        always {
            echo "Cleaning up..."
            sh "docker system prune -f"
        }
        
        success {
            echo "Pipeline completed successfully!"
        }
        
        failure {
            echo "Pipeline failed!"
        }
    }
}