
import { apiRequest } from "./client";

export interface CreateOrderRequest {
  assetId: number;
}

export interface Order {
  id: number;
  userId: number;
  assetId: number;
  assetName: string;
  status: string;
  requestedAt: string;
  approvedAt: string | null;
  dueAt: string | null;
  returnedAt: string | null;
  fineAmount: number;
}

export function createOrder(
  request: CreateOrderRequest
): Promise<Order> {
  return apiRequest<Order>("/orders", {
    method: "POST",
    body: JSON.stringify(request),
  });
}

export function getMyOrders(): Promise<Order[]> {
  return apiRequest<Order[]>("/orders/my");
}

export function returnOrder(orderId: number): Promise<Order> {
  return apiRequest<Order>(`/orders/${orderId}/return`, {
    method: "PATCH",
  });
}

