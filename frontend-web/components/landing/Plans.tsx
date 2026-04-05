"use client";
import { useState } from "react";

export default function Plans() {
  const [annual, setAnnual] = useState(false);

  const plans = [
    {
      name: "Basic", monthly: 19, annual: 15, featured: false,
      desc: "Perfect for casual gym-goers who want access to the essentials.",
      features: ["Gym access (6am–10pm)", "2 classes/month", "Activity tracking", "Mobile app access"],
    },
    {
      name: "Pro", monthly: 39, annual: 31, featured: true,
      desc: "For dedicated members who want the full Veltro experience.",
      features: ["Unlimited gym access", "Unlimited classes", "Coach messaging", "AI chatbot assistant", "Performance dashboard"],
    },
    {
      name: "Elite", monthly: 69, annual: 55, featured: false,
      desc: "The ultimate package with personal coaching and premium perks.",
      features: ["Everything in Pro", "2x personal training/month", "Nutrition consultation", "Priority class booking", "Shop discounts"],
    },
  ];

  return (
    <section id="plans" className="max-w-6xl mx-auto px-8 py-20">
      <div className="text-xs font-medium text-purple-600 tracking-widest uppercase mb-3">Pricing</div>
      <h2 className="text-4xl font-light mb-4">Choose your plan</h2>
      <p className="text-gray-500 font-light max-w-lg leading-relaxed mb-6">Flexible plans for every lifestyle. Cancel or upgrade anytime.</p>
      <div className="flex items-center gap-3 mb-8">
        <span className="text-sm font-light text-gray-500">Monthly</span>
        <button
          onClick={() => setAnnual(!annual)}
          className={`relative w-11 h-6 rounded-full border transition-colors duration-300 cursor-pointer ${annual ? "bg-purple-600 border-purple-600" : "bg-gray-100 border-gray-200"}`}
        >
          <div className={`absolute top-0.5 w-5 h-5 bg-white rounded-full transition-all duration-300 ${annual ? "left-5" : "left-0.5"}`} />
        </button>
        <span className="text-sm font-light text-gray-500">Annual</span>
        <span className="bg-purple-100 text-purple-700 text-xs px-3 py-1 rounded-full">Save 20%</span>
      </div>
      <div className="grid grid-cols-3 gap-6">
        {plans.map((plan, i) => (
          <div
            key={plan.name}
            className={`rounded-2xl p-6 relative transition-all duration-300 hover:shadow-lg hover:-translate-y-1 ${plan.featured ? "border-2 border-purple-600" : "border border-gray-100"}`}
            style={{ animationDelay: `${i * 100}ms` }}
          >
            {plan.featured && (
              <div className="absolute -top-3 left-1/2 -translate-x-1/2 bg-purple-600 text-white text-xs px-4 py-1 rounded-full whitespace-nowrap">Most popular</div>
            )}
            <div className="text-sm font-light text-gray-500 mb-2">{plan.name}</div>
            <div className="text-4xl font-medium mb-1 transition-all duration-300">
              {annual ? plan.annual : plan.monthly}
              <span className="text-base font-light text-gray-400">{annual ? "/mo, billed annually" : "/mo"}</span>
            </div>
            <div className="text-sm font-light text-gray-500 mb-6 leading-relaxed">{plan.desc}</div>
            <ul className="mb-6 space-y-2">
              {plan.features.map((f) => (
                <li key={f} className="flex items-center gap-2 text-sm font-light">
                  <div className="w-4 h-4 rounded-full bg-purple-100 flex items-center justify-center flex-shrink-0">
                    <svg className="w-2.5 h-2.5 stroke-purple-600 fill-none" strokeWidth={2.5} viewBox="0 0 12 12"><polyline points="2,6 5,9 10,3" /></svg>
                  </div>
                  {f}
                </li>
              ))}
            </ul>
            <button className={`w-full py-2.5 rounded-lg text-sm cursor-pointer transition-all duration-200 hover:scale-[1.02] active:scale-95 ${plan.featured ? "bg-purple-600 text-white hover:bg-purple-700" : "border border-gray-200 hover:bg-gray-50"}`}>
              Get started
            </button>
          </div>
        ))}
      </div>
    </section>
  );
}