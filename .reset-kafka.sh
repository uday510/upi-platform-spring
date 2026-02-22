#!/bin/bash

echo "Stopping Kafka..."
docker-compose down -v

echo "Cleaning Docker..."
docker system prune -f

echo "Generating new cluster ID..."
CLUSTER_ID=$(docker run --rm confluentinc/cp-kafka:7.5.0 kafka-storage random-uuid)

echo "New Cluster ID: $CLUSTER_ID"

echo "👉 Update docker-compose.yml with this ID!"

echo "Restart Kafka after update:"
echo "docker-compose up -d"