# Use a slim Python image
FROM python:3.10-slim

# Set the working directory
WORKDIR /usr/src/app

# Install cron and other necessary tools
RUN apt-get update && \
    apt-get install -y cron && \
    rm -rf /var/lib/apt/lists/*

# Install Python dependencies (Adjust this line if your script needs other libraries)
# We assume 'requests' might be needed:
RUN pip install --no-cache-dir requests

# Copy the application script
COPY currency_exchange_rate.py .

# Copy the cronjob file to the cron configuration directory
COPY cronjob /etc/cron.d/cronjob

# Give execution permission to the cron job file
RUN chmod 0644 /etc/cron.d/cronjob

# Copy and make the entrypoint script executable
COPY docker-entrypoint.sh /usr/local/bin/
RUN chmod +x /usr/local/bin/docker-entrypoint.sh

# Set the entrypoint to the shell script
ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]

# Command is optional
CMD [""]