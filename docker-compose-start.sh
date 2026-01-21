#!/bin/bash

set -e

COMPOSE_FILE="docker-compose.yml"

echo "Starting services..."
docker-compose -f "$COMPOSE_FILE" up -d
