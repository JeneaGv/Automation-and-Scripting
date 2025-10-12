# Task Scheduler (cron) Configuration

# Obiectiv 

Învațăm cum să configurezi **planificatorul de sarcini (cron)** pentru a **automatiza executarea scripturilor**.

# Sarcina

În proiectul de automatizare, creează o ramură (branch) lab03. Creează un director lab03 și copiază acolo fișierele din lucrarea de laborator nr. 2 (folderul lab02).

<img width="757" height="110" alt="image" src="https://github.com/user-attachments/assets/6abe80b3-a87f-4ba9-8313-25c2c8660937" />


<img width="752" height="465" alt="image" src="https://github.com/user-attachments/assets/dc97c7ae-09f6-499a-8394-b29095108b4a" />


În directorul lab03, creează un fișier numit cronjob. În acest fișier, specifică sarcinile cron care vor rula scriptul currency_exchange_rate.py:

Zilnic, la ora 06:00, pentru a obține cursul de schimb MDL către EUR pentru ziua precedentă.

Săptămânal, vinerea la ora 17:00, pentru a obține cursul de schimb MDL către USD pentru săptămâna precedentă.

fisierul cronjob :

```
0 6 * * * cd /app && /usr/local/bin/python currency_exchange_rate.py MDL EUR $(date -d "yesterday" +\%Y-\%m-\%d) >> /var/log/cron.log 2>&1
0 17 * * 5 cd /app && /usr/local/bin/python currency_exchange_rate.py MDL USD $(date -d "last friday" +\%Y-\%m-\%d) >> /var/log/cron.log 2>&1

```
**Important:**

Fișierul trebuie să aibă line endings de tip Unix (LF, nu CRLF)
Trebuie să existe un newline la sfârșitul fișierului
Nu lăsați linii goale între intrări

Creează un fișier Dockerfile în directorul lab03, bazat pe imaginea Ubuntu sau pe imaginea oficială Python, care va:

Instala toate dependențele necesare pentru rularea scriptului (cron, Python și bibliotecile necesare).

Copia scriptul currency_exchange_rate.py, fișierul cronjob și scriptul entrypoint în container.

Configura serviciul cron pentru a executa sarcinile specificate în fișierul cronjob.

Porni cron în mod de fundal (background) atunci când containerul este lansat.

Scrie rezultatele executării sarcinilor cron în fișierul /var/log/cron.log.

```
# Use official Python image as base
FROM python:3.11-slim

# Install cron and required system dependencies
RUN apt-get update && \
    apt-get install -y cron && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy requirements file and install Python dependencies
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy the Python script
COPY currency_exchange_rate.py .

# Copy the cronjob file
COPY cronjob /etc/cronjob

# Copy the entrypoint script
COPY entrypoint.sh /entrypoint.sh

# Make the entrypoint script executable
RUN chmod +x /entrypoint.sh

# Create data directory for JSON files
RUN mkdir -p /app/data

# Set the entrypoint
ENTRYPOINT ["/entrypoint.sh"]
```

Pentru o utilizare mai ușoară a cron, se recomandă folosirea unui script entrypoint care va configura și porni cron-ul.

```
#!/bin/sh

env > /etc/environment
crontab /etc/cronjob

create_log_file() {
    echo "Creating log file..."
    touch /var/log/cron.log
    chmod 666 /var/log/cron.log
    echo "Log file created at /var/log/cron.log"
}

monitor_logs() {
    echo "=== Monitoring cron logs ==="
    tail -f /var/log/cron.log
}

run_cron() {
    echo "=== Starting cron daemon ==="
    exec cron -f
}

create_log_file
monitor_logs &
run_cron
```

Creează un fișier docker-compose.yml în directorul lab03, care va folosi fișierul Dockerfile creat anterior pentru a construi imaginea și a rula containerul.

```
services:
  web:
    image: php:8.3-apache
    container_name: php_apache
    ports:
      - "8080:80"
    volumes:
      - ./app:/var/www/html
    env_file:
      - .env
    restart: unless-stopped
    networks:
      - my_app_network

  cron:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: cron_container
    env_file:
      - .env
    volumes:
      - ./data:/app/data
      - cron_logs:/var/log
    restart: unless-stopped
    depends_on:
      - web
    networks:
      - my_app_network

networks:
  my_app_network:
    driver: bridge

volumes:
  cron_logs:
```

Dupa construirea tuturor fisierelor necesare  construim și să rulăm containerul cu cron

Folosim comanda docker-compose up --build -d care

Construiește din nou imaginea și pornește containerul în fundal

<img width="747" height="189" alt="image" src="https://github.com/user-attachments/assets/6eeda6c0-b287-4f8f-8d86-dbe3a06a88b3" />

folosim comanda docker-compose logs -f cron pentru a vedea functionalitatea acestuia:

asteptam ora 6:00 si vedem ca acesta a salvat datele cu succes

<img width="742" height="159" alt="image" src="https://github.com/user-attachments/assets/ee6598bb-8fa1-405b-9d3d-e88cb0d9bc08" />

# Structura proiectului 

lab03/
│
├── currency_exchange_rate.py     Scriptul Python care obține cursurile valutare (logica principală)
├── cronjob                       Fișierul cu programările cron (când se rulează scriptul)
├── entrypoint.sh                 Scriptul care pornește cron-ul și afișează logurile (entrypoint-ul containerului)
├── Dockerfile                    Definiția pentru construirea imaginii Docker
├── docker-compose.yml            Simplifică procesul de construire și rulare a containerului
└── logs/                         Directorul unde sunt salvate logurile în afara containerului
 
