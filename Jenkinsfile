pipeline {
    agent any

    environment {
        DOCKER_USER = "alnwalker"
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/alan-j-thomas/movie-journal-app.git'
            }
        }

        stage('Build JARs') {
            steps {
                sh '''
                cd UserService && mvn clean package -DskipTests
                cd ../MovieService && mvn clean package -DskipTests
                cd ../WatchlistService && mvn clean package -DskipTests
                cd ../JournalService && mvn clean package -DskipTests
                cd ../AuthenticationService && mvn clean package -DskipTests
                cd ../NotificationService && mvn clean package -DskipTests
                cd ../CompanionService && mvn clean package -DskipTests
                cd ../AI-Service && mvn clean package -DskipTests
                cd ../ChatService && mvn clean package -DskipTests
                cd ../ApiGateway && mvn clean package -DskipTests
                cd ../ServiceRegistry && mvn clean package -DskipTests
                '''
            }
        }

        stage('Docker Build') {
            steps {
                sh '''
                docker build -t $DOCKER_USER/user-service ./UserService
                docker build -t $DOCKER_USER/movie-service ./MovieService
                docker build -t $DOCKER_USER/watchlist-service ./WatchlistService
                docker build -t $DOCKER_USER/journal-service ./JournalService
                docker build -t $DOCKER_USER/auth-service ./AuthenticationService
                docker build -t $DOCKER_USER/notification-service ./NotificationService
                docker build -t $DOCKER_USER/companion-service ./CompanionService
                docker build -t $DOCKER_USER/ai-service ./AI-Service
                docker build -t $DOCKER_USER/chat-service ./ChatService
                docker build -t $DOCKER_USER/api-gateway ./ApiGateway
                docker build -t $DOCKER_USER/service-registry ./ServiceRegistry
                '''
            }
        }

        stage('Push to Docker Hub') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'docker-creds', usernameVariable: 'USER', passwordVariable: 'PASS')]) {
                    sh '''
                    echo $PASS | docker login -u $USER --password-stdin

                    docker push $DOCKER_USER/user-service
                    docker push $DOCKER_USER/movie-service
                    docker push $DOCKER_USER/watchlist-service
                    docker push $DOCKER_USER/journal-service
                    docker push $DOCKER_USER/auth-service
                    docker push $DOCKER_USER/notification-service
                    docker push $DOCKER_USER/companion-service
                    docker push $DOCKER_USER/ai-service
                    docker push $DOCKER_USER/chat-service
                    docker push $DOCKER_USER/api-gateway
                    docker push $DOCKER_USER/service-registry
                    '''
                }
            }
        }

        stage('Deploy') {
            steps {
                sh '''
                cd /project
                docker-compose down || true
                docker-compose up -d
                '''
            }
        }
    }
}