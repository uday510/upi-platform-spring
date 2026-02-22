#!/bin/bash

set -e

echo "==============================="
echo " Kafka Dev Environment Reset "
echo "==============================="

PROJECT_DIR=$(pwd)

KAFKA_IMAGE="confluentinc/cp-kafka:7.5.0"
COMPOSE_FILE="docker-compose.yml"

# -------------------------------
# Step 1: Stop + Clean
# -------------------------------
echo "Stopping existing containers..."
docker-compose down -v || true

echo "Cleaning unused Docker resources..."
docker system prune -f

# -------------------------------
# Step 2: Generate Cluster ID
# -------------------------------
echo "Generating new KRaft Cluster ID..."

CLUSTER_ID=$(
  docker run --rm $KAFKA_IMAGE \
  kafka-storage random-uuid
)

echo "New Cluster ID: $CLUSTER_ID"

# -------------------------------
# Step 3: Update docker-compose
# -------------------------------
echo "Updating docker-compose.yml..."

if grep -q "KAFKA_CLUSTER_ID" "$COMPOSE_FILE"; then
  sed -i.bak "s/KAFKA_CLUSTER_ID:.*/KAFKA_CLUSTER_ID: \"$CLUSTER_ID\"/" "$COMPOSE_FILE"
else
  echo "⚠️  KAFKA_CLUSTER_ID not found in docker-compose.yml"
  echo "Please add it manually."
  exit 1
fi

# -------------------------------
# Step 4: Start Kafka
# -------------------------------
echo "Starting Kafka..."
docker-compose up -d

echo "Waiting for Kafka to boot..."
sleep 20

# -------------------------------
# Step 5: Health Check
# -------------------------------
echo "Checking Kafka status..."

docker ps | grep kafka

# -------------------------------
# Step 6: Create Topics
# -------------------------------
echo "Creating topics..."

docker exec kafka kafka-topics \
--create \
--if-not-exists \
--topic transfer.completed \
--bootstrap-server localhost:9092 \
--partitions 1 \
--replication-factor 1

docker exec kafka kafka-topics \
--create \
--if-not-exists \
--topic transfer.completed.dlq \
--bootstrap-server localhost:9092 \
--partitions 1 \
--replication-factor 1

# -------------------------------
# Step 7: List Topics
# -------------------------------
echo "Available topics:"

docker exec kafka kafka-topics \
--list \
--bootstrap-server localhost:9092

echo "==============================="
echo " Kafka Ready ✅"
echo "==============================="

echo "Now you can start services:"
echo " - api-gateway"
echo " - auth-service"
echo " - upi-service"
echo " - notification-service"