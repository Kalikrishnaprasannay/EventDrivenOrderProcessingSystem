# Event-Driven Order Processing System

A production-grade microservices system built with Spring Boot, Kafka, and Redis.

## Architecture Highlights
- **Order Service**: Entry point (Port 8081). Persists orders to H2 and emits `OrderCreatedEvent`.
- **Payment Service**: Consumer (Port 8082). Implements **Idempotency** via Redis and **Retry/DLQ** for failure handling.
- **Notification Service**: Consumer (Port 8083). Logs final success notifications.
- **Kafka**: Message broker for decoupled, asynchronous communication.
- **Redis**: Distributed lock/state for ensuring exactly-once processing (idempotency).

## Project Structure
```text
.
├── docker-compose.yml          # Infrastructure (Kafka, Zookeeper, Redis, Kafka UI)
├── pom.xml                     # Parent Maven Project
├── order-service/              # Order Service implementation
├── payment-service/            # Payment Service implementation
├── notification-service/       # Notification Service implementation
└── k6/                         # Load testing script
```

## Setup & Running

### 1. Start Infrastructure
Ensure Docker is running, then execute:
```bash
docker-compose up -d
```
You can access **Kafka UI** at `http://localhost:8080` to monitor topics.

### 2. Build and Run Services
Run each service from its respective directory or via terminal:
```bash
# Order Service
mvn spring-boot:run -pl order-service

# Payment Service
mvn spring-boot:run -pl payment-service

# Notification Service
mvn spring-boot:run -pl notification-service
```

## Testing

### Manual Testing
Send a POST request to create an order:
```bash
curl -X POST http://localhost:8081/orders \
-H "Content-Type: application/json" \
-d '{"customerId": "USER_123", "amount": 150.00}'
```

### Idempotency Verification
The Payment Service uses Redis to ensure an order is only processed once. If the same `OrderCreatedEvent` is re-delivered (e.g., due to network jitter), the service will detect the `orderId` in Redis and skip processing.

### Error Handling & DLQ
If the `amount` is exactly `999.99`, the Payment Service will simulate a failure.
1. It will retry **3 times** (with 2s backoff).
2. If it still fails, the message will be moved to `order-topic.DLT`.

### Load Testing
Run the provided k6 script (requires k6 installed):
```bash
k6 run k6/load-test.js
```

## Technical Explanation

### 1. How Idempotency Works
We use a **Redis-based Distributed Lock** strategy. Before processing, we call `SETNX` (set if absent) with the `orderId`.
- If successful: Proceed with payment.
- If failed: An entry already exists, meaning another instance is already processing or has finished this order.

### 2. How Retry and DLQ work
Spring Kafka's `DefaultErrorHandler` is configured with a `FixedBackOff`.
- **Retry**: On exception, the consumer pauses and waits before trying again.
- **DLQ**: If retries are exhausted, the `DeadLetterPublishingRecoverer` republishes the message to a special `.DLT` topic. This prevents "poison pill" messages from blocking the main partition.

### 3. Scaling for Millions of Requests
- **Kafka Partitions**: Increase partitions for `order-topic`. Each partition can be consumed by one instance in a consumer group, allowing linear scaling.
- **Redis Clustering**: Ensure Redis is clustered to handle high-frequency idempotency checks.
- **Stateless Services**: All services are stateless and can be horizontally scaled using Kubernetes pods.
