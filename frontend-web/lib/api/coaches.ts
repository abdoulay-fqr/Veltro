import api from "@/lib/api";
import type { PageResponse } from "./members";

export interface CoachResponse {
  id: number;
  userId: number;
  firstname: string;
  lastname: string;
  phone: string | null;
  dateOfBirth: string | null;
  avatarUrl: string | null;
  bio: string | null;
  specialization: string | null;
  certifications: string | null;
  status: "ACTIVE" | "SUSPENDED";
  createdAt: string;
  updatedAt: string;
}

export interface CreateCoachRequest {
  userId: number;
  identifier: string;
  firstname: string;
  lastname: string;
  phone?: string;
  bio?: string;
  specialization?: string;
  certifications?: string;
}

export const coachesApi = {
  // Returns the coach profile for the currently authenticated coach (uses X-User-Id header)
  getMe() {
    return api.get<{ data: CoachResponse }>("/users/coaches/me");
  },

  list(params?: { status?: string; page?: number; size?: number }) {
    return api.get<{ data: PageResponse<CoachResponse> }>("/users/coaches", { params });
  },

  getById(id: number) {
    return api.get<{ data: CoachResponse }>(`/users/coaches/${id}`);
  },

  create(body: CreateCoachRequest) {
    return api.post<{ data: CoachResponse }>("/users/coaches", body);
  },

  update(id: number, body: Partial<CreateCoachRequest>) {
    return api.put<{ data: CoachResponse }>(`/users/coaches/${id}`, body);
  },

  delete(id: number) {
    return api.delete<{ data: null }>(`/users/coaches/${id}`);
  },

  suspend(id: number) {
    return api.put<{ data: CoachResponse }>(`/users/coaches/${id}/suspend`);
  },

  activate(id: number) {
    return api.put<{ data: CoachResponse }>(`/users/coaches/${id}/activate`);
  },

  uploadAvatar(id: number, file: File) {
    const form = new FormData();
    form.append("file", file);
    return api.post<{ data: CoachResponse }>(`/users/coaches/${id}/avatar`, form, {
      headers: { "Content-Type": "multipart/form-data" },
    });
  },
};
