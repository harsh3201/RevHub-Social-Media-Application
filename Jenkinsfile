pipeline {
    agent any

    environment {
        COMPOSE_PROJECT_NAME = "revhub_cicd"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Deploy') {
            steps {
                script {
                    if (isUnix()) {
                        sh 'docker-compose down --rmi local --remove-orphans'
                        sh 'docker-compose up -d --build'
                    } else {
                        // Windows Environment
                        bat 'docker-compose down --rmi local --remove-orphans'
                        bat 'docker-compose up -d --build'
                    }
                }
            }
        }
        
        stage('Health Check') {
            steps {
                script {
                    sleep 30 // Wait for containers to initialize
                    if (isUnix()) {
                        sh 'docker ps'
                    } else {
                        bat 'docker ps'
                    }
                }
            }
        }
    }

    post {
        always {
            cleanWs()
            // Optional: Prune builder cache to save space, but might slow down future builds
            // script {
            //    sh 'docker builder prune -f'
            // }
        }
        failure {
             echo 'Deployment Failed. Check logs.'
             // Capture logs for debugging
             script {
                 if (isUnix()) {
                     sh 'docker-compose logs'
                 } else {
                     bat 'docker-compose logs'
                 }
             }
        }
    }
}
