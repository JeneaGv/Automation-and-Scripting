# Configurarea Jenkins pentru Automatizarea Sarcinilor DevOps

# Obiectiv 

Învață cum să configurezi Jenkins pentru automatizarea sarcinilor DevOps, incluzând crearea și gestionarea pipeline-urilor CI/CD

# Configurarea Controlerului Jenkins

Adaugă configurația serviciului Controlerului Jenkins la fișierul docker-compose.yml:

```
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

volumes:
  jenkins_home:
  jenkins_agent_volume:

networks:
  jenkins-network:
    driver: bridge
```

# Configurarea Agentului SSH

Creează un dosar (director) numit secrets la rădăcina proiectului tău și adaugă cheile SSH necesare pentru conectarea la serverele la distanță (remote).

```
mkdir secrets
cd secrets
ssh-keygen -t rsa -b 4096 -f jenkins_agent_ssh_key -m PEM
```

Creaza un fișier Dockerfile pentru un agent SSH cu urmatorul conținut 

```
FROM jenkins/ssh-agent

RUN apt-get update && apt-get install -y php-cli
```

Adaugă configurația serviciului Agentului SSH la fișierul docker-compose.yml:

```
ssh-agent:

    build:
      context: .
      dockerfile: Dockerfile
    container_name: ssh-agent
    environment:
      - JENKINS_AGENT_SSH_PUBKEY=${JENKINS_AGENT_SSH_PUBKEY}
    volumes:
      - jenkins_agent_volume:/home/jenkins/agent
    depends_on:
      - jenkins-controller
    networks:
      - jenkins-network
```

Creează un fișier .env la rădăcina proiectului tău și adaugă variabila de mediu JENKINS_AGENT_SSH_PUBKEY

<img width="1135" height="57" alt="image" src="https://github.com/user-attachments/assets/bcdbecf4-4b37-40a7-b9f7-6c7c9969ac09" />

Repornește proiectul Docker Compose pentru a aplica modificările.

# Conectarea Agentului SSH la Jenkins

Verificarea și Instalarea Plugin-ului

Verifică dacă "SSH Agents Plugin" este instalat în Jenkins. Dacă nu este, instalează-l navigând la: Manage Jenkins > Manage Plugins (Administrare Jenkins > Administrare Plugin-uri).

<img width="1384" height="232" alt="image" src="https://github.com/user-attachments/assets/a62b2c6c-bf45-4a62-a733-d6ee8f2169fd" />

Înregistrarea Cheilor SSH în Jenkins

Autentifică-te în interfața web Jenkins la adresa http://localhost:8080.

Navighează la: Manage Jenkins > Manage Credentials (Administrare Jenkins > Administrare Credențiale).

Adaugă o cheie SSH nouă, setând numele de utilizator (username) la jenkins și selectând cheia privată corespunzătoare din dosarul secrets.

<img width="590" height="403" alt="image" src="https://github.com/user-attachments/assets/840ed31e-ffd3-4f78-8b63-bc83c403d0a9" />

Adăugarea unui Nod Agent Jenkins

Navighează la: Manage Jenkins > Manage Nodes and Clouds > New Node (Administrare Jenkins > Administrare Noduri și Cloud-uri > Nod Nou).

Numește nodul ssh-agent1 și selectează tipul Permanent Agent (Agent Permanent).

Adaugă eticheta (label) php-agent pentru nod.

Configurează nodul specificând:

Remote root directory (Director rădăcină la distanță): /home/jenkins/agent

Launch method (Metodă de lansare): Launch agents via SSH (Lansează agenți prin SSH)

Host (Gazdă): ssh-agent

Credentials (Credențiale): selectează cheia SSH adăugată anterior.

<img width="612" height="243" alt="image" src="https://github.com/user-attachments/assets/f3db954c-9446-4fe9-a425-146753604802" />

<img width="588" height="401" alt="image" src="https://github.com/user-attachments/assets/0bf046c7-d8c0-4b81-b828-4d3d67c1c57a" />

# Crearea unui Pipeline Jenkins pentru Automatizarea Sarcinilor DevOps

Alege un repository (depozit) cu un proiect PHP pe GitHub (de exemplu, unul din cursurile "Programare PHP" sau "Virtualizare și Containerizare").

Proiectul trebuie să conțină teste unitare.

Am ales proiectul in care sa lucrat cu GitHub Actions https://github.com/JeneaGv/containers08

<img width="1242" height="718" alt="image" src="https://github.com/user-attachments/assets/c5ae84ce-5d4d-46bf-b165-ff992b365fa7" />

Creează un pipeline Jenkins nou folosind următorul fișier Jenkinsfile(care lam adaugat la proiectul existent):

```
pipeline {
    agent {
        label 'php-agent'
    }
    
    stages {        
        stage('Install Dependencies') {
            steps {
                // Project preparation (install dependencies if needed)
                echo 'Preparing project...'
                // Add project-specific commands here
            }
        }
        
        stage('Test') {
            steps {
                // Running tests
                echo 'Running tests...'
                // Add commands to run your tests here
            }
        }
    }
    
    post {
        always {
            echo 'Pipeline completed.'
        }
        success {
            echo 'All stages completed successfully!'
        }
        failure {
            echo 'Errors detected in the pipeline.'
        }
    }
}
```

Verifică dacă pipeline-ul se execută cu succes și că testele unitare trec.

<img width="1572" height="445" alt="image" src="https://github.com/user-attachments/assets/c719b35c-22ed-47d0-b2c3-26f731492e08" />

<img width="940" height="244" alt="image" src="https://github.com/user-attachments/assets/69d86398-eb8f-4b99-a81a-5cafcd236d40" />

# Raspunsuri la intrebari 

1.Avantajele utilizării Jenkins pentru automatizarea sarcinilor DevOps:

Principalele avantaje sunt: Automatizarea CI/CD: Asigură integrarea continuă (CI) și livrarea/implementarea continuă (CD), reducând erorile manuale și accelerând ciclul de lansare. 

Scalabilitate și Distribuție: Poate distribui sarcinile pe mai mulți agenți, permițând rularea testelor și a build-urilor în paralel pentru proiecte mari. 

Extensibilitate: Are o comunitate vastă și mii de plugin-uri care îi permit să se integreze cu aproape orice instrument DevOps (Git, Docker, Kubernetes, etc.). 

Open Source și Maturitate: Este un instrument gratuit, bine stabilit și larg adoptat în industrie.

2.Alte tipuri de Agenți Jenkins

Jenkins oferă următoarele tipuri de agenți:

Agenți Permanenți (care lam folosit in cadrul laboratorului)

Agenți Dinamici (Agenți la Cerere)

3.Probleme întâmpinate la configurarea Jenkins și soluțiile lor

Greseala de atenție căci n-am adaugat deplin fișierul cu cheia SSH din fișierul jenkins_agent_ssh_key dar,doar cheia.

Această greșeala am rezolvat analizând fișierul de loguri.

În rest alte probleme nu s-au întâlnit

