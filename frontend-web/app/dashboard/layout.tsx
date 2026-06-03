"use client";

import { useAuth } from "@/lib/auth/useAuth";
import AuthSkeleton from "@/components/AuthSkeleton";
import Sidebar from "@/components/dashboard/Sidebar";
import ChatWidget from "@/components/ChatWidget";
import { Toaster } from "sonner";
import { useRouter } from "next/navigation";
import { useEffect } from "react";

export default function DashboardLayout({ children }: { children: React.ReactNode }) {
  const { user, loading } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (!loading && !user) router.replace("/login");
  }, [loading, user, router]);

  if (loading) return <AuthSkeleton />;
  if (!user) return null;

  return (
    <div className="flex min-h-screen bg-zinc-50">
      <Sidebar />
      <main className="flex-1 overflow-auto">
        {children}
      </main>
      <Toaster richColors position="top-right" />
      <ChatWidget />
    </div>
  );
}
