"use client";

import { useEffect, useState, useCallback } from "react";
import {
  useReactTable, getCoreRowModel, getFilteredRowModel, flexRender, ColumnDef,
} from "@tanstack/react-table";
import { toast } from "sonner";
import { coursesApi, CourseResponse, BookingResponse } from "@/lib/api/courses";
import { useAuth } from "@/lib/auth/useAuth";
import CreateCourseModal from "@/components/dashboard/CreateCourseModal";
import { PlusCircle, Search, Users, X } from "lucide-react";

const STATUS_COLORS: Record<string, string> = {
  SCHEDULED: "bg-green-100 text-green-700",
  CANCELLED:  "bg-red-100 text-red-700",
  COMPLETED:  "bg-zinc-100 text-zinc-500",
};

const LEVEL_COLORS: Record<string, string> = {
  BEGINNER:     "bg-blue-100 text-blue-700",
  INTERMEDIATE: "bg-amber-100 text-amber-700",
  ADVANCED:     "bg-purple-100 text-purple-700",
};

export default function CoursesPage() {
  const { user } = useAuth();
  const [courses, setCourses] = useState<CourseResponse[]>([]);
  const [globalFilter, setGlobalFilter] = useState("");
  const [loading, setLoading] = useState(true);
  const [showCreate, setShowCreate] = useState(false);
  const [membersModal, setMembersModal] = useState<{ courseId: number; name: string; bookings: BookingResponse[] } | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      // For COACH role, filter by their userId (booking-service stores coachId = X-User-Id)
      const params: Parameters<typeof coursesApi.list>[0] = { size: 100 };
      if (user?.role === "COACH" && user.userId) params.coachId = user.userId;
      const res = await coursesApi.list(params);
      setCourses(res.data.data?.content ?? []);
    } catch { toast.error("Failed to load courses"); }
    finally { setLoading(false); }
  }, [user]);

  useEffect(() => { load(); }, [load]);

  const handleCancel = async (id: number) => {
    if (!confirm("Cancel this course? All booked members will be notified.")) return;
    try { await coursesApi.cancel(id); toast.success("Course cancelled"); load(); }
    catch { toast.error("Failed to cancel course"); }
  };

  const loadMembers = async (course: CourseResponse) => {
    try {
      const res = await coursesApi.bookings.getCourseRegistrations(course.id);
      setMembersModal({ courseId: course.id, name: course.name, bookings: res.data.data ?? [] });
    } catch { toast.error("Failed to load registrations"); }
  };

  const columns: ColumnDef<CourseResponse>[] = [
    { header: "Name", accessorKey: "name", cell: (i) => <span className="font-medium text-zinc-900">{i.getValue() as string}</span> },
    { header: "Date/Time", accessorKey: "dateTime", cell: (i) => new Date(i.getValue() as string).toLocaleString() },
    { header: "Level", accessorKey: "level", cell: (i) => (
      <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${LEVEL_COLORS[i.getValue() as string]}`}>{i.getValue() as string}</span>
    )},
    { header: "Room", accessorKey: "room", cell: (i) => i.getValue() ?? "—" },
    {
      header: "Fill",
      id: "fill",
      cell: (info) => {
        const c = info.row.original;
        const pct = c.capacity > 0 ? Math.round((c.enrolledCount / c.capacity) * 100) : 0;
        return (
          <div className="flex items-center gap-2">
            <div className="w-16 h-1.5 bg-zinc-100 rounded-full overflow-hidden">
              <div className="h-full bg-zinc-900 rounded-full" style={{ width: `${pct}%` }} />
            </div>
            <span className="text-xs text-zinc-500">{c.enrolledCount}/{c.capacity}</span>
          </div>
        );
      },
    },
    { header: "Status", accessorKey: "status", cell: (i) => (
      <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${STATUS_COLORS[i.getValue() as string]}`}>{i.getValue() as string}</span>
    )},
    {
      id: "actions", header: "",
      cell: (info) => {
        const c = info.row.original;
        return (
          <div className="flex gap-2 justify-end text-xs">
            <button onClick={() => loadMembers(c)} className="text-zinc-500 hover:text-zinc-800 hover:underline">
              <Users size={14} />
            </button>
            {c.status === "SCHEDULED" && (
              <button onClick={() => handleCancel(c.id)} className="text-red-600 hover:underline">Cancel</button>
            )}
          </div>
        );
      },
    },
  ];

  const table = useReactTable({
    data: courses, columns, state: { globalFilter },
    onGlobalFilterChange: setGlobalFilter,
    getCoreRowModel: getCoreRowModel(), getFilteredRowModel: getFilteredRowModel(),
  });

  return (
    <div className="p-6 space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-semibold text-zinc-900">My Courses</h1>
          <p className="text-sm text-zinc-500 mt-0.5">{courses.length} total</p>
        </div>
        <button onClick={() => setShowCreate(true)}
          className="flex items-center gap-2 rounded-lg bg-zinc-900 px-4 py-2 text-sm font-medium text-white hover:bg-zinc-700 transition-colors">
          <PlusCircle size={15} /> New Course
        </button>
      </div>

      <div className="relative max-w-sm">
        <Search size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-zinc-400" />
        <input value={globalFilter} onChange={(e) => setGlobalFilter(e.target.value)}
          placeholder="Search courses…"
          className="w-full rounded-lg border border-zinc-200 bg-white py-2 pl-8 pr-3 text-sm focus:outline-none focus:ring-2 focus:ring-zinc-900" />
      </div>

      <div className="rounded-xl border border-zinc-200 bg-white overflow-hidden">
        {loading ? <div className="p-8 text-center text-sm text-zinc-400">Loading…</div> : (
          <table className="w-full text-sm">
            <thead className="bg-zinc-50 border-b border-zinc-200">
              {table.getHeaderGroups().map((hg) => (
                <tr key={hg.id}>{hg.headers.map((h) => (
                  <th key={h.id} className="px-4 py-3 text-left text-xs font-medium text-zinc-500 uppercase tracking-wide">
                    {flexRender(h.column.columnDef.header, h.getContext())}
                  </th>
                ))}</tr>
              ))}
            </thead>
            <tbody className="divide-y divide-zinc-100">
              {table.getRowModel().rows.length === 0 ? (
                <tr><td colSpan={columns.length} className="px-4 py-8 text-center text-zinc-400">No courses found</td></tr>
              ) : (
                table.getRowModel().rows.map((row) => (
                  <tr key={row.id} className="hover:bg-zinc-50">
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

      {showCreate && (
        <CreateCourseModal onClose={() => setShowCreate(false)} onCreated={() => { setShowCreate(false); load(); }} />
      )}

      {membersModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm">
          <div className="w-full max-w-md bg-white rounded-2xl shadow-xl p-6 space-y-4 max-h-[80vh] overflow-y-auto">
            <div className="flex items-center justify-between">
              <h2 className="text-base font-semibold text-zinc-900">{membersModal.name}</h2>
              <button onClick={() => setMembersModal(null)} className="text-zinc-400 hover:text-zinc-700"><X size={18} /></button>
            </div>
            {membersModal.bookings.length === 0 ? (
              <p className="text-sm text-zinc-400">No registrations yet.</p>
            ) : membersModal.bookings.map((b) => (
              <div key={b.id} className="flex items-center justify-between rounded-lg border border-zinc-100 px-4 py-3">
                <div>
                  <p className="text-sm font-medium text-zinc-900">Member #{b.memberId}</p>
                  <p className="text-xs text-zinc-500">{b.memberEmail ?? "—"}</p>
                </div>
                <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${b.status === "BOOKED" ? "bg-green-100 text-green-700" : "bg-yellow-100 text-yellow-700"}`}>
                  {b.status}
                </span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
