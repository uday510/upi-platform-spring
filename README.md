# Event-Driven UPI Payment Platform

A scalable microservices-based UPI payment system built using Spring Boot and Apache Kafka.  
This platform demonstrates reliable payment processing using event-driven architecture and the Outbox Pattern.

---

## Features

- Microservices-based architecture
- Reliable event processing using Kafka and Outbox Pattern
- Asynchronous notification system with retry and idempotency
- Service discovery and routing using Eureka and API Gateway
- Secure inter-service communication using Feign
- Fault tolerance with Resilience4j Circuit Breakers
- PostgreSQL-backed persistence

---

## Architecture Overview

Client → API Gateway → Microservices → Kafka → Notification Service

Services communicate asynchronously using Kafka and synchronously using Feign clients.

---

## Microservices

| Service | Description |
|---------|-------------|
| API Gateway | Entry point for all client requests |
| Auth Service | Authentication and authorization |
| UPI Service | Core transaction processing |
| Account Service | Balance validation |
| Notification Service | Email notifications |
| Service Registry | Eureka server |

---

## Tech Stack

- Java 17
- Spring Boot
- Spring Cloud (Gateway, Eureka, Feign)
- Apache Kafka
- PostgreSQL
- Docker
- Resilience4j
- Maven

---

## Prerequisites

Install the following:

- Java 17+
- Maven
- Docker & Docker Compose
- PostgreSQL

---

## Running the Project

Start the services in the following order.

---
## Start All Services

```bash
# 1. Start Kafka
docker-compose up -d

# 2. Start Service Registry (Eureka)
cd service-registry
./mvnw spring-boot:run

# 3. Start Auth Service
cd ../auth-service
./mvnw spring-boot:run

# 4. Start UPI Service
cd ../upi-service
./mvnw spring-boot:run

# 5. Start Notification Service
cd ../notification-service
./mvnw spring-boot:run

# 6. Start API Gateway
cd ../api-gateway
./mvnw spring-boot:run
