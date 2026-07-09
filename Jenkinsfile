pipeline {
    agent any

    environment {
        IMAGE_NAME = "rayenemejri42/stage-test"
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
                script {
                    def javaHome = tool name: 'jdk-17', type: 'jdk'
                    def mavenHome = tool name: 'maven-3', type: 'maven'
                    withEnv([
                        "JAVA_HOME=${javaHome}",
                        "PATH=${javaHome}/bin:${mavenHome}/bin:${env.PATH}"
                    ]) {
                        sh '''
                            java -version
                            mvn --version
                            mvn clean compile
                            mvn test
                            mvn package -DskipTests
                        '''
                    }
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                script {
                    def javaHome = tool name: 'jdk-17', type: 'jdk'
                    def mavenHome = tool name: 'maven-3', type: 'maven'
                    withEnv([
                        "JAVA_HOME=${javaHome}",
                        "PATH=${javaHome}/bin:${mavenHome}/bin:${env.PATH}"
                    ]) {
                        withSonarQubeEnv('sonarqube') {
                            sh '''
                                mvn sonar:sonar \
                                    -Dsonar.projectKey=stage-test \
                                    -Dsonar.projectName="Stage Test" \
                                    -Dsonar.java.binaries=target/classes \
                                    -Dsonar.java.test.binaries=target/test-classes
                            '''
                        }
                    }
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
                script {
                    def javaHome = tool name: 'jdk-17', type: 'jdk'
                    def mavenHome = tool name: 'maven-3', type: 'maven'
                    withEnv([
                        "JAVA_HOME=${javaHome}",
                        "PATH=${javaHome}/bin:${mavenHome}/bin:${env.PATH}"
                    ]) {
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