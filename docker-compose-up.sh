#!/bin/bash

set -e

COMPOSE_FILE="docker-compose.local.yml"

if ! docker network ls | grep -q "wa-local-network"; then
    echo "Creating wa-local-network..."
    docker network create wa-local-network || true
fi

KAFKA_RUNNING=false
KAFKA_UI_RUNNING=false

if docker ps --format '{{.Names}}' | grep -q "^wa-kafka$"; then
    KAFKA_RUNNING=true
    echo "Container wa-kafka is already running"
fi

if docker ps --format '{{.Names}}' | grep -q "^wa-kafka-ui$"; then
    KAFKA_UI_RUNNING=true
    echo "Container wa-kafka-ui is already running"
fi

if [ "$KAFKA_RUNNING" = true ]; then
    docker network connect wa-local-network wa-kafka 2>/dev/null || true
fi

if [ "$KAFKA_UI_RUNNING" = true ]; then
    docker network connect wa-local-network wa-kafka-ui 2>/dev/null || true
fi

if [ "$KAFKA_RUNNING" = true ] && [ "$KAFKA_UI_RUNNING" = true ]; then
    echo "Starting services without Kafka (already running)..."
    docker-compose -f "$COMPOSE_FILE" up -d
else
    echo "Starting all services including Kafka..."
    docker-compose -f "$COMPOSE_FILE" --profile infra up -d || {
        if docker ps -a --format '{{.Names}}' | grep -q "^wa-kafka$"; then
            echo "Kafka container exists, starting it and connecting to network..."
            docker start wa-kafka 2>/dev/null || true
            docker network connect wa-local-network wa-kafka 2>/dev/null || true
        fi
        if docker ps -a --format '{{.Names}}' | grep -q "^wa-kafka-ui$"; then
            echo "Kafka UI container exists, starting it and connecting to network..."
            docker start wa-kafka-ui 2>/dev/null || true
            docker network connect wa-local-network wa-kafka-ui 2>/dev/null || true
        fi
        docker-compose -f "$COMPOSE_FILE" up -d
    }
fi
