"use client";

import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth/useAuth";
import type { UserRole } from "@/lib/auth/AuthProvider";

const schema = z.object({
  identifier: z
    .string()
    .min(1, "Email is required")
    .regex(
      /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
      "Please enter a valid email address"
    ),
  password: z.string().min(1, "Password is required"),
});

type FormData = z.infer<typeof schema>;

export default function LoginPage() {
  const router = useRouter();
  const [serverError, setServerError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  const { setUser } = useAuth();

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<FormData>({
    resolver: zodResolver(schema),
  });

  const onSubmit = async (data: FormData) => {
  setLoading(true);
  setServerError(null);

  try {
    const res = await fetch("/api/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(data),
    });

    const json = await res.json();

    if (!res.ok) {
      setServerError(json.message || "Something went wrong");
      return;
    }

    const role = json.role as UserRole;
    setUser({ identifier: data.identifier, role });

    if (role === "ADMIN" || role === "SUPER_ADMIN") {
      router.push("/dashboard");
    } else if (role === "COACH") {
      router.push("/coach");
    } else {
      router.push("/unauthorized");
    }
  } catch {
    setServerError("Unable to reach the server. Please try again.");
  } finally {
    setLoading(false);
  }
};

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4">
      <div className="w-full max-w-md animate-in fade-in slide-in-from-bottom-4 duration-500">

        {/* Logo */}
        <div className="text-center mb-8">
          <div className="text-3xl font-bold text-purple-600 uppercase tracking-tight mb-2">Veltro</div>
          <p className="text-sm font-light text-gray-500">Sign in to your dashboard</p>
        </div>

        {/* Card */}
        <div className="bg-white border border-gray-100 rounded-2xl p-8 shadow-sm">
          <h1 className="text-xl font-medium mb-1">Welcome back</h1>
          <p className="text-sm font-light text-gray-400 mb-6">Enter your credentials to continue</p>

          <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4">

            {/* Identifier */}
            <div className="flex flex-col gap-1.5">
              <label className="text-xs font-light text-gray-500">Email</label>
              <input
                {...register("identifier")}
                type="text"
                placeholder="john@example.com"
                className={`px-4 py-2.5 border rounded-lg text-sm font-light focus:outline-none transition-colors duration-200 ${
                  errors.identifier
                    ? "border-red-300 focus:border-red-400"
                    : "border-gray-200 focus:border-purple-400"
                }`}
              />
              {errors.identifier && (
                <span className="text-xs text-red-500 font-light">{errors.identifier.message}</span>
              )}
            </div>

            {/* Password */}
<div className="flex flex-col gap-1.5">
  <label className="text-xs font-light text-gray-500">Password</label>
  <div className="relative">
    <input
      {...register("password")}
      type={showPassword ? "text" : "password"}
      placeholder="••••••••"
      className={`w-full px-4 py-2.5 pr-10 border rounded-lg text-sm font-light focus:outline-none transition-colors duration-200 ${
        errors.password
          ? "border-red-300 focus:border-red-400"
          : "border-gray-200 focus:border-purple-400"
      }`}
    />
    <button
      type="button"
      onClick={() => setShowPassword(!showPassword)}
      className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 transition-colors duration-200 bg-transparent border-none cursor-pointer"
    >
      {showPassword ? (
        <svg className="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={2}>
          <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94"/>
          <path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19"/>
          <line x1="1" y1="1" x2="23" y2="23"/>
        </svg>
      ) : (
        <svg className="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={2}>
          <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
          <circle cx="12" cy="12" r="3"/>
        </svg>
      )}
    </button>
  </div>
  {errors.password && (
    <span className="text-xs text-red-500 font-light">{errors.password.message}</span>
  )}
</div>

            {/* Server error */}
            {serverError && (
              <div className="bg-red-50 border border-red-100 text-red-600 text-sm font-light px-4 py-3 rounded-lg animate-in fade-in duration-200">
                {serverError}
              </div>
            )}

            {/* Submit */}
            <button
              type="submit"
              disabled={loading}
              className="bg-purple-600 hover:bg-purple-700 disabled:opacity-60 disabled:cursor-not-allowed text-white py-2.5 rounded-lg text-sm font-light cursor-pointer transition-all duration-200 hover:scale-[1.02] active:scale-95 mt-1"
            >
              {loading ? (
                <span className="flex items-center justify-center gap-2">
                  <svg className="w-4 h-4 animate-spin" viewBox="0 0 24 24" fill="none">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"/>
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v8z"/>
                  </svg>
                  Signing in...
                </span>
              ) : (
                "Sign in"
              )}
            </button>
          </form>
        </div>

        <p className="text-center text-xs font-light text-gray-400 mt-6">
          © 2026 Veltro. All rights reserved.
        </p>
      </div>
    </div>
  );
}