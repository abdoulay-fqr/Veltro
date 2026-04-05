"use client";

import { useAuth } from "@/lib/auth/useAuth";
import AuthSkeleton from "@/components/AuthSkeleton";

export default function DashboardPage() {
  const { user, loading } = useAuth();
  if (loading) return <AuthSkeleton />;
  return <div className="p-8 text-xl font-light">Dashboard — coming soon</div>;
}