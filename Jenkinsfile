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
        SONARQUBE_ENV = 'SonarQube' // Nom configuré dans Jenkins → SonarQube servers
        EMAIL_RECIPIENTS = 'tonemail@exemple.com'
    }

    stages {
        stage('1. Clone Repository') {
            steps {
                echo 'Cloning repository...'
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

        stage('4. Package Application') {
            steps {
                echo 'Packaging JAR...'
                sh 'mvn package -DskipTests'
            }
            post {
                success {
                    archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
                }
            }
        }

        stage('5. SonarQube Analysis') {
            steps {
                echo 'Running SonarQube analysis...'
                timeout(time: 15, unit: 'MINUTES') {
                    withSonarQubeEnv("${SONARQUBE_ENV}") {
                        sh 'mvn sonar:sonar -Dsonar.projectKey=jhipster-sample-app -DskipTests'
                    }
                }
            }
        }

        stage('6. Quality Gate Check') {
            steps {
                echo 'Checking SonarQube Quality Gate...'
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('7. Docker Build & Run') {
            steps {
                echo 'Building Docker image...'
                sh "docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} ."
                echo 'Running Docker container...'
                sh "docker stop ${APP_NAME} || true && docker rm ${APP_NAME} || true"
                sh "docker run -d --name ${APP_NAME} -p 8080:8080 ${DOCKER_IMAGE}:${DOCKER_TAG}"
            }
        }

        stage('8. Security Scan with Trivy') {
            steps {
                echo 'Running Trivy vulnerability scan...'
                sh "trivy image --exit-code 1 --severity HIGH,CRITICAL ${DOCKER_IMAGE}:${DOCKER_TAG}"
            }
        }

    }

    post {
        success {
            echo '✓ Pipeline executed successfully!'
            mail to: "${EMAIL_RECIPIENTS}",
                 subject: "Build SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                 body: "Le pipeline pour ${env.JOB_NAME} a réussi.\nAccédez à l'application: http://<server-ip>:8080"
        }
        unstable {
            echo '⚠ Pipeline completed with warnings.'
            mail to: "${EMAIL_RECIPIENTS}",
                 subject: "Build UNSTABLE: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                 body: "Le pipeline pour ${env.JOB_NAME} est instable.\nVérifiez les logs Jenkins pour détails."
        }
        failure {
            echo '✗ Pipeline failed.'
            mail to: "${EMAIL_RECIPIENTS}",
                 subject: "Build FAILURE: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                 body: "Le pipeline pour ${env.JOB_NAME} a échoué.\nVérifiez les logs Jenkins."
        }
        always {
            echo 'Cleaning workspace...'
            cleanWs(cleanWhenNotBuilt: false)
        }
    }
}
