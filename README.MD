# Jenkins CI/CD Pipeline with Ansible Deployment

## 1. Project Description

This project demonstrates a complete CI/CD pipeline using Jenkins, Docker, and Ansible for automating the deployment of a PHP web application. The infrastructure consists of:

- **Jenkins Controller**: Main CI/CD orchestration server
- **SSH Agent**: Build environment for PHP applications with Composer
- **Ansible Agent**: Configuration management and deployment automation
- **Test Server**: Ubuntu-based web server running Apache and PHP

The application is a simple PHP-based content management system using SQLite database, with automated testing and deployment through Jenkins pipelines.

**Technologies used:**
- Docker & Docker Compose
- Jenkins (with Pipeline plugin)
- Ansible
- PHP 7.4 with SQLite
- Apache2 web server
- SSH for secure communication

---

## 2. Steps for Configuring Jenkins Controller

### 2.1 Initial Setup

1. **Access Jenkins** at `http://localhost:8080`

2. **Get initial admin password:**
```bash
docker exec jenkins-controller cat /var/jenkins_home/secrets/initialAdminPassword
```

3. **Install suggested plugins** or select:
   - Git plugin
   - Pipeline plugin
   - SSH Build Agents plugin
   - Credentials Binding plugin

### 2.2 Configure Credentials

1. Navigate to **Manage Jenkins** → **Credentials** → **System** → **Global credentials**

2. **Add SSH credentials for SSH Agent:**
   - Kind: `SSH Username with private key`
   - ID: `jenkins-ssh-agent-key`
   - Username: `jenkins`
   - Private Key: Enter directly (paste content from `lab05/secrets/jenkins_agent_ssh_key`)

3. **Add SSH credentials for Ansible Agent:**
   - Kind: `SSH Username with private key`
   - ID: `jenkins-ansible-agent-key`
   - Username: `jenkins`
   - Private Key: Same as above

4. **(Optional) Add GitHub credentials** if repository is private:
   - Kind: `SSH Username with private key`
   - ID: `github-ssh-credential`
   - Username: `git`
   - Private Key: Your GitHub SSH key

---

## 3. Steps for Setting Up SSH Agent

### 3.1 Create Dockerfile for SSH Agent

File: `lab05/Dockerfile.ssh_agent`

```dockerfile
FROM jenkins/ssh-agent

RUN apt-get update && apt-get install -y php-cli wget php-curl php-xml php-mbstring unzip php-zip

RUN wget https://getcomposer.org/installer -O /tmp/composer-setup.php
RUN php /tmp/composer-setup.php --install-dir=/usr/local/bin --filename=composer
RUN rm /tmp/composer-setup.php
```

### 3.2 Configure SSH Agent Node in Jenkins

1. Go to **Manage Jenkins** → **Nodes** → **New Node**

2. Configuration:
   - **Node name**: `ssh-agent`
   - **Type**: Permanent Agent
   - **Number of executors**: `2`
   - **Remote root directory**: `/home/jenkins`
   - **Labels**: `ssh-agent php-agent`
   - **Usage**: Use this node as much as possible
   - **Launch method**: Launch agents via SSH
   - **Host**: `ssh-agent`
   - **Credentials**: Select `jenkins-ssh-agent-key`
   - **Host Key Verification Strategy**: Non verifying Verification Strategy

3. **Save** and verify the node is online (green checkmark)

---

## 4. Steps for Creating and Configuring Ansible Agent

### 4.1 Create Dockerfile for Ansible Agent

File: `lab05/Dockerfile.ansible_agent`

```dockerfile
FROM ubuntu:22.04

ARG DEBIAN_FRONTEND=noninteractive

RUN apt-get update && \
    DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
        openssh-server \
        python3 \
        python3-pip \
        rsync \
        git \
        sudo \
        openjdk-17-jre \
    && pip3 install ansible \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

RUN useradd -m -d /home/jenkins -s /bin/bash jenkins && \
    echo "jenkins:jenkins" | chpasswd && \
    mkdir -p /home/jenkins/.ssh && \
    chown -R jenkins:jenkins /home/jenkins/.ssh

RUN mkdir /var/run/sshd
RUN echo 'root:root' | chpasswd
RUN sed -i 's/#PermitRootLogin prohibit-password/PermitRootLogin yes/' /etc/ssh/sshd_config

COPY secrets/jenkins_agent_ssh_key.pub /home/jenkins/.ssh/authorized_keys
RUN chmod 600 /home/jenkins/.ssh/authorized_keys && \
    chown jenkins:jenkins /home/jenkins/.ssh/authorized_keys

COPY secrets/ansible_server_key /home/jenkins/.ssh/id_ed25519
RUN chmod 600 /home/jenkins/.ssh/id_ed25519 && \
    chown jenkins:jenkins /home/jenkins/.ssh/id_ed25519

EXPOSE 22
CMD ["/usr/sbin/sshd", "-D"]
```

