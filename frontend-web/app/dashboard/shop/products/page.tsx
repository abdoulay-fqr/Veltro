"use client";

import { useEffect, useState, useCallback } from "react";
import { toast } from "sonner";
import { shopApi, ProductResponse, ProductCategory } from "@/lib/api/shop";
import { PlusCircle, X } from "lucide-react";
import { useForm } from "react-hook-form";

const CATEGORY_LABELS: Record<ProductCategory, string> = {
  SUPPLEMENT: "Supplement",
  CLOTHING: "Clothing",
  EQUIPMENT: "Equipment",
};

const CATEGORY_COLORS: Record<ProductCategory, string> = {
  SUPPLEMENT: "bg-green-100 text-green-700",
  CLOTHING:   "bg-blue-100 text-blue-700",
  EQUIPMENT:  "bg-amber-100 text-amber-700",
};

interface ProductFormValues {
  name: string;
  description: string;
  category: ProductCategory;
  price: number;
  stockQuantity: number;
  imageUrl: string;
}

function ProductModal({ product, onClose, onSaved }: {
  product?: ProductResponse;
  onClose: () => void;
  onSaved: () => void;
}) {
  const { register, handleSubmit, formState: { isSubmitting } } = useForm<ProductFormValues>({
    defaultValues: product ? {
      name: product.name,
      description: product.description ?? "",
      category: product.category,
      price: product.price,
      stockQuantity: product.stockQuantity,
      imageUrl: product.imageUrl ?? "",
    } : undefined,
  });

  const onSubmit = async (values: ProductFormValues) => {
    try {
      if (product) {
        await shopApi.products.update(product.id, values);
        toast.success("Product updated");
      } else {
        await shopApi.products.create(values);
        toast.success("Product created");
      }
      onSaved();
    } catch { toast.error("Failed to save product"); }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm">
      <div className="w-full max-w-md bg-white rounded-2xl shadow-xl p-6 max-h-[90vh] overflow-y-auto">
        <div className="flex items-center justify-between mb-5">
          <h2 className="text-base font-semibold text-zinc-900">{product ? "Edit Product" : "Add Product"}</h2>
          <button onClick={onClose} className="text-zinc-400 hover:text-zinc-700"><X size={18} /></button>
        </div>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          {[
            { name: "name" as const, label: "Name", type: "text" },
            { name: "description" as const, label: "Description", type: "text" },
            { name: "price" as const, label: "Price ($)", type: "number" },
            { name: "stockQuantity" as const, label: "Stock Quantity", type: "number" },
            { name: "imageUrl" as const, label: "Image URL", type: "text" },
          ].map(({ name, label, type }) => (
            <div key={name}>
              <label className="block text-xs font-medium text-zinc-600 mb-1">{label}</label>
              <input type={type} {...register(name)} step={type === "number" ? "0.01" : undefined}
                className="w-full rounded-lg border border-zinc-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-zinc-900" />
            </div>
          ))}
          <div>
            <label className="block text-xs font-medium text-zinc-600 mb-1">Category</label>
            <select {...register("category")} className="w-full rounded-lg border border-zinc-200 px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-zinc-900">
              {Object.keys(CATEGORY_LABELS).map((c) => (
                <option key={c} value={c}>{CATEGORY_LABELS[c as ProductCategory]}</option>
              ))}
            </select>
          </div>
          <div className="flex gap-3 pt-2">
            <button type="button" onClick={onClose} className="flex-1 rounded-lg border border-zinc-200 py-2 text-sm font-medium text-zinc-600 hover:bg-zinc-50">Cancel</button>
            <button type="submit" disabled={isSubmitting} className="flex-1 rounded-lg bg-zinc-900 py-2 text-sm font-medium text-white hover:bg-zinc-700 disabled:opacity-50">
              {isSubmitting ? "Saving…" : product ? "Update" : "Create"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default function ShopProductsPage() {
  const [products, setProducts] = useState<ProductResponse[]>([]);
  const [showModal, setShowModal] = useState(false);
  const [editProduct, setEditProduct] = useState<ProductResponse | undefined>(undefined);
  const [loading, setLoading] = useState(true);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const res = await shopApi.products.list({ size: 100 });
      setProducts(res.data.data?.content ?? []);
    } catch { toast.error("Failed to load products"); }
    finally { setLoading(false); }
  }, []);

  useEffect(() => { load(); }, [load]);

  const handleDelete = async (id: number) => {
    if (!confirm("Deactivate this product?")) return;
    try { await shopApi.products.delete(id); toast.success("Product deactivated"); load(); }
    catch { toast.error("Failed to deactivate product"); }
  };

  return (
    <div className="p-6 space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-semibold text-zinc-900">Products</h1>
          <p className="text-sm text-zinc-500 mt-0.5">{products.length} items</p>
        </div>
        <button onClick={() => { setEditProduct(undefined); setShowModal(true); }}
          className="flex items-center gap-2 rounded-lg bg-zinc-900 px-4 py-2 text-sm font-medium text-white hover:bg-zinc-700">
          <PlusCircle size={15} /> Add Product
        </button>
      </div>

      <div className="rounded-xl border border-zinc-200 bg-white overflow-hidden">
        {loading ? <div className="p-8 text-center text-sm text-zinc-400">Loading…</div> : (
          <table className="w-full text-sm">
            <thead className="bg-zinc-50 border-b border-zinc-200">
              <tr>
                {["Image", "Name", "Category", "Price", "Stock", "Active", ""].map((h) => (
                  <th key={h} className="px-4 py-3 text-left text-xs font-medium text-zinc-500 uppercase tracking-wide">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-zinc-100">
              {products.map((p) => (
                <tr key={p.id} className="hover:bg-zinc-50">
                  <td className="px-4 py-3">
                    {p.imageUrl
                      ? <img src={p.imageUrl} alt={p.name} className="w-10 h-10 rounded-lg object-cover border border-zinc-100" />
                      : <div className="w-10 h-10 rounded-lg bg-zinc-100 flex items-center justify-center text-zinc-400 text-xs">IMG</div>
                    }
                  </td>
                  <td className="px-4 py-3 font-medium text-zinc-900">{p.name}</td>
                  <td className="px-4 py-3">
                    <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${CATEGORY_COLORS[p.category]}`}>
                      {CATEGORY_LABELS[p.category]}
                    </span>
                  </td>
                  <td className="px-4 py-3">${p.price}</td>
                  <td className="px-4 py-3">
                    <span className={p.stockQuantity < 5 ? "text-red-600 font-medium" : ""}>{p.stockQuantity}</span>
                  </td>
                  <td className="px-4 py-3">
                    <span className={`text-xs ${p.active ? "text-green-600" : "text-zinc-400"}`}>{p.active ? "Active" : "Inactive"}</span>
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex gap-2 justify-end text-xs">
                      <button onClick={() => { setEditProduct(p); setShowModal(true); }} className="text-zinc-500 hover:underline">Edit</button>
                      {p.active && <button onClick={() => handleDelete(p.id)} className="text-red-600 hover:underline">Deactivate</button>}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {showModal && (
        <ProductModal
          product={editProduct}
          onClose={() => setShowModal(false)}
          onSaved={() => { setShowModal(false); load(); }}
        />
      )}
    </div>
  );
}
