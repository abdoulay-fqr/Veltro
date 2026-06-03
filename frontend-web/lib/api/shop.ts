import api from "@/lib/api";
import type { PageResponse } from "./members";

export type ProductCategory = "SUPPLEMENT" | "CLOTHING" | "EQUIPMENT";
export type OrderStatus = "PENDING" | "CONFIRMED" | "SHIPPED" | "DELIVERED" | "CANCELLED";

export interface ProductResponse {
  id: number;
  name: string;
  description: string | null;
  category: ProductCategory;
  price: number;
  stockQuantity: number;
  imageUrl: string | null;
  active: boolean;
  createdAt: string;
}

export interface OrderItemResponse {
  id: number;
  productId: number;
  productName: string;
  quantity: number;
  unitPrice: number;
  lineTotal: number;
}

export interface OrderResponse {
  id: number;
  memberId: number;
  status: OrderStatus;
  totalAmount: number;
  shippingAddress: string;
  items: OrderItemResponse[];
  createdAt: string;
  updatedAt: string;
}

export const shopApi = {
  products: {
    list(params?: { category?: ProductCategory; page?: number; size?: number }) {
      return api.get<{ data: PageResponse<ProductResponse> }>("/shop/products", { params });
    },
    getById(id: number) {
      return api.get<{ data: ProductResponse }>(`/shop/products/${id}`);
    },
    create(body: Partial<ProductResponse>) {
      return api.post<{ data: ProductResponse }>("/shop/products", body);
    },
    update(id: number, body: Partial<ProductResponse>) {
      return api.put<{ data: ProductResponse }>(`/shop/products/${id}`, body);
    },
    delete(id: number) {
      return api.delete(`/shop/products/${id}`);
    },
  },

  orders: {
    place(items: Array<{ productId: number; quantity: number }>, shippingAddress: string) {
      return api.post<{ data: OrderResponse }>("/shop/orders", { items, shippingAddress });
    },
    myOrders(page = 0) {
      return api.get<{ data: PageResponse<OrderResponse> }>("/shop/orders", { params: { page } });
    },
    getById(id: number) {
      return api.get<{ data: OrderResponse }>(`/shop/orders/${id}`);
    },
    cancel(id: number) {
      return api.put<{ data: OrderResponse }>(`/shop/orders/${id}/cancel`);
    },
  },

  admin: {
    listOrders(params?: { status?: OrderStatus; page?: number }) {
      return api.get<{ data: PageResponse<OrderResponse> }>("/shop/admin/orders", { params });
    },
    confirm(id: number) {
      return api.put<{ data: OrderResponse }>(`/shop/admin/orders/${id}/confirm`);
    },
    ship(id: number) {
      return api.put<{ data: OrderResponse }>(`/shop/admin/orders/${id}/ship`);
    },
    revenue() {
      return api.get<{ data: { totalRevenue: number } }>("/shop/admin/revenue");
    },
  },
};
