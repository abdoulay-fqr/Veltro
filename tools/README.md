# Veltro Virtual Hardware Tools

This directory contains Node-RED flows that simulate physical gym hardware for development and demo purposes.

## Prerequisites

- Node.js 18+
- Veltro backend services running (docker-compose up)
- A valid JWT token from `/api/v1/auth/login`

## Install Node-RED

```bash
npm install -g node-red
```

## Start Node-RED

```bash
node-red
```

Then open `http://localhost:1880` in your browser.

## Import the Flows

1. Open Node-RED UI at `http://localhost:1880`
2. Click the hamburger menu (top-right) → **Import**
3. Click **select a file to import** and choose `nodered-flows.json`
4. Click **Import**

## Configure the JWT Token

The flows use an environment variable `VELTRO_JWT` for the Authorization header.

### Option A: Node-RED Environment Variable (recommended)
In Node-RED, go to **Menu → Settings → Environment Variables** and add:
```
VELTRO_JWT=your_jwt_token_here
```

### Option B: Edit the function node directly
Open the "Toggle IN/OUT" function node and replace `env.get('VELTRO_JWT')` with your token string.

### Getting a JWT token
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"identifier":"admin@veltro.gym","password":"yourpassword"}'
```
Copy the `accessToken` from the response.

## Flow 1 — NFC Scan Simulator

**Tab:** "NFC Scan Simulator"

Simulates 5 virtual NFC cards (card001–card005). Each card must be activated in the User Service first.

**How to use:**
1. Activate a card: `POST /api/v1/nfc/activate` with `{"memberProfileId": 1, "cardUid": "card001"}`
2. Click the **Scan card001** inject button to simulate an IN scan
3. Click again to simulate an OUT scan (automatically toggles IN/OUT)
4. The **Entry Result** debug node shows the member name and timestamp returned by the API

**Flow:** Inject → Toggle IN/OUT → POST `/api/v1/activity/entry` → Format response → Debug

**Error handling:**
- `✅` = successful entry recorded
- `❌` = card deactivated or member suspended
- `⚠️` = service unavailable (user-service down) or unexpected error

## Flow 2 — Machine Session Injector

**Tab:** "Machine Session Injector"

Injects randomized machine workout sessions directly into the activity service.

**How to use:**
1. Set the correct `memberId` in the inject payload (default: 1)
2. Click any inject button (Treadmill, Bike, or Rowing)
3. The function node randomizes:
   - `durationMinutes`: 20–90 min
   - `caloriesBurned`: 200–800 kcal
   - `avgHeartRate`: 110–175 bpm
   - `distanceKm`: 1.0–15.0 km
4. The **Session Result** debug node confirms the session was saved

**Flow:** Inject → Randomize values → POST `/api/v1/activity/session` → Format response → Debug

## Smoke Test Instructions (manual)

After importing the flows and configuring JWT:

1. **NFC Entry Test:**
   - Click "Scan card001" → expect `✅ IN | [MemberName] @ [timestamp]`
   - Click again → expect `✅ OUT | [MemberName] @ [timestamp]`
   - Check admin live log: `GET /api/v1/activity/entries/live`

2. **Machine Session Test:**
   - Click "Inject Session" (Treadmill)
   - Check member stats: `GET /api/v1/activity/stats/{memberId}`
   - Verify `totalSessions` incremented and `weeklyCalories` updated

3. **Admin Heatmap Test:**
   - Run multiple NFC scans at different times
   - Check: `GET /api/v1/activity/stats/admin`
   - Verify `entriesByHour` map reflects scan times

4. **Low Activity Alert Test:**
   - Ensure a member has < 2 sessions this week
   - Trigger the scheduler manually (or wait for Monday 8am UTC)
   - Check notification-service logs for `LowActivityAlert` event
