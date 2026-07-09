pipeline {
    agent any

    environment {
        IMAGE_NAME = "rayenemejri42/stage-test"
        MAVEN_HOME = tool name: 'maven-3', type: 'maven'
    }

    options {
        skipDefaultCheckout(true)
    }

    stages {

        stage('Checkout') {
            steps {
                git(
                    branch: 'main',
                    url: 'https://github.com/Rayene-Mejri/Stage-test.git',
                    credentialsId: 'Github'
                )
            }
        }

        stage('Maven Build & Test') {
            steps {
                sh '''
                    mvn clean compile
                    mvn test
                    mvn package -DskipTests
                '''
            }
            post {
                success {
                    echo 'Maven build completed successfully!'
                }
                failure {
                    echo 'Maven build failed!'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('sonarqube') {
                    sh '''
                        mvn sonar:sonar \
                            -Dsonar.projectKey=stage-test \
                            -Dsonar.projectName="Stage Test" \
                            -Dsonar.java.binaries=target/classes \
                            -Dsonar.java.test.binaries=target/test-classes \
                            -Dsonar.sources=src/main/java \
                            -Dsonar.tests=src/test/java
                    '''
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Flyway Migration') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'db-credentials',
                        usernameVariable: 'DB_USER',
                        passwordVariable: 'DB_PASSWORD'
                    )
                ]) {
                    sh '''
                        mvn flyway:migrate \
                            -Dflyway.url=jdbc:mysql://localhost:3306/stage_test \
                            -Dflyway.user=$DB_USER \
                            -Dflyway.password=$DB_PASSWORD \
                            -Dflyway.baselineOnMigrate=true
                    '''
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                sh '''
                    docker build -t $IMAGE_NAME:$BUILD_NUMBER .
                    docker tag $IMAGE_NAME:$BUILD_NUMBER $IMAGE_NAME:latest
                '''
            }
        }

        stage('Push Docker Image') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhub',
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'
                    )
                ]) {
                    sh '''
                        echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin
                        docker push $IMAGE_NAME:$BUILD_NUMBER
                        docker push $IMAGE_NAME:latest
                    '''
                }
            }
        }
    }

    post {

        success {
            echo "Pipeline completed successfully!"
            echo "Image: $IMAGE_NAME:$BUILD_NUMBER pushed to Docker Hub"
        }

        failure {
            echo "Pipeline failed!"
            echo "Check the logs above for errors."
        }

        always {
            cleanWs()
        }
    }
}