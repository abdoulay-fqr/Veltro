# Veltro — Presentation Deck Outline (14 Slides)

---

## Slide 1 — Title
**Veltro: Connected Gym Management Platform**
*A microservices-based full-stack application demonstrating 17 architecture patterns*

> **Speaker notes:** Start with the hook — "What if every gym could have the technology of a Silicon Valley startup?" Introduce Veltro as the answer: a production-ready platform built with modern cloud-native patterns.

---

## Slide 2 — The Problem
**Gym management is fragmented**
- Attendance: paper registers or basic apps
- Bookings: WhatsApp groups and phone calls
- Member monitoring: spreadsheets
- No AI assistance for members
- No real-time hardware integration

> **Speaker notes:** Many gyms still use paper sign-in sheets. Coaches have no visibility into member activity between sessions. Members can't easily track their progress. There's a gap between what modern gyms need and what existing tools provide.

---

## Slide 3 — The Solution
**Veltro: One platform, every role**
- **Members**: book classes, track activity, chat with coach, shop gear
- **Coaches**: manage courses, mark attendance, message members
- **Admins**: full financial reporting, member management, live occupancy
- **Hardware**: NFC card integration (real + virtual via Node-RED)
- **AI**: Gemini-powered assistant with real member context

> **Speaker notes:** Show the user journey: member scans NFC card → system logs entry → coach sees it → AI assistant can answer "did I go to the gym today?" with real data.

---

## Slide 4 — Tech Stack
| Layer | Technology |
|-------|-----------|
| Backend | Spring Boot 3.5, Spring Cloud 2025 |
| API Gateway | Spring Cloud Gateway (WebMVC) |
| Service Discovery | Netflix Eureka |
| Message Broker | RabbitMQ 3.12 |
| Databases | MySQL 8.0 (one per service) |
| AI | Google Gemini 1.5 Flash API |
| Web Frontend | Next.js 16, Tailwind CSS, Recharts |
| Mobile | Flutter 3.10, Riverpod, go_router |
| CI/CD | GitHub Actions |
| Load Testing | k6 |
| Virtual Hardware | Node-RED |

> **Speaker notes:** All technologies are production-grade and open-source (except Gemini API which has a free tier). No "toy" frameworks — this is the same stack used at scale.

---

## Slide 5 — Architecture Diagram
*Show the ASCII diagram from README.md — all 11 services connected through API Gateway and RabbitMQ*

**Key design decisions:**
1. API Gateway = single entry point, JWT validation once
2. Each service = independent process, own database, own port
3. RabbitMQ = all async communication, no direct service-to-service HTTP for events
4. No shared database — each bounded context owns its data

> **Speaker notes:** Point to the RabbitMQ central position — it's the nervous system of the platform. Everything that doesn't need an immediate response goes through it.

---

## Slide 6 — 17 Architecture Patterns (one slide, mapped)

| # | Pattern | How Implemented in Veltro |
|---|---------|--------------------------|
| 1 | Microservices | 11 Spring Boot services, independent ports/DBs |
| 2 | DDD/Bounded Context | Each service = one bounded context, own schema |
| 3 | Decomposition by capability | auth/user/booking/activity/etc. |
| 4 | Sync + Async communication | Feign REST + RabbitMQ events |
| 5 | Service Discovery | Netflix Eureka (8761) |
| 6 | Load Balancing | Spring Cloud LoadBalancer |
| 7 | API Gateway | JWT filter + route forwarding |
| 8 | Database per Service | 7 MySQL schemas, separate ports |
| 9 | CQRS | booking/activity/shop services |
| 10 | Event Sourcing | GymEntry append-only log |
| 11 | Domain Events | 11 event types via RabbitMQ |
| 12 | Circuit Breaker | Resilience4j on all Feign calls |
| 13 | Security | JWT + RBAC + token blacklist |
| 14 | Event-Driven Architecture | 6 topic exchanges + DLX |
| 15 | Service Registry | Eureka auto-registration |
| 16 | Fault Tolerance | DLQ chains, 3-retry policy |
| 17 | Distributed Transactions | Saga pattern (order + absence) |

> **Speaker notes:** Every pattern is demonstrable in code — not theoretical. We can navigate to any of these in the live demo.

---

## Slide 7 — Demo Overview
**3-minute demo flow:**
1. Admin login → financial dashboard
2. Revenue charts (real-time from subscription-service)
3. Node-RED NFC scan → activity live log updates (5s polling)
4. Mobile: member books course → chatbot answers "my next class?"
5. Mobile: shop purchase → admin confirms order

> **Speaker notes:** The goal of the demo is to show the system as a single integrated whole — not individual services in isolation. Every action triggers real backend processing.

---

## Slide 8 — Key Features Walkthrough

**Auth & JWT Flow:**
- Login → 15-min access token + 24-hour refresh token
- Gateway validates JWT, injects X-User-Id/X-User-Role → services trust headers

**Booking & Waitlist:**
- 7-day booking window, subscription check (circuit breaker)
- If course full → waitlist with position; on cancel → auto-promote → WaitlistPromoted event

**Activity Tracking:**
- NFC IN/OUT → GymEntry (append-only, linked by sessionId)
- Machine sessions → weekly calories chart, streak counter, personal records

