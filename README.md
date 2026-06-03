# Veltro — Connected Gym Management Platform

![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.13-green?logo=spring)
![Java](https://img.shields.io/badge/Java-17-orange?logo=java)
![Flutter](https://img.shields.io/badge/Flutter-3.10-blue?logo=flutter)
![Next.js](https://img.shields.io/badge/Next.js-16-black?logo=next.js)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.12-orange?logo=rabbitmq)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?logo=mysql)
![Gemini AI](https://img.shields.io/badge/Gemini-1.5_Flash-purple?logo=google)

**Veltro** is a production-ready gym management platform built as a microservices showcase demonstrating 17 architecture patterns across 11 independent Spring Boot services.

---

## Architecture

```
                     ┌────────────────────────────────────────┐
                     │         API Gateway (8080)              │
                     │   JWT validation · Route forwarding     │
                     └─────────────────┬──────────────────────┘
                                       │
        ┌──────────────────────────────┼───────────────────────────┐
        │             │                │              │             │
  ┌─────┴────┐  ┌────┴───┐   ┌───────┴──┐   ┌──────┴───┐  ┌─────┴──────┐
  │  Auth    │  │  User  │   │   Sub.   │   │ Booking  │  │  Activity  │
  │  (8081)  │  │  (8082)│   │  (8083)  │   │  (8084)  │  │   (8085)   │
  └──────────┘  └────────┘   └──────────┘   └──────────┘  └────────────┘
                                       │
                               ┌───────┴──────┐
                               │   RabbitMQ   │ 5672 / 15672 UI
                               └───────┬──────┘
                                       │
        ┌──────────────────────────────┼────────────────────────────┐
        │             │                │              │              │
  ┌─────┴────┐  ┌────┴────┐  ┌────────┴──┐  ┌───────┴───┐  ┌──────┴──┐
  │Messaging │  │  Shop   │  │ Notif.    │  │  Chatbot  │  │ Eureka  │
  │  (8087)  │  │  (8088) │  │  (8086)   │  │  (8089)   │  │  (8761) │
  └──────────┘  └─────────┘  └───────────┘  └───────────┘  └─────────┘
```

---

## Quick Start

### 1. Clone & start all services

```bash
git clone https://github.com/your-org/veltro.git
cd veltro
docker-compose -f docker-compose.dev.yml up --build -d
```

### 2. Load seed data (wait ~30s for databases to init)

```bash
mysql -h 127.0.0.1 -P 3307 -u root -proot veltro_auth        < tools/seeds/auth.sql
mysql -h 127.0.0.1 -P 3308 -u root -proot veltro_user        < tools/seeds/user.sql
mysql -h 127.0.0.1 -P 3309 -u root -proot veltro_subscription < tools/seeds/subscription.sql
mysql -h 127.0.0.1 -P 3310 -u root -proot veltro_booking     < tools/seeds/booking.sql
mysql -h 127.0.0.1 -P 3311 -u root -proot veltro_activity    < tools/seeds/activity.sql
mysql -h 127.0.0.1 -P 3312 -u root -proot veltro_shop        < tools/seeds/shop.sql
```

### 3. Start frontends

```bash
# Web dashboard
cd frontend-web && npm install && npm run dev
# → http://localhost:3000

# Mobile (requires Flutter SDK)
cd frontend-mobile && flutter pub get && flutter run
```

---

## Service Ports

| Service | Port | Database | DB Port |
|---------|------|----------|---------|
| Eureka | 8761 | — | — |
| API Gateway | 8080 | — | — |
| Auth | 8081 | veltro_auth | 3307 |
| User | 8082 | veltro_user | 3308 |
| Subscription | 8083 | veltro_subscription | 3309 |
| Booking | 8084 | veltro_booking | 3310 |
| Activity | 8085 | veltro_activity | 3311 |
| Notification | 8086 | — | — |
| Messaging | 8087 | veltro_messaging | 3313 |
| Shop | 8088 | veltro_shop | 3312 |
| Chatbot | 8089 | — | — |

---

## Default Credentials

**Password for all accounts: `Veltro@2024`**

| Role | Email |
|------|-------|
| Admin | admin@veltro.gym |
| Coach | coach1@veltro.gym |
| Member | member1@veltro.gym |

Full list: [`tools/demo-credentials.md`](tools/demo-credentials.md)

---

## Running Tests

```bash
# Build shared library first
cd backend/common-lib && mvn install

# Run a service's tests
cd backend/booking-service && mvn test
```

---

## Load Testing

```bash
# Install k6 (https://k6.io)
k6 run tools/k6-load-test.js
# Target: 100 VUs, 5 min, p95 < 500ms, error rate < 1%
```

---

## Virtual Hardware (Node-RED)

```bash
npm install -g node-red && node-red
# http://localhost:1880 → Import tools/nodered-flows.json
# Set env var: VELTRO_JWT = your admin Bearer token
```

---

## Swagger / OpenAPI

| Service | URL |
|---------|-----|
| User | http://localhost:8082/swagger-ui.html |
| Booking | http://localhost:8084/swagger-ui.html |
| Activity | http://localhost:8085/swagger-ui.html |
| Chatbot | http://localhost:8089/swagger-ui.html |

---

## Key Environment Variables

| Variable | Used By |
|----------|---------|
| `GEMINI_API_KEY` | chatbot-service → Google Gemini AI |
| `VELTRO_JWT` | Node-RED → API authentication |
