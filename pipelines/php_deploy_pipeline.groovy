pipeline {
    agent { label 'ansible-agent' }
    stages {
        stage('Clone PHP App') {
            steps {
                git branch: 'main', url: 'https://github.com/JeneaGv/containers08'
            }
        }
        stage('Deploy to Test Server') {
            steps {
                sh '''
                export ANSIBLE_HOST_KEY_CHECKING=False
                echo "[webservers]\ntest-server ansible_host=test-server ansible_user=ansible" > hosts.ini
                
                ansible webservers -i hosts.ini -m synchronize -a "src=. dest=/var/www/html/ delete=yes rsync_opts='--exclude=.git' mode=push" --become
                '''
            }
        }
    }
}