pipeline {
    agent any

    environment {
        IMAGE_NAME = "rayenemejri42/stage-test"
        JAVA_HOME = "/usr/lib/jvm/java-17-openjdk-amd64"  // No /bin/java!
        CI_DB_CONTAINER = "stage-test-ci-mysql-${BUILD_NUMBER}"
        CI_DB_PORT = "3307"
        CI_DB_NAME = "stage_test_ci"
        CI_DB_USER = "stage_test_ci"
        CI_DB_PASSWORD = "stage-test-ci-password"
    }

    options {
        skipDefaultCheckout(true)
        disableConcurrentBuilds()
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

        stage('Start CI Database') {
            steps {
                sh '''
                    docker run --detach --rm \
                        --name "$CI_DB_CONTAINER" \
                        --publish "$CI_DB_PORT:3306" \
                        --env MYSQL_DATABASE="$CI_DB_NAME" \
                        --env MYSQL_USER="$CI_DB_USER" \
                        --env MYSQL_PASSWORD="$CI_DB_PASSWORD" \
                        --env MYSQL_ROOT_PASSWORD="$CI_DB_PASSWORD" \
                        mysql:8

                    for attempt in $(seq 1 30); do
                        if docker exec "$CI_DB_CONTAINER" mysqladmin ping -h localhost -u"$CI_DB_USER" -p"$CI_DB_PASSWORD" --silent; then
                            exit 0
                        fi
                        sleep 2
                    done

                    docker logs "$CI_DB_CONTAINER"
                    exit 1
                '''
            }
        }

        stage('Flyway Migration') {
            steps {
                withEnv([
                    "JAVA_HOME=${env.JAVA_HOME}",
                    "PATH=${env.JAVA_HOME}/bin:${env.PATH}"
                ]) {
                    sh '''
                        mvn flyway:migrate \
                            -Dflyway.url=jdbc:mysql://localhost:$CI_DB_PORT/$CI_DB_NAME \
                            -Dflyway.user=$CI_DB_USER \
                            -Dflyway.password=$CI_DB_PASSWORD \
                            -Dflyway.baselineOnMigrate=true
                    '''
                }
            }
        }

        stage('Maven Build & Test') {
            steps {
                withEnv([
                    "JAVA_HOME=${env.JAVA_HOME}",
                    "PATH=${env.JAVA_HOME}/bin:${env.PATH}"
                ]) {
                    sh '''
                        echo "JAVA_HOME is: $JAVA_HOME"
                        java -version
                        mvn --version
                        mvn clean verify \
                            "-Dspring.datasource.url=jdbc:mysql://localhost:$CI_DB_PORT/$CI_DB_NAME?useSSL=false&serverTimezone=UTC" \
                            -Dspring.datasource.username=$CI_DB_USER \
                            -Dspring.datasource.password=$CI_DB_PASSWORD
                    '''
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withEnv([
                    "JAVA_HOME=${env.JAVA_HOME}",
                    "PATH=${env.JAVA_HOME}/bin:${env.PATH}"
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
            echo "Pipeline failed! "
            echo "Check the logs above for errors."
        }
        always {
            sh 'docker rm --force "$CI_DB_CONTAINER" || true'
            cleanWs()
        }
    }
}
