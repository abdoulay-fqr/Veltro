import api from "@/lib/api";

export interface MemberResponse {
  id: number;
  userId: number;
  email: string | null;
  firstname: string;
  lastname: string;
  phone: string | null;
  dateOfBirth: string | null;
  avatarUrl: string | null;
  status: "ACTIVE" | "SUSPENDED";
  createdAt: string;
  updatedAt: string;
}

export interface CreateMemberRequest {
  userId: number;
  identifier: string;
  firstname: string;
  lastname: string;
  phone?: string;
  dateOfBirth?: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export const membersApi = {
  list(params?: { status?: string; page?: number; size?: number }) {
    return api.get<{ data: PageResponse<MemberResponse> }>("/users/members", { params });
  },

  getById(id: number) {
    return api.get<{ data: MemberResponse }>(`/users/members/${id}`);
  },

  create(body: CreateMemberRequest) {
    return api.post<{ data: MemberResponse }>("/users/members", body);
  },

  update(id: number, body: Partial<CreateMemberRequest>) {
    return api.put<{ data: MemberResponse }>(`/users/members/${id}`, body);
  },

  delete(id: number) {
    return api.delete<{ data: null }>(`/users/members/${id}`);
  },

  suspend(id: number) {
    return api.put<{ data: MemberResponse }>(`/users/members/${id}/suspend`);
  },

  activate(id: number) {
    return api.put<{ data: MemberResponse }>(`/users/members/${id}/activate`);
  },

  uploadAvatar(id: number, file: File) {
    const form = new FormData();
    form.append("file", file);
    return api.post<{ data: MemberResponse }>(`/users/members/${id}/avatar`, form, {
      headers: { "Content-Type": "multipart/form-data" },
    });
  },
};
