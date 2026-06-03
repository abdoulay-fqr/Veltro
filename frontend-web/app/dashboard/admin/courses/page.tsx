"use client";

import { useEffect, useState } from "react";
import { toast } from "sonner";
import { coursesApi, CourseResponse } from "@/lib/api/courses";
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
} from "recharts";

interface CoachStats {
  coachId: number;
  count: number;
  avgFill: number;
}

export default function AdminCoursesPage() {
  const [courses, setCourses] = useState<CourseResponse[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    coursesApi.list({ size: 200 })
      .then((res) => setCourses(res.data.data?.content ?? []))
      .catch(() => toast.error("Failed to load courses"))
      .finally(() => setLoading(false));
  }, []);

  const topCourses = [...courses]
    .sort((a, b) => b.enrolledCount - a.enrolledCount)
    .slice(0, 10);

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
      <div>
        <h1 className="text-xl font-semibold text-zinc-900">Course Statistics</h1>
        <p className="text-sm text-zinc-500 mt-0.5">All courses across all coaches</p>
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
    </div>
  );
}
