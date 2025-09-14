# Writing a Simple Shell Script for Task Automation

# Obiectiv 

Invatam să cream si sa executam scripturi simple de Shell pentru a automatiza sarcinile de rutină în sistemul de operare Linux.

# Sarcina

Curatarea fisierelor temporare:

Pregatim mediul de executare 

<img width="753" height="156" alt="image" src="https://github.com/user-attachments/assets/7d8b1ef7-90a5-4fd4-b1f5-9ff5eaa9d8b1" />


Scriptul trebuie să se numească cleanup.sh

<img width="408" height="58" alt="image" src="https://github.com/user-attachments/assets/543d71ba-1331-49ab-9559-25542adca9b2" />

Scriptul trebuie să accepte cel puțin un argument: calea către directorul care trebuie curățat

```
if [ "$#" -lt 1 ]; then
    echo "Usage: $0 <directory_to_clean> [file_type_1] [file_type_2] ..."
    echo "By default, files with a .tmp extension are deleted."
    exit 1
fi
```

Argumentele rămase sunt opționale și specifică tipurile de fișiere care trebuie șterse (ex: .tmp, .log).

1. Extragerea argumentelor opționale

```
shift
FILE_TYPES=("$@")
```

shift-muta toti parametrii de pozitie ($1, $2, $3, etc.) cu o pozitie la stanga. 

Astfel, primul argument ($1, care era directorul) este eliminat, iar $2 devine noul $1, $3 devine noul $2 si asa mai departe.


Implicit, fișierele cu extensia .tmp sunt șterse.

```
if [ ${#FILE_TYPES[@]} -eq 0 ]; then
    FILE_TYPES=(".tmp")
fi
```

La finalul execuției, scriptul trebuie să afișeze numărul de fișiere șterse

```
DELETED_COUNT=0 #initializam contorul de fisiere sterse

echo "Starting cleanup in directory '$CLEAN_DIR'..."

for TYPE in "${FILE_TYPES[@]}"; do #parcurgem fiecare tip de fisier specificat

    echo "Searching for files with extension '$TYPE'..." #afisam tipul curent de fisier

    CURRENT_DELETED=$(find "$CLEAN_DIR" -type f -name "*$TYPE" -delete -print | wc -l) #stergem fisierele si numaram cate au fost sterse
    
    DELETED_COUNT=$((DELETED_COUNT + CURRENT_DELETED)) #actualizam contorul total
done
```

Scriptul trebuie să verifice dacă directorul specificat există și să afișeze mesaje de eroare corespunzătoare.

```
if [ ! -d "$CLEAN_DIR" ]; then
    echo "Error: Directory '$CLEAN_DIR' does not exist."
    exit 1
fi
```

# Verificarea lucrarii scriptului 

cu ajutorul comenzii touch cream fisiere cu diferite extensii

<img width="456" height="143" alt="image" src="https://github.com/user-attachments/assets/3cace306-b9f4-423e-a519-d42f7413c5e8" />

Prima data incercam scriptul fara a indica extensiile dorite:

<img width="753" height="81" alt="image" src="https://github.com/user-attachments/assets/919e7965-20d3-4461-a1f0-3c93368cd93a" />

<img width="436" height="93" alt="image" src="https://github.com/user-attachments/assets/84ffc11b-2b1f-4d6c-9c17-32b2e09ba589" />

Observam rezultatul ca au fost sterse fisierele cu extensia .tmp

Acum cream din nou fisierele sterse si incercam sa specificam extensiile dorite 

<img width="749" height="79" alt="image" src="https://github.com/user-attachments/assets/712ef66d-2a9a-4d85-8fcf-f0f5550dcab6" />

<img width="439" height="56" alt="image" src="https://github.com/user-attachments/assets/cfe64424-d117-466a-a28d-91e25b8d2ff7" />

Observam ca a ramas doar fisierul .txt => scriptul a fost creat cu succes

# Concluzie 

Scriptul cleanup.sh este un exemplu excelent de automatizare a unei sarcini simple, dar repetitive, pe care o poți rula pe orice sistem de operare bazat pe Linux, inclusiv WSL. A

m reușit să creăm un script care este nu doar funcțional, dar și robust, îndeplinind toate cerințele inițiale: validarea argumentelor, flexibilitatea de a accepta tipuri de fișiere personalizate, 

gestionarea erorilor prin verificarea existenței directorului și oferirea unui feedback clar prin afișarea numărului de fișiere șterse. 

Acest script demonstrează cum câteva rânduri de cod de shell pot economisi timp și simplifica sarcinile de mentenanță de bază, fiind o bază solidă pentru a construi scripturi mai complexe în viitor.

# Bilbiografia

https://www.shellscript.sh/

https://tldp.org/LDP/abs/html/

https://tldp.org/LDP/Bash-Beginners-Guide/html/

https://linuxcommand.org/tlcl.php

https://www.gnu.org/software/bash/manual/bash.html






   
