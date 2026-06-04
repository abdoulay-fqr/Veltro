import api from "@/lib/api";
import type { PageResponse } from "./members";

export type CourseLevel = "BEGINNER" | "INTERMEDIATE" | "ADVANCED";
export type CourseStatus = "SCHEDULED" | "CANCELLED" | "COMPLETED";
export type RegistrationStatus = "BOOKED" | "WAITLISTED" | "CANCELLED";

export interface CourseResponse {
  id: number;
  coachId: number;
  name: string;
  description: string | null;
  dateTime: string;
  durationMinutes: number;
  capacity: number;
  enrolledCount: number;
  availableSpots: number;
  fillRate: number;
  level: CourseLevel;
  room: string | null;
  status: CourseStatus;
  createdAt: string;
  updatedAt: string;
}

export interface BookingResponse {
  id: number;
  courseId: number;
  memberId: number;
  memberEmail: string | null;
  status: RegistrationStatus;
  waitlistPosition: number | null;
  registeredAt: string;
  cancelledAt: string | null;
}

export interface AttendanceResponse {
  id: number;
  courseId: number;
  memberId: number;
  memberEmail: string | null;
  present: boolean;
  markedAt: string;
  markedByCoachId: number;
}

export interface CreateCourseRequest {
  coachId?: number;       // admin sets this; COACH endpoint ignores it (uses X-User-Id)
  name: string;
  description?: string;
  dateTime: string;
  durationMinutes: number;
  capacity: number;
  level: CourseLevel;
  room?: string;
}

export const coursesApi = {
  list(params?: {
    from?: string; to?: string; level?: CourseLevel;
    coachId?: number; status?: CourseStatus; page?: number; size?: number;
  }) {
    return api.get<{ data: PageResponse<CourseResponse> }>("/courses", { params });
  },

  getById(id: number) {
    return api.get<{ data: CourseResponse }>(`/courses/${id}`);
  },

  create(body: CreateCourseRequest) {
    return api.post<{ data: CourseResponse }>("/courses", body);
  },

  update(id: number, body: Partial<CreateCourseRequest>) {
    return api.put<{ data: CourseResponse }>(`/courses/${id}`, body);
  },

  cancel(id: number) {
    return api.delete<{ data: null }>(`/courses/${id}`);
  },

  bookings: {
    book(courseId: number, memberEmail?: string) {
      return api.post<{ data: BookingResponse }>("/bookings", { courseId, memberEmail });
    },

    cancelBooking(id: number) {
      return api.delete<{ data: BookingResponse }>(`/bookings/${id}`);
    },

    getMemberBookings(memberId: number, status?: RegistrationStatus) {
      return api.get<{ data: BookingResponse[] }>(`/bookings/member/${memberId}`, {
        params: status ? { status } : {},
      });
    },

    getCourseRegistrations(courseId: number) {
      return api.get<{ data: BookingResponse[] }>(`/bookings/course/${courseId}`);
    },
  },

  attendance: {
    mark(courseId: number, records: Array<{ memberId: number; memberEmail?: string; present: boolean }>) {
      return api.post<{ data: AttendanceResponse[] }>("/attendance", { courseId, records });
    },
    get(courseId: number) {
      return api.get<{ data: AttendanceResponse[] }>(`/attendance/course/${courseId}`);
    },
  },
};
