
import { apiRequest } from "./client";

export interface CreateOrderRequest {
  assetId: number;
}

export interface Order {
  id: number;
  userId: number;
  userName: string;
  userEmail: string;
  assetId: number;
  assetName: string;
  status: string;
  requestedAt: string;
  approvedAt: string | null;
  dueAt: string | null;
  returnedAt: string | null;
  fineAmount: number;
}

// Student: Request an asset
export function createOrder(
  request: CreateOrderRequest
): Promise<Order> {
  return apiRequest<Order>("/orders", {
    method: "POST",
    body: JSON.stringify(request),
  });
}

// Student: Get personal borrowing history
export function getMyOrders(): Promise<Order[]> {
  return apiRequest<Order[]>("/orders/my");
}

// Student: Return a borrowed asset
export function returnOrder(orderId: number): Promise<Order> {
  return apiRequest<Order>(`/orders/${orderId}/return`, {
    method: "PATCH",
  });
}

// Administrator: Get pending borrowing requests
export function getPendingOrders(): Promise<Order[]> {
  return apiRequest<Order[]>("/admin/orders/pending");
}

// Administrator: Approve a borrowing request
export function approveOrder(orderId: number): Promise<Order> {
  return apiRequest<Order>(
    `/admin/orders/${orderId}/approve`,
    {
      method: "PATCH",
    }
  );
}

// Administrator: Reject a borrowing request
export function rejectOrder(orderId: number): Promise<Order> {
  return apiRequest<Order>(
    `/admin/orders/${orderId}/reject`,
    {
      method: "PATCH",
    }
  );
}

// Administrator: Get complete borrowing history
export function getAllOrders(): Promise<Order[]> {
  return apiRequest<Order[]>("/admin/orders");
}
