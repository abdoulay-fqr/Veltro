import axios from "axios";

/**
 * All API calls route through the Next.js proxy at /api/v1/...
 * The proxy reads the httpOnly access_token cookie and injects
 * Authorization: Bearer before forwarding to the Spring Boot gateway.
 *
 * On 401, we call the Next.js /api/auth/refresh route (not the proxy),
 * because that route handles setting the new httpOnly cookie correctly.
 */
const api = axios.create({
  baseURL: "/api/v1",
  headers: { "Content-Type": "application/json" },
  withCredentials: true,
});

let isRefreshing = false;
let pendingQueue: Array<{
  resolve: (value: unknown) => void;
  reject: (reason?: unknown) => void;
}> = [];

function flushQueue(error: unknown) {
  pendingQueue.forEach((p) => (error ? p.reject(error) : p.resolve(undefined)));
  pendingQueue = [];
}

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const original = error.config;

    // Only attempt refresh on 401; let 403/404/422/etc. pass straight through
    if (error.response?.status !== 401 || original._retried) {
      return Promise.reject(error);
    }

    // If already refreshing, queue this request until refresh completes
    if (isRefreshing) {
      return new Promise((resolve, reject) => {
        pendingQueue.push({ resolve, reject });
      })
        .then(() => api(original))
        .catch((err) => Promise.reject(err));
    }

    original._retried = true;
    isRefreshing = true;

    try {
      // Call the Next.js refresh route — it reads the httpOnly refresh_token
      // cookie, calls the gateway, and sets a new httpOnly access_token cookie
      const refreshRes = await fetch("/api/auth/refresh", { method: "POST" });
      if (!refreshRes.ok) {
        flushQueue(new Error("Session expired"));
        window.location.href = "/login";
        return Promise.reject(error);
      }

      flushQueue(null);
      return api(original);
    } catch (refreshError) {
      flushQueue(refreshError);
      window.location.href = "/login";
      return Promise.reject(refreshError);
    } finally {
      isRefreshing = false;
    }
  }
);

export default api;
