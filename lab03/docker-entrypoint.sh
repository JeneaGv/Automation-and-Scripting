#!/bin/bash

# Load the cronjob file into the system crontab
crontab /etc/cron.d/cronjob

# Create the log file and set permissions
touch /var/log/cron.log
chmod 644 /var/log/cron.log

# Start the cron service in the background
cron

# Keep the container running by continuously outputting the log file content
echo "Cron service started. Monitoring logs..."
exec tail -f /var/log/cron.log