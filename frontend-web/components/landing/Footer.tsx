export default function Footer() {
  return (
    <footer className="bg-gray-50 border-t border-gray-100 px-8 pt-12 pb-6">
      <div className="max-w-6xl mx-auto">
        <div className="grid grid-cols-4 gap-8 mb-10">
          <div>
            <div className="text-xl font-bold text-purple-600 uppercase tracking-tight mb-3">Veltro</div>
            <p className="text-sm font-light text-gray-500 leading-relaxed">The smarter way to run a modern gym. Everything your team and members need, in one place.</p>
          </div>
          {[
            ["Platform", ["Dashboard", "Mobile app", "Coaching tools", "Analytics"]],
            ["Company", ["About", "Careers", "Blog", "Press"]],
            ["Support", ["Help center", "Contact", "Privacy policy", "Terms of use"]],
          ].map(([title, links]) => (
            <div key={title as string}>
              <h4 className="text-sm font-medium mb-3">{title as string}</h4>
              <ul className="flex flex-col gap-2">
                {(links as string[]).map((l) => (
                  <li key={l} className="text-sm font-light text-gray-500 hover:text-gray-800 cursor-pointer">{l}</li>
                ))}
              </ul>
            </div>
          ))}
        </div>
        <div className="border-t border-gray-100 pt-6 flex justify-between items-center">
          <span className="text-xs font-light text-gray-400">© 2026 Veltro. All rights reserved.</span>
          <div className="flex gap-6">
            {["Privacy", "Terms", "Cookies"].map((l) => (
              <a key={l} href="#" className="text-xs font-light text-gray-400 hover:text-gray-600 no-underline">{l}</a>
            ))}
          </div>
        </div>
      </div>
    </footer>
  );
}