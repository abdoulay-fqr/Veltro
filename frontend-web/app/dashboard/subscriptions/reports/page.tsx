"use client";

import { useEffect, useState } from "react";
import { toast } from "sonner";
import { subscriptionsApi, Plan } from "@/lib/api/subscriptions";
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell, Legend,
} from "recharts";
import { Download } from "lucide-react";

const PLAN_COLORS_PIE: Record<Plan, string> = {
  TRIAL:   "#a1a1aa",
  SESSION: "#60a5fa",
  MONTHLY: "#a78bfa",
  ANNUAL:  "#fbbf24",
};

const MONTHS = ["Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"];

export default function ReportsPage() {
  const [revenue, setRevenue] = useState<Array<{ label: string; total: number }>>([]);
  const [distribution, setDistribution] = useState<Array<{ plan: Plan; count: number }>>([]);
  const [summary, setSummary] = useState<Record<string, number>>({});
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      subscriptionsApi.reports.revenue(),
      subscriptionsApi.reports.distribution(),
      subscriptionsApi.reports.summary(),
    ]).then(([revRes, distRes, sumRes]) => {
      const rev = revRes.data.data ?? [];
      setRevenue(rev.map((r) => ({
        label: `${MONTHS[(r.month ?? 1) - 1]} ${r.year}`,
        total: Number(r.total ?? 0),
      })).reverse());
      setDistribution((distRes.data as unknown as { data: Array<{ plan: Plan; count: number }> }).data ?? []);
      setSummary((sumRes.data as unknown as { data: Record<string, number> }).data ?? {});
    }).catch(() => toast.error("Failed to load report data"))
      .finally(() => setLoading(false));
  }, []);

  const exportCsv = () => {
    const rows = ["Month,Revenue", ...revenue.map((r) => `${r.label},${r.total}`)];
    const blob = new Blob([rows.join("\n")], { type: "text/csv" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = "veltro-revenue.csv";
    a.click();
    URL.revokeObjectURL(url);
  };

  if (loading) return <div className="p-6 text-sm text-zinc-400">Loading reports…</div>;

  return (
    <div className="p-6 space-y-8">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-semibold text-zinc-900">Financial Reports</h1>
          <p className="text-sm text-zinc-500 mt-0.5">Revenue, plan distribution, and growth analytics</p>
        </div>
        <button
          onClick={exportCsv}
          className="flex items-center gap-2 rounded-lg border border-zinc-200 px-4 py-2 text-sm font-medium text-zinc-600 hover:bg-zinc-50 transition-colors"
        >
          <Download size={14} />
          Export CSV
        </button>
      </div>

      {/* KPI cards */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        {[
          { label: "Total Revenue", value: `$${Number(summary.totalRevenue ?? 0).toFixed(2)}` },
          { label: "This Month", value: `$${Number(summary.currentMonthRevenue ?? 0).toFixed(2)}` },
          { label: "Active Subscriptions", value: summary.totalActive ?? 0 },
          { label: "Active Trials", value: summary.trialsActive ?? 0 },
        ].map(({ label, value }) => (
          <div key={label} className="rounded-xl border border-zinc-200 bg-white p-4">
            <p className="text-xs text-zinc-500">{label}</p>
            <p className="text-2xl font-semibold text-zinc-900 mt-1">{value}</p>
          </div>
        ))}
      </div>

      {/* Revenue bar chart */}
      <section className="rounded-xl border border-zinc-200 bg-white p-6">
        <h2 className="text-sm font-semibold text-zinc-900 mb-5">Monthly Revenue</h2>
        {revenue.length === 0 ? (
          <p className="text-sm text-zinc-400">No revenue data yet.</p>
        ) : (
          <ResponsiveContainer width="100%" height={280}>
            <BarChart data={revenue} margin={{ top: 4, right: 16, bottom: 4, left: 0 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f4f4f5" />
              <XAxis dataKey="label" tick={{ fontSize: 12, fill: "#71717a" }} />
              <YAxis tick={{ fontSize: 12, fill: "#71717a" }} tickFormatter={(v) => `$${v}`} />
              <Tooltip formatter={(v) => [`$${v}`, "Revenue"]} />
              <Bar dataKey="total" fill="#18181b" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        )}
      </section>

      {/* Plan distribution pie chart */}
      <section className="rounded-xl border border-zinc-200 bg-white p-6">
        <h2 className="text-sm font-semibold text-zinc-900 mb-5">Active Plan Distribution</h2>
        {distribution.length === 0 ? (
          <p className="text-sm text-zinc-400">No active subscriptions yet.</p>
        ) : (
          <div className="flex items-center gap-8">
            <ResponsiveContainer width={220} height={220}>
              <PieChart>
                <Pie
                  data={distribution}
                  dataKey="count"
                  nameKey="plan"
                  cx="50%"
                  cy="50%"
                  outerRadius={90}
                  label={({ plan, percent }) =>
                    `${plan} ${(percent * 100).toFixed(0)}%`
                  }
                  labelLine={false}
                >
                  {distribution.map((entry) => (
                    <Cell
                      key={entry.plan}
                      fill={PLAN_COLORS_PIE[entry.plan] ?? "#e4e4e7"}
                    />
                  ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
            <div className="space-y-2">
              {distribution.map((d) => (
                <div key={d.plan} className="flex items-center gap-2 text-sm">
                  <span
                    className="w-3 h-3 rounded-sm flex-shrink-0"
                    style={{ background: PLAN_COLORS_PIE[d.plan] ?? "#e4e4e7" }}
                  />
                  <span className="text-zinc-700">{d.plan}</span>
                  <span className="text-zinc-400 ml-auto">{d.count}</span>
                </div>
              ))}
            </div>
          </div>
        )}
      </section>
    </div>
  );
}
