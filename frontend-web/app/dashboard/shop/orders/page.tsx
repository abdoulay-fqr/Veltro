"use client";

import { useEffect, useState, useCallback } from "react";
import { toast } from "sonner";
import { shopApi, OrderResponse, OrderStatus } from "@/lib/api/shop";
import { X } from "lucide-react";

const STATUS_COLORS: Record<OrderStatus, string> = {
  PENDING:   "bg-yellow-100 text-yellow-700",
  CONFIRMED: "bg-blue-100 text-blue-700",
  SHIPPED:   "bg-purple-100 text-purple-700",
  DELIVERED: "bg-green-100 text-green-700",
  CANCELLED: "bg-red-100 text-red-700",
};

const TABS: Array<{ label: string; value: OrderStatus | "" }> = [
  { label: "All", value: "" },
  { label: "Pending", value: "PENDING" },
  { label: "Confirmed", value: "CONFIRMED" },
  { label: "Shipped", value: "SHIPPED" },
  { label: "Delivered", value: "DELIVERED" },
  { label: "Cancelled", value: "CANCELLED" },
];

export default function ShopOrdersPage() {
  const [orders, setOrders] = useState<OrderResponse[]>([]);
  const [tab, setTab] = useState<OrderStatus | "">("");
  const [detail, setDetail] = useState<OrderResponse | null>(null);
  const [loading, setLoading] = useState(true);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const res = await shopApi.admin.listOrders({ status: tab || undefined, size: 100 });
      setOrders(res.data.data?.content ?? []);
    } catch { toast.error("Failed to load orders"); }
    finally { setLoading(false); }
  }, [tab]);

  useEffect(() => { load(); }, [load]);

  const handleConfirm = async (id: number) => {
    try { await shopApi.admin.confirm(id); toast.success("Order confirmed"); load(); }
    catch { toast.error("Failed to confirm order"); }
  };

  const handleShip = async (id: number) => {
    try { await shopApi.admin.ship(id); toast.success("Order shipped"); load(); }
    catch { toast.error("Failed to ship order"); }
  };

  return (
    <div className="p-6 space-y-6">
      <div>
        <h1 className="text-xl font-semibold text-zinc-900">Orders</h1>
        <p className="text-sm text-zinc-500 mt-0.5">{orders.length} orders</p>
      </div>

      <div className="flex gap-1 border-b border-zinc-200 overflow-x-auto">
        {TABS.map(({ label, value }) => (
          <button key={value} onClick={() => setTab(value)}
            className={`px-4 py-2.5 text-sm font-medium whitespace-nowrap border-b-2 transition-colors ${
              tab === value ? "border-zinc-900 text-zinc-900" : "border-transparent text-zinc-500 hover:text-zinc-700"
            }`}>
            {label}
          </button>
        ))}
      </div>

      <div className="rounded-xl border border-zinc-200 bg-white overflow-hidden">
        {loading ? <div className="p-8 text-center text-sm text-zinc-400">Loading…</div> : (
          <table className="w-full text-sm">
            <thead className="bg-zinc-50 border-b border-zinc-200">
              <tr>
                {["Order #", "Member", "Items", "Total", "Status", "Date", ""].map((h) => (
                  <th key={h} className="px-4 py-3 text-left text-xs font-medium text-zinc-500 uppercase tracking-wide">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-zinc-100">
              {orders.length === 0 ? (
                <tr><td colSpan={7} className="px-4 py-8 text-center text-zinc-400">No orders found</td></tr>
              ) : orders.map((o) => (
                <tr key={o.id} className="hover:bg-zinc-50">
                  <td className="px-4 py-3 font-mono text-xs text-zinc-500">#{o.id}</td>
                  <td className="px-4 py-3">Member #{o.memberId}</td>
                  <td className="px-4 py-3 text-zinc-500 text-xs">{o.items.map(i => `${i.quantity}x ${i.productName}`).join(", ")}</td>
                  <td className="px-4 py-3 font-medium">${o.totalAmount}</td>
                  <td className="px-4 py-3">
                    <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${STATUS_COLORS[o.status]}`}>{o.status}</span>
                  </td>
                  <td className="px-4 py-3 text-xs text-zinc-500">{new Date(o.createdAt).toLocaleDateString()}</td>
                  <td className="px-4 py-3">
                    <div className="flex gap-2 justify-end text-xs">
                      <button onClick={() => setDetail(o)} className="text-zinc-500 hover:underline">Detail</button>
                      {o.status === "PENDING" && <button onClick={() => handleConfirm(o.id)} className="text-blue-600 hover:underline">Confirm</button>}
                      {o.status === "CONFIRMED" && <button onClick={() => handleShip(o.id)} className="text-purple-600 hover:underline">Ship</button>}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {detail && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm">
          <div className="w-full max-w-lg bg-white rounded-2xl shadow-xl p-6 space-y-4 max-h-[80vh] overflow-y-auto">
            <div className="flex items-center justify-between">
              <div>
                <h2 className="text-base font-semibold text-zinc-900">Order #{detail.id}</h2>
                <p className="text-xs text-zinc-500 mt-0.5">{detail.shippingAddress}</p>
              </div>
              <button onClick={() => setDetail(null)} className="text-zinc-400 hover:text-zinc-700"><X size={18} /></button>
            </div>
            <div className="space-y-2">
              {detail.items.map((item) => (
                <div key={item.id} className="flex items-center justify-between rounded-lg border border-zinc-100 px-4 py-3">
                  <div>
                    <p className="text-sm font-medium text-zinc-900">{item.productName}</p>
                    <p className="text-xs text-zinc-500">Qty: {item.quantity} × ${item.unitPrice}</p>
                  </div>
                  <span className="text-sm font-semibold">${item.lineTotal}</span>
                </div>
              ))}
            </div>
            <div className="flex items-center justify-between pt-2 border-t border-zinc-100">
              <span className="text-sm font-semibold text-zinc-900">Total</span>
              <span className="text-sm font-semibold">${detail.totalAmount}</span>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
