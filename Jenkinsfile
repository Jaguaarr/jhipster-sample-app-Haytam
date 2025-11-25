pipeline {
    agent any
    tools {
        maven 'Maven-3.9.11'
        jdk 'JDK-17'
    }

    environment {
        MAVEN_OPTS = '-Xmx2048m'
        DOCKER_IMAGE = 'jhipster-app'
        DOCKER_TAG = "${env.BUILD_NUMBER}"
        APP_NAME = 'jhipster-app'
        K8S_NAMESPACE = 'default'             // Namespace Kubernetes
        K8S_DEPLOYMENT = 'jhipster-app'      // Nom du déploiement
        K8S_SERVICE = 'jhipster-app-service' // Nom du service
    }

    stages {
        stage('1. Clone Repository') {
            steps {
                echo 'Cloning repository from GitHub...'
                checkout scm
            }
        }

        stage('2. Compile Project') {
            steps {
                echo 'Compiling Maven project...'
                sh 'mvn clean compile -DskipTests'
            }
        }

        stage('3. Run Tests with Failure Tolerance') {
            steps {
                script {
                    catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                        sh '''
                            mvn test \
                                -DskipTests=false \
                                -Dtest="!DTOValidationTest,!MailServiceTest,!HibernateTimeZoneIT,!OperationResourceAdditionalTest" \
                                -DfailIfNoTests=false
                        '''
                    }
                }
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('4. Generate JAR Package') {
            steps {
                echo 'Creating JAR package...'
                sh 'mvn package -DskipTests'
            }
            post {
                success {
                    archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true, fingerprint: true
                }
            }
        }

        stage('5. SonarQube Analysis') {
            steps {
                echo 'Running SonarQube analysis...'
                timeout(time: 30, unit: 'MINUTES') {
                    withSonarQubeEnv('SonarQube') {
                        sh 'mvn sonar:sonar -Dsonar.projectKey=yourwaytoltaly -DskipTests'
                    }
                }
            }
        }

        stage('6. Docker Build & Run') {
            steps {
                echo 'Building Docker image and running container...'
                script {
                    // Stop and remove existing container if exists
                    sh """
                        if [ \$(docker ps -aq -f name=${APP_NAME}) ]; then
                            echo 'Stopping existing container...'
                            docker rm -f ${APP_NAME}
                        fi

                        echo 'Building Docker image...'
                        docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} .

                        echo 'Running Docker container...'
                        docker run -d --name ${APP_NAME} -p 8080:8080 ${DOCKER_IMAGE}:${DOCKER_TAG}
                    """
                }
            }
        }

        stage('7. Security Scan with Trivy') {
            steps {
                echo 'Running security scan with Trivy...'
                script {
                    // Catch errors to not fail the pipeline
                    catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                        sh """
                            echo 'Scanning Docker image with Trivy...'
                            trivy image --exit-code 0 --format json -o trivy-report.json ${DOCKER_IMAGE}:${DOCKER_TAG}
                            trivy image --exit-code 0 --format template --template "@contrib/html.tpl" -o trivy-report.html ${DOCKER_IMAGE}:${DOCKER_TAG}
                        """
                    }
                }
            }
            post {
                always {
                    archiveArtifacts artifacts: 'trivy-report.*', allowEmptyArchive: true
                }
            }
        }
    }
stage('8. Deploy to Kubernetes') {
    steps {
        echo 'Deploying Docker image to Kubernetes...'
        script {
            catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                sh """
                    # Appliquer les fichiers YAML depuis la racine
                    kubectl apply -f deployment.yaml
                    kubectl apply -f service.yaml

                    # Mettre à jour l'image du déploiement
                    kubectl set image deployment/${K8S_DEPLOYMENT} \
                        ${K8S_DEPLOYMENT}=ton-utilisateur/jhipster-app:${DOCKER_TAG} \
                        -n ${K8S_NAMESPACE}

                    # Attendre que le déploiement soit prêt
                    kubectl rollout status deployment/${K8S_DEPLOYMENT} -n ${K8S_NAMESPACE}
                """
            }
        }
    }
}



    post {
        success {
            echo '✓ Pipeline executed successfully!'
            echo 'ℹ️ Docker container is running. Access the app at http://<agent-ip>:8080'
        }
        failure {
            echo '✗ Pipeline failed.'
        }
        always {
            echo 'Cleaning workspace files...'
            cleanWs(cleanWhenNotBuilt: false, deleteDirs: true, disableDeferredWipeout: false)
        }
    }
}
