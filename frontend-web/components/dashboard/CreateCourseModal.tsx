"use client";

import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { toast } from "sonner";
import { coursesApi } from "@/lib/api/courses";
import { X } from "lucide-react";

const schema = z.object({
  name: z.string().min(1, "Required"),
  description: z.string().optional(),
  dateTime: z.string().min(1, "Required"),
  durationMinutes: z.number({ coerce: true }).int().positive(),
  capacity: z.number({ coerce: true }).int().min(1),
  level: z.enum(["BEGINNER", "INTERMEDIATE", "ADVANCED"]),
  room: z.string().optional(),
});

type FormValues = z.infer<typeof schema>;

interface Props { onClose: () => void; onCreated: () => void; }

export default function CreateCourseModal({ onClose, onCreated }: Props) {
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { durationMinutes: 60, capacity: 10, level: "BEGINNER" },
  });

  const onSubmit = async (values: FormValues) => {
    try {
      await coursesApi.create({ ...values, dateTime: values.dateTime + ":00" });
      toast.success("Course created");
      onCreated();
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message ?? "Failed to create course";
      toast.error(msg);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm">
      <div className="w-full max-w-md bg-white rounded-2xl shadow-xl p-6 max-h-[90vh] overflow-y-auto">
        <div className="flex items-center justify-between mb-5">
          <h2 className="text-base font-semibold text-zinc-900">Create Course</h2>
          <button onClick={onClose} className="text-zinc-400 hover:text-zinc-700"><X size={18} /></button>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          {[
            { name: "name" as const, label: "Course Name", type: "text" },
            { name: "description" as const, label: "Description (optional)", type: "text" },
            { name: "dateTime" as const, label: "Date & Time", type: "datetime-local" },
            { name: "durationMinutes" as const, label: "Duration (minutes)", type: "number" },
            { name: "capacity" as const, label: "Capacity", type: "number" },
            { name: "room" as const, label: "Room (optional)", type: "text" },
          ].map(({ name, label, type }) => (
            <div key={name}>
              <label className="block text-xs font-medium text-zinc-600 mb-1">{label}</label>
              <input type={type} {...register(name)}
                className="w-full rounded-lg border border-zinc-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-zinc-900" />
              {errors[name] && <p className="mt-1 text-xs text-red-500">{errors[name]?.message}</p>}
            </div>
          ))}

          <div>
            <label className="block text-xs font-medium text-zinc-600 mb-1">Level</label>
            <select {...register("level")} className="w-full rounded-lg border border-zinc-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-zinc-900">
              <option value="BEGINNER">Beginner</option>
              <option value="INTERMEDIATE">Intermediate</option>
              <option value="ADVANCED">Advanced</option>
            </select>
          </div>

          <div className="flex gap-3 pt-2">
            <button type="button" onClick={onClose} className="flex-1 rounded-lg border border-zinc-200 py-2 text-sm font-medium text-zinc-600 hover:bg-zinc-50">Cancel</button>
            <button type="submit" disabled={isSubmitting} className="flex-1 rounded-lg bg-zinc-900 py-2 text-sm font-medium text-white hover:bg-zinc-700 disabled:opacity-50">
              {isSubmitting ? "Creating…" : "Create Course"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
