"use client";

export default function ContactUs() {
  const info = [
    { label: "Address", val: "12 Fitness Boulevard, Tlemcen 13000, Algeria", icon: <><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></> },
    { label: "Phone", val: "+213 43 123 456", icon: <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07A19.5 19.5 0 0 1 4.69 13.1a19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 3.6 2.5h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L7.91 10.09a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0 1 22 16.92z"/> },
    { label: "Email", val: "hello@veltro.com", icon: <><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></> },
    { label: "Hours", val: "Mon–Fri 6am–11pm · Sat–Sun 7am–9pm", icon: <><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></> },
  ];

  return (
    <section id="contact" className="bg-gray-50 py-20">
      <div className="max-w-6xl mx-auto px-8">
        <div className="text-xs font-medium text-purple-600 tracking-widest uppercase mb-3">Contact</div>
        <h2 className="text-4xl font-light mb-4">Get in touch</h2>
        <p className="text-gray-500 font-light max-w-lg leading-relaxed mb-12">Have a question or want to book a visit? We would love to hear from you.</p>
        <div className="grid grid-cols-2 gap-16">
          <div className="flex flex-col gap-6">
            {info.map((item, i) => (
              <div
                key={item.label}
                className="flex gap-4 items-start group"
                style={{ animationDelay: `${i * 80}ms` }}
              >
                <div className="w-10 h-10 bg-purple-100 rounded-xl flex items-center justify-center flex-shrink-0 group-hover:bg-purple-600 transition-colors duration-300">
                  <svg className="w-5 h-5 stroke-purple-600 group-hover:stroke-white fill-none transition-colors duration-300" strokeWidth={2} viewBox="0 0 24 24">{item.icon}</svg>
                </div>
                <div>
                  <div className="text-xs font-light text-gray-400 mb-1">{item.label}</div>
                  <div className="text-sm font-medium">{item.val}</div>
                </div>
              </div>
            ))}
          </div>
          <form className="flex flex-col gap-3" onSubmit={(e) => e.preventDefault()}>
            <div className="grid grid-cols-2 gap-3">
              <div className="flex flex-col gap-1.5"><label className="text-xs font-light text-gray-400">First name</label><input type="text" placeholder="John" className="px-3 py-2.5 border border-gray-200 rounded-lg text-sm font-light focus:outline-none focus:border-purple-400 transition-colors duration-200" /></div>
              <div className="flex flex-col gap-1.5"><label className="text-xs font-light text-gray-400">Last name</label><input type="text" placeholder="Doe" className="px-3 py-2.5 border border-gray-200 rounded-lg text-sm font-light focus:outline-none focus:border-purple-400 transition-colors duration-200" /></div>
            </div>
            <div className="flex flex-col gap-1.5"><label className="text-xs font-light text-gray-400">Email</label><input type="email" placeholder="john@example.com" className="px-3 py-2.5 border border-gray-200 rounded-lg text-sm font-light focus:outline-none focus:border-purple-400 transition-colors duration-200" /></div>
            <div className="flex flex-col gap-1.5"><label className="text-xs font-light text-gray-400">Subject</label><input type="text" placeholder="Membership enquiry" className="px-3 py-2.5 border border-gray-200 rounded-lg text-sm font-light focus:outline-none focus:border-purple-400 transition-colors duration-200" /></div>
            <div className="flex flex-col gap-1.5"><label className="text-xs font-light text-gray-400">Message</label><textarea placeholder="Tell us how we can help..." className="px-3 py-2.5 border border-gray-200 rounded-lg text-sm font-light resize-none h-28 focus:outline-none focus:border-purple-400 transition-colors duration-200" /></div>
            <button type="submit" className="bg-purple-600 hover:bg-purple-700 text-white py-3 rounded-lg text-sm font-light cursor-pointer transition-all duration-200 hover:scale-[1.02] active:scale-95">
              Send message
            </button>
          </form>
        </div>
      </div>
    </section>
  );
}