"use client";

import { useEffect, useState, useCallback } from "react";
import {
  useReactTable,
  getCoreRowModel,
  getFilteredRowModel,
  flexRender,
  ColumnDef,
} from "@tanstack/react-table";
import { toast } from "sonner";
import {
  subscriptionsApi,
  SubscriptionResponse,
  PaymentRecordResponse,
} from "@/lib/api/subscriptions";
import { Search, X } from "lucide-react";

const PLAN_COLORS: Record<string, string> = {
  TRIAL:   "bg-zinc-100 text-zinc-600",
  SESSION: "bg-blue-100 text-blue-700",
  MONTHLY: "bg-purple-100 text-purple-700",
  ANNUAL:  "bg-amber-100 text-amber-700",
};

const STATUS_COLORS: Record<string, string> = {
  ACTIVE:    "bg-green-100 text-green-700",
  PAUSED:    "bg-yellow-100 text-yellow-700",
  CANCELLED: "bg-red-100 text-red-700",
  EXPIRED:   "bg-zinc-100 text-zinc-500",
};

export default function SubscriptionsPage() {
  const [subs, setSubs] = useState<SubscriptionResponse[]>([]);
  const [summary, setSummary] = useState<Record<string, number>>({});
  const [globalFilter, setGlobalFilter] = useState("");
  const [loading, setLoading] = useState(true);
  const [invoiceModal, setInvoiceModal] = useState<{
    sub: SubscriptionResponse;
    invoices: PaymentRecordResponse[];
  } | null>(null);

  const loadData = useCallback(async () => {
    setLoading(true);
    try {
      const [subsRes, sumRes] = await Promise.all([
        subscriptionsApi.list({ size: 200 }),
        subscriptionsApi.reports.summary(),
      ]);
      setSubs(sumRes.data.data ? subsRes.data.data?.content ?? [] : subsRes.data.data?.content ?? []);
      setSummary((sumRes.data as unknown as { data: Record<string, number> }).data ?? {});
    } catch {
      toast.error("Failed to load subscriptions");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { loadData(); }, [loadData]);

  const handleCancel = async (id: number) => {
    try { await subscriptionsApi.cancel(id); toast.success("Subscription cancelled"); loadData(); }
    catch { toast.error("Failed to cancel subscription"); }
  };

  const loadInvoices = async (sub: SubscriptionResponse) => {
    try {
      const res = await subscriptionsApi.getInvoices(sub.memberId);
      setInvoiceModal({ sub, invoices: res.data.data ?? [] });
    } catch { toast.error("Failed to load invoices"); }
  };

  const columns: ColumnDef<SubscriptionResponse>[] = [
    {
      header: "Member ID",
      accessorKey: "memberId",
      cell: (i) => <span className="font-mono text-xs">#{i.getValue() as number}</span>,
    },
    { header: "Email", accessorKey: "memberEmail", cell: (i) => i.getValue() ?? "—" },
    {
      header: "Plan",
      accessorKey: "plan",
      cell: (i) => (
        <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${PLAN_COLORS[i.getValue() as string]}`}>
          {i.getValue() as string}
        </span>
      ),
    },
    {
      header: "Status",
      accessorKey: "status",
      cell: (i) => (
        <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${STATUS_COLORS[i.getValue() as string]}`}>
          {i.getValue() as string}
        </span>
      ),
    },
    {
      header: "Expires",
      accessorKey: "endDate",
      cell: (i) => new Date(i.getValue() as string).toLocaleDateString(),
    },
    {
      header: "Days Left",
      accessorKey: "daysRemaining",
      cell: (i) => {
        const d = i.getValue() as number;
        return <span className={d <= 7 ? "text-red-600 font-medium" : ""}>{d}</span>;
      },
    },
    {
      header: "Auto-renew",
      accessorKey: "autoRenew",
      cell: (i) => (i.getValue() ? "✓" : "—"),
    },
    {
      id: "actions",
      header: "",
      cell: (info) => {
        const s = info.row.original;
        return (
          <div className="flex gap-2 justify-end text-xs">
            <button onClick={() => loadInvoices(s)} className="text-zinc-500 hover:text-zinc-800 hover:underline">
              Invoices
            </button>
            {s.status === "ACTIVE" && (
              <button onClick={() => handleCancel(s.id)} className="text-red-600 hover:underline">
                Cancel
              </button>
            )}
          </div>
        );
      },
    },
  ];

  const table = useReactTable({
    data: subs,
    columns,
    state: { globalFilter },
    onGlobalFilterChange: setGlobalFilter,
    getCoreRowModel: getCoreRowModel(),
    getFilteredRowModel: getFilteredRowModel(),
  });

  return (
    <div className="p-6 space-y-6">
      <div>
        <h1 className="text-xl font-semibold text-zinc-900">Subscriptions</h1>
        <p className="text-sm text-zinc-500 mt-0.5">Manage member plans and billing</p>
      </div>

      {/* Summary cards */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        {[
          { label: "Active Plans", value: summary.totalActive ?? "—" },
          { label: "Paused", value: summary.totalPaused ?? "—" },
          { label: "Active Trials", value: summary.trialsActive ?? "—" },
          { label: "Monthly Revenue", value: summary.currentMonthRevenue != null ? `$${Number(summary.currentMonthRevenue).toFixed(2)}` : "—" },
        ].map(({ label, value }) => (
          <div key={label} className="rounded-xl border border-zinc-200 bg-white p-4">
            <p className="text-xs text-zinc-500">{label}</p>
            <p className="text-2xl font-semibold text-zinc-900 mt-1">{value}</p>
          </div>
        ))}
      </div>

      {/* Search */}
      <div className="relative max-w-sm">
        <Search size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-zinc-400" />
        <input
          value={globalFilter}
          onChange={(e) => setGlobalFilter(e.target.value)}
          placeholder="Search by email or member ID…"
          className="w-full rounded-lg border border-zinc-200 bg-white py-2 pl-8 pr-3 text-sm focus:outline-none focus:ring-2 focus:ring-zinc-900"
        />
      </div>

      <div className="rounded-xl border border-zinc-200 bg-white overflow-hidden">
        {loading ? (
          <div className="p-8 text-center text-sm text-zinc-400">Loading…</div>
        ) : (
          <table className="w-full text-sm">
            <thead className="bg-zinc-50 border-b border-zinc-200">
              {table.getHeaderGroups().map((hg) => (
                <tr key={hg.id}>
                  {hg.headers.map((h) => (
                    <th key={h.id} className="px-4 py-3 text-left text-xs font-medium text-zinc-500 uppercase tracking-wide">
                      {flexRender(h.column.columnDef.header, h.getContext())}
                    </th>
                  ))}
                </tr>
              ))}
            </thead>
            <tbody className="divide-y divide-zinc-100">
              {table.getRowModel().rows.length === 0 ? (
                <tr><td colSpan={columns.length} className="px-4 py-8 text-center text-zinc-400">No subscriptions found</td></tr>
              ) : (
                table.getRowModel().rows.map((row) => (
                  <tr key={row.id} className="hover:bg-zinc-50 transition-colors">
                    {row.getVisibleCells().map((cell) => (
                      <td key={cell.id} className="px-4 py-3">
                        {flexRender(cell.column.columnDef.cell, cell.getContext())}
                      </td>
                    ))}
                  </tr>
                ))
              )}
            </tbody>
          </table>
        )}
      </div>

      {/* Invoice modal */}
      {invoiceModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm">
          <div className="w-full max-w-lg bg-white rounded-2xl shadow-xl p-6 space-y-4 max-h-[80vh] overflow-y-auto">
            <div className="flex items-center justify-between">
              <div>
                <h2 className="text-base font-semibold text-zinc-900">Payment History</h2>
                <p className="text-xs text-zinc-500">Member #{invoiceModal.sub.memberId} · {invoiceModal.sub.plan}</p>
              </div>
              <button onClick={() => setInvoiceModal(null)} className="text-zinc-400 hover:text-zinc-700"><X size={18} /></button>
            </div>
            {invoiceModal.invoices.length === 0 ? (
              <p className="text-sm text-zinc-400">No invoices found.</p>
            ) : (
              <div className="space-y-2">
                {invoiceModal.invoices.map((inv) => (
                  <div key={inv.id} className="flex items-center justify-between rounded-lg border border-zinc-100 px-4 py-3">
                    <div>
                      <p className="text-sm font-medium text-zinc-900">${inv.amount}</p>
                      <p className="text-xs text-zinc-500">{inv.method} · {new Date(inv.paidAt).toLocaleDateString()}</p>
                    </div>
                    <span className="font-mono text-xs text-zinc-400">{inv.invoiceRef}</span>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
