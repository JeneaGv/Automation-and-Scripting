#!/bin/bash

if [ "$#" -lt 1 ] || [ "$#" -gt 2 ]; then
    echo "Usage: $0 <source_directory> [destination_directory]"
    echo "The destination directory is optional and defaults to /backup."
    exit 1
fi

SOURCE_DIR="$1"
DEST_DIR="${2:-/backup}" 

if [ ! -d "$SOURCE_DIR" ]; then
    echo "Error: Source directory '$SOURCE_DIR' does not exist."
    exit 1
fi


if [ ! -d "$DEST_DIR" ]; then
    echo "Creating destination directory '$DEST_DIR'..."
    mkdir -p "$DEST_DIR"
    if [ $? -ne 0 ]; then
        echo "Error: Could not create destination directory '$DEST_DIR'."
        exit 1
    fi
fi

DATE=$(date +%Y-%m-%d)
FILENAME="backup_$(basename "$SOURCE_DIR")_$DATE.tar.gz"
ARCHIVE_PATH="$DEST_DIR/$FILENAME"

echo "Backing up '$SOURCE_DIR' to '$ARCHIVE_PATH'..."

tar -czf "$ARCHIVE_PATH" -C "$(dirname "$SOURCE_DIR")" "$(basename "$SOURCE_DIR")"

if [ $? -eq 0 ]; then
    echo "Backup successful!"
    echo "Archive saved at: $ARCHIVE_PATH"
else
    echo "Error: Backup failed. Please check the logs."
    exit 1
fi

exit 0
if [ "$#" -lt 1 ] || [ "$#" -gt 2 ]; then
    echo "Usage: $0 <source_directory> [destination_directory]"
    echo "The destination directory is optional and defaults to /backup."
    exit 1
fi

SOURCE_DIR="$1"
DEST_DIR="${2:-/backup}"

if [ ! -d "$SOURCE_DIR" ]; then
    echo "Error: Source directory '$SOURCE_DIR' does not exist."
    exit 1
fi

if [ ! -d "$DEST_DIR" ]; then
    echo "Creating destination directory '$DEST_DIR'..."
    mkdir -p "$DEST_DIR"
    if [ $? -ne 0 ]; then
        echo "Error: Could not create destination directory '$DEST_DIR'."
        exit 1
    fi
fi

DATE=$(date +%Y-%m-%d)
FILENAME="backup_$(basename "$SOURCE_DIR")_$DATE.tar.gz"
ARCHIVE_PATH="$DEST_DIR/$FILENAME"

echo "Backing up '$SOURCE_DIR' to '$ARCHIVE_PATH'..."

tar -czf "$ARCHIVE_PATH" -C "$(dirname "$SOURCE_DIR")" "$(basename "$SOURCE_DIR")"

if [ $? -eq 0 ]; then
    echo "Backup successful!"
    echo "Archive saved at: $ARCHIVE_PATH"
else
    echo "Error: Backup failed. Please check the logs."
    exit 1
fi

exit 0
