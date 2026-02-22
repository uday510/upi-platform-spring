#!/bin/bash

CONTAINER=kafka

echo "========== Kafka Status =========="
docker ps | grep $CONTAINER


echo "========== Kafka Logs =========="
docker logs --tail=50 $CONTAINER


echo "========== List Topics =========="
docker exec -it $CONTAINER kafka-topics \
  --bootstrap-server localhost:9092 \
  --list


echo "========== Create Topic =========="
docker exec -it $CONTAINER kafka-topics \
  --bootstrap-server localhost:9092 \
  --create \
  --topic upi.transactions \
  --partitions 1 \
  --replication-factor 1


echo "========== Describe Topic =========="
docker exec -it $CONTAINER kafka-topics \
  --bootstrap-server localhost:9092 \
  --describe \
  --topic upi.transactions