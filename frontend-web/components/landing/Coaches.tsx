"use client";
import { useState } from "react";

const coaches = [
  { img: "https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=400&q=80", name: "Adam Torres", spec: "Strength & Conditioning", exp: "8 years", classes: "12/week", rating: "4.9 / 5", members: "47 active", bio: "Adam specializes in powerlifting and hypertrophy training. He holds a CSCS certification and has coached competitive athletes at national level." },
  { img: "https://images.unsplash.com/photo-1518611012118-696072aa579a?w=400&q=80", name: "Sofia Laurent", spec: "Yoga & Mindfulness", exp: "6 years", classes: "10/week", rating: "5.0 / 5", members: "38 active", bio: "Sofia is a certified Ashtanga and Vinyasa yoga instructor. Her classes blend movement, breathwork and meditation for a holistic experience." },
  { img: "https://images.unsplash.com/photo-1567013127542-490d757e51fc?w=400&q=80", name: "Marcus Reid", spec: "Cardio & HIIT", exp: "10 years", classes: "15/week", rating: "4.8 / 5", members: "62 active", bio: "Marcus is a former sprinter turned HIIT coach. His high-energy sessions are designed to maximize fat burn and cardiovascular endurance." },
  { img: "https://images.unsplash.com/photo-1594381898411-846e7d193883?w=400&q=80", name: "Lena Müller", spec: "Aquatics & Swimming", exp: "5 years", classes: "8/week", rating: "4.9 / 5", members: "29 active", bio: "Lena is a certified swim coach with a background in competitive open-water swimming. She offers beginner to advanced swimming programs." },
  { img: "https://images.unsplash.com/photo-1583454110551-21f2fa2afe61?w=400&q=80", name: "James Park", spec: "CrossFit & Functional", exp: "7 years", classes: "14/week", rating: "4.7 / 5", members: "55 active", bio: "James holds a CrossFit Level 2 certification. His functional fitness approach helps everyday athletes build practical strength and agility." },
];

export default function Coaches() {
  const [selected, setSelected] = useState<number | null>(null);
  const [offset, setOffset] = useState(0);

  return (
    <section id="coaches" className="bg-gray-50 py-20">
      <div className="max-w-6xl mx-auto px-8">
        <div className="text-xs font-medium text-purple-600 tracking-widest uppercase mb-3">Our team</div>
        <h2 className="text-4xl font-light mb-4">Meet our coaches</h2>
        <p className="text-gray-500 font-light max-w-lg leading-relaxed mb-12">World-class trainers dedicated to helping you achieve your fitness goals.</p>
        <div className="relative overflow-hidden">
          <button onClick={() => setOffset(Math.max(0, offset - 1))} className="absolute left-0 top-1/2 -translate-y-1/2 z-10 w-9 h-9 bg-white border border-gray-200 rounded-full flex items-center justify-center cursor-pointer hover:border-purple-300 hover:shadow-md transition-all duration-200">
            <svg className="w-4 h-4" viewBox="0 0 24 24" stroke="currentColor" fill="none" strokeWidth={2}><polyline points="15,18 9,12 15,6" /></svg>
          </button>
          <div className="flex gap-6 transition-transform duration-500 ease-in-out px-12" style={{ transform: `translateX(-${offset * 236}px)` }}>
            {coaches.map((c, i) => (
              <div
                key={i}
                onClick={() => setSelected(i)}
                className="min-w-[220px] border border-gray-100 rounded-2xl overflow-hidden cursor-pointer hover:border-purple-300 hover:shadow-md hover:-translate-y-1 transition-all duration-300 bg-white"
              >
                <div className="overflow-hidden">
                  <img src={c.img} alt={c.name} className="w-full h-36 object-cover object-top hover:scale-105 transition-transform duration-500" />
                </div>
                <div className="p-4">
                  <div className="text-base font-medium mb-1">{c.name}</div>
                  <div className="text-sm text-purple-600 font-light">{c.spec}</div>
                  <div className="text-xs font-light text-gray-400 mt-1">{c.exp} experience</div>
                </div>
              </div>
            ))}
          </div>
          <button onClick={() => setOffset(Math.min(coaches.length - 4, offset + 1))} className="absolute right-0 top-1/2 -translate-y-1/2 z-10 w-9 h-9 bg-white border border-gray-200 rounded-full flex items-center justify-center cursor-pointer hover:border-purple-300 hover:shadow-md transition-all duration-200">
            <svg className="w-4 h-4" viewBox="0 0 24 24" stroke="currentColor" fill="none" strokeWidth={2}><polyline points="9,18 15,12 9,6" /></svg>
          </button>
        </div>
      </div>

      {selected !== null && (
        <div
          className="fixed inset-0 bg-black/40 z-50 flex items-center justify-center animate-in fade-in duration-200"
          onClick={() => setSelected(null)}
        >
          <div
            className="bg-white rounded-2xl p-8 max-w-md w-full mx-4 relative animate-in fade-in slide-in-from-bottom-4 duration-300"
            onClick={(e) => e.stopPropagation()}
          >
            <button onClick={() => setSelected(null)} className="absolute top-4 right-4 text-gray-400 hover:text-gray-600 text-xl cursor-pointer bg-transparent border-none transition-colors duration-200">✕</button>
            <img src={coaches[selected].img} alt={coaches[selected].name} className="w-20 h-20 rounded-full object-cover object-top mx-auto mb-4 ring-4 ring-purple-100" />
            <div className="text-xl font-medium text-center mb-1">{coaches[selected].name}</div>
            <div className="text-sm text-purple-600 font-light text-center mb-6">{coaches[selected].spec}</div>
            <div className="grid grid-cols-2 gap-3 mb-4">
              {[["Experience", coaches[selected].exp], ["Classes/week", coaches[selected].classes], ["Rating", coaches[selected].rating], ["Members", coaches[selected].members]].map(([lbl, val]) => (
                <div key={lbl} className="bg-gray-50 rounded-xl p-3">
                  <div className="text-xs font-light text-gray-400 mb-1">{lbl}</div>
                  <div className="text-sm font-medium">{val}</div>
                </div>
              ))}
            </div>
            <p className="text-sm font-light text-gray-500 leading-relaxed">{coaches[selected].bio}</p>
          </div>
        </div>
      )}
    </section>
  );
}