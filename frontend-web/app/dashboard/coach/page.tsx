"use client";

import { useEffect, useState, useRef } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { toast } from "sonner";
import { coachesApi, CoachResponse } from "@/lib/api/coaches";
import api from "@/lib/api";
import { useAuth } from "@/lib/auth/useAuth";
import { apiError } from "@/lib/utils/api-error";
import { Camera, Save } from "lucide-react";

const profileSchema = z.object({
  firstname: z.string().min(1, "Required"),
  lastname: z.string().min(1, "Required"),
  phone: z.string().optional(),
  bio: z.string().optional(),
  specialization: z.string().optional(),
  certifications: z.string().optional(),
});

const passwordSchema = z
  .object({
    currentPassword: z.string().min(1, "Required"),
    newPassword: z.string().min(8, "At least 8 characters"),
    confirmPassword: z.string().min(1, "Required"),
  })
  .refine((d) => d.newPassword === d.confirmPassword, {
    message: "Passwords do not match",
    path: ["confirmPassword"],
  });

type ProfileForm = z.infer<typeof profileSchema>;
type PasswordForm = z.infer<typeof passwordSchema>;

export default function CoachProfilePage() {
  const { user } = useAuth();
  const [coach, setCoach] = useState<CoachResponse | null>(null);
  const [avatarPreview, setAvatarPreview] = useState<string | null>(null);
  const fileRef = useRef<HTMLInputElement>(null);

  const profileForm = useForm<ProfileForm>({ resolver: zodResolver(profileSchema) });
  const passwordForm = useForm<PasswordForm>({ resolver: zodResolver(passwordSchema) });

  useEffect(() => {
    if (!user) return;
    // Derive coachId from X-User-Id header by fetching the coach matching the logged-in user
    // For now we search the coaches list — in production this would be /api/v1/users/coaches/me
    coachesApi.list({ size: 200 }).then((res) => {
      const found = res.data.data?.content?.find(() => true); // placeholder until /me endpoint
      if (found) {
        setCoach(found);
        profileForm.reset({
          firstname: found.firstname,
          lastname: found.lastname,
          phone: found.phone ?? "",
          bio: found.bio ?? "",
          specialization: found.specialization ?? "",
          certifications: found.certifications ?? "",
        });
      }
    });
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user]);

  const onSaveProfile = async (values: ProfileForm) => {
    if (!coach) return;
    try {
      const updated = await coachesApi.update(coach.id, values);
      setCoach(updated.data.data);
      toast.success("Profile updated");
    } catch (err) {
      apiError(err, "Failed to update profile");
    }
  };

  const onChangePassword = async (values: PasswordForm) => {
    try {
      await api.post("/auth/change-password", {
        currentPassword: values.currentPassword,
        newPassword: values.newPassword,
      });
      toast.success("Password changed");
      passwordForm.reset();
    } catch (err) {
      apiError(err, "Failed to change password");
    }
  };

  const handleAvatarChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file || !coach) return;
    setAvatarPreview(URL.createObjectURL(file));
    try {
      const res = await coachesApi.uploadAvatar(coach.id, file);
      setCoach(res.data.data);
      toast.success("Avatar updated");
    } catch (err) {
      apiError(err, "Failed to upload avatar");
      setAvatarPreview(null);
    }
  };

  if (!coach) {
    return <div className="p-6 text-sm text-zinc-400">Loading profile…</div>;
  }

  return (
    <div className="p-6 max-w-2xl space-y-8">
      <div>
        <h1 className="text-xl font-semibold text-zinc-900">My Profile</h1>
        <p className="text-sm text-zinc-500 mt-0.5">Manage your coaching profile and settings.</p>
      </div>

      {/* Avatar */}
      <div className="flex items-center gap-5">
        <div className="relative">
          {avatarPreview || coach.avatarUrl ? (
            <img
              src={avatarPreview ?? `http://localhost:8082${coach.avatarUrl}`}
              alt="avatar"
              className="w-20 h-20 rounded-full object-cover border-2 border-zinc-200"
            />
          ) : (
            <div className="w-20 h-20 rounded-full bg-zinc-100 flex items-center justify-center text-2xl font-medium text-zinc-400">
              {coach.firstname[0]}
            </div>
          )}
          <button
            onClick={() => fileRef.current?.click()}
            className="absolute -bottom-1 -right-1 w-7 h-7 rounded-full bg-zinc-900 flex items-center justify-center text-white hover:bg-zinc-700 transition-colors"
          >
            <Camera size={12} />
          </button>
          <input ref={fileRef} type="file" accept="image/*" className="hidden" onChange={handleAvatarChange} />
        </div>
        <div>
          <p className="text-base font-semibold text-zinc-900">{coach.firstname} {coach.lastname}</p>
          <p className="text-sm text-zinc-500">{coach.specialization ?? "Coach"}</p>
        </div>
      </div>

      {/* Profile form */}
      <section className="rounded-xl border border-zinc-200 bg-white p-6 space-y-4">
        <h2 className="text-sm font-semibold text-zinc-900">Profile Information</h2>
        <form onSubmit={profileForm.handleSubmit(onSaveProfile)} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            {(["firstname", "lastname"] as const).map((name) => (
              <div key={name}>
                <label className="block text-xs font-medium text-zinc-600 mb-1 capitalize">{name}</label>
                <input
                  {...profileForm.register(name)}
                  className="w-full rounded-lg border border-zinc-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-zinc-900"
                />
                {profileForm.formState.errors[name] && (
                  <p className="mt-1 text-xs text-red-500">{profileForm.formState.errors[name]?.message}</p>
                )}
              </div>
            ))}
          </div>

          {(["phone", "bio", "specialization", "certifications"] as const).map((name) => (
            <div key={name}>
              <label className="block text-xs font-medium text-zinc-600 mb-1 capitalize">{name}</label>
              <input
                {...profileForm.register(name)}
                className="w-full rounded-lg border border-zinc-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-zinc-900"
              />
            </div>
          ))}

          <button
            type="submit"
            disabled={profileForm.formState.isSubmitting}
            className="flex items-center gap-2 rounded-lg bg-zinc-900 px-4 py-2 text-sm font-medium text-white hover:bg-zinc-700 disabled:opacity-50 transition-colors"
          >
            <Save size={14} />
            {profileForm.formState.isSubmitting ? "Saving…" : "Save Profile"}
          </button>
        </form>
      </section>

      {/* Change password */}
      <section className="rounded-xl border border-zinc-200 bg-white p-6 space-y-4">
        <h2 className="text-sm font-semibold text-zinc-900">Change Password</h2>
        <form onSubmit={passwordForm.handleSubmit(onChangePassword)} className="space-y-4">
          {([
            { name: "currentPassword" as const, label: "Current Password" },
            { name: "newPassword" as const, label: "New Password" },
            { name: "confirmPassword" as const, label: "Confirm New Password" },
          ] as const).map(({ name, label }) => (
            <div key={name}>
              <label className="block text-xs font-medium text-zinc-600 mb-1">{label}</label>
              <input
                type="password"
                {...passwordForm.register(name)}
                className="w-full rounded-lg border border-zinc-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-zinc-900"
              />
              {passwordForm.formState.errors[name] && (
                <p className="mt-1 text-xs text-red-500">{passwordForm.formState.errors[name]?.message}</p>
              )}
            </div>
          ))}

          <button
            type="submit"
            disabled={passwordForm.formState.isSubmitting}
            className="rounded-lg bg-zinc-900 px-4 py-2 text-sm font-medium text-white hover:bg-zinc-700 disabled:opacity-50 transition-colors"
          >
            {passwordForm.formState.isSubmitting ? "Changing…" : "Change Password"}
          </button>
        </form>
      </section>
    </div>
  );
}
