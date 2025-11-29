pipeline {
    agent { label 'ansible-agent' }

    stages {
        stage('Clone PHP Application') {
            steps {
                git branch: 'main', url: 'https://github.com/JeneaGv/containers08.git'
            }
        }

        stage('Deploy Files via Synchronize (SSH)') {
            steps {
                sh '''
                echo "--- Preparing deployment environment ---"
                
                
                export ANSIBLE_HOST_KEY_CHECKING=False

                
                echo "[webservers]\\ntest-server ansible_host=test-server ansible_user=ansible" > hosts.ini

               
                echo "Files to be copied:"
                ls -la site/

                echo "--- Starting deployment using Ansible synchronize ---"

               
                ansible webservers -i hosts.ini -m synchronize -a "src=site/ dest=/var/www/html/ delete=yes rsync_opts=--exclude=.git" --become
                
                
                echo "--- Setting permissions and verifying deployment ---"
                
                
                ansible webservers -i hosts.ini -m shell -a "chown -R www-data:www-data /var/www/html/ && service apache2 restart" --become
                '''
            }
        }
    }
    
   
    post {
        always {
            cleanWs()
        }
        failure {
             echo "Deployment failed."
        }
        success {
             echo "Deployment successful."
        }
    }
}
