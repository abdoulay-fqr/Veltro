import api from "@/lib/api";
import type { PageResponse } from "./members";

export type SubscriptionStatus = "ACTIVE" | "PAUSED" | "CANCELLED" | "EXPIRED";
export type Plan = "TRIAL" | "SESSION" | "MONTHLY" | "ANNUAL";
export type PaymentMethod = "CARD" | "CASH" | "TRANSFER" | "AUTO_RENEWAL";

export interface SubscriptionResponse {
  id: number;
  memberId: number;
  memberEmail: string | null;
  plan: Plan;
  planPrice: number;
  status: SubscriptionStatus;
  startDate: string;
  endDate: string;
  autoRenew: boolean;
  pausedMonthsUsed: number;
  daysRemaining: number;
  createdAt: string;
  updatedAt: string;
}

export interface PaymentRecordResponse {
  id: number;
  subscriptionId: number;
  amount: number;
  paidAt: string;
  method: PaymentMethod;
  invoiceRef: string;
  createdAt: string;
}

export interface CreateSubscriptionRequest {
  memberId: number;
  memberEmail?: string;
  plan: Plan;
  paymentMethod?: PaymentMethod;
  autoRenew?: boolean;
}

export interface PlanInfo {
  plan: Plan;
  durationDays: number;
  price: number;
  description: string;
}

export const subscriptionsApi = {
  create(body: CreateSubscriptionRequest) {
    return api.post<{ data: SubscriptionResponse }>("/subscriptions", body);
  },

  getActive(memberId: number) {
    return api.get<{ data: SubscriptionResponse }>(`/subscriptions/${memberId}`);
  },

  getAll(memberId: number) {
    return api.get<{ data: SubscriptionResponse[] }>(`/subscriptions/${memberId}/all`);
  },

  list(params?: { page?: number; size?: number }) {
    return api.get<{ data: PageResponse<SubscriptionResponse> }>("/subscriptions", { params });
  },

  cancel(id: number) {
    return api.put<{ data: SubscriptionResponse }>(`/subscriptions/${id}/cancel`);
  },

  pause(id: number) {
    return api.put<{ data: SubscriptionResponse }>(`/subscriptions/${id}/pause`);
  },

  resume(id: number) {
    return api.put<{ data: SubscriptionResponse }>(`/subscriptions/${id}/resume`);
  },

  toggleAutoRenew(id: number) {
    return api.put<{ data: SubscriptionResponse }>(`/subscriptions/${id}/auto-renew`);
  },

  getInvoices(memberId: number) {
    return api.get<{ data: PaymentRecordResponse[] }>(`/subscriptions/${memberId}/invoices`);
  },

  getPlans() {
    return api.get<{ data: PlanInfo[] }>("/subscriptions/plans");
  },

  reports: {
    summary() {
      return api.get<{ data: Record<string, number> }>("/subscriptions/reports/summary");
    },
    revenue() {
      return api.get<{ data: Array<{ year: number; month: number; total: number }> }>(
        "/subscriptions/reports/revenue"
      );
    },
    distribution() {
      return api.get<{ data: Array<{ plan: Plan; count: number }> }>(
        "/subscriptions/reports/distribution"
      );
    },
  },
};
