"use client";

import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { toast } from "sonner";
import { coachesApi, CoachResponse } from "@/lib/api/coaches";
import api from "@/lib/api";
import { X } from "lucide-react";

// Base fields shared by create and edit
const baseSchema = z.object({
  identifier: z.string().optional(),
  firstname: z.string().min(1, "Required"),
  lastname: z.string().min(1, "Required"),
  phone: z.string().optional(),
  bio: z.string().optional(),
  specialization: z.string().optional(),
  certifications: z.string().optional(),
});

// In create mode, identifier must be a valid email
const createSchema = baseSchema.extend({
  identifier: z.string().email("Must be a valid email"),
});

type FormValues = z.infer<typeof baseSchema>;

interface Props {
  coach?: CoachResponse;
  onClose: () => void;
  onSaved: () => void;
}

export default function CreateCoachForm({ coach, onClose, onSaved }: Props) {
  const isEdit = !!coach;
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({
    resolver: zodResolver(isEdit ? baseSchema : createSchema),
    defaultValues: coach
      ? {
          identifier: "",
          firstname: coach.firstname,
          lastname: coach.lastname,
          phone: coach.phone ?? "",
          bio: coach.bio ?? "",
          specialization: coach.specialization ?? "",
          certifications: coach.certifications ?? "",
        }
      : undefined,
  });

  const onSubmit = async (values: FormValues) => {
    try {
      if (isEdit && coach) {
        await coachesApi.update(coach.id, values);
        toast.success("Coach updated successfully");
      } else {
        // Auto-register the auth user to get userId, then create the coach profile
        const authRes = await api.post<{ data: { userId: number } }>("/auth/register", {
          identifier: values.identifier,
          password: "Veltro@2024",
          role: "COACH",
        });
        const userId = authRes.data.data.userId;
        await coachesApi.create({ ...values, userId });
        toast.success("Coach created successfully");
      }
      onSaved();
    } catch (err: unknown) {
      const msg =
        (err as { response?: { data?: { message?: string } } })?.response?.data?.message ??
        `Failed to ${isEdit ? "update" : "create"} coach`;
      toast.error(msg);
    }
  };

  const fields = [
    { name: "identifier" as const, label: "Email", type: "email", hidden: isEdit },
    { name: "firstname" as const, label: "First Name", type: "text" },
    { name: "lastname" as const, label: "Last Name", type: "text" },
    { name: "phone" as const, label: "Phone", type: "tel" },
    { name: "bio" as const, label: "Bio", type: "text" },
    { name: "specialization" as const, label: "Specialization", type: "text" },
    { name: "certifications" as const, label: "Certifications", type: "text" },
  ];

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm">
      <div className="w-full max-w-md bg-white rounded-2xl shadow-xl p-6 max-h-[90vh] overflow-y-auto">
        <div className="flex items-center justify-between mb-5">
          <h2 className="text-base font-semibold text-zinc-900">
            {isEdit ? "Edit Coach" : "Add New Coach"}
          </h2>
          <button onClick={onClose} className="text-zinc-400 hover:text-zinc-700">
            <X size={18} />
          </button>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          {fields
            .filter((f) => !f.hidden)
            .map(({ name, label, type }) => (
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
              {isSubmitting ? "Saving…" : isEdit ? "Save Changes" : "Create Coach"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
