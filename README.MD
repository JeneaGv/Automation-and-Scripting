# Laborator 05: Automatizarea Configurării Serverului cu Ansible și Jenkins

## 1. Descrierea Proiectului
Acest laborator extinde pipeline-ul CI/CD din lucrările anterioare prin integrarea Ansible pentru gestionarea configurației (Configuration Management). Obiectivul principal a fost automatizarea completă a ciclului de viață al aplicației: provizionarea infrastructurii (instalare Apache/PHP), testarea codului și livrarea (deployment) pe un server dedicat.

Arhitectura proiectului utilizează Docker pentru a simula o infrastructură reală compusă din 4 servere distincte:
* **Jenkins Controller:** Serverul de automatizare.
* **SSH Agent (PHP):** Agentul care construiește și testează aplicația.
* **Ansible Agent:** Agentul care configurează serverele remote.
* **Test Server:** Serverul țintă unde rulează aplicația web.

## 2. Infrastructura și Pornirea Serviciilor
Infrastructura este definită în fișierul `compose.yaml`. Am creat imagini Docker personalizate pentru agenți (`Dockerfile.ansible_agent`) și server (`Dockerfile.test_server`) pentru a include dependențele necesare (`ansible`, `rsync`, `java`, `openssh`).

<img width="606" height="872" alt="image" src="https://github.com/user-attachments/assets/2c452bde-0a80-4265-b9b4-97c589f673bb" />

**Execuția:**
După configurare, am pornit întreaga infrastructură folosind Docker Compose. Se poate observa că toate cele 4 containere rulează simultan.

<img width="974" height="140" alt="image" src="https://github.com/user-attachments/assets/070d5943-b7f6-4638-8c79-798b5c132917" />

## 3. Gestionarea Securității (SSH)
Comunicarea între Jenkins, agenți și serverul țintă se realizează securizat prin SSH, fără parole.
* Am generat o pereche de chei pentru **Jenkins -> Agenți**.
* Am generat o pereche de chei dedicată pentru **Ansible -> Test Server**.

<img width="974" height="428" alt="image" src="https://github.com/user-attachments/assets/2909bc94-f5f3-46a7-95d0-82fbf73002fd" />

## 4. Configurarea Jenkins
Pentru a permite rularea playbook-urilor, am configurat un nou agent în Jenkins (`ansible-agent`).

1.  **Adăugarea Credențialelor:** Am introdus cheia privată SSH în Jenkins pentru a permite autentificarea automată.
    ![Credențiale Jenkins](Calea_catre_poza_cu_credentials_private_key.png)
 
    <img width="974" height="623" alt="image" src="https://github.com/user-attachments/assets/161d065c-101d-4ac3-88b3-c44c20ec9170" />

2.  **Configurarea Nodului:** Am definit agentul Ansible specificând host-ul din rețeaua Docker și directorul de lucru.

    <img width="974" height="439" alt="image" src="https://github.com/user-attachments/assets/56545696-1cc2-484f-939f-d04467792159" />

## 5. Implementarea Pipeline-urilor
Am creat trei pipeline-uri distincte pentru a separa logic etapele procesului.

### A. Pipeline 01-setup-server
Acest job folosește Ansible pentru a se conecta la `test-server`, instalează `apache2`, `php` și configurează mediul.
* **Rezultat:** Configurare reușită.

<img width="974" height="450" alt="image" src="https://github.com/user-attachments/assets/326dafe9-a82c-46fd-ac59-c760a59bb362" />

### B. Pipeline 02-build-and-test
Acest job descarcă codul sursă pe agentul PHP, instalează dependențele cu Composer și rulează testele unitare PHPUnit.
* **Rezultat:** Codul a trecut testele.

<img width="974" height="444" alt="image" src="https://github.com/user-attachments/assets/3532446a-62a6-49c1-a6ed-332fb6731ddb" />

### C. Pipeline 03-deploy
Acest job copiază fișierele validate pe serverul de producție folosind modulul `synchronize` (rsync) din Ansible.
* **Rezultat:** Fișierele au fost transferate cu succes.

<img width="974" height="445" alt="image" src="https://github.com/user-attachments/assets/f97c9212-f137-486b-9439-5b3ff6ee61ba" />

## 6. Verificarea Finală
După deployment, am accesat serverul web prin browser.
Deși accesarea directă a fișierului `Calculator.php` afișează o pagină albă (comportament corect pentru o clasă PHP fără output), lipsa erorii 404 confirmă că fișierul există pe server și serverul web funcționează.

<img width="974" height="477" alt="image" src="https://github.com/user-attachments/assets/e4035dbe-fe8c-407a-8552-a356e06331da" />

## 7. Probleme Întâmpinate și Soluții (Troubleshooting)

Pe parcursul laboratorului am rezolvat următoarele probleme tehnice:

1.  **Blocarea instalării pachetelor (`Geographic area`):**
    * *Cauză:* Comanda `apt-get` cerea interactiv fusul orar.
    * *Soluție:* Am adăugat `ARG DEBIAN_FRONTEND=noninteractive` în Dockerfile.

2.  **Eroare Java pe Ansible Agent:**
    * *Cauză:* Agentul se deconecta imediat deoarece imaginea Ubuntu nu avea Java instalat (necesar pentru Jenkins).
    * *Soluție:* Am instalat `openjdk-17-jre` în `Dockerfile.ansible_agent`.

3.  **Lipsa `rsync`:**
    * *Cauză:* Pipeline-ul de deploy eșua deoarece modulul Ansible `synchronize` necesită `rsync` pe ambele mașini.
    * *Soluție:* Am instalat pachetul `rsync` atât pe agent, cât și pe server.

4.  **Eroare "Connection Reset" în browser:**
    * *Cauză:* Serviciul Apache se oprea în container.
    * *Soluție:* Am verificat starea cu `service apache2 status` și l-am pornit manual.

5.  **Calea de Deployment:**
    * *Cauză:* Ansible a copiat întregul folder `03-deploy` în loc de conținutul acestuia.
    * *Soluție:* Am accesat aplicația folosind calea corectă: `http://localhost:8089/03-deploy/src/Calculator.php`.

## 8. Răspunsuri la Întrebări

Care sunt avantajele utilizării Ansible?**
Ansible este agentless (nu necesită instalare pe serverele țintă, folosind doar SSH), este idempotent (poate fi rulat repetat fără erori) și folosește o sintaxă YAML ușor de înțeles.

Ce alte module Ansible există?**
Pe lângă `apt` și `synchronize`, există module pentru:
* `service`/`systemd`: Gestionarea serviciilor.
* `user`/`group`: Gestionarea utilizatorilor.
* `git`: Clonarea repository-urilor.
* `copy`/`template`: Gestionarea fișierelor de configurare.
