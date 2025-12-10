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

        stage('Build Frontend') {
            steps {
                dir('RevHub') {
                    script {
                        if (isUnix()) {
                            sh 'npm ci'
                            sh 'npm run build -- --configuration=production'
                        } else {
                            bat 'npm ci'
                            bat 'npm run build -- --configuration=production'
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
                            sh 'mvn -B -DskipTests clean package'
                        } else {
                            bat 'mvn -B -DskipTests clean package'
                        }
                    }
                }
            }
        }

        stage('Docker Build') {
            steps {
                script {
                    if (isUnix()) {
                        sh 'docker-compose build'
                    } else {
                        bat 'docker-compose build'
                    }
                }
            }
        }

        stage('Docker Deploy') {
            steps {
                script {
                    if (isUnix()) {
                        sh 'docker-compose down'
                        sh 'docker-compose up -d'
                    } else {
                        bat 'docker-compose down'
                        bat 'docker-compose up -d'
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
