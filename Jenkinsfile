pipeline {
    agent any

    tools {
        nodejs 'node-lts'
        maven  'MAVEN3'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Agent Info') {
            steps {
                script {
                    if (isUnix()) {
                        sh 'uname -a || true'
                        sh 'node --version || true'
                        sh 'npm --version || true'
                        sh 'mvn -v || true'
                        sh 'docker --version || true'
                        sh 'docker-compose --version || true'
                    } else {
                        bat 'ver'
                        bat 'node --version || echo node-not-found'
                        bat 'npm --version || echo npm-not-found'
                        bat 'mvn -v || echo maven-not-found'
                        bat 'docker --version || echo docker-not-found'
                        bat 'docker-compose --version || echo docker-compose-not-found'
                    }
                }
            }
        }

        stage('Build Frontend') {
            steps {
                dir('RevHub') {
                    script {
                        if (isUnix()) {
                            sh 'npm --version'
                            sh 'npm install'
                            sh 'npm run build'
                        } else {
                            bat 'npm --version'
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
                        sh 'docker-compose down || true'
                        sh 'docker-compose up -d --build'
                    } else {
                        bat 'docker-compose down || echo docker-compose-down-failed'
                        bat 'docker-compose up -d --build || echo docker-compose-up-failed'
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
