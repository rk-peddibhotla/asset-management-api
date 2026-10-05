
import { apiRequest } from "./client";
import type { Order } from "./orders";

export function getPendingOrders(): Promise<Order[]> {
  return apiRequest<Order[]>("/admin/orders/pending");
}

export function approveOrder(orderId: number): Promise<Order> {
  return apiRequest<Order>(`/admin/orders/${orderId}/approve`, {
    method: "PATCH",
  });
}

export function rejectOrder(orderId: number): Promise<Order> {
  return apiRequest<Order>(`/admin/orders/${orderId}/reject`, {
    method: "PATCH",
  });
}
