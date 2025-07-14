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
                script {
                    sh """
                        echo "Current branch: ${env.BRANCH_NAME}"
                        echo "Git commit: ${env.GIT_COMMIT}"
                        echo "Repository URL: ${GITHUB_REPO}"
                    """
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                echo "Building Docker image..."
                script {
                    dockerImage = docker.build("${IMAGE_NAME}:${BUILD_TAG}")
                    dockerImage.tag("latest")
                }
            }
        }

        stage('Push to Docker Hub (Debug Mode)') {
            steps {
                script {
                    echo "Logging into Docker Hub for debug..."
                    withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', usernameVariable: 'USER', passwordVariable: 'PASS')]) {
                        sh 'echo $PASS | docker login -u $USER --password-stdin'
                    }

                    echo "Listing Docker images before push..."
                    sh "docker images"

                    docker.withRegistry('https://registry.hub.docker.com', 'dockerhub-credentials') {
                        echo "Pushing image: ${IMAGE_NAME}:${BUILD_TAG}"
                        docker.image("${IMAGE_NAME}:${BUILD_TAG}").push()
                        echo "Pushing tag: latest"
                        docker.image("${IMAGE_NAME}:${BUILD_TAG}").push("latest")

                        if (env.BRANCH_NAME == 'main') {
                            docker.image("${IMAGE_NAME}:${BUILD_TAG}").push("production")
                            docker.image("${IMAGE_NAME}:${BUILD_TAG}").push("v1.0.0")
                        } else if (env.BRANCH_NAME == 'develop') {
                            docker.image("${IMAGE_NAME}:${BUILD_TAG}").push("staging")
                            docker.image("${IMAGE_NAME}:${BUILD_TAG}").push("develop-latest")
                        }
                    }
                }
            }
        }

        // Optional: Add your Test, Scan, and Deploy stages back here
    }

    post {
        always {
            echo "Cleaning up..."
            script {
                sh "docker stop ${TEST_CONTAINER_NAME} || true"
                sh "docker rm ${TEST_CONTAINER_NAME} || true"
                sh "docker image prune -f --filter 'until=24h'"
            }
        }

        success {
            echo "Pipeline completed successfully!"
        }

        failure {
            echo "Pipeline failed!"
        }
    }
}
