"use client";

import { useState } from "react";
import { MemberResponse, membersApi } from "@/lib/api/members";
import { useAuth } from "@/lib/auth/useAuth";
import { toast } from "sonner";
import { X, Pencil, Trash2 } from "lucide-react";

interface Props {
  member: MemberResponse;
  onClose: () => void;
  onSuspend: (id: number) => void;
  onActivate: (id: number) => void;
  onDeleted: () => void;
  onUpdated: (m: MemberResponse) => void;
}

export default function MemberModal({ member, onClose, onSuspend, onActivate, onDeleted, onUpdated }: Props) {
  const { user } = useAuth();
  const isAdmin = user?.role === "ADMIN" || user?.role === "SUPER_ADMIN";

  const [mode, setMode] = useState<"view" | "edit" | "confirmDelete">("view");
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState({
    firstname: member.firstname,
    lastname: member.lastname,
    phone: member.phone ?? "",
  });

  const handleSaveEdit = async () => {
    setSaving(true);
    try {
      const res = await membersApi.update(member.id, form);
      toast.success("Member updated");
      onUpdated(res.data.data);
    } catch {
      toast.error("Failed to update member");
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    setSaving(true);
    try {
      await membersApi.delete(member.id);
      toast.success("Member deleted");
      onDeleted();
    } catch {
      toast.error("Failed to delete member");
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm">
      <div className="w-full max-w-md bg-white rounded-2xl shadow-xl p-6 space-y-4">

        {/* Header */}
        <div className="flex items-start justify-between">
          <div className="flex items-center gap-3">
            {member.avatarUrl ? (
              <img
                src={`${process.env.NEXT_PUBLIC_USER_SERVICE_URL ?? "http://localhost:8082"}${member.avatarUrl}`}
                alt="avatar"
                className="w-12 h-12 rounded-full object-cover border border-zinc-200"
              />
            ) : (
              <div className="w-12 h-12 rounded-full bg-zinc-100 flex items-center justify-center text-zinc-400 text-lg font-medium">
                {member.firstname[0]}
              </div>
            )}
            <div>
              <h2 className="text-base font-semibold text-zinc-900">
                {member.firstname} {member.lastname}
              </h2>
              <span className={`text-xs font-medium ${member.status === "ACTIVE" ? "text-green-600" : "text-red-600"}`}>
                {member.status}
              </span>
            </div>
          </div>
          <button onClick={onClose} className="text-zinc-400 hover:text-zinc-700 transition-colors">
            <X size={18} />
          </button>
        </div>

        {/* View mode */}
        {mode === "view" && (
          <>
            <dl className="grid grid-cols-2 gap-3 text-sm">
              <div>
                <dt className="text-zinc-500 text-xs">Email</dt>
                <dd className="text-zinc-900">{member.email ?? "—"}</dd>
              </div>
              <div>
                <dt className="text-zinc-500 text-xs">Phone</dt>
                <dd className="text-zinc-900">{member.phone ?? "—"}</dd>
              </div>
              <div>
                <dt className="text-zinc-500 text-xs">Date of Birth</dt>
                <dd className="text-zinc-900">{member.dateOfBirth ?? "—"}</dd>
              </div>
              <div>
                <dt className="text-zinc-500 text-xs">Member ID</dt>
                <dd className="text-zinc-900">#{member.id}</dd>
              </div>
              <div>
                <dt className="text-zinc-500 text-xs">Joined</dt>
                <dd className="text-zinc-900">{new Date(member.createdAt).toLocaleDateString()}</dd>
              </div>
            </dl>

            {isAdmin && (
              <div className="space-y-2 pt-2">
                {/* Edit + Delete row */}
                <div className="flex gap-2">
                  <button
                    onClick={() => setMode("edit")}
                    className="flex flex-1 items-center justify-center gap-1.5 rounded-lg border border-zinc-200 py-2 text-sm font-medium text-zinc-700 hover:bg-zinc-50 transition-colors"
                  >
                    <Pencil size={14} /> Edit
                  </button>
                  <button
                    onClick={() => setMode("confirmDelete")}
                    className="flex flex-1 items-center justify-center gap-1.5 rounded-lg border border-red-200 py-2 text-sm font-medium text-red-600 hover:bg-red-50 transition-colors"
                  >
                    <Trash2 size={14} /> Delete
                  </button>
                </div>
                {/* Suspend / Activate */}
                <div className="flex gap-2">
                  {member.status === "ACTIVE" ? (
                    <button
                      onClick={() => { onSuspend(member.id); onClose(); }}
                      className="flex-1 rounded-lg border border-red-200 py-2 text-sm font-medium text-red-600 hover:bg-red-50 transition-colors"
                    >
                      Suspend Member
                    </button>
                  ) : (
                    <button
                      onClick={() => { onActivate(member.id); onClose(); }}
                      className="flex-1 rounded-lg border border-green-200 py-2 text-sm font-medium text-green-600 hover:bg-green-50 transition-colors"
                    >
                      Activate Member
                    </button>
                  )}
                  <button
                    onClick={onClose}
                    className="flex-1 rounded-lg bg-zinc-900 py-2 text-sm font-medium text-white hover:bg-zinc-700 transition-colors"
                  >
                    Close
                  </button>
                </div>
              </div>
            )}
          </>
        )}

        {/* Edit mode */}
        {mode === "edit" && (
          <div className="space-y-3">
            {(["firstname", "lastname", "phone"] as const).map((field) => (
              <div key={field}>
                <label className="block text-xs font-medium text-zinc-600 mb-1 capitalize">{field}</label>
                <input
                  value={form[field]}
                  onChange={(e) => setForm((p) => ({ ...p, [field]: e.target.value }))}
                  className="w-full rounded-lg border border-zinc-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-zinc-900"
                />
              </div>
            ))}
            <div className="flex gap-2 pt-1">
              <button
                onClick={() => setMode("view")}
                className="flex-1 rounded-lg border border-zinc-200 py-2 text-sm font-medium text-zinc-600 hover:bg-zinc-50 transition-colors"
              >
                Cancel
              </button>
              <button
                onClick={handleSaveEdit}
                disabled={saving}
                className="flex-1 rounded-lg bg-zinc-900 py-2 text-sm font-medium text-white hover:bg-zinc-700 disabled:opacity-50 transition-colors"
              >
                {saving ? "Saving…" : "Save Changes"}
              </button>
            </div>
          </div>
        )}

        {/* Delete confirmation */}
        {mode === "confirmDelete" && (
          <div className="space-y-4">
            <p className="text-sm text-zinc-700">
              Are you sure you want to delete <span className="font-semibold">{member.firstname} {member.lastname}</span>?
              This will remove their profile permanently and cannot be undone.
            </p>
            <div className="flex gap-2">
              <button
                onClick={() => setMode("view")}
                className="flex-1 rounded-lg border border-zinc-200 py-2 text-sm font-medium text-zinc-600 hover:bg-zinc-50 transition-colors"
              >
                Cancel
              </button>
              <button
                onClick={handleDelete}
                disabled={saving}
                className="flex-1 rounded-lg bg-red-600 py-2 text-sm font-medium text-white hover:bg-red-700 disabled:opacity-50 transition-colors"
              >
                {saving ? "Deleting…" : "Delete Member"}
              </button>
            </div>
          </div>
        )}

      </div>
    </div>
  );
}
