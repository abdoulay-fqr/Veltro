/**
 * Veltro Platform — k6 Load Test
 * Target: 100 virtual users, 5 minutes
 * Goal:   p95 < 500ms, error rate < 1%
 *
 * Run: k6 run tools/k6-load-test.js
 * With summary: k6 run --summary-trend-stats="min,avg,med,p(95),p(99),max" tools/k6-load-test.js
 */
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// Custom metrics
const errorRate   = new Rate('error_rate');
const loginDur    = new Trend('login_duration',    true);
const coursesDur  = new Trend('courses_duration',  true);
const bookingDur  = new Trend('booking_duration',  true);
const chatbotDur  = new Trend('chatbot_duration',  true);

// ── Config ────────────────────────────────────────────────────────────────────
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080/api/v1';

export const options = {
  stages: [
    { duration: '1m',  target: 25  },   // ramp-up
    { duration: '3m',  target: 100 },   // steady state
    { duration: '1m',  target: 0   },   // ramp-down
  ],
  thresholds: {
    http_req_duration:         ['p(95)<500'],  // 95% under 500ms
    error_rate:                ['rate<0.01'],  // < 1% errors
    login_duration:            ['p(95)<300'],
    courses_duration:          ['p(95)<400'],
    booking_duration:          ['p(95)<500'],
    chatbot_duration:          ['p(95)<5000'], // AI calls can be slower
    http_req_failed:           ['rate<0.01'],
  },
};

// ── Helper ────────────────────────────────────────────────────────────────────
function jsonHeaders(token) {
  const h = { 'Content-Type': 'application/json' };
  if (token) h['Authorization'] = `Bearer ${token}`;
  return { headers: h };
}

function ok(res, name) {
  const success = res.status >= 200 && res.status < 300;
  errorRate.add(!success);
  check(res, {
    [`${name} → 2xx`]: () => success,
  });
  return success;
}

// ── Seed accounts (must exist before load test) ───────────────────────────────
const USERS = [
  { identifier: 'member1@veltro.gym',  password: 'Veltro@2024' },
  { identifier: 'member2@veltro.gym',  password: 'Veltro@2024' },
  { identifier: 'member3@veltro.gym',  password: 'Veltro@2024' },
  { identifier: 'member4@veltro.gym',  password: 'Veltro@2024' },
  { identifier: 'member5@veltro.gym',  password: 'Veltro@2024' },
];

// ── VU logic ──────────────────────────────────────────────────────────────────
export default function () {
  const user = USERS[Math.floor(Math.random() * USERS.length)];

  // 1. Login
  const start1 = Date.now();
  const loginRes = http.post(
    `${BASE_URL}/auth/login`,
    JSON.stringify({ identifier: user.identifier, password: user.password }),
    jsonHeaders()
  );
  loginDur.add(Date.now() - start1);
  if (!ok(loginRes, 'login')) { sleep(1); return; }

  const token = loginRes.json('data.accessToken');
  if (!token) { errorRate.add(1); sleep(1); return; }

  sleep(0.5);

  // 2. List courses
  const start2 = Date.now();
  const coursesRes = http.get(
    `${BASE_URL}/courses?size=10&status=SCHEDULED`,
    jsonHeaders(token)
  );
  coursesDur.add(Date.now() - start2);
  ok(coursesRes, 'list courses');

  sleep(0.5);

  // 3. Get member activity stats
  const memberId = loginRes.json('data.userId');
  if (memberId) {
    http.get(`${BASE_URL}/activity/stats/${memberId}`, jsonHeaders(token));
    sleep(0.3);
  }

  // 4. Get subscription
  if (memberId) {
    http.get(`${BASE_URL}/subscriptions/${memberId}`, jsonHeaders(token));
    sleep(0.3);
  }

  // 5. Try to book a course (if courses exist)
  const courses = coursesRes.json('data.content');
  if (courses && courses.length > 0) {
    const courseId = courses[Math.floor(Math.random() * courses.length)].id;
    const start4 = Date.now();
    const bookRes = http.post(
      `${BASE_URL}/bookings`,
      JSON.stringify({ courseId, memberEmail: user.identifier }),
      jsonHeaders(token)
    );
    bookingDur.add(Date.now() - start4);
    // 4xx is acceptable (already booked, subscription issues, etc.)
    check(bookRes, { 'booking attempt responded': () => bookRes.status !== 0 });
    errorRate.add(bookRes.status >= 500);
  }

  sleep(0.5);

  // 6. Chatbot message (every 5th VU to reduce Gemini load)
  if (__VU % 5 === 0 && memberId) {
    const start5 = Date.now();
    const chatRes = http.post(
      `${BASE_URL}/chat/message`,
      JSON.stringify({
        userId: memberId,
        message: 'What is my active subscription?',
        history: [],
      }),
      jsonHeaders(token)
    );
    chatbotDur.add(Date.now() - start5);
    ok(chatRes, 'chatbot');
  }

  sleep(1);
}

export function handleSummary(data) {
  console.log('\n=== VELTRO LOAD TEST SUMMARY ===');
  console.log(`Total requests:  ${data.metrics.http_reqs.values.count}`);
  console.log(`Error rate:      ${(data.metrics.http_req_failed.values.rate * 100).toFixed(2)}%`);
  console.log(`p95 latency:     ${data.metrics.http_req_duration.values['p(95)'].toFixed(0)}ms`);
  console.log('================================\n');
  return {};
}
