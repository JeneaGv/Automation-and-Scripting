# Creating a Python Script to Interact with an API

# Scop

Învață să interacționezi cu un API Web folosind un script Python.

# Pregătire

Descarcă proiectul atașat acestei sarcini și despachetează-l într-o locație convenabilă. Pornește serviciul folosind instrucțiunile din fișierul README.md

# Sarcina

In interiorul proiectului descarcat copiem fisier .env 

<img width="650" height="70" alt="image" src="https://github.com/user-attachments/assets/bdb34b80-d8e4-4a6c-92d9-5cec25f3a2ff" />

Pornim serverul

<img width="1456" height="510" alt="image" src="https://github.com/user-attachments/assets/b54b33ae-bf13-407a-8f82-a0def114ccc4" />

Cream si ne schimbam pe branch numit "lab02"

<img width="700" height="68" alt="image" src="https://github.com/user-attachments/assets/c5d2a5ae-2365-4984-a339-b5ea09b138b2" />

# Partea 2

Cream scriptul Python care va interactiona cu API.

currency_rate.py

```

import sys
import os
import json
import logging
import requests
from datetime import datetime

API_BASE_URL = "http://localhost:8080/"
# API_KEY este luată din sample.env/README.md
API_KEY = "EXAMPLE_API_KEY"
DATA_DIR = "data"
LOG_FILE = "error.log"

# Intervalul maxim de date suportat conform documentatiei proiectului
MAX_DATE_STR = "2025-09-15"
MAX_DATE = datetime.strptime(MAX_DATE_STR, '%Y-%m-%d')

# Configurează logarea pentru a scrie erorile în error.log
logging.basicConfig(
    filename=LOG_FILE,
    level=logging.ERROR,
    format='%(asctime)s - %(levelname)s - %(message)s'
)

def get_exchange_rate(from_currency: str, to_currency: str, date_str: str) -> dict:
# Construiește parametrii GET (URL query parameters)
    params = {
        'from': from_currency.upper(),
        'to': to_currency.upper(),
        'date': date_str
    }
    
    # Pregătește datele POST pentru cheia API
    post_data = {
        'key': API_KEY
    }

    print(f"-> Requesting rate for {from_currency.upper()}/{to_currency.upper()} on {date_str}...")

    # Execută cererea POST
    # Trimitem cheia API în corpul cererii (data) și parametri (from, to, date) în URL (params)
    response = requests.post(API_BASE_URL, params=params, data=post_data)
    response.raise_for_status() # Ridică o excepție pentru erori HTTP (4xx sau 5xx)

    # Parsează răspunsul JSON
    data = response.json()
    
    # Gestionează erorile din corpul răspunsului API (e.g., monedă/dată invalidă)
    if data.get('error'):
        # API-ul returnează cod 200 chiar și pe erori de validare
        raise ValueError(f"API Error: {data['error']}")

    # Returnează datele ratei
    return data

def save_data(data: dict, from_currency: str, to_currency: str, date_str: str):
# Asigură-te că directorul 'data' există la rădăcina proiectului.
    os.makedirs(DATA_DIR, exist_ok=True)
    
    # Construiește numele fișierului: e.g., USD_RON_2026-01-01.json
    filename = f"{from_currency.upper()}_{to_currency.upper()}_{date_str}.json"
    filepath = os.path.join(DATA_DIR, filename)
    
    # Scrie datele în fișierul JSON cu indentare
    with open(filepath, 'w') as f:
        json.dump(data, f, indent=4)
    
    print(f"-> Successfully saved data to {filepath}")

def main():
    # Verifică argumentele din linia de comandă
    if len(sys.argv) != 4:
        print("Usage: python lab02/currency_exchange_rate.py <FROM_CURRENCY> <TO_CURRENCY> <YYYY-MM-DD>")
        print("Example: python lab02/currency_exchange_rate.py USD EUR 2025-03-05")
        sys.exit(1)

    # 2. Asignează argumentele
    from_currency, to_currency, date_str = sys.argv[1:4]

    try:
        # Validare simplă a formatului datei
        request_date = datetime.strptime(date_str, '%Y-%m-%d')
        
        # VERIFICARE: Dacă data cerută depășește data maximă suportată de API
        if request_date > MAX_DATE:
            raise ValueError(f"Input Error: Date {date_str} is outside the supported range (up to {MAX_DATE_STR}).")
        
        # Obține rata
        rate_data = get_exchange_rate(from_currency, to_currency, date_str)
        
        # Salvează rata
        save_data(rate_data, from_currency, to_currency, date_str)

        # 5fișează rezultatul final
        final_rate = rate_data.get('data', {}).get('rate')
        print(f"SUCCESS: Exchange Rate (1 {from_currency.upper()} to {to_currency.upper()} on {date_str}) is: {final_rate}")
        
    except (requests.exceptions.RequestException, ValueError, Exception) as e:
        # Gestionează erorile și loghează-le
        error_message = f"ERROR processing request: {e}"
        print(f"\nFATAL: {error_message}")
        print(f"Details saved to {LOG_FILE}")
        
        # Loghează eroarea în fișier
        logging.error(f"Parameters: FROM={from_currency}, TO={to_currency}, DATE={date_str} | Message: {e}")

if __name__ == "__main__":
    main()


```

Instalam dependentele cu ajutorul comenzii

```
pip install -r requirments.txt
```

continutul fisierului:

<img width="190" height="58" alt="image" src="https://github.com/user-attachments/assets/e07a94a0-c6ce-42e1-af37-354520d5720b" />


<img width="1273" height="426" alt="image" src="https://github.com/user-attachments/assets/3ebd928e-289e-4d9f-b018-ddbbf9432b09" />

# Partea 3(Testarea)

<img width="1050" height="579" alt="image" src="https://github.com/user-attachments/assets/ee907df6-9ff0-4e36-8fb5-0a83756e4474" />

<img width="907" height="215" alt="image" src="https://github.com/user-attachments/assets/d3cc3c9e-42ac-4072-951d-34a556cc6c9b" />

Observam ca toate convertirile au avut loc cu succes si sau salvat in formate json in directorul data

Acum executam comenzile cu greseli sa verificam daca se va crea fisierul error.log cu continutul necesar

<img width="1080" height="105" alt="image" src="https://github.com/user-attachments/assets/c4c436a5-ca65-4091-9a07-79ebb3ba455c" />

<img width="1362" height="104" alt="image" src="https://github.com/user-attachments/assets/bc7d69d2-d4db-4fb7-b170-7d7e6c586a03" />

<img width="1408" height="104" alt="image" src="https://github.com/user-attachments/assets/cd81d153-d89f-44e8-aad6-46e77f5e8c54" />

Gestionarea errorilor a avut loc cu succes

# Concluzie 

Acest laborator a finalizat cu succes sarcina de a crea un instrument Python de automatizare, stabilind o comunicare funcțională și securizată cu API-ul local. 

Prin implementarea corectă a autentificării și a preluării datelor prin parametri de linie de comandă, scriptul a demonstrat modularitate. 

Cel mai important, s-a integrat o logică de logare robustă și validare locală a datelor, asigurând astfel că orice eroare de rețea, autentificare sau introducere invalidă (cum ar fi data în afara intervalului) este capturată, afișată și înregistrată în error.log. 

Aceasta transformă scriptul dintr-o simplă cerere HTTP într-o soluție de extragere de date stabilă și de încredere.

# Bibliografie

https://docs.python.org/3/

https://docs.docker.com/compose/

