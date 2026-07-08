pipeline {

    agent any

    environment {
        IMAGE_NAME = "rayenemejri42/stage-test"
    }

    stages {

        stage('Checkout') {
            steps {
                git(
                    branch: 'main',
                    url: 'https://github.com/Rayene-Mejri/Stage-test.git',
                    credentialsId: 'github-credentials'
                )
            }
        }


        stage('Flyway Migration') {
            steps {

                withCredentials([
                    usernamePassword(
                        credentialsId: 'mysql-credentials',
                        usernameVariable: 'DB_USER',
                        passwordVariable: 'DB_PASSWORD'
                    )
                ]) {

                    sh '''
                    mvn flyway:migrate \
                    -Dflyway.url=jdbc:mysql://localhost:3306/stage_test \
                    -Dflyway.user=$DB_USER \
                    -Dflyway.password=$DB_PASSWORD
                    '''

                }
            }
        }


        stage('Build Docker Image') {
            steps {
                sh '''
                docker build -t $IMAGE_NAME:$BUILD_NUMBER .
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
                    echo $DOCKER_PASS | docker login \
                    -u $DOCKER_USER --password-stdin

                    docker push $IMAGE_NAME:$BUILD_NUMBER
                    '''
                }
            }
        }
    }
}