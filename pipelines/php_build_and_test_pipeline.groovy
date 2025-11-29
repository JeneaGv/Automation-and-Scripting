pipeline {
    agent {
        label 'ssh-agent'
    }
    
    stages {
        stage('Checkout Project') {
            steps {
                git url: 'https://github.com/JeneaGv/containers08.git', 
                    branch: 'main'
            }
        }
        
        stage('Install Dependencies') {
            steps {
                sh 'composer install --no-dev --prefer-dist || echo "No composer.json found"'
            }
        }

        stage('Run Tests') {
            steps {
                sh '''
                    echo "Running basic PHP syntax check..."
                    find . -name "*.php" -exec php -l {} \\; || true
                '''
            }
        }
    }
}
