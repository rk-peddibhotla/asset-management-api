
import { apiRequest } from "./client";

export interface CreateBorrowingRequest {
  resourceId: number;
}

export interface Borrowing {
  id: number;
  userId: number;
  userName: string;
  userEmail: string;
  resourceId: number;
  resourceName: string;
  status: string;
  requestedAt: string;
  activatedAt: string | null;
  dueAt: string | null;
  returnedAt: string | null;
  lateFeeAmount: number;
}

export function createBorrowing(
  request: CreateBorrowingRequest
): Promise<Borrowing> {
  return apiRequest<Borrowing>("/borrowings", {
    method: "POST",
    body: JSON.stringify(request),
  });
}

export function getMyBorrowings(): Promise<Borrowing[]> {
  return apiRequest<Borrowing[]>("/borrowings/my");
}

export function returnBorrowing(
  borrowingId: number
): Promise<Borrowing> {
  return apiRequest<Borrowing>(`/borrowings/${borrowingId}/return`, {
    method: "PATCH",
  });
}

export function getPendingBorrowings(): Promise<Borrowing[]> {
  return apiRequest<Borrowing[]>("/admin/borrowings/pending");
}

export function approveBorrowing(
  borrowingId: number
): Promise<Borrowing> {
  return apiRequest<Borrowing>(
    `/admin/borrowings/${borrowingId}/approve`,
    { method: "PATCH" }
  );
}

export function rejectBorrowing(
  borrowingId: number
): Promise<Borrowing> {
  return apiRequest<Borrowing>(
    `/admin/borrowings/${borrowingId}/reject`,
    { method: "PATCH" }
  );
}

export function getAllBorrowings(): Promise<Borrowing[]> {
  return apiRequest<Borrowing[]>("/admin/borrowings");
}
