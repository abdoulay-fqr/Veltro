import { toast } from "sonner";

type ApiErrorShape = {
  response?: {
    data?: { message?: string; success?: boolean };
    status?: number;
  };
  message?: string;
};

/**
 * Extract the user-facing message from an API error and show it as a toast.
 *
 * Priority:
 *  1. err.response.data.message  (backend ApiResponse<T>.message field)
 *  2. fallback                   (caller-provided human-readable default)
 *
 * HTTP-specific overrides:
 *  401 → "Your session has expired. Please log in again."
 *  403 → "You don't have permission to perform this action."
 *  429 → "Too many requests. Please wait a moment."
 *  503 → "Service temporarily unavailable. Please try again."
 */
export function apiError(err: unknown, fallback = "Something went wrong"): void {
  const e = err as ApiErrorShape;
  const status = e.response?.status;
  const serverMsg = e.response?.data?.message;

  let message: string;

  switch (status) {
    case 401:
      message = "Your session has expired. Please log in again.";
      break;
    case 403:
      message = "You don't have permission to perform this action.";
      break;
    case 429:
      message = serverMsg ?? "Too many requests. Please wait a moment and try again.";
      break;
    case 503:
      message = "Service temporarily unavailable. Please try again shortly.";
      break;
    default:
      message = serverMsg ?? fallback;
  }

  toast.error(message);
}

/**
 * Same as apiError but also returns the message string for cases where
 * the caller needs to use it (e.g. inline form error display).
 */
export function extractApiMessage(
  err: unknown,
  fallback = "Something went wrong"
): string {
  const e = err as ApiErrorShape;
  const status = e.response?.status;
  const serverMsg = e.response?.data?.message;

  if (status === 401) return "Your session has expired.";
  if (status === 403) return "Permission denied.";
  if (status === 429) return serverMsg ?? "Too many requests.";
  if (status === 503) return "Service temporarily unavailable.";
  return serverMsg ?? fallback;
}
