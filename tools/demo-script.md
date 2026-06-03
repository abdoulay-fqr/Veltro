# Veltro — 3-Minute Demo Script

## Prerequisites
- All services running: `docker-compose -f docker-compose.dev.yml up -d`
- Seeds loaded (see tools/seeds/README.md)
- Web app at http://localhost:3000
- Node-RED at http://localhost:1880 with VELTRO_JWT configured

---

## Demo Flow

### Slide 1 — Admin Dashboard (30 seconds)
1. Open http://localhost:3000/login → log in as **admin@veltro.gym / Veltro@2024**
2. Show the sidebar: Members, Subscriptions, Revenue Reports, Activity, Shop

### Slide 2 — Revenue & Subscriptions (20 seconds)
3. Click **Revenue Reports** → show bar chart (monthly revenue) + pie chart (plan distribution)
4. Point out: "Revenue automatically calculated from all confirmed orders and subscriptions"

### Slide 3 — Activity Dashboard (30 seconds)
5. Click **Activity** → show occupancy heatmap + live entry log
6. Switch to Node-RED → click "Scan card001" (Alice IN)
7. The web dashboard **auto-refreshes** (5s polling) and shows Alice's entry in real time

### Slide 4 — Member Experience (mobile) (30 seconds)
8. Open Flutter app → log in as **member1@veltro.gym**
9. Show HomeScreen → tap "My Subscription" → see Alice's MONTHLY plan with progress bar
10. Tap "My Performance" → show 30-day activity streak + bar chart

### Slide 5 — Course Booking (20 seconds)
11. On mobile: tap "Classes" → browse courses list → tap "Morning Strength"
12. Tap "Book" → booking confirmed with animation
13. Show web dashboard → "Course Stats" → fill rate updated

### Slide 6 — AI Chatbot (20 seconds)
14. On web: click the teal chat bubble (bottom-right corner)
15. Click chip: "My next class?"
16. AI responds with Alice's actual upcoming booking (fetched from booking-service)
17. Ask: "How many sessions did I have this week?" → AI reads from activity-service

### Slide 7 — Shop (10 seconds)
18. On mobile: tap "Shop" → browse products grid → add Whey Protein to cart
19. Tap cart → enter address → "Place Order" → success animation

---

## Key Demo Points to Mention
- **All data is real**: AI reads live subscription/booking/activity data
- **Event-driven**: NFC scan → activity stored → appears in admin dashboard instantly
- **Fault tolerant**: if one service is down, chatbot still works with partial context
- **17 architecture concepts** all demonstrated in this single flow

---

## Backup Plan (if live demo fails)
- Show Swagger UI: http://localhost:8089/swagger-ui.html (chatbot)
- Show Eureka: http://localhost:8761 (all 8 services green)
- Show RabbitMQ: http://localhost:15672 → Exchanges → show all veltro.*.exchange
