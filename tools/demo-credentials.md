# Veltro Demo Credentials

**All accounts use password: `Veltro@2024`**

## Member Accounts

| Email | Name | Subscription | NFC Card |
|-------|------|-------------|---------|
| member1@veltro.gym | Alice Martin | MONTHLY (Active) | card001 |
| member2@veltro.gym | Bob Johnson | ANNUAL (Active) | card002 |
| member3@veltro.gym | Chloe Bernard | SESSION (Active) | card003 |
| member4@veltro.gym | David Moreau | MONTHLY (Active) | card004 |
| member5@veltro.gym | Emma Petit | TRIAL (Expiring soon) | card005 |
| member7@veltro.gym | Grace Laurent | MONTHLY (Expiring in 3 days) | card007 |

## Coach Accounts

| Email | Name | Specialization |
|-------|------|---------------|
| coach1@veltro.gym | Sophie Rousseau | Strength & Conditioning |
| coach2@veltro.gym | Thomas Garnier | Yoga & Mindfulness |
| coach3@veltro.gym | Laura Fontaine | Cardio & HIIT |

## Admin Account

| Email | Name | Role |
|-------|------|------|
| admin@veltro.gym | Admin Veltro | ADMIN |

## Service URLs

| Service | URL |
|---------|-----|
| Web Dashboard | http://localhost:3000 |
| API Gateway | http://localhost:8080 |
| Eureka Dashboard | http://localhost:8761 |
| RabbitMQ Management | http://localhost:15672 (veltro / veltro123) |
| Swagger — User Service | http://localhost:8082/swagger-ui.html |
| Swagger — Booking | http://localhost:8084/swagger-ui.html |
| Swagger — Activity | http://localhost:8085/swagger-ui.html |
| Swagger — Chatbot | http://localhost:8089/swagger-ui.html |

## Node-RED

| URL | http://localhost:1880 |
|-----|----------------------|
| JWT Env Var | `VELTRO_JWT` — set to admin JWT from POST /auth/login |
| NFC Cards | card001–card005 → members 1–5 |
