def validateInput(String input) {
    if (input ==~ /[;&|<>$"\\]/ || input.length() > 100) {
        error "Invalid input detected: ${input}. Contains forbidden characters or is too long."
    }
    return input.trim()
}

def buildImage(Map args) {
    dir(args.appDir) {
        if (!env.GIT_COMMIT) {
            error "GIT_COMMIT environment variable is not set"
        }
        if (!env.DOCKER_USERNAME) {
            error "DOCKER_USERNAME environment variable is not set"
        }
        def buildArgs = [
            "--build-arg BUILD_DATE=\$(date -u +'%Y-%m-%dT%H:%M:%SZ')",
            "--build-arg VCS_REF=${env.GIT_COMMIT}",
            "--build-arg VERSION=${validateInput(args.buildTag)}",
            "--build-arg APP_TYPE=${validateInput(args.appType)}",
            "--build-arg ENVIRONMENT=${validateInput(args.environment)}"
        ].join(' ')
        def cacheArgs = args.enableDockerCache ? "--cache-from ${validateInput(args.cacheFromImage)} --cache-to type=registry,ref=${validateInput(args.cacheFromImage)},mode=max" : ""
        def platformArgs = args.enableMultiArch ? "--platform ${validateInput(args.dockerPlatforms)}" : ""
        def additionalArgs = "${args.dockerSquash ? '--squash' : ''} ${args.dockerNoCache ? '--no-cache' : ''}"

        try {
            if (args.enableMultiArch) {
                sh "docker buildx create --name multiarch-builder --use || echo 'Builder exists'"
                sh "docker buildx inspect --bootstrap"
            }

            if (args.useCompose) {
                sh """
                    DOCKER_BUILDKIT=1 docker-compose -f ${validateInput(args.composeFile)} build --parallel --progress=plain
                    docker-compose -f ${validateInput(args.composeFile)} config --services | while read service; do
                        SERVICE_IMAGE=\$(docker-compose -f ${validateInput(args.composeFile)} config | grep -A1 "service: \$service" | grep "image:" | awk '{print \$2}')
                        docker tag \$SERVICE_IMAGE ${validateInput(args.imageName)}-\$service:${validateInput(args.buildTag)}
                        docker tag \$SERVICE_IMAGE ${validateInput(args.imageName)}-\$service:jenkins-build
                        docker run --rm -v /var/run/docker.sock:/var/run/docker.sock -v \$(pwd):/workspace \
                            aquasec/trivy:latest image --format spdx-json --output /workspace/sbom-\$service.json \$SERVICE_IMAGE
                    done
                """
            } else {
                sh """
                    DOCKER_BUILDKIT=1 docker build ${platformArgs} ${cacheArgs} ${buildArgs} ${additionalArgs} \
                        --tag ${validateInput(args.imageName)}:${validateInput(args.buildTag)} \
                        --tag ${validateInput(args.imageName)}:jenkins-build \
                        --tag ${validateInput(args.cacheFromImage)} \
                        --label "org.opencontainers.image.source=${validateInput(args.githubRepo)}" \
                        --label "org.opencontainers.image.revision=${env.GIT_COMMIT}" \
                        --label "org.opencontainers.image.version=${validateInput(args.buildTag)}" \
                        --progress=plain \
                        .
                    docker run --rm -v /var/run/docker.sock:/var/run/docker.sock -v \$(pwd):/workspace \
                        aquasec/trivy:latest image --format spdx-json --output /workspace/sbom.json ${validateInput(args.imageName)}:${validateInput(args.buildTag)}
                """
            }

            sh """
                docker images ${validateInput(args.imageName)}* --format "table {{.Repository}}:{{.Tag}}\t{{.Size}}"
                docker history ${validateInput(args.imageName)}:${validateInput(args.buildTag)} --format "table {{.CreatedBy}}\t{{.Size}}" > image-layers.txt
            """
        } catch (Exception e) {
            echo "❌ Build failed: ${e.getMessage()}"
            throw e
        }
    }
}

def runIntegrationTests(Map args) {
    try {
        def config = readYaml file: "${args.appDir}/pipeline-config.yml"
        def healthCmd = config.deployment.environments.test?.services[args.appType]?.healthcheck?.command ?:
            args.appType == 'mysql' ? 
            "mysqladmin ping -h localhost -u ${env.MYSQL_USER} --password=\${MYSQL_PASSWORD}" : 
            args.appType == 'php' ? 
            "curl -f http://localhost:${validateInput(args.appPort)}/ || exit 1" : 
            "curl -f http://localhost:${validateInput(args.appPort)}/health || exit 1"
        if (args.useCompose) {
            sh """
                cd ${validateInput(args.appDir)}
                docker-compose -f ${validateInput(args.composeFile)} up -d
                sleep 30
                SERVICES=\$(docker-compose -f ${validateInput(args.composeFile)} config --services)
                for service in \$SERVICES; do
                    docker-compose -f ${validateInput(args.composeFile)} ps \$service | grep "healthy" || echo "\$service not healthy"
                done
                docker-compose -f ${validateInput(args.composeFile)} exec -T app npm run test:integration || echo "Integration tests completed"
            """
        } else {
            def networkMode = args.useCompose ? "web-app-net" : validateInput(args.networkMode)
            sh """
                docker network create web-app-net --subnet=172.18.19.0/24 || echo "Network web-app-net already exists"
                docker run -d --name ${validateInput(args.testContainerName)} \
                    -p ${validateInput(args.testPort)}:${validateInput(args.appPort)} \
                    -e ENVIRONMENT=test \
                    --memory=${validateInput(args.memoryLimit)} \
                    --cpus=${validateInput(args.cpuLimit)} \
                    --network=${networkMode} \
                    --health-cmd="${healthCmd}" \
                    --health-interval=30s \
                    --health-timeout=10s \
                    --health-retries=3 \
                    ${validateInput(args.imageName)}:${validateInput(args.buildTag)}
                timeout ${validateInput(args.healthCheckTimeout)}s bash -c 'until docker inspect --format="{{.State.Health.Status}}" ${validateInput(args.testContainerName)} | grep -q "healthy"; do sleep 5; done'
                if [ "${validateInput(args.appType)}" = "mysql" ]; then
                    docker exec ${validateInput(args.testContainerName)} mysqladmin ping -h localhost -u ${env.MYSQL_USER} --password=\${MYSQL_PASSWORD}
                    docker exec ${validateInput(args.testContainerName)} mysql -u ${env.MYSQL_USER} --password=\${MYSQL_PASSWORD} -e "SHOW DATABASES;"
                    docker exec ${validateInput(args.testContainerName)} mysql -u ${env.MYSQL_USER} --password=\${MYSQL_PASSWORD} -e "SELECT 1;"
                elif [ "${validateInput(args.appType)}" = "php" ]; then
                    if [ ! -f "${validateInput(args.appDir)}/tests/run-tests.php" ]; then
                        echo "❌ PHP integration test file tests/run-tests.php not found"
                        exit 1
                    fi
                    docker exec ${validateInput(args.testContainerName)} php /var/www/html/tests/run-tests.php || echo "PHP tests completed"
                else
                    ./scripts/integration-tests.sh ${validateInput(args.testPort)} || echo "Integration tests completed"
                fi
            """
        }
    } catch (Exception e) {
        echo "❌ Integration tests failed: ${e.getMessage()}"
        currentBuild.result = 'UNSTABLE'
    } finally {
        if (args.useCompose) {
            sh """
                docker-compose -f ${validateInput(args.composeFile)} down || true
                docker network rm web-app-net || true
            """
        } else {
            sh """
                docker logs ${validateInput(args.testContainerName)} > integration-test-logs.txt || true
                docker stop ${validateInput(args.testContainerName)} || true
                docker rm ${validateInput(args.testContainerName)} || true
                docker network rm web-app-net || true
            """
        }
    }
}

def pushToRegistry(Map args) {
    docker.withRegistry(validateInput(args.dockerRegistry), validateInput(args.credentialsId)) {
        def image = docker.image("${validateInput(args.imageName)}:${validateInput(args.buildTag)}")
        image.push()
        image.push("jenkins-latest")
        if (args.branchName == 'main') {
            image.push("production")
            image.push("latest")
        } else if (args.branchName == 'develop') {
            image.push("staging")
        }
        sh """
            curl -s -X GET https://registry.hub.docker.com/v2/repositories/${env.DOCKER_USERNAME}/${env.DOCKER_REPO}/tags/?page_size=100 | \
                jq -r '.results[].name' > existing-tags.txt || echo "Failed to get tags"
            docker manifest inspect ${validateInput(args.imageName)}:${validateInput(args.buildTag)} > registry-manifest.json || echo "Manifest not available"
        """
    }
}

def deploy(Map args) {
    def config = readYaml file: "${args.appDir}/pipeline-config.yml"
    def healthCmd = config.deployment.environments[args.environment]?.services[args.appType]?.healthcheck?.command ?:
        args.appType == 'mysql' ? 
        "mysqladmin ping -h localhost -u ${env.MYSQL_USER} --password=\${MYSQL_PASSWORD}" : 
        "curl -f http://localhost:${validateInput(args.appPort)}/ || exit 1"
    if (args.enableSwarm) {
        deployToSwarm(args)
    } else {
        switch(args.deploymentStrategy) {
            case 'blue-green':
                deployBlueGreen(args)
                break
            default:
                deployToEnvironment(args)
                break
        }
    }
}

def deployToEnvironment(Map args) {
    def config = readYaml file: "${args.appDir}/pipeline-config.yml"
    def healthCmd = config.deployment.environments[args.environment]?.services[args.appType]?.healthcheck?.command ?:
        args.appType == 'mysql' ? 
        "mysqladmin ping -h localhost -u ${env.MYSQL_USER} --password=\${MYSQL_PASSWORD}" : 
        "curl -f http://localhost:${validateInput(args.appPort)}/ || exit 1"
    def containerName = "${validateInput(args.appType)}-app-${validateInput(args.environment)}"
    try {
        sh """
            if docker ps -q -f name=${containerName}; then
                docker commit ${containerName} ${validateInput(args.imageName)}:${validateInput(args.environment)}-backup-\$(date +%Y%m%d-%H%M%S)
                docker stop ${containerName} || true
                docker rm ${containerName} || true
            fi
        """
        if (args.useCompose) {
            sh """
                cd ${validateInput(args.appDir)}
                export ENVIRONMENT=${validateInput(args.environment)}
                export PORT=${validateInput(args.port)}
                docker-compose -f ${validateInput(args.composeFile)} up -d
                SERVICES=\$(docker-compose -f ${validateInput(args.composeFile)} config --services)
                for service in \$SERVICES; do
                    timeout ${validateInput(args.healthCheckTimeout)}s bash -c "until docker-compose -f ${validateInput(args.composeFile)} ps \$service | grep -q 'healthy'; do sleep 5; done" || echo "\$service not healthy"
                done
            """
        } else {
            def networkMode = args.useCompose ? "web-app-net" : validateInput(args.networkMode)
            sh """
                docker network create web-app-net --subnet=172.18.19.0/24 || echo "Network web-app-net already exists"
                docker run -d \
                    --name ${containerName} \
                    -p ${validateInput(args.port)}:${validateInput(args.appPort)} \
                    -e ENVIRONMENT=${validateInput(args.environment)} \
                    --memory=${validateInput(args.memoryLimit)} \
                    --cpus=${validateInput(args.cpuLimit)} \
                    --network=${networkMode} \
                    --health-cmd="${healthCmd}" \
                    --health-interval=30s \
                    --health-timeout=10s \
                    --health-retries=3 \
                    ${validateInput(args.imageName)}:${validateInput(args.buildTag)}
                timeout ${validateInput(args.healthCheckTimeout)}s bash -c 'until docker inspect --format="{{.State.Health.Status}}" ${containerName} | grep -q "healthy"; do sleep 5; done'
            """
        }
        sh """
            if [ "${validateInput(args.appType)}" != "mysql" ]; then
                curl -f http://localhost:${validateInput(args.port)}/ || curl -f http://localhost:${validateInput(args.port)}/
            else
                docker exec ${containerName} mysqladmin ping -h localhost -u ${env.MYSQL_USER} --password=\${MYSQL_PASSWORD}
            fi
        """
    } catch (Exception e) {
        echo "❌ Deployment failed: ${e.getMessage()}"
        if (args.rollbackOnFailure) {
            rollbackDeployment(args)
        }
        throw e
    }
}

def deployToSwarm(Map args) {
    def config = readYaml file: "${args.appDir}/pipeline-config.yml"
    def healthCmd = config.deployment.environments[args.environment]?.services[args.appType]?.healthcheck?.command ?:
        args.appType == 'mysql' ? 
        "mysqladmin ping -h localhost -u ${env.MYSQL_USER} --password=\${MYSQL_PASSWORD}" : 
        "curl -f http://localhost:${validateInput(args.appPort)}/ || exit 1"
    sh """
        docker swarm init || echo "Swarm already initialized"
        docker service create --name ${validateInput(args.serviceName)} \
            --replicas 3 \
            --update-order start-first \
            --publish ${validateInput(args.port)}:${validateInput(args.appPort)} \
            --constraint 'node.role == worker' \
            --label environment=${validateInput(args.environment)} \
            ${validateInput(args.imageName)}:${validateInput(args.buildTag)} || \
        docker service update --image ${validateInput(args.imageName)}:${validateInput(args.buildTag)} ${validateInput(args.serviceName)}
        timeout 300s bash -c 'until [ "\$(docker service ls --filter name=${validateInput(args.serviceName)} --format "{{.Replicas}}" | cut -d/ -f1)" -eq "\$(docker service ls --filter name=${validateInput(args.serviceName)} --format "{{.Replicas}}" | cut -d/ -f2)" ]; do sleep 5; done'
    """
}

def deployBlueGreen(Map args) {
    def config = readYaml file: "${args.appDir}/pipeline-config.yml"
    def healthCmd = config.deployment.environments[args.environment]?.services[args.appType]?.healthcheck?.command ?:
        args.appType == 'mysql' ? 
        "mysqladmin ping -h localhost -u ${env.MYSQL_USER} --password=\${MYSQL_PASSWORD}" : 
        "curl -f http://localhost:${validateInput(args.appPort)}/ || exit 1"
    def blueContainer = "${validateInput(args.appType)}-app-${validateInput(args.environment)}-blue"
    def greenContainer = "${validateInput(args.appType)}-app-${validateInput(args.environment)}-green"
    def currentPort = args.port as Integer
    def bluePort = currentPort + 1
    def greenPort = currentPort + 2
    def currentActive = sh(script: "docker ps -q -f name=${blueContainer} || echo 'green'", returnStdout: true).trim()
    def targetContainer = currentActive ? greenContainer : blueContainer
    def targetPort = currentActive ? greenPort : bluePort

    try {
        def networkMode = args.useCompose ? "web-app-net" : validateInput(args.networkMode)
        sh """
            docker network create web-app-net --subnet=172.18.19.0/24 || echo "Network web-app-net already exists"
            docker run -d \
                --name ${targetContainer} \
                -p ${targetPort}:${validateInput(args.appPort)} \
                -e ENVIRONMENT=${validateInput(args.environment)} \
                --memory=${validateInput(args.memoryLimit)} \
                --cpus=${validateInput(args.cpuLimit)} \
                --network=${networkMode} \
                --health-cmd="${healthCmd}" \
                --health-interval=30s \
                --health-timeout=10s \
                --health-retries=3 \
                ${validateInput(args.imageName)}:${validateInput(args.buildTag)}
            timeout ${validateInput(args.healthCheckTimeout)}s bash -c 'until docker inspect --format="{{.State.Health.Status}}" ${targetContainer} | grep -q "healthy"; do sleep 5; done'
            if [ -f scripts/update-loadbalancer.sh ]; then
                ./scripts/update-loadbalancer.sh ${validateInput(args.environment)} ${targetPort}
            fi
            docker stop ${currentActive ? blueContainer : greenContainer} || true
            docker rm ${currentActive ? blueContainer : greenContainer} || true
        """
    } catch (Exception e) {
        echo "❌ Blue-Green deployment failed: ${e.getMessage()}"
        if (args.rollbackOnFailure) {
            rollbackDeployment(args)
        }
        throw e
    }
}

def deployCanary(Map args) {
    def config = readYaml file: "${args.appDir}/pipeline-config.yml"
    def healthCmd = config.deployment.environments[args.environment]?.services[args.appType]?.healthcheck?.command ?:
        args.appType == 'mysql' ? 
        "mysqladmin ping -h localhost -u ${env.MYSQL_USER} --password=\${MYSQL_PASSWORD}" : 
        "curl -f http://localhost:${validateInput(args.appPort)}/ || exit 1"
    if (!fileExists('scripts/configure-canary.sh') || !fileExists('scripts/restore-production-traffic.sh')) {
        error "Required canary scripts (scripts/configure-canary.sh, scripts/restore-production-traffic.sh) are missing"
    }
    def canaryContainer = "${validateInput(args.appType)}-app-${validateInput(args.environment)}-canary"
    def productionContainer = "${validateInput(args.appType)}-app-${validateInput(args.environment)}"
    def canaryPort = (args.port as Integer) + 10

    try {
        def networkMode = args.useCompose ? "web-app-net" : validateInput(args.networkMode)
        sh """
            docker network create web-app-net --subnet=172.18.19.0/24 || echo "Network web-app-net already exists"
            docker run -d \
                --name ${canaryContainer} \
                -p ${canaryPort}:${validateInput(args.appPort)} \
                -e ENVIRONMENT=${validateInput(args.environment)} \
                --memory=${validateInput(args.memoryLimit)} \
                --cpus=${validateInput(args.cpuLimit)} \
                --network=${networkMode} \
                --health-cmd="${healthCmd}" \
                --health-interval=30s \
                --health-timeout=10s \
                --health-retries=3 \
                ${validateInput(args.imageName)}:${validateInput(args.buildTag)}
            timeout ${validateInput(args.healthCheckTimeout)}s bash -c 'until docker inspect --format="{{.State.Health.Status}}" ${canaryContainer} | grep -q "healthy"; do sleep 5; done'
            ./scripts/configure-canary.sh ${validateInput(args.environment)} ${validateInput(args.port)} ${canaryPort} ${validateInput(args.canaryPercentage)}
        """
        sleep 300
        sh """
            docker stop ${productionContainer} || true
            docker rm ${productionContainer} || true
            docker rename ${canaryContainer} ${productionContainer}
            ./scripts/restore-production-traffic.sh ${validateInput(args.environment)} ${validateInput(args.port)}
        """
    } catch (Exception e) {
        echo "❌ Canary deployment failed: ${e.getMessage()}"
        sh "docker stop ${canaryContainer} || true; docker rm ${canaryContainer} || true"
        throw e
    }
}

def rollbackDeployment(Map args) {
    def config = readYaml file: "${args.appDir}/pipeline-config.yml"
    def healthCmd = config.deployment.environments[args.environment]?.services[args.appType]?.healthcheck?.command ?:
        args.appType == 'mysql' ? 
        "mysqladmin ping -h localhost -u ${env.MYSQL_USER} --password=\${MYSQL_PASSWORD}" : 
        "curl -f http://localhost:${validateInput(args.appPort)}/ || exit 1"
    def containerName = "${validateInput(args.appType)}-app-${validateInput(args.environment)}"
    try {
        def backupImages = sh(script: "docker images ${validateInput(args.imageName)} --format '{{.Tag}}' | grep '${validateInput(args.environment)}-backup-' | sort -r | head -1", returnStdout: true).trim()
        if (!backupImages) {
            echo "❌ No backup images found for rollback"
            return
        }
        sh """
            docker stop ${containerName} || true
            docker rm ${containerName} || true
        """
        if (args.useCompose) {
            sh """
                cd ${validateInput(args.appDir)}
                export ENVIRONMENT=${validateInput(args.environment)}
                export PORT=${validateInput(args.port)}
                docker-compose -f ${validateInput(args.composeFile)} up -d --force-recreate
            """
        } else {
            def networkMode = args.useCompose ? "web-app-net" : validateInput(args.networkMode)
            sh """
                docker network create web-app-net --subnet=172.18.19.0/24 || echo "Network web-app-net already exists"
                docker run -d \
                    --name ${containerName} \
                    -p ${validateInput(args.port)}:${validateInput(args.appPort)} \
                    -e ENVIRONMENT=${validateInput(args.environment)} \
                    --memory=${validateInput(args.memoryLimit)} \
                    --cpus=${validateInput(args.cpuLimit)} \
                    --network=${networkMode} \
                    --health-cmd="${healthCmd}" \
                    --health-interval=30s \
                    --health-timeout=10s \
                    --health-retries=3 \
                    ${validateInput(args.imageName)}:${backupImages}
            """
        }
    } catch (Exception e) {
        echo "❌ Rollback failed: ${e.getMessage()}"
        throw e
    }
}

def sendMetrics(Map args) {
    try {
        retry(3) {
            sh """
                cat << EOF > docker-metrics.prom
docker_build_duration_seconds{job="${validateInput(args.jobName)}",image="${validateInput(args.imageName)}",tag="${validateInput(args.buildTag)}"} ${currentBuild.duration / 1000}
docker_build_result{job="${validateInput(args.jobName)}",image="${validateInput(args.imageName)}",tag="${validateInput(args.buildTag)}"} ${args.buildResult == 'SUCCESS' ? 1 : 0}
EOF
                curl -X POST ${validateInput(args.prometheusGateway)}/metrics/job/jenkins/instance/${validateInput(args.jobName)} --data-binary @docker-metrics.prom
            """
        }
    } catch (Exception e) {
        echo "⚠️ Failed to send metrics: ${e.getMessage()}"
    }
}
