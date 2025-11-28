pipeline {
    agent {
        label 'ansible-agent'
    }

    stages {
        stage('Checkout Ansible Playbooks') {
            steps {
                git url: 'git@github.com:your-repo/ansible-playbooks.git', 
                    credentialsId: 'github-ssh-credential'
            }
        }

        stage('Setup Test Server with Ansible') {
            steps { 
                sh 'ansible-playbook -i ansible/hosts.ini ansible/setup_test_server.yml'
            }
        }
    }
}