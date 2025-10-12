import sys
import os
import json
import logging
import requests
from datetime import datetime

# --- Constants and Configuration ---
API_BASE_URL = os.getenv("API_BASE_URL", "http://localhost:8080/")
# API_KEY este luată din sample.env/README.md
API_KEY = os.getenv("API_KEY", "EXAMPLE_API_KEY")
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
    """
    Recuperează rata de schimb de la API-ul local.
    Cheia API este trimisă în corpul POST.
    """
    
    # 1. Construiește parametrii GET (URL query parameters)
    params = {
        'from': from_currency.upper(),
        'to': to_currency.upper(),
        'date': date_str
    }
    
    # 2. Pregătește datele POST pentru cheia API
    post_data = {
        'key': API_KEY
    }

    print(f"-> Requesting rate for {from_currency.upper()}/{to_currency.upper()} on {date_str}...")

    # 3. Execută cererea POST
    # Trimitem cheia API în corpul cererii (data) și parametri (from, to, date) în URL (params)
    response = requests.post(API_BASE_URL, params=params, data=post_data)
    response.raise_for_status() # Ridică o excepție pentru erori HTTP (4xx sau 5xx)

    # 4. Parsează răspunsul JSON
    data = response.json()
    
    # 5. Gestionează erorile din corpul răspunsului API (e.g., monedă/dată invalidă)
    if data.get('error'):
        # API-ul returnează cod 200 chiar și pe erori de validare
        raise ValueError(f"API Error: {data['error']}")

    # 6. Returnează datele ratei
    return data

def save_data(data: dict, from_currency: str, to_currency: str, date_str: str):
    """
    Salvează datele primite într-un fișier JSON în directorul 'data'.
    """
    
    # 1. Asigură-te că directorul 'data' există la rădăcina proiectului.
    os.makedirs(DATA_DIR, exist_ok=True)
    
    # 2. Construiește numele fișierului: e.g., USD_RON_2026-01-01.json
    filename = f"{from_currency.upper()}_{to_currency.upper()}_{date_str}.json"
    filepath = os.path.join(DATA_DIR, filename)
    
    # 3. Scrie datele în fișierul JSON cu indentare
    with open(filepath, 'w') as f:
        json.dump(data, f, indent=4)
    
    print(f"-> Successfully saved data to {filepath}")

def main():
    """
    Funcția principală pentru a valida input-ul și a apela funcțiile de preluare/salvare.
    """
    # 1. Verifică argumentele din linia de comandă
    if len(sys.argv) != 4:
        print("Usage: python lab02/currency_exchange_rate.py <FROM_CURRENCY> <TO_CURRENCY> <YYYY-MM-DD>")
        print("Example: python lab02/currency_exchange_rate.py USD EUR 2025-03-05")
        sys.exit(1)

    # 2. Asignează argumentele
    from_currency, to_currency, date_str = sys.argv[1:4]

    try:
        # Validare simplă a formatului datei
        request_date = datetime.strptime(date_str, '%Y-%m-%d')
        
        # 3. Obține rata
        rate_data = get_exchange_rate(from_currency, to_currency, date_str)
        
        # 4. Salvează rata
        save_data(rate_data, from_currency, to_currency, date_str)

        # 5. Afișează rezultatul final
        final_rate = rate_data.get('data', {}).get('rate')
        print(f"SUCCESS: Exchange Rate (1 {from_currency.upper()} to {to_currency.upper()} on {date_str}) is: {final_rate}")
        
    except (requests.exceptions.RequestException, ValueError, Exception) as e:
        # 6. Gestionează erorile și loghează-le
        error_message = f"ERROR processing request: {e}"
        print(f"\nFATAL: {error_message}")
        print(f"Details saved to {LOG_FILE}")
        
        # Loghează eroarea în fișier
        logging.error(f"Parameters: FROM={from_currency}, TO={to_currency}, DATE={date_str} | Message: {e}")

if __name__ == "__main__":
    main()
