"use client";

import { useEffect, useState, useCallback } from "react";
import { toast } from "sonner";
import { coursesApi, CourseResponse, BookingResponse } from "@/lib/api/courses";
import { useAuth } from "@/lib/auth/useAuth";
import { CheckCircle2, XCircle, Clock } from "lucide-react";

interface AttendanceToggle {
  memberId: number;
  memberEmail: string | null;
  present: boolean;
}

export default function AttendancePage() {
  const { user } = useAuth();
  const [courses, setCourses] = useState<CourseResponse[]>([]);
  const [selectedCourse, setSelectedCourse] = useState<CourseResponse | null>(null);
  const [registrations, setRegistrations] = useState<BookingResponse[]>([]);
  const [attendance, setAttendance] = useState<AttendanceToggle[]>([]);
  const [submitting, setSubmitting] = useState(false);
  const [now, setNow] = useState(new Date());

  useEffect(() => {
    const timer = setInterval(() => setNow(new Date()), 60_000);
    return () => clearInterval(timer);
  }, []);

  const loadCourses = useCallback(async () => {
    try {
      const today = new Date().toISOString().split("T")[0];
      const res = await coursesApi.list({ from: today + "T00:00:00", to: today + "T23:59:59", status: "SCHEDULED", size: 50 });
      setCourses(res.data.data?.content ?? []);
    } catch { toast.error("Failed to load courses"); }
  }, []);

  useEffect(() => { loadCourses(); }, [loadCourses]);

  const selectCourse = async (course: CourseResponse) => {
    setSelectedCourse(course);
    try {
      const res = await coursesApi.bookings.getCourseRegistrations(course.id);
      const regs = res.data.data ?? [];
      setRegistrations(regs);
      setAttendance(regs.map((r) => ({ memberId: r.memberId, memberEmail: r.memberEmail, present: true })));
    } catch { toast.error("Failed to load registrations"); }
  };

  const toggle = (memberId: number) => {
    setAttendance((prev) => prev.map((a) => a.memberId === memberId ? { ...a, present: !a.present } : a));
  };

  const submit = async () => {
    if (!selectedCourse) return;
    setSubmitting(true);
    try {
      await coursesApi.attendance.mark(selectedCourse.id, attendance);
      toast.success("Attendance recorded");
      setSelectedCourse(null);
      loadCourses();
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message ?? "Failed to submit attendance";
      toast.error(msg);
    } finally { setSubmitting(false); }
  };

  const courseStarted = (course: CourseResponse) => new Date(course.dateTime) <= now;
  const minutesUntil = (course: CourseResponse) => Math.round((new Date(course.dateTime).getTime() - now.getTime()) / 60000);

  return (
    <div className="p-6 space-y-6">
      <div>
        <h1 className="text-xl font-semibold text-zinc-900">Today's Attendance</h1>
        <p className="text-sm text-zinc-500 mt-0.5">Mark attendance after each course starts</p>
      </div>

      <div className="grid gap-4 md:grid-cols-2">
        {courses.map((course) => {
          const started = courseStarted(course);
          const mins = minutesUntil(course);
          const isSelected = selectedCourse?.id === course.id;

          return (
            <div key={course.id}
              className={`rounded-xl border p-4 cursor-pointer transition ${isSelected ? "border-zinc-900 bg-zinc-50" : "border-zinc-200 bg-white hover:border-zinc-300"}`}
              onClick={() => selectCourse(course)}>
              <div className="flex items-start justify-between">
                <div>
                  <p className="text-sm font-semibold text-zinc-900">{course.name}</p>
                  <p className="text-xs text-zinc-500 mt-0.5">{new Date(course.dateTime).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })} · {course.room ?? "—"}</p>
                </div>
                {started ? (
                  <span className="flex items-center gap-1 text-xs font-medium text-green-600 bg-green-50 px-2 py-1 rounded-full">
                    <CheckCircle2 size={12} /> Started
                  </span>
                ) : (
                  <span className="flex items-center gap-1 text-xs text-zinc-500 bg-zinc-100 px-2 py-1 rounded-full">
                    <Clock size={12} /> {mins}m
                  </span>
                )}
              </div>
              <p className="text-xs text-zinc-400 mt-2">{course.enrolledCount}/{course.capacity} enrolled</p>
            </div>
          );
        })}
        {courses.length === 0 && (
          <p className="text-sm text-zinc-400 col-span-2">No courses scheduled for today.</p>
        )}
      </div>

      {selectedCourse && (
        <div className="rounded-xl border border-zinc-200 bg-white p-6 space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-sm font-semibold text-zinc-900">
              Attendance: {selectedCourse.name}
            </h2>
            {!courseStarted(selectedCourse) && (
              <p className="text-xs text-amber-600 bg-amber-50 px-2 py-1 rounded-full">
                Course hasn't started yet — attendance can be submitted after start time
              </p>
            )}
          </div>

          <div className="space-y-2">
            {attendance.length === 0 && <p className="text-sm text-zinc-400">No registered members.</p>}
            {attendance.map((a) => (
              <div key={a.memberId}
                className="flex items-center justify-between rounded-lg border border-zinc-100 px-4 py-3">
                <div>
                  <p className="text-sm font-medium text-zinc-900">Member #{a.memberId}</p>
                  <p className="text-xs text-zinc-500">{a.memberEmail ?? "—"}</p>
                </div>
                <button onClick={() => toggle(a.memberId)} className="transition">
                  {a.present
                    ? <CheckCircle2 size={22} className="text-green-500" />
                    : <XCircle size={22} className="text-red-500" />}
                </button>
              </div>
            ))}
          </div>

          {attendance.length > 0 && (
            <button onClick={submit} disabled={submitting || !courseStarted(selectedCourse)}
              className="w-full rounded-lg bg-zinc-900 py-2.5 text-sm font-medium text-white hover:bg-zinc-700 disabled:opacity-50 transition-colors">
              {submitting ? "Submitting…" : "Submit Attendance"}
            </button>
          )}
        </div>
      )}
    </div>
  );
}
