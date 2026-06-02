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
import { membersApi, MemberResponse } from "@/lib/api/members";
import { useAuth } from "@/lib/auth/useAuth";
import MemberModal from "@/components/dashboard/MemberModal";
import CreateMemberForm from "@/components/dashboard/CreateMemberForm";
import { UserPlus, Search } from "lucide-react";

export default function MembersPage() {
  const { user } = useAuth();
  const [members, setMembers] = useState<MemberResponse[]>([]);
  const [globalFilter, setGlobalFilter] = useState("");
  const [statusFilter, setStatusFilter] = useState<"" | "ACTIVE" | "SUSPENDED">("");
  const [selected, setSelected] = useState<MemberResponse | null>(null);
  const [showCreate, setShowCreate] = useState(false);
  const [loading, setLoading] = useState(true);

  const loadMembers = useCallback(async () => {
    setLoading(true);
    try {
      const res = await membersApi.list({ status: statusFilter || undefined, size: 100 });
      setMembers(res.data.data?.content ?? []);
    } catch {
      toast.error("Failed to load members");
    } finally {
      setLoading(false);
    }
  }, [statusFilter]);

  useEffect(() => { loadMembers(); }, [loadMembers]);

  const handleSuspend = async (id: number) => {
    try {
      await membersApi.suspend(id);
      toast.success("Member suspended");
      loadMembers();
    } catch {
      toast.error("Failed to suspend member");
    }
  };

  const handleActivate = async (id: number) => {
    try {
      await membersApi.activate(id);
      toast.success("Member activated");
      loadMembers();
    } catch {
      toast.error("Failed to activate member");
    }
  };

  const columns: ColumnDef<MemberResponse>[] = [
    {
      header: "Name",
      accessorFn: (r) => `${r.firstname} ${r.lastname}`,
      cell: (info) => (
        <button
          className="text-left font-medium text-zinc-900 hover:underline"
          onClick={() => setSelected(info.row.original)}
        >
          {info.getValue() as string}
        </button>
      ),
    },
    { header: "Phone", accessorKey: "phone", cell: (i) => i.getValue() ?? "—" },
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
      header: "Joined",
      accessorKey: "createdAt",
      cell: (i) => new Date(i.getValue() as string).toLocaleDateString(),
    },
    {
      id: "actions",
      header: "",
      cell: (info) => {
        const m = info.row.original;
        const isAdmin = user?.role === "ADMIN" || user?.role === "SUPER_ADMIN";
        if (!isAdmin) return null;
        return (
          <div className="flex gap-2 justify-end">
            {m.status === "ACTIVE" ? (
              <button
                onClick={() => handleSuspend(m.id)}
                className="text-xs text-red-600 hover:underline"
              >
                Suspend
              </button>
            ) : (
              <button
                onClick={() => handleActivate(m.id)}
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
    data: members,
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
          <h1 className="text-xl font-semibold text-zinc-900">Members</h1>
          <p className="text-sm text-zinc-500 mt-0.5">{members.length} total</p>
        </div>
        <button
          onClick={() => setShowCreate(true)}
          className="flex items-center gap-2 rounded-lg bg-zinc-900 px-4 py-2 text-sm font-medium text-white hover:bg-zinc-700 transition-colors"
        >
          <UserPlus size={15} />
          Add Member
        </button>
      </div>

      <div className="flex gap-3">
        <div className="relative flex-1 max-w-sm">
          <Search size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-zinc-400" />
          <input
            value={globalFilter}
            onChange={(e) => setGlobalFilter(e.target.value)}
            placeholder="Search members…"
            className="w-full rounded-lg border border-zinc-200 bg-white py-2 pl-8 pr-3 text-sm focus:outline-none focus:ring-2 focus:ring-zinc-900"
          />
        </div>
        <select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value as "" | "ACTIVE" | "SUSPENDED")}
          className="rounded-lg border border-zinc-200 bg-white px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-zinc-900"
        >
          <option value="">All statuses</option>
          <option value="ACTIVE">Active</option>
          <option value="SUSPENDED">Suspended</option>
        </select>
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
                    No members found
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

      {selected && (
        <MemberModal
          member={selected}
          onClose={() => setSelected(null)}
          onSuspend={handleSuspend}
          onActivate={handleActivate}
        />
      )}

      {showCreate && (
        <CreateMemberForm
          onClose={() => setShowCreate(false)}
          onCreated={() => { setShowCreate(false); loadMembers(); }}
        />
      )}
    </div>
  );
}
