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
                docker run --rm -v $PWD:/app -w /app maven:3.9-eclipse-temurin-21 mvn clean package -DskipTests -f UserService/pom.xml
                docker run --rm -v $PWD:/app -w /app maven:3.9-eclipse-temurin-21 mvn clean package -DskipTests -f MovieService/pom.xml
                docker run --rm -v $PWD:/app -w /app maven:3.9-eclipse-temurin-21 mvn clean package -DskipTests -f WatchlistService/pom.xml
                docker run --rm -v $PWD:/app -w /app maven:3.9-eclipse-temurin-21 mvn clean package -DskipTests -f JournalService/pom.xml
                docker run --rm -v $PWD:/app -w /app maven:3.9-eclipse-temurin-21 mvn clean package -DskipTests -f AuthenticationService/pom.xml
                docker run --rm -v $PWD:/app -w /app maven:3.9-eclipse-temurin-21 mvn clean package -DskipTests -f NotificationService/pom.xml
                docker run --rm -v $PWD:/app -w /app maven:3.9-eclipse-temurin-21 mvn clean package -DskipTests -f CompanionService/pom.xml
                docker run --rm -v $PWD:/app -w /app maven:3.9-eclipse-temurin-21 mvn clean package -DskipTests -f AI-Service/pom.xml
                docker run --rm -v $PWD:/app -w /app maven:3.9-eclipse-temurin-21 mvn clean package -DskipTests -f ChatService/pom.xml
                docker run --rm -v $PWD:/app -w /app maven:3.9-eclipse-temurin-21 mvn clean package -DskipTests -f ApiGateway/pom.xml
                docker run --rm -v $PWD:/app -w /app maven:3.9-eclipse-temurin-21 mvn clean package -DskipTests -f ServiceRegistry/pom.xml
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
                docker-compose down || true
                docker-compose up -d
                '''
            }
        }
    }
}