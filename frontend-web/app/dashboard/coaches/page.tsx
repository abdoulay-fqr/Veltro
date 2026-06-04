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
import { coursesApi, CourseResponse } from "@/lib/api/courses";
import { useSearchParams } from "next/navigation";
import NfcSimulator from "@/components/dashboard/NfcSimulator";
import CreateCoachForm from "@/components/dashboard/CreateCoachForm";
import { apiError } from "@/lib/utils/api-error";
import { UserPlus, Search, CreditCard, Users, X, Trash2 } from "lucide-react";

type Tab = "list" | "nfc";

// ── Coach Detail Modal ────────────────────────────────────────────────────────
function CoachDetailModal({ coach, onClose }: { coach: CoachResponse; onClose: () => void }) {
  const [courses, setCourses] = useState<CourseResponse[]>([]);
  const [loadingCourses, setLoadingCourses] = useState(true);

  useEffect(() => {
    // course.coachId stores the auth userId, not the coach profile id
    coursesApi.list({ coachId: coach.userId, status: "SCHEDULED", size: 20 })
      .then((res) => setCourses(res.data.data?.content ?? []))
      .catch(() => {})
      .finally(() => setLoadingCourses(false));
  }, [coach.userId]);

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm">
      <div className="w-full max-w-lg bg-white rounded-2xl shadow-xl p-6 max-h-[85vh] overflow-y-auto space-y-5">
        {/* Header */}
        <div className="flex items-start justify-between">
          <div className="flex items-center gap-3">
            {coach.avatarUrl ? (
              <img
                src={`${process.env.NEXT_PUBLIC_USER_SERVICE_URL ?? "http://localhost:8082"}${coach.avatarUrl}`}
                alt="avatar"
                className="w-12 h-12 rounded-full object-cover border border-zinc-200"
              />
            ) : (
              <div className="w-12 h-12 rounded-full bg-zinc-100 flex items-center justify-center text-zinc-400 text-lg font-medium">
                {coach.firstname[0]}
              </div>
            )}
            <div>
              <h2 className="text-base font-semibold text-zinc-900">
                {coach.firstname} {coach.lastname}
              </h2>
              <span className={`text-xs font-medium ${coach.status === "ACTIVE" ? "text-green-600" : "text-red-600"}`}>
                {coach.status}
              </span>
            </div>
          </div>
          <button onClick={onClose} className="text-zinc-400 hover:text-zinc-700 transition-colors">
            <X size={18} />
          </button>
        </div>

        {/* Profile details */}
        <dl className="grid grid-cols-2 gap-3 text-sm">
          <div>
            <dt className="text-zinc-500 text-xs">Phone</dt>
            <dd className="text-zinc-900">{coach.phone ?? "—"}</dd>
          </div>
          <div>
            <dt className="text-zinc-500 text-xs">Specialization</dt>
            <dd className="text-zinc-900">{coach.specialization ?? "—"}</dd>
          </div>
          {coach.certifications && (
            <div className="col-span-2">
              <dt className="text-zinc-500 text-xs">Certifications</dt>
              <dd className="text-zinc-900">{coach.certifications}</dd>
            </div>
          )}
          {coach.bio && (
            <div className="col-span-2">
              <dt className="text-zinc-500 text-xs">Bio</dt>
              <dd className="text-zinc-700 text-xs leading-relaxed">{coach.bio}</dd>
            </div>
          )}
        </dl>

        {/* Upcoming classes */}
        <div>
          <h3 className="text-sm font-semibold text-zinc-900 mb-3">Upcoming Classes</h3>
          {loadingCourses ? (
            <p className="text-xs text-zinc-400">Loading courses…</p>
          ) : courses.length === 0 ? (
            <p className="text-xs text-zinc-400">No scheduled classes.</p>
          ) : (
            <ul className="space-y-2">
              {courses.map((c) => (
                <li key={c.id} className="flex items-center justify-between rounded-lg border border-zinc-100 px-3 py-2.5">
                  <div>
                    <p className="text-sm font-medium text-zinc-900">{c.name}</p>
                    <p className="text-xs text-zinc-500 mt-0.5">
                      {new Date(c.dateTime).toLocaleString([], {
                        month: "short", day: "numeric",
                        hour: "2-digit", minute: "2-digit",
                      })}
                      {c.room ? ` · ${c.room}` : ""}
                    </p>
                  </div>
                  <span className={`text-xs rounded-full px-2 py-0.5 font-medium ${
                    c.level === "BEGINNER"     ? "bg-blue-100 text-blue-700"   :
                    c.level === "INTERMEDIATE" ? "bg-amber-100 text-amber-700" :
                                                 "bg-purple-100 text-purple-700"
                  }`}>
                    {c.level}
                  </span>
                </li>
              ))}
            </ul>
          )}
        </div>

        <button
          onClick={onClose}
          className="w-full rounded-lg bg-zinc-900 py-2 text-sm font-medium text-white hover:bg-zinc-700 transition-colors"
        >
          Close
        </button>
      </div>
    </div>
  );
}

