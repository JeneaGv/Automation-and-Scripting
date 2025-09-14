#!/bin/bash

if [ "$#" -lt 1 ]; then
    echo "Usage: $0 <directory_to_clean> [file_type_1] [file_type_2] ..."
    echo "By default, files with a .tmp extension are deleted."
    exit 1
fi

CLEAN_DIR="$1"

shift
FILE_TYPES=("$@")
if [ ${#FILE_TYPES[@]} -eq 0 ]; then
    FILE_TYPES=(".tmp")
fi

if [ ! -d "$CLEAN_DIR" ]; then
    echo "Error: Directory '$CLEAN_DIR' does not exist."
    exit 1
fi

DELETED_COUNT=0 #initializam contorul de fisiere sterse

echo "Starting cleanup in directory '$CLEAN_DIR'..."

for TYPE in "${FILE_TYPES[@]}"; do #parcurgem fiecare tip de fisier specificat

    echo "Searching for files with extension '$TYPE'..." #afisam tipul curent de fisier

    CURRENT_DELETED=$(find "$CLEAN_DIR" -type f -name "*$TYPE" -delete -print | wc -l) #stergem fisierele si numaram cate au fost sterse
    
    DELETED_COUNT=$((DELETED_COUNT + CURRENT_DELETED)) #actualizam contorul total
done

echo "Cleanup complete."
echo "Total files deleted: $DELETED_COUNT"

exit 0
