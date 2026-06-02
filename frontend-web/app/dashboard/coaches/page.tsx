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
import { coachesApi, CoachResponse } from "@/lib/api/coaches";
import { useSearchParams } from "next/navigation";
import NfcSimulator from "@/components/dashboard/NfcSimulator";
import CreateCoachForm from "@/components/dashboard/CreateCoachForm";
import { UserPlus, Search, CreditCard, Users } from "lucide-react";

type Tab = "list" | "nfc";

export default function CoachesPage() {
  const searchParams = useSearchParams();
  const initialTab: Tab = searchParams.get("tab") === "nfc" ? "nfc" : "list";
  const [tab, setTab] = useState<Tab>(initialTab);

  const [coaches, setCoaches] = useState<CoachResponse[]>([]);
  const [globalFilter, setGlobalFilter] = useState("");
  const [showCreate, setShowCreate] = useState(false);
  const [editCoach, setEditCoach] = useState<CoachResponse | null>(null);
  const [loading, setLoading] = useState(true);

  const loadCoaches = useCallback(async () => {
    setLoading(true);
    try {
      const res = await coachesApi.list({ size: 100 });
      setCoaches(res.data.data?.content ?? []);
    } catch {
      toast.error("Failed to load coaches");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { loadCoaches(); }, [loadCoaches]);

  const handleSuspend = async (id: number) => {
    try {
      await coachesApi.suspend(id);
      toast.success("Coach suspended");
      loadCoaches();
    } catch {
      toast.error("Failed to suspend coach");
    }
  };

  const handleActivate = async (id: number) => {
    try {
      await coachesApi.activate(id);
      toast.success("Coach activated");
      loadCoaches();
    } catch {
      toast.error("Failed to activate coach");
    }
  };

  const columns: ColumnDef<CoachResponse>[] = [
    {
      header: "Name",
      accessorFn: (r) => `${r.firstname} ${r.lastname}`,
      cell: (info) => (
        <span className="font-medium text-zinc-900">{info.getValue() as string}</span>
      ),
    },
    { header: "Specialization", accessorKey: "specialization", cell: (i) => i.getValue() ?? "—" },
    {
      header: "Status",
      accessorKey: "status",
      cell: (info) => {
        const v = info.getValue() as string;
        return (
          <span
            className={`inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium ${
              v === "ACTIVE" ? "bg-green-100 text-green-700" : "bg-red-100 text-red-700"
            }`}
          >
            {v}
          </span>
        );
      },
    },
    {
      id: "actions",
      header: "",
      cell: (info) => {
        const c = info.row.original;
        return (
          <div className="flex gap-3 justify-end">
            <button
              onClick={() => setEditCoach(c)}
              className="text-xs text-zinc-500 hover:text-zinc-800 hover:underline"
            >
              Edit
            </button>
            {c.status === "ACTIVE" ? (
              <button
                onClick={() => handleSuspend(c.id)}
                className="text-xs text-red-600 hover:underline"
              >
                Suspend
              </button>
            ) : (
              <button
                onClick={() => handleActivate(c.id)}
                className="text-xs text-green-600 hover:underline"
              >
                Activate
              </button>
            )}
          </div>
        );
      },
    },
  ];

  const table = useReactTable({
    data: coaches,
    columns,
    state: { globalFilter },
    onGlobalFilterChange: setGlobalFilter,
    getCoreRowModel: getCoreRowModel(),
    getFilteredRowModel: getFilteredRowModel(),
  });

  return (
    <div className="p-6 space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-semibold text-zinc-900">Coaches</h1>
          <p className="text-sm text-zinc-500 mt-0.5">{coaches.length} total</p>
        </div>
        <button
          onClick={() => setShowCreate(true)}
          className="flex items-center gap-2 rounded-lg bg-zinc-900 px-4 py-2 text-sm font-medium text-white hover:bg-zinc-700 transition-colors"
        >
          <UserPlus size={15} />
          Add Coach
        </button>
      </div>

      <div className="flex gap-1 border-b border-zinc-200">
        <button
          onClick={() => setTab("list")}
          className={`flex items-center gap-2 px-4 py-2.5 text-sm font-medium transition-colors border-b-2 ${
            tab === "list"
              ? "border-zinc-900 text-zinc-900"
              : "border-transparent text-zinc-500 hover:text-zinc-700"
          }`}
        >
          <Users size={14} /> Coach List
        </button>
        <button
          onClick={() => setTab("nfc")}
          className={`flex items-center gap-2 px-4 py-2.5 text-sm font-medium transition-colors border-b-2 ${
            tab === "nfc"
              ? "border-zinc-900 text-zinc-900"
              : "border-transparent text-zinc-500 hover:text-zinc-700"
          }`}
        >
          <CreditCard size={14} /> NFC Simulator
        </button>
      </div>

      {tab === "nfc" && <NfcSimulator />}

      {tab === "list" && (
        <>
          <div className="relative max-w-sm">
            <Search size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-zinc-400" />
            <input
              value={globalFilter}
              onChange={(e) => setGlobalFilter(e.target.value)}
              placeholder="Search coaches…"
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
                        <th
                          key={h.id}
                          className="px-4 py-3 text-left text-xs font-medium text-zinc-500 uppercase tracking-wide"
                        >
                          {flexRender(h.column.columnDef.header, h.getContext())}
                        </th>
                      ))}
                    </tr>
                  ))}
                </thead>
                <tbody className="divide-y divide-zinc-100">
                  {table.getRowModel().rows.length === 0 ? (
                    <tr>
                      <td colSpan={columns.length} className="px-4 py-8 text-center text-zinc-400">
                        No coaches found
                      </td>
                    </tr>
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
        </>
      )}

      {showCreate && (
        <CreateCoachForm
          onClose={() => setShowCreate(false)}
          onSaved={() => { setShowCreate(false); loadCoaches(); }}
        />
      )}

      {editCoach && (
        <CreateCoachForm
          coach={editCoach}
          onClose={() => setEditCoach(null)}
          onSaved={() => { setEditCoach(null); loadCoaches(); }}
        />
      )}
    </div>
  );
}
