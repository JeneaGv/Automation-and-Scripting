pipeline {
    agent {
        label 'ssh-agent'
    }

    tools {
        git 'Default'
    }
    
    stages {
        stage('Checkout Project') {
            steps {
                git url: 'https://github.com/JeneaGv/containers08', 
                    credentialsId: 'github-ssh-credential' 
            }
        }
        
        stage('Install Dependencies') {
            steps {
                sh 'composer install --no-dev --prefer-dist' 
            }
        }

        stage('Run Unit Tests') {
            steps {
                sh '~/.config/composer/vendor/bin/phpunit --log-junit reports/junit.xml'
            }
        }
        
        stage('Report Test Results') {
            steps {
                junit 'reports/junit.xml'
            }
        }
    }
}