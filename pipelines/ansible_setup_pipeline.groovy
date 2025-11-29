pipeline {
    agent {
        label 'ansible-agent'
    }

    stages {
        stage('Checkout Ansible Playbooks') {
            steps {
                git url: 'https://github.com/JeneaGv/Automation-and-Scripting.git', 
                    branch: 'lab5',
                    credentialsId: ''
                    
                sh 'ls -R'
            }
        }

        stage('Setup Test Server with Ansible') {
            steps { 
                sh 'ansible-playbook -i ansible/hosts.ini ansible/setup_test_server.yml'
            }
        }
    }
}