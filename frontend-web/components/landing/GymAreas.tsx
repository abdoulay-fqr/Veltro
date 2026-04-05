export default function GymAreas() {
  const areas = [
    { img: "https://images.unsplash.com/photo-1534438327276-14e5300c3a48?w=400&q=80", name: "Strength", desc: "Free weights, barbells, machines and power racks for all levels." },
    { img: "https://images.unsplash.com/photo-1576678927484-cc907957088c?w=400&q=80", name: "Cardio", desc: "Treadmills, bikes, rowers and ellipticals with heart rate tracking." },
    { img: "https://images.unsplash.com/photo-1588286840104-8957b019727f?w=400&q=80", name: "Yoga", desc: "Calm, dedicated studio space for yoga, pilates and stretching classes." },
    { img: "https://images.unsplash.com/photo-1575429198097-0414ec08e8cd?w=400&q=80", name: "Pool", desc: "Olympic lanes, water aerobics sessions and swimming lessons." },
  ];

  return (
    <section id="areas" className="bg-gray-50 py-20">
      <div className="max-w-6xl mx-auto px-8">
        <div className="text-xs font-medium text-purple-600 tracking-widest uppercase mb-3">Gym areas</div>
        <h2 className="text-4xl font-light mb-4">Explore our facilities</h2>
        <p className="text-gray-500 font-light max-w-lg leading-relaxed mb-12">World-class equipment and dedicated spaces designed to help you reach your goals.</p>
        <div className="grid grid-cols-4 gap-4">
          {areas.map((area, i) => (
            <div
              key={area.name}
              className="border border-gray-100 rounded-2xl overflow-hidden cursor-pointer bg-white group hover:shadow-lg hover:-translate-y-1 transition-all duration-300"
              style={{ animationDelay: `${i * 80}ms` }}
            >
              <div className="overflow-hidden">
                <img src={area.img} alt={area.name} className="w-full h-36 object-cover group-hover:scale-105 transition-transform duration-500" />
              </div>
              <div className="p-4">
                <div className="text-base font-medium mb-1">{area.name}</div>
                <div className="text-sm font-light text-gray-500">{area.desc}</div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}