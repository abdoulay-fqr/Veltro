# Veltro — Architecture Reference

This document maps all **17 software architecture patterns** demonstrated in the Veltro platform to specific code locations.

---

## 1. Microservices

**11 independently deployable Spring Boot services**, each owning its own process, database, and deployment unit.

| Service | Port | Package | Main Class |
|---------|------|---------|-----------|
| Eureka Server | 8761 | com.veltro.eureka | EurekaServerApplication |
| API Gateway | 8080 | com.veltro.api_gateway | ApiGatewayApplication |
| Auth Service | 8081 | com.veltro.auth | AuthServiceApplication |
| User Service | 8082 | com.veltro.user | UserServiceApplication |
| Subscription Service | 8083 | com.veltro.subscription | SubscriptionServiceApplication |
| Booking Service | 8084 | com.veltro.booking | BookingServiceApplication |
| Activity Service | 8085 | com.veltro.activity | ActivityServiceApplication |
| Notification Service | 8086 | com.veltro.notification | NotificationServiceApplication |
| Messaging Service | 8087 | com.veltro.messaging | MessagingServiceApplication |
| Shop Service | 8088 | com.veltro.shop | ShopServiceApplication |
| Chatbot Service | 8089 | com.veltro.chatbot | ChatbotServiceApplication |

---

## 2. Domain-Driven Design / Bounded Context

Each service is a bounded context with its own domain model, own database, and no cross-context entity sharing.

| Service | Domain Entities | Database |
|---------|----------------|---------|
| auth-service | AppUser, TokenBlacklist | veltro_auth |
| user-service | MemberProfile, CoachProfile, NfcCard, HealthProfile | veltro_user |
| subscription-service | Subscription, PaymentRecord | veltro_subscription |
| booking-service | Course, CourseRegistration, Attendance | veltro_booking |
| activity-service | GymEntry, MachineSession | veltro_activity |
| messaging-service | Conversation, Message | veltro_messaging |
| shop-service | Product, ShopOrder, OrderItem | veltro_shop |

---

## 3. Decomposition by Business Capability

Services are decomposed by **business capability** not by technical layer:
- Authentication capability → auth-service
- Member management capability → user-service
- Billing capability → subscription-service
- Scheduling capability → booking-service
- Fitness tracking capability → activity-service
- Communication capability → messaging-service
- Commerce capability → shop-service
- AI assistance capability → chatbot-service
- Notification delivery → notification-service

---

## 4. Communication Patterns

### Synchronous (REST via Feign)
| Caller | Callee | Endpoint |
|--------|--------|---------|
| booking-service | subscription-service | GET /api/v1/subscriptions/{memberId} |
| booking-service | user-service | PUT /api/v1/users/members/{id}/suspend |
| activity-service | user-service | POST /api/v1/nfc/simulate-scan |
| chatbot-service | subscription-service | GET /api/v1/subscriptions/{memberId} |
| chatbot-service | booking-service | GET /api/v1/bookings/member/{memberId} |
| chatbot-service | activity-service | GET /api/v1/activity/entries/{memberId} |

### Asynchronous (RabbitMQ Domain Events)
| Event | Exchange | Routing Key | Publisher | Consumer |
|-------|----------|-------------|---------|---------|
| UserCreatedEvent | veltro.user.exchange | user.created | user-service | subscription-service |
| NfcCardActivatedEvent | veltro.user.exchange | nfc.card.activated | user-service | — |
| SubscriptionExpiringEvent | veltro.subscription.exchange | subscription.expiring | subscription-service | notification-service |
| GymEntryRecordedEvent | veltro.activity.exchange | activity.entry.recorded | activity-service | — |
| LowActivityAlertEvent | veltro.activity.exchange | activity.low-activity.alert | activity-service | notification-service |
| CourseCancelledEvent | veltro.booking.exchange | course.cancelled | booking-service | notification-service |
| WaitlistPromotedEvent | veltro.booking.exchange | waitlist.promoted | booking-service | notification-service |
| MemberWarningEvent | veltro.booking.exchange | member.warning | booking-service | notification-service |
| CourseReminderEvent | veltro.booking.exchange | course.reminder | booking-service | notification-service |
| MessageReceivedEvent | veltro.messaging.exchange | message.received | messaging-service | notification-service |
| OrderPlacedEvent | veltro.shop.exchange | order.placed | shop-service | notification-service |

