def buildImage(Map args) {
    dir(args.appDir) {
        def buildArgs = [
            "--build-arg BUILD_DATE=\$(date -u +'%Y-%m-%dT%H:%M:%SZ')",
            "--build-arg VCS_REF=${env.GIT_COMMIT}",
            "--build-arg VERSION=${args.buildTag}",
            "--build-arg APP_TYPE=${args.appType}",
            "--build-arg ENVIRONMENT=${args.environment}"
        ].join(' ')
        def cacheArgs = args.enableDockerCache ? "--cache-from ${args.cacheFromImage} --cache-to type=registry,ref=${args.cacheFromImage},mode=max" : ""
        def platformArgs = args.enableMultiArch ? "--platform ${args.dockerPlatforms}" : ""
        def additionalArgs = "${args.dockerSquash ? '--squash' : ''} ${args.dockerNoCache ? '--no-cache' : ''}"

        try {
            if (args.enableMultiArch) {
                sh "docker buildx create --name multiarch-builder --use || echo 'Builder exists'"
                sh "docker buildx inspect --bootstrap"
            }

            if (args.useCompose) {
                sh """
                    DOCKER_BUILDKIT=1 docker-compose -f ${args.composeFile} build --parallel --progress=plain
                    docker-compose -f ${args.composeFile} config --services | while read service; do
                        SERVICE_IMAGE=\$(docker-compose -f ${args.composeFile} config | grep "image:" | head -1 | awk '{print \$2}')
                        docker tag \$SERVICE_IMAGE ${args.imageName}:${args.buildTag}
                        docker tag \$SERVICE_IMAGE ${args.imageName}:jenkins-build
                    done
                """
            } else {
                sh """
                    DOCKER_BUILDKIT=1 docker build ${platformArgs} ${cacheArgs} ${buildArgs} ${additionalArgs} \
                        --tag ${args.imageName}:${args.buildTag} \
                        --tag ${args.imageName}:jenkins-build \
                        --tag ${args.cacheFromImage} \
                        --label "org.opencontainers.image.source=${args.githubRepo}" \
                        --label "org.opencontainers.image.revision=${env.GIT_COMMIT}" \
                        --label "org.opencontainers.image.version=${args.buildTag}" \
                        --progress=plain \
                        .
                """
            }

            sh """
                docker images ${args.imageName}:${args.buildTag} --format "table {{.Repository}}:{{.Tag}}\t{{.Size}}"
                docker history ${args.imageName}:${args.buildTag} --format "table {{.CreatedBy}}\t{{.Size}}" > image-layers.txt
                docker run --rm -v /var/run/docker.sock:/var/run/docker.sock -v \$(pwd):/workspace \
                    aquasec/trivy:latest image --format spdx-json --output /workspace/sbom.json ${args.imageName}:${args.buildTag} || echo "SBOM completed"
            """
        } catch (Exception e) {
            echo "❌ Build failed: ${e.getMessage()}"
            throw e
        }
    }
}

def runIntegrationTests(Map args) {
    try {
        if (args.useCompose) {
            sh """
                cd ${args.appDir}
                docker-compose -f ${args.composeFile} up -d
                sleep 30
                SERVICES=\$(docker-compose -f ${args.composeFile} config --services)
                for service in \$SERVICES; do
                    docker-compose -f ${args.composeFile} ps \$service | grep "healthy" || echo "\$service not healthy"
                done
                docker-compose -f ${args.composeFile} exec -T app npm run test:integration || echo "Integration tests completed"
            """
        } else {
            def healthCmd = args.appType == 'mysql' ? 
                "mysqladmin ping -h localhost -u root --password=\${MYSQL_ROOT_PASSWORD:-rootpass}" : 
                "curl -f http://localhost:${args.appPort}/health || exit 1"
            sh """
                docker run -d --name ${args.testContainerName} \
                    -p ${args.testPort}:${args.appPort} \
                    -e ENVIRONMENT=test \
                    --memory=${args.memoryLimit} \
                    --cpus=${args.cpuLimit} \
                    --network=${args.networkMode} \
                    --health-cmd="${healthCmd}" \
                    --health-interval=30s \
                    --health-timeout=10s \
                    --health-retries=3 \
                    ${args.imageName}:${args.buildTag}
                timeout ${args.healthCheckTimeout}s bash -c 'until docker inspect --format="{{.State.Health.Status}}" ${args.testContainerName} | grep -q "healthy"; do sleep 5; done'
                if [ "${args.appType}" = "mysql" ]; then
                    docker exec ${args.testContainerName} mysqladmin ping -h localhost -u root --password=\${MYSQL_ROOT_PASSWORD:-rootpass}
                    docker exec ${args.testContainerName} mysql -u root --password=\${MYSQL_ROOT_PASSWORD:-rootpass} -e "SHOW DATABASES;"
                else
                    ./scripts/integration-tests.sh ${args.testPort} || echo "Integration tests completed"
                fi
            """
        }
    } catch (Exception e) {
        echo "❌ Integration tests failed: ${e.getMessage()}"
        currentBuild.result = 'UNSTABLE'
    } finally {
        if (args.useCompose) {
            sh "docker-compose -f ${args.composeFile} down || true"
        } else {
            sh """
                docker logs ${args.testContainerName} > integration-test-logs.txt || true
                docker stop ${args.testContainerName} || true
                docker rm ${args.testContainerName} || true
            """
        }
    }
}

