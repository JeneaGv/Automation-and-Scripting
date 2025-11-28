pipeline {
    agent {
        label 'ansible-agent'
    }
    
    stages {
        stage('Checkout Project') {
            steps {
                git url: 'https://github.com/JeneaGv/containers08', 
                    credentialsId: 'github-ssh-credential' 
            }
        }

        stage('Deploy Project to Test Server') {
            steps {
                sh """
                  cat > deploy_temp.yml <<EOF
                  - name: Deploy PHP Project
                    hosts: test_servers
                    become: yes
                    tasks:
                      - name: Copy project files
                        ansible.builtin.copy:
                          src: .
                          dest: /var/www/html/php_project
                          owner: ansible
                          group: ansible
                          mode: '0755'
                          # The 'remote_src' parameter can be used if files are on a different remote
                          # but here we copy from the Jenkins workspace (Ansible Agent)
                          
                      - name: Configure project (if needed, e.g., symlinks, database)
                        ansible.builtin.file:
                          path: /var/www/html/php_project/config/settings.php
                          state: touch
                          owner: ansible
                          group: ansible
                          mode: '0644'
                  EOF
                  ansible-playbook -i ansible/hosts.ini deploy_temp.yml
                """
            }
        }
    }
}