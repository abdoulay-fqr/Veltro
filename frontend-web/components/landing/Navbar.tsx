"use client";
import { useRouter } from "next/navigation";

export default function Navbar() {
  const router = useRouter();

  const scrollTo = (id: string) => {
    document.getElementById(id)?.scrollIntoView({ behavior: "smooth" });
  };

  return (
    <nav className="sticky top-0 z-50 bg-white/80 backdrop-blur-md border-b border-gray-100 px-8 flex items-center justify-between h-16 animate-in fade-in slide-in-from-top-2 duration-500">
      <div className="text-2xl font-bold text-purple-600 tracking-tight uppercase">Veltro</div>
      <ul className="flex gap-8 list-none">
        {[["why", "Why us"], ["areas", "Areas"], ["plans", "Plans"], ["coaches", "Coaches"], ["reviews", "Reviews"], ["contact", "Contact"]].map(([id, label]) => (
          <li key={id}>
            <button onClick={() => scrollTo(id)} className="text-sm font-light text-gray-500 hover:text-gray-900 cursor-pointer bg-transparent border-none transition-colors duration-200">
              {label}
            </button>
          </li>
        ))}
      </ul>
      <button
        onClick={() => router.push("/login")}
        className="bg-purple-600 hover:bg-purple-700 text-white px-5 py-2 rounded-lg text-sm cursor-pointer font-light transition-all duration-200 hover:scale-105 active:scale-95"
      >
        Login
      </button>
    </nav>
  );
}