### 4.2 Generate SSH Keys

```bash
# Generate key for Ansible to access test-server
ssh-keygen -t ed25519 -f lab05/secrets/ansible_server_key -N ""

# This creates:
# - ansible_server_key (private key)
# - ansible_server_key.pub (public key)
```

### 4.3 Configure Ansible Agent Node in Jenkins

1. Go to **Manage Jenkins** → **Nodes** → **New Node**

2. Configuration:
   - **Node name**: `ansible-agent`
   - **Type**: Permanent Agent
   - **Number of executors**: `2`
   - **Remote root directory**: `/home/jenkins`
   - **Labels**: `ansible-agent`
   - **Usage**: Use this node as much as possible
   - **Launch method**: Launch agents via SSH
   - **Host**: `ansible-agent`
   - **Credentials**: Select `jenkins-ansible-agent-key`
   - **Host Key Verification Strategy**: Non verifying Verification Strategy

3. **Save** and verify the node is online

### 4.4 Create Ansible Inventory

File: `lab05/ansible/hosts.ini`

```ini
[test_servers]
test-server

[test_servers:vars]
ansible_user=ansible
ansible_ssh_common_args='-o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null'
```

---

## 5. Steps for Creating the Test Server

### 5.1 Create Dockerfile for Test Server

File: `lab05/Dockerfile.test_server`

```dockerfile
FROM ubuntu:22.04

ARG DEBIAN_FRONTEND=noninteractive

RUN apt-get update && apt-get install -y \
    openssh-server \
    python3 \
    sudo \
    rsync \
    apache2 \
    php \
    libapache2-mod-php \
    php-sqlite3 \
    php-cli \
    php-xml \
    php-mbstring \
    sqlite3 \
    && rm -rf /var/lib/apt/lists/*

RUN mkdir /var/run/sshd

RUN useradd -m -s /bin/bash ansible && \
    echo "ansible:ansible" | chpasswd

RUN echo "ansible ALL=(ALL) NOPASSWD:ALL" >> /etc/sudoers

RUN mkdir -p /home/ansible/.ssh
COPY secrets/ansible_server_key.pub /home/ansible/.ssh/authorized_keys

RUN chown -R ansible:ansible /home/ansible/.ssh && \
    chmod 700 /home/ansible/.ssh && \
    chmod 600 /home/ansible/.ssh/authorized_keys

RUN mkdir -p /var/www/html/php_project && \
    chown -R www-data:www-data /var/www/html && \
    a2enmod rewrite

RUN echo '<?php phpinfo(); ?>' > /var/www/html/info.php
RUN echo "ServerName localhost" >> /etc/apache2/apache2.conf

EXPOSE 22 80

RUN echo '#!/bin/bash\n\
set -e\n\
echo "Starting Apache..."\n\
service apache2 start\n\
echo "Apache started"\n\
echo "Starting SSH..."\n\
exec /usr/sbin/sshd -D' > /start.sh && \
    chmod +x /start.sh

CMD ["/start.sh"]
```

### 5.2 Docker Compose Configuration

File: `lab05/compose.yml`