**AI Chatbot:**
- Gemini 1.5 Flash + real member context (subscription, bookings, today's entry)
- 5-min cache to avoid hammering services; rate limit 20 req/hour

> **Speaker notes:** Each feature was designed as a vertical slice — backend service, API, web UI, and mobile screen all implemented together.

---

## Slide 9 — Virtual Hardware Setup
**Node-RED flows simulate physical NFC scanners:**
- 5 inject nodes (one per virtual card: card001–card005)
- Each click toggles IN/OUT direction (flow context variable)
- HTTP request → POST /api/v1/activity/entry → activity-service
- Response shows member name in debug panel

**Machine session injector:**
- Randomizes realistic values (200–800 kcal, 110–175 BPM, 1–15km)
- Proves the system works without any physical hardware

> **Speaker notes:** This demonstrates that the platform is ready for real hardware integration — Node-RED just simulates the signal. A real NFC reader would call the same API endpoint.

---

## Slide 10 — Testing Strategy

| Type | Tool | Coverage |
|------|------|---------|
| Unit + Integration | JUnit 5 + Testcontainers | All business logic, service layer |
| Database | Testcontainers MySQL | Real MySQL, not H2 |
| Load Testing | k6 | 100 VUs, 5 min, p95 < 500ms |
| API Testing | Postman Collection | 20 requests, full user journey |
| Manual QA | Demo script | 3-minute user journey |

**Key test patterns:**
- `@MockitoBean Clock clock` — all scheduled jobs testable with frozen time
- `@MockitoBean RabbitTemplate` — event publishing tested via Mockito verify()
- `@MockitoBean FeignClient` — circuit breaker fallbacks tested by throwing ServiceUnavailableException

> **Speaker notes:** We chose Testcontainers over H2 because production uses MySQL. Tests that pass against H2 can still fail in production due to dialect differences.

---

## Slide 11 — Security Implementation
**JWT Security:**
- RS256-compatible HMAC-SHA256 (HS256) tokens
- 15-minute access tokens (short-lived = smaller attack window)
- Refresh tokens stored in DB (can be revoked)
- Token blacklist on logout → prevents replay after logout
- Gateway validates once → downstream services never touch cryptography

**Rate Limiting:**
- Auth endpoints: prevent brute force (configurable)
- Chatbot: 20 req/hour per userId (sliding window, in-memory)

**SQL Injection Prevention:**
- All queries via JPA / Spring Data repositories
- No native string concatenation in queries
- Native queries use parameterized format (`@Query(nativeQuery=true)` with `:params`)

**Circuit Breakers:**
- Open after 50% failure rate in 5-request window
- Fallback returns meaningful error — never crashes the calling service

---

## Slide 12 — Challenges & Solutions

| Challenge | Solution |
|-----------|---------|
| RabbitMQ fan-out: one event, multiple consumers | Each service declares its own queue bound to the exchange → true fan-out |
| Concurrent order placement (race condition on stock) | `@Version` optimistic locking → ObjectOptimisticLockingFailureException → 409 |
| Chatbot context latency (3 downstream calls per message) | Caffeine cache (5 min TTL) + circuit breaker graceful skip |
| Testing @Scheduled jobs | Inject `Clock` bean → tests use `@MockitoBean Clock` with `Instant.parse(...)` |
| Mobile on Android emulator can't reach localhost | `DioClient.baseUrl` = `http://10.0.2.2:8080/api/v1` for emulator |
| JWT cookie not forwarding in Next.js API routes | `withCredentials: true` on Axios + `httpOnly` cookie handling in `/api/auth/*` routes |

---

## Slide 13 — Learnings

**What worked well:**
- **Testcontainers**: tests are realistic, failures caught before production
- **Clock injection**: made all scheduled job tests deterministic
- **FallbackFactory pattern**: circuit breaker fallbacks are clean and testable
- **Database per service**: no schema migrations breaking other services

**What I'd do differently:**
- **Saga orchestration**: use a saga orchestrator (Axon/Temporal) instead of ad-hoc transaction flows
- **Redis**: replace in-memory caches and rate limiter with Redis for multi-instance support
- **WebSockets**: replace polling in messaging and live entry log with WebSocket connections
- **gRPC**: replace some Feign clients with gRPC for lower-latency internal communication

---

## Slide 14 — Future Roadmap

| Phase | Feature | Technology |
|-------|---------|-----------|
| Phase 9 | Real-time messaging | WebSocket (Spring WebFlux) |
| Phase 10 | Payment gateway | Stripe integration |
| Phase 11 | Physical NFC hardware | Raspberry Pi + PN532 reader |
| Phase 12 | Real FCM tokens | Firebase device registration |
| Phase 13 | Multi-tenancy | Tenant isolation per gym chain |
| Phase 14 | Analytics pipeline | Kafka + Apache Flink |
| Phase 15 | Mobile push (real) | Firebase Cloud Messaging v1 |

> **Speaker notes:** Veltro is designed for extensibility. Each service can be independently scaled, replaced, or upgraded. Adding Stripe, for example, would only touch the shop-service — no other service needs to change.

---

*End of presentation. Total estimated time: 25–30 minutes with live demo.*
