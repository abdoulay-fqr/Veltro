"use client";

import { useEffect, useState, useCallback } from "react";
import { toast } from "sonner";
import { coursesApi, CourseResponse, CourseLevel } from "@/lib/api/courses";
import { coachesApi, CoachResponse } from "@/lib/api/coaches";
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
} from "recharts";
import { PlusCircle, X } from "lucide-react";

interface CoachStats {
  coachId: number;
  count: number;
  avgFill: number;
}

// ── New Course Modal ──────────────────────────────────────────────────────────
function NewCourseModal({ onClose, onCreated }: { onClose: () => void; onCreated: () => void }) {
  const [coaches, setCoaches] = useState<CoachResponse[]>([]);
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState({
    coachId: "",
    name: "",
    description: "",
    dateTime: "",
    durationMinutes: "60",
    capacity: "15",
    level: "BEGINNER" as CourseLevel,
    room: "",
  });

  useEffect(() => {
    coachesApi.list({ size: 100 })
      .then((res) => setCoaches(res.data.data?.content ?? []))
      .catch(() => toast.error("Failed to load coaches"));
  }, []);

  const set = (k: string, v: string) => setForm((p) => ({ ...p, [k]: v }));

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!form.coachId) { toast.error("Please select a coach"); return; }
    if (!form.dateTime) { toast.error("Please enter a date and time"); return; }

    setSaving(true);
    try {
      await coursesApi.create({
        coachId: Number(form.coachId),       // admin sets coach explicitly
        name: form.name,
        description: form.description || undefined,
        dateTime: new Date(form.dateTime).toISOString().replace("Z", ""), // LocalDateTime format
        durationMinutes: Number(form.durationMinutes),
        capacity: Number(form.capacity),
        level: form.level,
        room: form.room || undefined,
      });
      toast.success("Course created");
      onCreated();
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })
        ?.response?.data?.message ?? "Failed to create course";
      toast.error(msg);
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm">
      <div className="w-full max-w-lg bg-white rounded-2xl shadow-xl p-6 max-h-[90vh] overflow-y-auto">
        <div className="flex items-center justify-between mb-5">
          <h2 className="text-base font-semibold text-zinc-900">New Course</h2>
          <button onClick={onClose} className="text-zinc-400 hover:text-zinc-700"><X size={18} /></button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          {/* Course Name */}
          <div>
            <label className="block text-xs font-medium text-zinc-600 mb-1">Course Name *</label>
            <input required value={form.name} onChange={(e) => set("name", e.target.value)}
              className="w-full rounded-lg border border-zinc-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-zinc-900" />
          </div>

          {/* Coach */}
          <div>
            <label className="block text-xs font-medium text-zinc-600 mb-1">Assign Coach *</label>
            <select required value={form.coachId} onChange={(e) => set("coachId", e.target.value)}
              className="w-full rounded-lg border border-zinc-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-zinc-900 bg-white">
              <option value="">Select a coach…</option>
              {coaches.map((c) => (
                <option key={c.id} value={c.userId}>
                  {c.firstname} {c.lastname}{c.specialization ? ` — ${c.specialization}` : ""}
                </option>
              ))}
            </select>
          </div>

          {/* Date & Time */}
          <div>
            <label className="block text-xs font-medium text-zinc-600 mb-1">Date &amp; Time *</label>
            <input required type="datetime-local" value={form.dateTime} onChange={(e) => set("dateTime", e.target.value)}
              className="w-full rounded-lg border border-zinc-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-zinc-900" />
          </div>

          {/* Duration + Capacity (2 cols) */}
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-medium text-zinc-600 mb-1">Duration (min) *</label>
              <input required type="number" min={15} value={form.durationMinutes}
                onChange={(e) => set("durationMinutes", e.target.value)}
                className="w-full rounded-lg border border-zinc-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-zinc-900" />
            </div>
            <div>
              <label className="block text-xs font-medium text-zinc-600 mb-1">Capacity *</label>
              <input required type="number" min={1} value={form.capacity}
                onChange={(e) => set("capacity", e.target.value)}
                className="w-full rounded-lg border border-zinc-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-zinc-900" />
            </div>
          </div>

          {/* Level + Room (2 cols) */}
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-medium text-zinc-600 mb-1">Level *</label>
              <select value={form.level} onChange={(e) => set("level", e.target.value)}
                className="w-full rounded-lg border border-zinc-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-zinc-900 bg-white">
                <option value="BEGINNER">Beginner</option>
                <option value="INTERMEDIATE">Intermediate</option>
                <option value="ADVANCED">Advanced</option>
              </select>
            </div>
            <div>
              <label className="block text-xs font-medium text-zinc-600 mb-1">Room</label>
              <input value={form.room} onChange={(e) => set("room", e.target.value)}
                placeholder="e.g. Studio A"
                className="w-full rounded-lg border border-zinc-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-zinc-900" />
            </div>
          </div>

          {/* Description */}
          <div>
            <label className="block text-xs font-medium text-zinc-600 mb-1">Description</label>
            <textarea value={form.description} onChange={(e) => set("description", e.target.value)}
              rows={2} placeholder="Optional description…"
              className="w-full rounded-lg border border-zinc-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-zinc-900 resize-none" />
          </div>

          <div className="flex gap-3 pt-2">
            <button type="button" onClick={onClose}
              className="flex-1 rounded-lg border border-zinc-200 py-2 text-sm font-medium text-zinc-600 hover:bg-zinc-50">
              Cancel
            </button>
            <button type="submit" disabled={saving}
              className="flex-1 rounded-lg bg-zinc-900 py-2 text-sm font-medium text-white hover:bg-zinc-700 disabled:opacity-50">
              {saving ? "Creating…" : "Create Course"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

// ── Main Page ─────────────────────────────────────────────────────────────────
export default function AdminCoursesPage() {
  const [courses, setCourses] = useState<CourseResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [showNew, setShowNew] = useState(false);

  const loadCourses = useCallback(() => {
    setLoading(true);
    coursesApi.list({ size: 200 })
      .then((res) => setCourses(res.data.data?.content ?? []))
      .catch(() => toast.error("Failed to load courses"))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => { loadCourses(); }, [loadCourses]);

  const topCourses = [...courses].sort((a, b) => b.enrolledCount - a.enrolledCount).slice(0, 10);

  const fillChartData = topCourses.map((c) => ({
    name: c.name.length > 15 ? c.name.slice(0, 15) + "…" : c.name,
    fill: Math.round(c.fillRate * 100),
  }));

  const coachStats: Record<number, CoachStats> = {};
  for (const c of courses) {
    if (!coachStats[c.coachId]) coachStats[c.coachId] = { coachId: c.coachId, count: 0, avgFill: 0 };
    coachStats[c.coachId].count++;
    coachStats[c.coachId].avgFill += c.fillRate;
  }
  const coachRows = Object.values(coachStats).map((s) => ({
    ...s, avgFill: s.count > 0 ? Math.round((s.avgFill / s.count) * 100) : 0,
  }));

  if (loading) return <div className="p-6 text-sm text-zinc-400">Loading…</div>;

  return (
    <div className="p-6 space-y-8">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-semibold text-zinc-900">Course Statistics</h1>
          <p className="text-sm text-zinc-500 mt-0.5">All courses across all coaches</p>
        </div>
        <button
          onClick={() => setShowNew(true)}
          className="flex items-center gap-2 rounded-lg bg-zinc-900 px-4 py-2 text-sm font-medium text-white hover:bg-zinc-700 transition-colors"
        >
          <PlusCircle size={15} /> New Course
        </button>
      </div>

      <div className="grid grid-cols-3 gap-4">
        {[
          { label: "Total Courses", value: courses.length },
          { label: "Scheduled", value: courses.filter((c) => c.status === "SCHEDULED").length },
          { label: "Avg Fill Rate", value: courses.length > 0 ? Math.round(courses.reduce((s, c) => s + c.fillRate, 0) / courses.length * 100) + "%" : "0%" },
        ].map(({ label, value }) => (
          <div key={label} className="rounded-xl border border-zinc-200 bg-white p-4">
            <p className="text-xs text-zinc-500">{label}</p>
            <p className="text-2xl font-semibold text-zinc-900 mt-1">{value}</p>
          </div>
        ))}
      </div>

      <section className="rounded-xl border border-zinc-200 bg-white p-6">
        <h2 className="text-sm font-semibold text-zinc-900 mb-5">Top Courses by Fill Rate</h2>
        {fillChartData.length === 0 ? (
          <p className="text-sm text-zinc-400">No data yet.</p>
        ) : (
          <ResponsiveContainer width="100%" height={280}>
            <BarChart data={fillChartData} margin={{ top: 4, right: 16, bottom: 4, left: 0 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f4f4f5" />
              <XAxis dataKey="name" tick={{ fontSize: 11, fill: "#71717a" }} />
              <YAxis tick={{ fontSize: 11, fill: "#71717a" }} tickFormatter={(v) => `${v}%`} domain={[0, 100]} />
              <Tooltip formatter={(v) => [`${v}%`, "Fill Rate"]} />
              <Bar dataKey="fill" fill="#18181b" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        )}
      </section>

      <section className="rounded-xl border border-zinc-200 bg-white p-6">
        <h2 className="text-sm font-semibold text-zinc-900 mb-5">Coach Performance</h2>
        <table className="w-full text-sm">
          <thead className="border-b border-zinc-100">
            <tr>
              {["Coach ID", "Courses Taught", "Avg Fill Rate"].map((h) => (
                <th key={h} className="pb-2 text-left text-xs font-medium text-zinc-500 uppercase">{h}</th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-zinc-50">
            {coachRows.sort((a, b) => b.avgFill - a.avgFill).map((r) => (
              <tr key={r.coachId}>
                <td className="py-3 font-mono text-xs text-zinc-600">#{r.coachId}</td>
                <td className="py-3">{r.count}</td>
                <td className="py-3">
                  <span className={`font-medium ${r.avgFill >= 70 ? "text-green-600" : r.avgFill >= 40 ? "text-amber-600" : "text-red-600"}`}>
                    {r.avgFill}%
                  </span>
                </td>
              </tr>
            ))}
            {coachRows.length === 0 && (
              <tr><td colSpan={3} className="py-6 text-center text-zinc-400">No data yet</td></tr>
            )}
          </tbody>
        </table>
      </section>

      {showNew && (
        <NewCourseModal
          onClose={() => setShowNew(false)}
          onCreated={() => { setShowNew(false); loadCourses(); }}
        />
      )}
    </div>
  );
}
