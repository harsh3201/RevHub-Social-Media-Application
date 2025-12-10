pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Frontend') {
            steps {
                dir('RevHub') {
                    script {
                        if (isUnix()) {
                            sh 'npm install'
                            sh 'npm run build'
                        } else {
                            bat 'npm install'
                            bat 'npm run build'
                        }
                    }
                }
            }
        }

        stage('Build Backend') {
            steps {
                dir('revHubBack') {
                    script {
                        if (isUnix()) {
                            // Using Maven Wrapper if available, else mvn
                            if (fileExists('mvnw')) {
                                sh './mvnw clean package -DskipTests'
                            } else {
                                sh 'mvn clean package -DskipTests'
                            }
                        } else {
                            if (fileExists('mvnw.cmd')) {
                                bat 'mvnw.cmd clean package -DskipTests'
                            } else {
                                bat 'mvn clean package -DskipTests'
                            }
                        }
                    }
                }
            }
        }

        stage('Docker Deploy') {
            steps {
                script {
                    if (isUnix()) {
                        sh 'docker-compose down'
                        sh 'docker-compose up -d --build'
                    } else {
                        bat 'docker-compose down'
                        bat 'docker-compose up -d --build'
                    }
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
    }
}
