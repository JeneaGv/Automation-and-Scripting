pipeline {
    agent {
        label 'ansible-agent'
    }
    
    stages {
        stage('Checkout Ansible Config') {
            steps {
                dir('ansible-config') {
                    git url: 'https://github.com/JeneaGv/Automation-and-Scripting.git', 
                        branch: 'lab5'
                }
            }
        }
        
        stage('Checkout PHP Application') {
            steps {
                dir('php-app') {
                    git url: 'https://github.com/JeneaGv/containers08.git', 
                        branch: 'main'
                }
            }
        }

        stage('Deploy Project to Test Server') {
            steps {
                sh '''
                  cat > /tmp/deploy_temp.yml <<'EOF'
---
- name: Deploy PHP Project
  hosts: test_servers
  become: yes
  tasks:
    - name: Ensure project directory exists
      ansible.builtin.file:
        path: /var/www/html/php_project
        state: directory
        owner: www-data
        group: www-data
        mode: '0755'

    - name: Copy site directory contents
      ansible.builtin.copy:
        src: php-app/site/
        dest: /var/www/html/php_project/
        owner: www-data
        group: www-data
        mode: '0755'

    - name: Restart Apache
      ansible.builtin.service:
        name: apache2
        state: restarted
EOF
                  # Ajustează path-ul către hosts.ini în funcție de structură
                  ansible-playbook -i ansible-config/lab05/ansible/hosts.ini /tmp/deploy_temp.yml
                '''
            }
        }
    }
}