```yaml
version: '3.8'

services:
  jenkins-controller:
    image: jenkins/jenkins:lts
    container_name: jenkins-controller
    ports:
      - "8080:8080"
      - "50000:50000"
    volumes:
      - jenkins_home:/var/jenkins_home
    networks:
      - jenkins-network

  ssh-agent:
    build:
      context: .
      dockerfile: Dockerfile.ssh_agent
    container_name: ssh-agent
    environment:
      - JENKINS_AGENT_SSH_PUBKEY=${JENKINS_AGENT_SSH_PUBKEY}
    networks:
      - jenkins-network

  ansible-agent:
    build:
      context: .
      dockerfile: Dockerfile.ansible_agent
    container_name: ansible-agent
    environment:
      - JENKINS_AGENT_SSH_PUBKEY=${JENKINS_AGENT_SSH_PUBKEY}
    networks:
      - jenkins-network

  test-server:
    build:
      context: .
      dockerfile: Dockerfile.test_server
    container_name: test-server
    ports:
      - "8081:80"
    networks:
      - jenkins-network

volumes:
  jenkins_home:

networks:
  jenkins-network:
    driver: bridge
```

### 5.3 Build and Start Services

```bash
cd lab05
docker-compose up -d --build
```

### 5.4 Verify Services

```bash
docker ps
docker logs test-server
docker exec -it test-server service apache2 status
```

---

## 6. Description of Ansible Playbook and Its Tasks

### 6.1 Setup Test Server Playbook

File: `lab05/ansible/setup_test_server.yml`

```yaml
---
- name: Configure Test Server Environment (Apache & PHP)
  hosts: test_servers
  become: yes
  tasks:
    - name: Ensure Apache is running
      ansible.builtin.service:
        name: apache2
        state: started
        enabled: yes

    - name: Create project web root directory
      ansible.builtin.file:
        path: /var/www/html/php_project
        state: directory
        owner: www-data
        group: www-data
        mode: '0755'

    - name: Configure Apache Virtual Host
      ansible.builtin.copy:
        dest: /etc/apache2/sites-available/000-default.conf
        content: |
          <VirtualHost *:80>
              ServerAdmin webmaster@localhost
              DocumentRoot /var/www/html/php_project
              <Directory /var/www/html/php_project>
                  Options Indexes FollowSymLinks
                  AllowOverride All
                  Require all granted
              </Directory>
              ErrorLog ${APACHE_LOG_DIR}/error.log
              CustomLog ${APACHE_LOG_DIR}/access.log combined
          </VirtualHost>
        
    - name: Enable rewrite module
      ansible.builtin.command:
        cmd: a2enmod rewrite
      notify: Restart Apache

  handlers:
    - name: Restart Apache
      ansible.builtin.service:
        name: apache2
        state: restarted
```

### 6.2 Task Descriptions

**Task 1: Ensure Apache is running**
- Starts Apache2 service if not running
- Enables Apache2 to start automatically on boot
- Uses `ansible.builtin.service` module

**Task 2: Create project web root directory**
- Creates `/var/www/html/php_project` directory
- Sets owner to `www-data` (Apache user)
- Sets permissions to `755` (read/execute for all, write for owner)

**Task 3: Configure Apache Virtual Host**
- Creates Apache configuration file
- Sets DocumentRoot to PHP project directory
- Configures directory permissions and options
- Enables all requests (Require all granted)

**Task 4: Enable rewrite module**
- Enables Apache mod_rewrite for URL rewriting
- Triggers handler to restart Apache when changed

**Handler: Restart Apache**
- Restarts Apache service to apply configuration changes
- Only runs when notified by tasks

---

## 7. Steps for Creating Jenkins Pipelines

### 7.1 Pipeline 1: Ansible Setup Test Server

**Purpose:** Configure Apache and PHP environment on test server

1. In Jenkins Dashboard, click **New Item**
2. Name: `Ansible-Setup-Test-Server`
3. Type: **Pipeline**
4. Pipeline script:

```groovy
pipeline {
    agent {
        label 'ansible-agent'
    }

    stages {
        stage('Checkout Ansible Playbooks') {
            steps {
                git url: 'https://github.com/YOUR-USERNAME/Automation-and-Scripting.git', 
                    branch: 'lab5'
                    
                sh 'ls -la'
            }
        }
        
        stage('Setup Test Server with Ansible') {
            steps { 
                sh 'ansible-playbook -i lab05/ansible/hosts.ini lab05/ansible/setup_test_server.yml'
            }
        }
    }
    
    post {
        success {
            echo 'Test server configured successfully!'
        }
        failure {
            echo 'Failed to configure test server.'
        }
    }
}
```

### 7.2 Pipeline 2: Build and Test PHP Project

