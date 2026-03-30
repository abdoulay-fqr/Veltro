# Veltro — Architecture & Coding Standards

## REST Conventions
- Base path: /api/v1/*
- All timestamps: UTC (ISO 8601)
- All responses wrapped in ApiResponse<T>
- HTTP status codes: 200 OK, 201 Created, 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found, 500 Internal Server Error

## Microservices & Ports
| Service               | Port |
|-----------------------|------|
| Eureka Server         | 8761 |
| API Gateway           | 8080 |
| Auth Service          | 8081 |
| User Service          | 8082 |
| Subscription Service  | 8083 |
| Booking Service       | 8084 |
| Activity Service      | 8085 |
| Notification Service  | 8086 |
| Chatbot Service       | 8087 |

## JWT Flow
1. Client sends credentials to POST /api/v1/auth/login
2. Auth Service validates and returns access token (15min) + refresh token (1day)
3. Client sends Bearer token in Authorization header on every request
4. API Gateway validates JWT and forwards X-User-Id and X-User-Role headers
5. Microservices trust these headers — they never validate JWT themselves
6. On expiry, client calls POST /api/v1/auth/refresh with refresh token

## RabbitMQ Events
| Event                  | Producer              | Consumer              |
|------------------------|-----------------------|-----------------------|
| UserCreated            | User Service          | Subscription Service  |
| SubscriptionExpiring   | Subscription Service  | Notification Service  |
| CourseCancelled        | Booking Service       | Notification Service  |
| WaitlistPromoted       | Booking Service       | Notification Service  |
| MemberWarning          | Booking Service       | Notification Service  |
| LowActivityAlert       | Activity Service      | Notification Service  |
| MessageReceived        | Messaging Service     | Notification Service  |

## Branch Strategy
- main: protected, stable, defence-ready
- develop: integration branch, all work merges here
- feature/phase-X-name: one branch per phase

## Commit Convention
- feat(scope): add new feature
- fix(scope): bug fix
- chore(scope): infrastructure / config changes
- test(scope): adding tests
- docs(scope): documentation only
