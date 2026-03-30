# Veltro — Connected Gym Management Platform

A full-stack microservices platform for modern gyms. Built with Spring Boot, Next.js, and Flutter.

## Stack
- Backend: Spring Boot 3 · 8 Microservices · Eureka · Spring Cloud Gateway
- Frontend Web: Next.js 14 + React TypeScript + Tailwind + shadcn/ui
- Frontend Mobile: Flutter (iOS & Android)
- Messaging: RabbitMQ
- Database: MySQL (one schema per service)
- AI: Google Gemini API
- Virtual Hardware: Node-RED

## Quickstart
docker-compose -f docker-compose.dev.yml up --build -d

Eureka dashboard: http://localhost:8761
API Gateway: http://localhost:8080
RabbitMQ UI: http://localhost:15672
Web Dashboard: http://localhost:3000