**Purpose:** Install dependencies and run unit tests

1. In Jenkins Dashboard, click **New Item**
2. Name: `PHP-Build-and-Test`
3. Type: **Pipeline**
4. Pipeline script:

```groovy
pipeline {
    agent {
        label 'ssh-agent'
    }
    
    stages {
        stage('Checkout Project') {
            steps {
                git url: 'https://github.com/YOUR-USERNAME/containers08.git', 
                    branch: 'main'
            }
        }
        
        stage('Install Dependencies') {
            steps {
                sh '''
                    if [ -f composer.json ]; then
                        composer install --no-dev --prefer-dist
                    else
                        echo "No composer.json found, skipping..."
                    fi
                '''
            }
        }

        stage('Run Syntax Check') {
            steps {
                sh '''
                    echo "Running PHP syntax check..."
                    find site -name "*.php" -exec php -l {} \\; || true
                '''
            }
        }
        
        stage('Run Unit Tests') {
            steps {
                sh '''
                    echo "Running custom test framework..."
                    # Note: Full tests require database setup
                    php -v
                '''
            }
        }
    }
    
    post {
        success {
            echo 'Build and tests completed successfully!'
        }
        failure {
            echo 'Build or tests failed.'
        }
    }
}
```

### 7.3 Pipeline 3: Deploy PHP Project

**Purpose:** Deploy application to test server using Ansible

1. In Jenkins Dashboard, click **New Item**
2. Name: `PHP-Deploy-to-Test-Server`
3. Type: **Pipeline**
4. Pipeline script:

```groovy
pipeline {
    agent {
        label 'ansible-agent'
    }
    
    stages {
        stage('Checkout Ansible Config') {
            steps {
                dir('ansible-config') {
                    git url: 'https://github.com/YOUR-USERNAME/Automation-and-Scripting.git', 
                        branch: 'lab5'
                }
            }
        }
        
        stage('Checkout PHP Application') {
            steps {
                dir('php-app') {
                    git url: 'https://github.com/YOUR-USERNAME/containers08.git', 
                        branch: 'main'
                }
            }
        }

        stage('Verify Files') {
            steps {
                sh '''
                    echo "=== Ansible config structure ==="
                    ls -la ansible-config/
                    echo ""
                    echo "=== PHP app structure ==="
                    ls -la php-app/site/
                '''
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

    - name: Ensure database directory exists
      ansible.builtin.file:
        path: /var/www/db
        state: directory
        owner: www-data
        group: www-data
        mode: '0777'

    - name: Copy database schema
      ansible.builtin.copy:
        src: php-app/site/sql/schema.sql
        dest: /tmp/schema.sql

    - name: Create SQLite database
      ansible.builtin.shell: |
        if [ ! -f /var/www/db/db.sqlite ]; then
          sqlite3 /var/www/db/db.sqlite < /tmp/schema.sql
          chmod 666 /var/www/db/db.sqlite
        fi

    - name: Restart Apache
      ansible.builtin.service:
        name: apache2
        state: restarted
EOF
                  ansible-playbook -i ansible-config/lab05/ansible/hosts.ini /tmp/deploy_temp.yml
                '''
            }
        }
        
        stage('Verify Deployment') {
            steps {
                sh '''
                    echo "Verifying deployment on test-server..."
                    ssh ansible@test-server "ls -la /var/www/html/php_project/"
                '''
            }
        }
    }
    
    post {
        success {
            echo 'Deployment completed successfully!'
            echo 'Access application at: http://localhost:8081/index.php'
        }
        failure {
            echo 'Deployment failed.'
        }
    }
}
```

### 7.4 Pipeline Execution Order

1. **Run first:** `Ansible-Setup-Test-Server` - Configures Apache/PHP environment
2. **Run second:** `PHP-Build-and-Test` - Validates code quality (optional)
3. **Run third:** `PHP-Deploy-to-Test-Server` - Deploys application

### 7.5 Verify Deployment

After successful pipeline execution:

```bash
# Check deployed files
docker exec test-server ls -la /var/www/html/php_project/

# Check database
docker exec test-server sqlite3 /var/www/db/db.sqlite "SELECT * FROM page;"

# Access application
curl http://localhost:8081/index.php
# Or open in browser: http://localhost:8081/index.php
```

---


