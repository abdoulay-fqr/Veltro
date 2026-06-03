"use client";

import { useEffect, useState, useCallback } from "react";
import { toast } from "sonner";
import { activityApi, EntryResponse, AdminStatsResponse } from "@/lib/api/activity";
import {
  PieChart, Pie, Cell, Tooltip, Legend, ResponsiveContainer,
} from "recharts";

const HOUR_LABELS = Array.from({ length: 24 }, (_, i) =>
  i === 0 ? "12am" : i < 12 ? `${i}am` : i === 12 ? "12pm" : `${i - 12}pm`
);

const MACHINE_COLORS: Record<string, string> = {
  TREADMILL: "#18181b", BIKE: "#3f3f46", ROWING: "#71717a",
  WEIGHTS: "#a1a1aa", OTHER: "#d4d4d8",
};

const intensityColor = (count: number, max: number) => {
  if (max === 0 || count === 0) return "#f4f4f5";
  const pct = count / max;
  if (pct > 0.75) return "#18181b";
  if (pct > 0.5) return "#3f3f46";
  if (pct > 0.25) return "#71717a";
  return "#d4d4d8";
};

export default function ActivityPage() {
  const [adminStats, setAdminStats] = useState<AdminStatsResponse | null>(null);
  const [liveEntries, setLiveEntries] = useState<EntryResponse[]>([]);
  const [lastEntry, setLastEntry] = useState<EntryResponse | null>(null);
  const [loading, setLoading] = useState(true);

  const loadStats = useCallback(async () => {
    try {
      const [statsRes] = await Promise.all([activityApi.getAdminStats()]);
      setAdminStats((statsRes.data as unknown as { data: AdminStatsResponse }).data);
    } catch { toast.error("Failed to load activity stats"); }
    finally { setLoading(false); }
  }, []);

  const loadLive = useCallback(async () => {
    try {
      const res = await activityApi.getLiveEntries();
      const entries = (res.data as unknown as { data: EntryResponse[] }).data ?? [];
      setLiveEntries(entries);
      if (entries.length > 0) setLastEntry(entries[0]);
    } catch {}
  }, []);

  useEffect(() => { loadStats(); loadLive(); }, [loadStats, loadLive]);

  // Poll live entries every 5 seconds
  useEffect(() => {
    const interval = setInterval(loadLive, 5000);
    return () => clearInterval(interval);
  }, [loadLive]);

  const maxHourCount = adminStats
    ? Math.max(...Object.values(adminStats.entriesByHour))
    : 0;

  return (
    <div className="p-6 space-y-8">
      <div>
        <h1 className="text-xl font-semibold text-zinc-900">Activity Dashboard</h1>
        <p className="text-sm text-zinc-500 mt-0.5">Real-time gym occupancy and usage analytics</p>
      </div>

      {/* Today's stats bar */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        {[
          { label: "Today's Entries", value: adminStats?.todayEntries ?? "—" },
          { label: "This Week", value: adminStats?.weeklyEntries ?? "—" },
          { label: "Current Occupancy", value: adminStats?.currentOccupancy ?? "—" },
          { label: "Peak Hour", value: adminStats ? `${HOUR_LABELS[adminStats.peakHour]}` : "—" },
        ].map(({ label, value }) => (
          <div key={label} className="rounded-xl border border-zinc-200 bg-white p-4">
            <p className="text-xs text-zinc-500">{label}</p>
            <p className="text-2xl font-semibold text-zinc-900 mt-1">{value}</p>
          </div>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Occupancy heatmap */}
        <section className="rounded-xl border border-zinc-200 bg-white p-6">
          <h2 className="text-sm font-semibold text-zinc-900 mb-4">Entries by Hour</h2>
          {loading ? (
            <p className="text-sm text-zinc-400">Loading…</p>
          ) : (
            <div className="space-y-2">
              {adminStats && Object.entries(adminStats.entriesByHour).map(([hour, count]) => (
                <div key={hour} className="flex items-center gap-3">
                  <span className="text-xs text-zinc-500 w-10 shrink-0">{HOUR_LABELS[parseInt(hour)]}</span>
                  <div className="flex-1 bg-zinc-100 rounded-full h-4 overflow-hidden">
                    <div
                      className="h-full rounded-full transition-all"
                      style={{
                        width: maxHourCount > 0 ? `${(count / maxHourCount) * 100}%` : "0%",
                        backgroundColor: intensityColor(count, maxHourCount),
                      }}
                    />
                  </div>
                  <span className="text-xs text-zinc-500 w-6 text-right">{count}</span>
                </div>
              ))}
            </div>
          )}
        </section>

        {/* Virtual entrance gate widget */}
        <section className="rounded-xl border border-zinc-200 bg-white p-6 flex flex-col">
          <h2 className="text-sm font-semibold text-zinc-900 mb-4">
            Live Entrance Gate
            <span className="ml-2 inline-block w-2 h-2 rounded-full bg-green-500 animate-pulse" />
          </h2>

          {lastEntry ? (
            <div className={`rounded-xl p-4 mb-4 ${lastEntry.direction === "IN" ? "bg-green-50 border border-green-200" : "bg-red-50 border border-red-200"}`}>
              <div className="flex items-center gap-3">
                <span className="text-3xl">{lastEntry.direction === "IN" ? "🟢" : "🔴"}</span>
                <div>
                  <p className="text-base font-semibold text-zinc-900">{lastEntry.memberName ?? "Unknown"}</p>
                  <p className="text-xs text-zinc-500">
                    {lastEntry.direction} · {new Date(lastEntry.timestamp).toLocaleTimeString()} · {lastEntry.cardUid}
                  </p>
                </div>
              </div>
            </div>
          ) : (
            <div className="rounded-xl bg-zinc-50 p-4 mb-4 text-sm text-zinc-400">Waiting for first scan…</div>
          )}

          <div className="flex-1 overflow-y-auto max-h-48 space-y-1.5">
            {liveEntries.slice(1, 8).map((e) => (
              <div key={e.id} className="flex items-center gap-2 text-xs text-zinc-600 rounded-lg border border-zinc-100 px-3 py-2">
                <span>{e.direction === "IN" ? "▶" : "◀"}</span>
                <span className="flex-1 truncate">{e.memberName ?? `Member #${e.memberId}`}</span>
                <span className="text-zinc-400">{new Date(e.timestamp).toLocaleTimeString()}</span>
              </div>
            ))}
          </div>
        </section>
      </div>

      {/* Live entry log table */}
      <section className="rounded-xl border border-zinc-200 bg-white p-6">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-sm font-semibold text-zinc-900">Live Entry Log</h2>
          <span className="text-xs text-zinc-400">Auto-refreshes every 5s</span>
        </div>
        <table className="w-full text-sm">
          <thead className="border-b border-zinc-100">
            <tr>
              {["Member", "Card UID", "Direction", "Time", "Session ID"].map((h) => (
                <th key={h} className="pb-2 text-left text-xs font-medium text-zinc-500 uppercase">{h}</th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-zinc-50">
            {liveEntries.map((e) => (
              <tr key={e.id} className="hover:bg-zinc-50">
                <td className="py-2">{e.memberName ?? `#${e.memberId}`}</td>
                <td className="py-2 font-mono text-xs text-zinc-500">{e.cardUid}</td>
                <td className="py-2">
                  <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${e.direction === "IN" ? "bg-green-100 text-green-700" : "bg-red-100 text-red-700"}`}>
                    {e.direction}
                  </span>
                </td>
                <td className="py-2 text-xs text-zinc-500">{new Date(e.timestamp).toLocaleTimeString()}</td>
                <td className="py-2 font-mono text-xs text-zinc-300 truncate max-w-24">{e.sessionId ?? "—"}</td>
              </tr>
            ))}
            {liveEntries.length === 0 && (
              <tr><td colSpan={5} className="py-6 text-center text-zinc-400">No entries yet</td></tr>
            )}
          </tbody>
        </table>
      </section>
    </div>
  );
}
