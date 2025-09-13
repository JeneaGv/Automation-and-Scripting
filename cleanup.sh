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

DELETED_COUNT=0

echo "Starting cleanup in directory '$CLEAN_DIR'..."

for TYPE in "${FILE_TYPES[@]}"; do

    echo "Searching for files with extension '$TYPE'..."

    CURRENT_DELETED=$(find "$CLEAN_DIR" -type f -name "*$TYPE" -delete -print | wc -l)
    
    DELETED_COUNT=$((DELETED_COUNT + CURRENT_DELETED))
done

echo "Cleanup complete."
echo "Total files deleted: $DELETED_COUNT"

exit 0
