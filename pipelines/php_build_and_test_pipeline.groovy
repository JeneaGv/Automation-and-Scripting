pipeline {
    agent {
        # Use the SSH Agent service defined in compose.yaml
        label 'ssh-agent'
    }
    
    # Credential ID for the SSH key configured in Jenkins for the PHP Agent
    # This assumes the Git repository is private and requires SSH access.
    # The SSH Agent plugin will load this key for use by 'git' inside the agent.
    tools {
        git 'Default'
    }
    
    stages {
        stage('Checkout Project') {
            steps {
                # Replace with your actual project repository URL
                git url: 'git@github.com:your-repo/php-project.git', 
                    credentialsId: 'github-ssh-credential' 
            }
        }
        
        stage('Install Dependencies') {
            steps {
                # Composer is globally installed in the ssh-agent container
                sh 'composer install --no-dev --prefer-dist' 
            }
        }

        stage('Run Unit Tests') {
            steps {
                # PHPUnit is globally installed in the ssh-agent container
                sh '~/.config/composer/vendor/bin/phpunit --log-junit reports/junit.xml'
            }
        }
        
        stage('Report Test Results') {
            steps {
                # Publish JUnit test results
                junit 'reports/junit.xml'
            }
        }
    }
}