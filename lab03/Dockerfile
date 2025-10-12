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