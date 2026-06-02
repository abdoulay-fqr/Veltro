"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useAuth } from "@/lib/auth/useAuth";
import {
  Users,
  UserCheck,
  LayoutDashboard,
  CreditCard,
  BarChart2,
  Receipt,
  LogOut,
} from "lucide-react";
import { cn } from "@/lib/utils";

const adminNav = [
  { label: "Overview", href: "/dashboard", icon: LayoutDashboard },
  { label: "Members", href: "/dashboard/members", icon: Users },
  { label: "Coaches", href: "/dashboard/coaches", icon: UserCheck },
  { label: "NFC Simulator", href: "/dashboard/coaches?tab=nfc", icon: CreditCard },
  { label: "Subscriptions", href: "/dashboard/subscriptions", icon: Receipt },
  { label: "Reports", href: "/dashboard/subscriptions/reports", icon: BarChart2 },
];

const coachNav = [
  { label: "My Profile", href: "/dashboard/coach", icon: UserCheck },
];

export default function Sidebar() {
  const { user, logout } = useAuth();
  const pathname = usePathname();
  const isAdmin = user?.role === "ADMIN" || user?.role === "SUPER_ADMIN";
  const nav = isAdmin ? adminNav : coachNav;

  return (
    <aside className="w-64 shrink-0 flex flex-col border-r border-zinc-200 bg-white min-h-screen">
      <div className="px-6 py-5 border-b border-zinc-100">
        <span className="text-lg font-semibold tracking-tight text-zinc-900">Veltro</span>
        <p className="text-xs text-zinc-500 mt-0.5 capitalize">{user?.role?.toLowerCase()} portal</p>
      </div>

      <nav className="flex-1 px-3 py-4 space-y-0.5">
        {nav.map(({ label, href, icon: Icon }) => {
          const active = href === "/dashboard"
            ? pathname === href
            : pathname.startsWith(href.split("?")[0]);
          return (
            <Link
              key={href}
              href={href}
              className={cn(
                "flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors",
                active
                  ? "bg-zinc-100 text-zinc-900"
                  : "text-zinc-500 hover:bg-zinc-50 hover:text-zinc-800"
              )}
            >
              <Icon size={16} />
              {label}
            </Link>
          );
        })}
      </nav>

      <div className="px-3 pb-4">
        <button
          onClick={logout}
          className="flex w-full items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium text-zinc-500 hover:bg-zinc-50 hover:text-zinc-800 transition-colors"
        >
          <LogOut size={16} />
          Sign out
        </button>
      </div>
    </aside>
  );
}