---

## 5. Service Discovery

**Eureka Server** at port 8761. All services auto-register on startup.

- Config: `eureka.client.service-url.defaultZone=http://localhost:8761/eureka/` in each `application.properties`
- Dependency: `spring-cloud-starter-netflix-eureka-client` in each service's `pom.xml`

---

## 6. Load Balancing

**Spring Cloud LoadBalancer** (included in spring-cloud-starter-netflix-eureka-client) resolves service instances from Eureka for Feign client calls. Configured implicitly — no additional code needed.

When multiple instances of a service are deployed, LoadBalancer distributes requests round-robin across all healthy instances registered in Eureka.

---

## 7. API Gateway

- **Class**: `backend/api-gateway/src/main/java/com/veltro/api_gateway/filter/JwtAuthenticationFilter.java`
- **Routes**: `backend/api-gateway/src/main/resources/application.properties` (routes[0]–[10])
- **Behavior**: validates JWT → extracts `userId` + `role` → forwards as `X-User-Id` + `X-User-Role` headers → downstream services trust headers, never re-validate JWT

Public paths (bypassed): `/api/v1/auth/**`, `/actuator/**`

---

## 8. Database per Service

Each service has its own MySQL database instance on a dedicated port. No shared schemas, no cross-schema JOINs.

| Service | JDBC URL |
|---------|---------|
| auth-service | `jdbc:mysql://localhost:3307/veltro_auth` |
| user-service | `jdbc:mysql://localhost:3308/veltro_user` |
| subscription-service | `jdbc:mysql://localhost:3309/veltro_subscription` |
| booking-service | `jdbc:mysql://localhost:3310/veltro_booking` |
| activity-service | `jdbc:mysql://localhost:3311/veltro_activity` |
| shop-service | `jdbc:mysql://localhost:3312/veltro_shop` |
| messaging-service | `jdbc:mysql://localhost:3313/veltro_messaging` |

Migrations: Flyway `V1__create_*_tables.sql` in each service's `src/main/resources/db/migration/`

---

## 9. CQRS (Command Query Responsibility Segregation)

Commands and queries are separated into distinct service classes in: booking-service, activity-service, shop-service.

**Booking Service:**
- Commands: `CourseCommandService`, `BookingCommandService`, `AttendanceService`
- Queries: `CourseQueryService`, `BookingQueryService`

**Activity Service:**
- Commands: `EntryCommandService`, `SessionCommandService`
- Queries: `ActivityQueryService`

**Shop Service:**
- Commands: `ProductCommandService`, `OrderCommandService`
- Queries: `ProductQueryService`, `OrderQueryService`

**Chatbot Service Context Enrichment** = CQRS query side in action: `ContextEnrichmentService` makes read-only calls to 3 services to build a temporary read model for the AI prompt.

---

## 10. Event Sourcing (Append-Only Log)

**GymEntry** (`backend/activity-service/.../entity/GymEntry.java`) is an immutable append-only log:
- Entries are never updated or deleted
- IN/OUT pairs are linked via `sessionId` (UUID)
- Full history queryable from day 1

**CourseRegistration** (`backend/booking-service/.../entity/CourseRegistration.java`) is an audit trail:
- All status transitions stored: `registeredAt`, `cancelledAt`, `promotedAt`
- Waitlist position tracked at each change

---

## 11. Domain Events

All event classes are in `event/` packages within each service:
- `backend/user-service/src/main/java/com/veltro/user/event/`
- `backend/subscription-service/src/main/java/com/veltro/subscription/event/`
- `backend/booking-service/src/main/java/com/veltro/booking/event/`
- `backend/activity-service/src/main/java/com/veltro/activity/event/`
- `backend/messaging-service/src/main/java/com/veltro/messaging/event/`
- `backend/shop-service/src/main/java/com/veltro/shop/event/`

All events are serialized as JSON via `Jackson2JsonMessageConverter` and published to topic exchanges.

---

## 12. Circuit Breaker

**Resilience4j** circuit breakers on all inter-service HTTP calls via OpenFeign.

Config location: `resilience4j.circuitbreaker.instances.*` in each service's `application.properties`

