# Event-Driven Order Processing System 🚀

A production-grade, high-throughput microservices system built with **Spring Boot**, **Apache Kafka**, and **Redis**. This project demonstrates a resilient architecture for handling order processing with strong guarantees on consistency and fault tolerance.

## 🏗️ Architecture Overview

The system consists of several microservices communicating asynchronously via Kafka:

-   **API Gateway** (Port `8080`): The single entry point for all client requests. Handles routing and cross-cutting concerns.
-   **Order Service** (Port `8081`): Orchestrates order creation, persists data to H2, and emits `OrderCreatedEvent`.
-   **Payment Service** (Port `8082`): Subscribes to `OrderCreatedEvent`. Implements **Idempotency** via Redis to prevent double-charging and handles payment logic.
-   **Notification Service** (Port `8083`): Final consumer that processes events to send user notifications.
-   **Kafka & Zookeeper**: Distributed event streaming platform for decoupled communication.
-   **Redis**: Used for distributed locking and idempotency checks.

## 📁 Project Structure

```text
.
├── gateway-service/         # Spring Cloud Gateway (Entry Point)
├── order-service/           # Order management & Event publisher
├── payment-service/         # Payment processing & Idempotency logic
├── notification-service/    # Notification & Logging consumer
├── k6/                      # Load testing scripts for performance validation
├── docker-compose.yml       # Infrastructure setup (Kafka, Redis, Kafka UI)
└── pom.xml                  # Parent Maven Project
```

## 🛠️ Setup & Running

### 1. Start Infrastructure
Ensure Docker is running, then launch the required middleware:
```bash
docker-compose up -d
```
-   **Kafka UI**: Access at [http://localhost:9000](http://localhost:9000) to monitor topics and messages.
-   **Redis**: Running on port `6379`.

### 2. Run Microservices
You can run all services using Maven from the root directory:
```bash
# Gateway Service (Required for routing)
mvn spring-boot:run -pl gateway-service

# Business Services
mvn spring-boot:run -pl order-service
mvn spring-boot:run -pl payment-service
mvn spring-boot:run -pl notification-service
```

## 🧪 Testing the System

### Manual Testing (via API Gateway)
Route your requests through the Gateway (Port `8080`):
```bash
curl -X POST http://localhost:8080/orders \
-H "Content-Type: application/json" \
-d '{"customerId": "USER_123", "amount": 150.00}'
```

### Idempotency Verification
The **Payment Service** ensures exactly-once processing. If a message is retried or duplicated:
1.  The service checks Redis for the `orderId`.
2.  If already processed, it logs a "Duplicate detected" message and skips the transaction.

### Resiliency: Error Handling & DLQ
The system is designed to handle failures gracefully:
-   **Retry Logic**: If the `amount` is `999.99`, the Payment Service fails and retries **3 times** with a 2s backoff.
-   **Dead Letter Queue (DLQ)**: After exhausted retries, the message is moved to `order-topic.DLT` for manual inspection, ensuring no data loss.

### Load Testing
Validate system performance under heavy load using **k6**:
```bash
k6 run k6/load-test.js
```

## 🛡️ Key Technical Features

-   **Idempotency Path**: Uses a `SETNX` strategy in Redis to ensure a distributed lock/state for every order ID.
-   **Non-Blocking I/O**: Leverages Spring Cloud Gateway for efficient request routing.
-   **Observability**: Integrated with Kafka UI for real-time stream monitoring.
-   **Scalability**: All services are stateless; Kafka partitions allow for horizontal scaling of consumers.