def pushToRegistry(Map args) {
    docker.withRegistry(args.dockerRegistry, args.credentialsId) {
        def image = docker.image("${args.imageName}:${args.buildTag}")
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
            docker manifest inspect ${args.imageName}:${args.buildTag} > registry-manifest.json || echo "Manifest not available"
        """
    }
}

def deploy(Map args) {
    if (args.enableSwarm) {
        deployToSwarm(args)
    } else {
        switch(args.deploymentStrategy) {
            case 'blue-green':
                deployBlueGreen(args)
                break
            case 'canary':
                deployCanary(args)
                break
            default:
                deployToEnvironment(args)
                break
        }
    }
}

def deployToEnvironment(Map args) {
    def containerName = "${args.appType}-app-${args.environment}"
    try {
        sh """
            if docker ps -q -f name=${containerName}; then
                docker commit ${containerName} ${args.imageName}:${args.environment}-backup-\$(date +%Y%m%d-%H%M%S)
                docker stop ${containerName} || true
                docker rm ${containerName} || true
            fi
        """
        if (args.useCompose) {
            sh """
                cd ${args.appDir}
                export ENVIRONMENT=${args.environment}
                export PORT=${args.port}
                docker-compose -f ${args.composeFile} up -d
                SERVICES=\$(docker-compose -f ${args.composeFile} config --services)
                for service in \$SERVICES; do
                    timeout ${args.healthCheckTimeout}s bash -c "until docker-compose -f ${args.composeFile} ps \$service | grep -q 'healthy'; do sleep 5; done" || echo "\$service not healthy"
                done
            """
        } else {
            def healthCmd = args.appType == 'mysql' ? 
                "mysqladmin ping -h localhost -u root --password=\${MYSQL_ROOT_PASSWORD:-rootpass}" : 
                "curl -f http://localhost:${args.appPort}/health || exit 1"
            sh """
                docker run -d \
                    --name ${containerName} \
                    -p ${args.port}:${args.appPort} \
                    -e ENVIRONMENT=${args.environment} \
                    --memory=${args.memoryLimit} \
                    --cpus=${args.cpuLimit} \
                    --network=${args.networkMode} \
                    --health-cmd="${healthCmd}" \
                    --health-interval=30s \
                    --health-timeout=10s \
                    --health-retries=3 \
                    ${args.imageName}:${args.buildTag}
                timeout ${args.healthCheckTimeout}s bash -c 'until docker inspect --format="{{.State.Health.Status}}" ${containerName} | grep -q "healthy"; do sleep 5; done'
            """
        }
        sh """
            if [ "${args.appType}" != "mysql" ]; then
                curl -f http://localhost:${args.port}/health || curl -f http://localhost:${args.port}/
            else
                docker exec ${containerName} mysqladmin ping -h localhost -u root --password=\${MYSQL_ROOT_PASSWORD:-rootpass}
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
    sh """
        docker swarm init || echo "Swarm already initialized"
        docker service create --name ${args.serviceName} \
            --replicas 3 \
            --update-order start-first \
            --publish ${args.port}:${args.appPort} \
            --constraint 'node.role == worker' \
            --label environment=${args.environment} \
            ${args.imageName}:${args.buildTag} || \
        docker service update --image ${args.imageName}:${args.buildTag} ${args.serviceName}
        timeout 300s bash -c 'until [ "\$(docker service ls --filter name=${args.serviceName} --format "{{.Replicas}}" | cut -d/ -f1)" -eq "\$(docker service ls --filter name=${args.serviceName} --format "{{.Replicas}}" | cut -d/ -f2)" ]; do sleep 5; done'
    """
}

def deployBlueGreen(Map args) {
    def blueContainer = "${args.appType}-app-${args.environment}-blue"
    def greenContainer = "${args.appType}-app-${args.environment}-green"
    def currentPort = args.port as Integer
    def bluePort = currentPort + 1
    def greenPort = currentPort + 2
    def currentActive = sh(script: "docker ps -q -f name=${blueContainer} || echo 'green'", returnStdout: true).trim()
    def targetContainer = currentActive ? greenContainer : blueContainer
    def targetPort = currentActive ? greenPort : bluePort

    try {
        def healthCmd = args.appType == 'mysql' ? 
            "mysqladmin ping -h localhost -u root --password=\${MYSQL_ROOT_PASSWORD:-rootpass}" : 
            "curl -f http://localhost:${args.appPort}/health || exit 1"
        sh """
            docker run -d \
                --name ${targetContainer} \
                -p ${targetPort}:${args.appPort} \
                -e ENVIRONMENT=${args.environment} \
                --memory=${args.memoryLimit} \
                --cpus=${args.cpuLimit} \
                --network=${args.networkMode} \
                --health-cmd="${healthCmd}" \
                --health-interval=30s \
                --health-timeout=10s \
                --health-retries=3 \
                ${args.imageName}:${args.buildTag}
            timeout ${args.healthCheckTimeout}s bash -c 'until docker inspect --format="{{.State.Health.Status}}" ${targetContainer} | grep -q "healthy"; do sleep 5; done'
            if [ -f scripts/update-loadbalancer.sh ]; then
                ./scripts/update-loadbalancer.sh ${args.environment} ${targetPort}
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
    def canaryContainer = "${args.appType}-app-${args.environment}-canary"
    def productionContainer = "${args.appType}-app-${args.environment}"
    def canaryPort = (args.port as Integer) + 10

    try {
        def healthCmd = args.appType == 'mysql' ? 
            "mysqladmin ping -h localhost -u root --password=\${MYSQL_ROOT_PASSWORD:-rootpass}" : 
            "curl -f http://localhost:${args.appPort}/health || exit 1"
        sh """
            docker run -d \
                --name ${canaryContainer} \
                -p ${canaryPort}:${args.appPort} \
                -e ENVIRONMENT=${args.environment} \
                --memory=${args.memoryLimit} \
                --cpus=${args.cpuLimit} \
                --network=${args.networkMode} \
                --health-cmd="${healthCmd}" \
                --health-interval=30s \
                --health-timeout=10s \
                --health-retries=3 \
                ${args.imageName}:${args.buildTag}
            timeout ${args.healthCheckTimeout}s bash -c 'until docker inspect --format="{{.State.Health.Status}}" ${canaryContainer} | grep -q "healthy"; do sleep 5; done'
            if [ -f scripts/configure-canary.sh ]; then
                ./scripts/configure-canary.sh ${args.environment} ${args.port} ${canaryPort} ${args.canaryPercentage}
            fi
        """
        sleep 300
        sh """
            docker stop ${productionContainer} || true
            docker rm ${productionContainer} || true
            docker rename ${canaryContainer} ${productionContainer}
            if [ -f scripts/restore-production-traffic.sh ]; then
                ./scripts/restore-production-traffic.sh ${args.environment} ${args.port}
            fi
        """
    } catch (Exception e) {
        echo "❌ Canary deployment failed: ${e.getMessage()}"
        sh "docker stop ${canaryContainer} || true; docker rm ${canaryContainer} || true"
        throw e
    }
}

def rollbackDeployment(Map args) {
    def containerName = "${args.appType}-app-${args.environment}"
    try {
        def backupImages = sh(script: "docker images ${args.imageName} --format '{{.Tag}}' | grep '${args.environment}-backup-' | sort -r | head -1", returnStdout: true).trim()
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
                cd ${args.appDir}
                export ENVIRONMENT=${args.environment}
                export PORT=${args.port}
                docker-compose -f ${args.composeFile} up -d
            """
        } else {
            def healthCmd = args.appType == 'mysql' ? 
                "mysqladmin ping -h localhost -u root --password=\${MYSQL_ROOT_PASSWORD:-rootpass}" : 
                "curl -f http://localhost:${args.appPort}/health || exit 1"
            sh """
                docker run -d \
                    --name ${containerName} \
                    -p ${args.port}:${args.appPort} \
                    -e ENVIRONMENT=${args.environment} \
                    --memory=${args.memoryLimit} \
                    --cpus=${args.cpuLimit} \
                    --network=${args.networkMode} \
                    --health-cmd="${healthCmd}" \
                    --health-interval=30s \
                    --health-timeout=10s \
                    --health-retries=3 \
                    ${args.imageName}:${backupImages}
            """
        }
    } catch (Exception e) {
        echo "❌ Rollback failed: ${e.getMessage()}"
        throw e
    }
}

def sendMetrics(Map args) {
    try {
        sh """
            cat << EOF > docker-metrics.prom
docker_build_duration_seconds{job="${args.jobName}",image="${args.imageName}",tag="${args.buildTag}"} ${currentBuild.duration / 1000}
docker_build_result{job="${args.jobName}",image="${args.imageName}",tag="${args.buildTag}"} ${args.buildResult == 'SUCCESS' ? 1 : 0}
EOF
            curl -X POST ${args.prometheusGateway}/metrics/job/jenkins/instance/${args.jobName} --data-binary @docker-metrics.prom
        """
    } catch (Exception e) {
        echo "⚠️ Failed to send metrics: ${e.getMessage()}"
    }
}