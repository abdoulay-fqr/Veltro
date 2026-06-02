"use client";

import { useAuth } from "@/lib/auth/useAuth";

export default function DashboardPage() {
  const { user } = useAuth();
  const isAdmin = user?.role === "ADMIN" || user?.role === "SUPER_ADMIN";

  return (
    <div className="p-6 space-y-6">
      <div>
        <h1 className="text-xl font-semibold text-zinc-900">
          Welcome back{user ? `, ${user.identifier.split("@")[0]}` : ""}
        </h1>
        <p className="text-sm text-zinc-500 mt-1">
          {isAdmin ? "Manage your gym from this dashboard." : "Manage your coaching profile and sessions."}
        </p>
      </div>

      {isAdmin && (
        <div className="grid grid-cols-3 gap-4">
          {[
            { label: "Members", href: "/dashboard/members", description: "View and manage gym members" },
            { label: "Coaches", href: "/dashboard/coaches", description: "View and manage coaches" },
            { label: "NFC Simulator", href: "/dashboard/coaches?tab=nfc", description: "Test NFC card scanning" },
          ].map(({ label, href, description }) => (
            <a
              key={href}
              href={href}
              className="block rounded-xl border border-zinc-200 bg-white p-5 hover:border-zinc-300 hover:shadow-sm transition"
            >
              <p className="text-sm font-semibold text-zinc-900">{label}</p>
              <p className="text-xs text-zinc-500 mt-1">{description}</p>
            </a>
          ))}
        </div>
      )}
    </div>
  );
}
