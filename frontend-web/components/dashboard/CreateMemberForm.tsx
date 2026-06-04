"use client";

import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { toast } from "sonner";
import { membersApi } from "@/lib/api/members";
import api from "@/lib/api";
import { X } from "lucide-react";

const schema = z.object({
  identifier: z.string().email("Must be a valid email"),
  firstname: z.string().min(1, "Required"),
  lastname: z.string().min(1, "Required"),
  phone: z.string().optional(),
  dateOfBirth: z.string().optional(),
});

type FormValues = z.infer<typeof schema>;

interface Props {
  onClose: () => void;
  onCreated: () => void;
}

export default function CreateMemberForm({ onClose, onCreated }: Props) {
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({ resolver: zodResolver(schema) });

  const onSubmit = async (values: FormValues) => {
    try {
      // 1. Register the auth user to obtain a userId
      const authRes = await api.post<{ data: { userId: number } }>("/auth/register", {
        identifier: values.identifier,
        password: "Veltro@2024",  // admin-created members use the default password
        role: "MEMBER",
      });
      const userId = authRes.data.data.userId;

      // 2. Create the member profile linked to that userId
      await membersApi.create({ ...values, userId });
      toast.success("Member created successfully");
      onCreated();
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })
        ?.response?.data?.message ?? "Failed to create member";
      toast.error(msg);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm">
      <div className="w-full max-w-md bg-white rounded-2xl shadow-xl p-6">
        <div className="flex items-center justify-between mb-5">
          <h2 className="text-base font-semibold text-zinc-900">Add New Member</h2>
          <button onClick={onClose} className="text-zinc-400 hover:text-zinc-700">
            <X size={18} />
          </button>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          {[
            { name: "identifier" as const, label: "Email", type: "email" },
            { name: "firstname" as const, label: "First Name", type: "text" },
            { name: "lastname" as const, label: "Last Name", type: "text" },
            { name: "phone" as const, label: "Phone (optional)", type: "tel" },
            { name: "dateOfBirth" as const, label: "Date of Birth (optional)", type: "date" },
          ].map(({ name, label, type }) => (
            <div key={name}>
              <label className="block text-xs font-medium text-zinc-600 mb-1">{label}</label>
              <input
                type={type}
                {...register(name)}
                className="w-full rounded-lg border border-zinc-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-zinc-900"
              />
              {errors[name] && (
                <p className="mt-1 text-xs text-red-500">{errors[name]?.message}</p>
              )}
            </div>
          ))}

          <div className="flex gap-3 pt-2">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 rounded-lg border border-zinc-200 py-2 text-sm font-medium text-zinc-600 hover:bg-zinc-50"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="flex-1 rounded-lg bg-zinc-900 py-2 text-sm font-medium text-white hover:bg-zinc-700 disabled:opacity-50"
            >
              {isSubmitting ? "Creating…" : "Create Member"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
