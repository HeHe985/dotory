pipeline {
    agent any

    environment {
        COMPOSE_FILE = 'infra/docker-compose.prod.yml'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Create env file') {
            steps {
                sh '''
                cat > .env.prod <<EOF
MYSQL_DATABASE=dotory_prod
MYSQL_USER=dotory
MYSQL_PASSWORD=${MYSQL_PASSWORD}
MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD}

DB_USERNAME=dotory
DB_PASSWORD=${MYSQL_PASSWORD}

SPRING_PROFILES_ACTIVE=prod
EOF
                '''
            }
        }

        stage('Deploy') {
            steps {
                sh '''
                docker compose -f $COMPOSE_FILE down
                docker compose -f $COMPOSE_FILE up -d --build
                '''
            }
        }

        stage('Clean') {
            steps {
                sh 'docker image prune -f'
            }
        }
    }
}