// ── Main page ─────────────────────────────────────────────────────────────────
export default function CoachesPage() {
  const searchParams = useSearchParams();
  const initialTab: Tab = searchParams.get("tab") === "nfc" ? "nfc" : "list";
  const [tab, setTab] = useState<Tab>(initialTab);

  const [coaches, setCoaches] = useState<CoachResponse[]>([]);
  const [globalFilter, setGlobalFilter] = useState("");
  const [showCreate, setShowCreate] = useState(false);
  const [editCoach, setEditCoach] = useState<CoachResponse | null>(null);
  const [detailCoach, setDetailCoach] = useState<CoachResponse | null>(null);
  const [loading, setLoading] = useState(true);

  const loadCoaches = useCallback(async () => {
    setLoading(true);
    try {
      const res = await coachesApi.list({ size: 100 });
      setCoaches(res.data.data?.content ?? []);
    } catch (err) {
      apiError(err, "Failed to load coaches");
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
    } catch (err) {
      apiError(err, "Failed to suspend coach");
    }
  };

  const handleActivate = async (id: number) => {
    try {
      await coachesApi.activate(id);
      toast.success("Coach activated");
      loadCoaches();
    } catch (err) {
      apiError(err, "Failed to activate coach");
    }
  };

  const handleDelete = async (coach: CoachResponse) => {
    if (!confirm(`Delete ${coach.firstname} ${coach.lastname}? This cannot be undone.`)) return;
    try {
      await coachesApi.delete(coach.id);
      toast.success("Coach deleted");
      loadCoaches();
    } catch (err) {
      apiError(err, "Failed to delete coach");
    }
  };

  const columns: ColumnDef<CoachResponse>[] = [
    {
      header: "Name",
      accessorFn: (r) => `${r.firstname} ${r.lastname}`,
      cell: (info) => (
        <button
          onClick={() => setDetailCoach(info.row.original)}
          className="font-medium text-zinc-900 hover:text-zinc-600 hover:underline text-left"
        >
          {info.getValue() as string}
        </button>
      ),
    },
    { header: "Specialization", accessorKey: "specialization", cell: (i) => i.getValue() ?? "—" },
    {
      header: "Status",
      accessorKey: "status",
      cell: (info) => {
        const v = info.getValue() as string;
        return (
          <span className={`inline-flex items-center rounded-full px-2 py-0.5 text-xs font-medium ${
            v === "ACTIVE" ? "bg-green-100 text-green-700" : "bg-red-100 text-red-700"
          }`}>
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
          <div className="flex gap-3 justify-end items-center">
            <button
              onClick={() => setEditCoach(c)}
              className="text-xs text-zinc-500 hover:text-zinc-800 hover:underline"
            >
              Edit
            </button>
            <button
              onClick={() => handleDelete(c)}
              className="text-zinc-400 hover:text-red-600 transition-colors"
              title="Delete coach"
            >
              <Trash2 size={14} />
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
            tab === "list" ? "border-zinc-900 text-zinc-900" : "border-transparent text-zinc-500 hover:text-zinc-700"
          }`}
        >
          <Users size={14} /> Coach List
        </button>
        <button
          onClick={() => setTab("nfc")}
          className={`flex items-center gap-2 px-4 py-2.5 text-sm font-medium transition-colors border-b-2 ${
            tab === "nfc" ? "border-zinc-900 text-zinc-900" : "border-transparent text-zinc-500 hover:text-zinc-700"
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
                        <th key={h.id} className="px-4 py-3 text-left text-xs font-medium text-zinc-500 uppercase tracking-wide">
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

      {detailCoach && (
        <CoachDetailModal
          coach={detailCoach}
          onClose={() => setDetailCoach(null)}
        />
      )}
    </div>
  );
}
