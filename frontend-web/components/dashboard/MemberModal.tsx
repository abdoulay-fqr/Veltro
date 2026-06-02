"use client";

import { MemberResponse } from "@/lib/api/members";
import { useAuth } from "@/lib/auth/useAuth";
import { X } from "lucide-react";

interface Props {
  member: MemberResponse;
  onClose: () => void;
  onSuspend: (id: number) => void;
  onActivate: (id: number) => void;
}

export default function MemberModal({ member, onClose, onSuspend, onActivate }: Props) {
  const { user } = useAuth();
  const isAdmin = user?.role === "ADMIN" || user?.role === "SUPER_ADMIN";

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm">
      <div className="w-full max-w-md bg-white rounded-2xl shadow-xl p-6 space-y-4">
        <div className="flex items-start justify-between">
          <div className="flex items-center gap-3">
            {member.avatarUrl ? (
              <img
                src={`http://localhost:8082${member.avatarUrl}`}
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
              <span
                className={`text-xs font-medium ${
                  member.status === "ACTIVE" ? "text-green-600" : "text-red-600"
                }`}
              >
                {member.status}
              </span>
            </div>
          </div>
          <button
            onClick={onClose}
            className="text-zinc-400 hover:text-zinc-700 transition-colors"
          >
            <X size={18} />
          </button>
        </div>

        <dl className="grid grid-cols-2 gap-3 text-sm">
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
          <div className="flex gap-3 pt-2">
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
        )}
      </div>
    </div>
  );
}
