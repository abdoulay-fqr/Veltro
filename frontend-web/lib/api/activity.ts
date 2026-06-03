import api from "@/lib/api";

export type Direction = "IN" | "OUT";
export type MachineType = "TREADMILL" | "BIKE" | "ROWING" | "WEIGHTS" | "OTHER";

export interface EntryResponse {
  id: number;
  memberId: number;
  memberName: string | null;
  cardUid: string;
  direction: Direction;
  timestamp: string;
  sessionId: string | null;
}

export interface SessionResponse {
  id: number;
  memberId: number;
  machineType: MachineType;
  durationMinutes: number;
  caloriesBurned: number | null;
  distanceKm: number | null;
  avgHeartRate: number | null;
  recordedAt: string;
}

export interface MemberStatsResponse {
  memberId: number;
  weeklyCalories: number;
  totalSessions: number;
  avgHeartRate: number | null;
  currentStreak: number;
  maxCaloriesInSession: number;
  maxDistanceKm: number;
  maxDurationMinutes: number;
}

export interface AdminStatsResponse {
  entriesByHour: Record<number, number>;
  peakHour: number;
  todayEntries: number;
  weeklyEntries: number;
  currentOccupancy: number;
}

export const activityApi = {
  recordEntry(cardUid: string, direction: Direction) {
    return api.post<{ data: EntryResponse }>("/activity/entry", { cardUid, direction });
  },

  recordSession(body: {
    memberId: number; memberEmail?: string; machineType: MachineType;
    durationMinutes: number; caloriesBurned?: number; distanceKm?: number; avgHeartRate?: number;
  }) {
    return api.post<{ data: SessionResponse }>("/activity/session", body);
  },

  getEntries(memberId: number, params?: { from?: string; to?: string; page?: number; size?: number }) {
    return api.get<{ data: { content: EntryResponse[]; totalElements: number } }>(
      `/activity/entries/${memberId}`, { params }
    );
  },

  getSessions(memberId: number, params?: { page?: number; size?: number }) {
    return api.get<{ data: { content: SessionResponse[]; totalElements: number } }>(
      `/activity/sessions/${memberId}`, { params }
    );
  },

  getMemberStats(memberId: number) {
    return api.get<{ data: MemberStatsResponse }>(`/activity/stats/${memberId}`);
  },

  getLiveEntries() {
    return api.get<{ data: EntryResponse[] }>("/activity/entries/live");
  },

  getAdminStats() {
    return api.get<{ data: AdminStatsResponse }>("/activity/stats/admin");
  },
};
