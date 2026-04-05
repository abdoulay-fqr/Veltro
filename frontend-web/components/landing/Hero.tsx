"use client";

export default function Hero() {
  return (
    <div className="max-w-6xl mx-auto px-8 py-24 grid grid-cols-2 gap-16 items-center">
      <div className="animate-in fade-in slide-in-from-left-8 duration-700">
        <h1 className="text-5xl font-light leading-tight mb-6 tracking-tight">
          Train smarter.<br /><span className="text-purple-600 font-medium">Manage better.</span>
        </h1>
        <p className="text-lg font-light text-gray-500 leading-relaxed mb-8 max-w-md">
          Veltro connects members, coaches, and administrators in one seamless platform. Track activity, book sessions, and grow your gym.
        </p>
      </div>
      <div className="animate-in fade-in slide-in-from-right-8 duration-700 bg-gray-50 border border-gray-100 rounded-2xl p-6 flex flex-col gap-4">
        <div className="grid grid-cols-3 gap-3">
          {[["1.2k", "Members"], ["98%", "Retention"], ["24/7", "Access"]].map(([val, lbl]) => (
            <div key={lbl} className="bg-white border border-gray-100 rounded-xl p-4 text-center hover:border-purple-200 hover:shadow-sm transition-all duration-200">
              <div className="text-2xl font-medium text-purple-600">{val}</div>
              <div className="text-xs font-light text-gray-400 mt-1">{lbl}</div>
            </div>
          ))}
        </div>
        <div className="bg-white border border-gray-100 rounded-xl p-4">
          <div className="text-sm font-light text-gray-400 mb-3">Weekly activity</div>
          <div className="flex items-end gap-1.5 h-16">
            {[30, 55, 70, 90, 65, 45, 20].map((h, i) => (
              <div
                key={i}
                className={`flex-1 rounded-t transition-all duration-500 hover:opacity-80 ${i === 3 ? "bg-purple-600" : "bg-purple-100"}`}
                style={{ height: `${h}%`, animationDelay: `${i * 80}ms` }}
              />
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}