| Service | Client | CB Name | Fallback |
|---------|--------|---------|---------|
| booking-service | SubscriptionClient | subscription-cb | Skip subscription check → reject booking |
| booking-service | UserClient | user-service | Log warn, suspension deferred |
| activity-service | NfcClient | user-service | ServiceUnavailableException → 503 |
| chatbot-service | SubscriptionClient | subscription-cb | Skip context block, continue with less data |
| chatbot-service | BookingClient | booking-cb | Skip bookings context block |
| chatbot-service | ActivityClient | activity-cb | Skip activity context block |

Fallbacks implemented as `FallbackFactory` inner classes in each Feign client interface.

---

## 13. Security

**JWT Token Security:**
- Filter: `JwtAuthenticationFilter.java` in api-gateway
- JWT utility: `common-lib/src/main/java/com/veltro/common/util/JwtUtil.java`
- Token blacklist: `backend/auth-service/.../entity/TokenBlacklist.java`
- Access token: 15 min | Refresh token: 24 hours
- Downstream services trust `X-User-Id` + `X-User-Role` headers (never re-validate JWT)

**RBAC:** role checks in controller methods via `requireAdmin(role)` / `requireCoach(role)` guard methods.

---

## 14. Event-Driven Architecture

RabbitMQ topology with **6 topic exchanges** and **DLX dead-letter exchange**:

```
veltro.user.exchange        → user.created, nfc.card.activated
veltro.subscription.exchange → subscription.expiring
veltro.booking.exchange     → course.cancelled, waitlist.promoted, member.warning, course.reminder
veltro.activity.exchange    → activity.entry.recorded, activity.session.recorded, activity.low-activity.alert
veltro.messaging.exchange   → message.received
veltro.shop.exchange        → order.placed

veltro.notification.dlx     → dead-letter exchange for failed notifications
```

Each notification queue is configured with `x-dead-letter-exchange` pointing to `veltro.notification.dlx`.

---

## 15. Service Registry

**Netflix Eureka Server** at `backend/eureka-server/`:
- All 11 services register on startup
- Heartbeat interval: default 30s
- Lease expiry: 90s
- Dashboard: http://localhost:8761

---

## 16. Fault Tolerance

| Mechanism | Location | Behavior |
|-----------|---------|---------|
| DLQ chains | notification-service RabbitMQConfig | 3 retries with exponential backoff (2s base, ×2 multiplier) → message goes to `.dlq` queue |
| Circuit breakers | All Feign clients | Opens after 50% failures in 5-request sliding window, waits 10–15s before half-open |
| Rate limiting | chatbot-service RateLimiterService | In-memory sliding window: 20 req/hour per userId → HTTP 429 |
| Graceful degradation | ContextEnrichmentService | Each service call wrapped in try/catch — chatbot works even if all 3 services are down |
| Optimistic locking | shop-service Product entity | `@Version` field — concurrent orders fail with ObjectOptimisticLockingFailureException → HTTP 409 |

---

## 17. Distributed Transactions (Saga Pattern)

**Order Placement Saga** (`OrderCommandService.placeOrder`):
1. Validate stock for all products (local read)
2. Decrement stock atomically (@Transactional)
3. Create Order + OrderItems (local write)
4. Publish `OrderPlacedEvent` (async, best-effort)
→ No 2PC; if event publish fails, order is still saved

**Absence Suspension Saga** (`AttendanceService.processAbsence`):
1. Mark attendance (local write)
2. Count unprocessed absences (local read)
3. If ≥ 3: call user-service via Feign to suspend member
4. Mark absences as `processedForSuspension=true` (local write)
5. Publish `MemberWarningEvent` (async)
→ Eventual consistency; user-service suspension is HTTP call with circuit breaker

**UserCreated → TRIAL Subscription Saga**:
1. user-service creates member, publishes `UserCreatedEvent`
2. subscription-service consumes event from `veltro.subscription.user-created.queue`
3. Creates TRIAL subscription for memberId
→ Fully async; idempotent (won't create duplicate if already exists)

---

## REST Conventions

- Base path: `/api/v1/*`
- All timestamps: UTC (ISO 8601), enforced via `spring.jackson.time-zone=UTC`
- All responses wrapped in `ApiResponse<T>` from `common-lib`
- HTTP status codes: 200, 201, 400, 401, 403, 404, 409, 422, 429, 503

## Commit Convention

```
feat(scope): new feature
fix(scope): bug fix
chore(scope): infrastructure/config
test(scope): tests
docs(scope): documentation
```
