export default function Reviews() {
  const reviews = [
    { img: "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=100&q=80", stars: 5, text: "The app makes booking classes so easy. I love being able to see my progress and chat with my coach directly through the platform.", name: "Sarah B.", role: "Pro member since 2024" },
    { img: "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100&q=80", stars: 5, text: "The AI chatbot is surprisingly helpful. I asked about my subscription and it knew exactly what sessions I had coming up. Very impressive.", name: "Kevin M.", role: "Elite member since 2023" },
    { img: "https://images.unsplash.com/photo-1531746020798-e6953c6e8e04?w=100&q=80", stars: 4, text: "The NFC entry system is seamless and I love seeing my weekly activity streaks. Keeps me motivated to come in consistently.", name: "Amina L.", role: "Basic member since 2024" },
  ];

  return (
    <section id="reviews" className="max-w-6xl mx-auto px-8 py-20">
      <div className="text-xs font-medium text-purple-600 tracking-widest uppercase mb-3">Testimonials</div>
      <h2 className="text-4xl font-light mb-4">What our members say</h2>
      <p className="text-gray-500 font-light max-w-lg leading-relaxed mb-12">Real feedback from real members who transformed their fitness journey with Veltro.</p>
      <div className="grid grid-cols-3 gap-6">
        {reviews.map((r, i) => (
          <div
            key={r.name}
            className="border border-gray-100 rounded-2xl p-6 hover:border-purple-200 hover:shadow-md hover:-translate-y-1 transition-all duration-300"
            style={{ animationDelay: `${i * 100}ms` }}
          >
            <div className="text-purple-600 text-sm mb-3">{"★".repeat(r.stars)}{"☆".repeat(5 - r.stars)}</div>
            <p className="text-sm font-light text-gray-500 leading-relaxed mb-4">&ldquo;{r.text}&rdquo;</p>
            <div className="flex items-center gap-3">
              <img src={r.img} alt={r.name} className="w-9 h-9 rounded-full object-cover ring-2 ring-purple-100" />
              <div>
                <div className="text-sm font-medium">{r.name}</div>
                <div className="text-xs font-light text-gray-400">{r.role}</div>
              </div>
            </div>
          </div>
        ))}
      </div>
    </section>
  );
}