pipeline {
    agent any
    
    environment {
        DOCKER_REGISTRY = 'docker.io'
        DOCKER_USERNAME = 'farajassulai'
        DOCKER_REPO = 'flask-app'
        IMAGE_NAME = "${DOCKER_USERNAME}/${DOCKER_REPO}"
        BUILD_TAG = "${env.BUILD_NUMBER}-${env.GIT_COMMIT?.take(7) ?: 'unknown'}"
        CONTAINER_NAME = "flask-app-${env.BUILD_NUMBER}"
        TEST_CONTAINER_NAME = "test-container-${env.BUILD_NUMBER}"
        GITHUB_REPO = 'https://github.com/mcsredhat/Jenkins'
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo "Checking out code from GitHub repository..."
                echo "Repository: ${GITHUB_REPO}"
                checkout scm
                
                // Display repository information
                script {
                    sh """
                        echo "Current branch: ${env.BRANCH_NAME}"
                        echo "Git commit: ${env.GIT_COMMIT}"
                        echo "Repository URL: ${GITHUB_REPO}"
                    """
                }
            }
        }
        
        stage('Build') {
            steps {
                echo "Building Docker image..."
                script {
                    def image = docker.build("${IMAGE_NAME}:${BUILD_TAG}")
                    // Also tag as latest for convenience
                    image.tag("latest")
                }
            }
        }
        
        stage('Test') {
            steps {
                echo "Running tests..."
                script {
                    try {
                        // Start container for testing with unique name
                        sh """
                            docker run -d --name ${TEST_CONTAINER_NAME} \
                            -p 5000:5000 \
                            -e ENVIRONMENT=test \
                            ${IMAGE_NAME}:${BUILD_TAG}
                        """
                        
                        // Wait for app to start and retry health check
                        echo "Waiting for application to start..."
                        retry(5) {
                            sleep 10
                            sh "curl -f http://localhost:5000/health || curl -f http://localhost:5000/"
                        }
                        
                        echo "Health check passed!"
                        
                    } catch (Exception e) {
                        echo "Test failed: ${e.getMessage()}"
                        throw e
                    } finally {
                        // Always clean up test container
                        sh """
                            docker stop ${TEST_CONTAINER_NAME} || true
                            docker rm ${TEST_CONTAINER_NAME} || true
                        """
                    }
                }
            }
        }
        
        stage('Security Scan') {
            steps {
                echo "Running security scan..."
                script {
                    // Basic security scan using docker scan or trivy
                    sh """
                        echo "Running basic security checks..."
                        docker run --rm -v /var/run/docker.sock:/var/run/docker.sock \
                        aquasec/trivy:latest image --exit-code 0 --severity HIGH,CRITICAL \
                        ${IMAGE_NAME}:${BUILD_TAG} || echo "Security scan completed with warnings"
                    """
                }
            }
        }
        
        stage('Push to Docker Hub') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                }
            }
            steps {
                echo "Pushing image to Docker Hub..."
                echo "Docker Hub Repository: https://hub.docker.com/r/${DOCKER_USERNAME}/${DOCKER_REPO}"
                script {
                    // Login to Docker Hub and push
                    docker.withRegistry('https://registry.hub.docker.com', 'dockerhub-credentials') {
                        def image = docker.image("${IMAGE_NAME}:${BUILD_TAG}")
                        
                        // Push specific build tag
                        image.push()
                        
                        // Push latest tag
                        image.push("latest")
                        
                        // Tag and push environment-specific images
                        if (env.BRANCH_NAME == 'main') {
                            image.push("production")
                            image.push("v1.0.0")
                        } else if (env.BRANCH_NAME == 'develop') {
                            image.push("staging")
                            image.push("develop-latest")
                        }
                        
                        echo "✅ Images pushed to Docker Hub successfully!"
                        echo "Available at: https://hub.docker.com/r/${DOCKER_USERNAME}/${DOCKER_REPO}/tags"
                    }
                }
            }
        }
        
        stage('Deploy to Staging') {
            when {
                branch 'develop'
            }
            steps {
                echo "Deploying to staging environment..."
                script {
                    deployToEnvironment('staging', '8080')
                }
            }
        }
        
        stage('Deploy to Production') {
            when {
                branch 'main'
            }
            steps {
                echo "Deploying to production environment..."
                script {
                    // Add manual approval for production deployment
                    input message: 'Deploy to production?', ok: 'Deploy'
                    deployToEnvironment('production', '80')
                }
            }
        }
    }
    
    post {
        always {
            echo "Cleaning up..."
            script {
                // Clean up any remaining test containers
                sh """
                    docker stop ${TEST_CONTAINER_NAME} || true
                    docker rm ${TEST_CONTAINER_NAME} || true
                """
                
                // Clean up dangling images but keep recent ones
                sh """
                    docker image prune -f --filter "until=24h"
                """
            }
        }
        
        success {
            echo "Pipeline completed successfully!"
            script {
                // Send notification (example with email)
                emailext (
                    subject: "✅ Build Success: ${env.JOB_NAME} - ${env.BUILD_NUMBER}",
                    body: """
                        Build completed successfully!
                        
                        Job: ${env.JOB_NAME}
                        Build Number: ${env.BUILD_NUMBER}
                        Branch: ${env.BRANCH_NAME}
                        Image: ${IMAGE_NAME}:${BUILD_TAG}
                        
                        GitHub Repository: ${GITHUB_REPO}
                        Docker Hub: https://hub.docker.com/r/${DOCKER_USERNAME}/${DOCKER_REPO}
                        
                        Build URL: ${env.BUILD_URL}
                        
                        Available Docker Images:
                        - ${IMAGE_NAME}:${BUILD_TAG}
                        - ${IMAGE_NAME}:latest
                        ${env.BRANCH_NAME == 'main' ? "- ${IMAGE_NAME}:production" : ""}
                        ${env.BRANCH_NAME == 'develop' ? "- ${IMAGE_NAME}:staging" : ""}
                    """,
                    recipientProviders: [developers()]
                )
            }
        }
        
        failure {
            echo "Pipeline failed!"
            script {
                // Send failure notification
                emailext (
                    subject: "❌ Build Failed: ${env.JOB_NAME} - ${env.BUILD_NUMBER}",
                    body: """
                        Build failed!
                        
                        Job: ${env.JOB_NAME}
                        Build Number: ${env.BUILD_NUMBER}
                        Branch: ${env.BRANCH_NAME}
                        
                        GitHub Repository: ${GITHUB_REPO}
                        Docker Hub: https://hub.docker.com/r/${DOCKER_USERNAME}/${DOCKER_REPO}
                        
                        Build URL: ${env.BUILD_URL}
                        Console Output: ${env.BUILD_URL}console
                        
                        Please check the logs and fix the issues.
                    """,
                    recipientProviders: [developers(), requestor()]
                )
            }
        }
        
        unstable {
            echo "Pipeline completed with warnings!"
        }
    }
}

// Helper function for deployment
def deployToEnvironment(environment, port) {
    def containerName = "flask-app-${environment}"
    
    try {
        // Stop and remove existing container
        sh """
            docker stop ${containerName} || true
            docker rm ${containerName} || true
        """
        
        // Deploy new container with proper configuration
        sh """
            docker run -d \
            --name ${containerName} \
            -p ${port}:5000 \
            -e ENVIRONMENT=${environment} \
            --restart=unless-stopped \
            --memory=512m \
            --cpus=0.5 \
            ${IMAGE_NAME}:${BUILD_TAG}
        """
        
        // Wait for deployment to stabilize
        echo "Waiting for ${environment} deployment to stabilize..."
        sleep 15
        
        // Verify deployment
        retry(3) {
            sh """
                curl -f http://localhost:${port}/health || \
                curl -f http://localhost:${port}/
            """
        }
        
        echo "✅ Successfully deployed to ${environment} environment"
        
    } catch (Exception e) {
        echo "❌ Deployment to ${environment} failed: ${e.getMessage()}"
        
        // Rollback logic could go here
        echo "Consider implementing rollback logic"
        
        throw e
    }
}
