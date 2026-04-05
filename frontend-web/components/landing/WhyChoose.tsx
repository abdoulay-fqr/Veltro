export default function WhyChoose() {
  const items = [
    {
      title: "All-in-one management",
      desc: "Manage members, subscriptions, bookings, and coaches from a single web dashboard. No juggling between apps.",
      icon: <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5" />,
    },
    {
      title: "Real-time activity tracking",
      desc: "NFC-based entry, machine session logging, and performance dashboards. Know exactly how your members train.",
      icon: <><circle cx="12" cy="12" r="10" /><polyline points="12 6 12 12 16 14" /></>,
    },
    {
      title: "AI-powered assistant",
      desc: "Members get instant answers about their subscription, bookings, and activity from Veltro's Gemini-powered chatbot.",
      icon: <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />,
    },
  ];

  return (
    <section id="why" className="max-w-6xl mx-auto px-8 py-20">
      <div className="text-xs font-medium text-purple-600 tracking-widest uppercase mb-3">Why choose us</div>
      <h2 className="text-4xl font-light mb-4">Everything your gym needs,<br />in one place</h2>
      <p className="text-gray-500 font-light max-w-lg leading-relaxed mb-12">From NFC entry tracking to AI-powered coaching assistance — Veltro gives you the tools to run a modern, connected gym.</p>
      <div className="grid grid-cols-3 gap-6">
        {items.map((item, i) => (
          <div
            key={item.title}
            className="border border-gray-100 rounded-2xl p-6 hover:border-purple-200 hover:shadow-md transition-all duration-300 group"
            style={{ animationDelay: `${i * 100}ms` }}
          >
            <div className="w-10 h-10 bg-purple-100 rounded-xl flex items-center justify-center mb-4 group-hover:bg-purple-600 transition-colors duration-300">
              <svg className="w-5 h-5 stroke-purple-600 group-hover:stroke-white fill-none transition-colors duration-300" strokeWidth={2} viewBox="0 0 24 24">{item.icon}</svg>
            </div>
            <div className="text-base font-medium mb-2">{item.title}</div>
            <div className="text-sm font-light text-gray-500 leading-relaxed">{item.desc}</div>
          </div>
        ))}
      </div>
    </section>
  );
}