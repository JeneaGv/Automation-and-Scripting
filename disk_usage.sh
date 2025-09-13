#!/bin/bash


if [ "$#" -lt 2 ]; then
    echo "Usage: $0 <directory_to_monitor> <max_volume_in_mb> [threshold_percent]"
    echo "The threshold is optional and defaults to 80%."
    exit 1
fi

MONITOR_DIR="$1"
MAX_VOLUME_MB="$2"
THRESHOLD_PERCENT="${3:-80}"

if [ ! -d "$MONITOR_DIR" ]; then
    echo "Error: Directory '$MONITOR_DIR' does not exist."
    exit 1
fi


CURRENT_USAGE_MB=$(du -sm "$MONITOR_DIR" | awk '{print $1}')

PERCENTAGE=$(echo "scale=2; ($CURRENT_USAGE_MB / $MAX_VOLUME_MB) * 100" | bc)

echo "$(date): Disk usage for '$MONITOR_DIR': $PERCENTAGE%" >> disk_usage.log
echo "Current disk usage: $PERCENTAGE%"

if (( $(echo "$PERCENTAGE > $THRESHOLD_PERCENT" | bc -l) )); then
    echo "Warning: Disk usage ($PERCENTAGE%) exceeds the threshold ($THRESHOLD_PERCENT%)."
    echo "A notification would be sent to the specified email."

else
    echo "Disk usage is within the acceptable range."
fi

exit 0
