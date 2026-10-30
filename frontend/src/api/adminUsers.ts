
import { apiRequest } from "./client";

export interface AdminUser {
  id: number;
  fullName: string;
  email: string;
  mobileNumber: string | null;
  role: string;
  accountStatus: string;
}

// Administrator: Get all registered users
export function getAllUsers(): Promise<AdminUser[]> {
  return apiRequest<AdminUser[]>("/admin/users");
}

// Administrator: Get users awaiting approval
export function getPendingUsers(): Promise<AdminUser[]> {
  return apiRequest<AdminUser[]>("/admin/users/pending");
}

// Administrator: Approve a user
export function approveUser(userId: number): Promise<AdminUser> {
  return apiRequest<AdminUser>(`/admin/users/${userId}/approve`, {
    method: "PATCH",
  });
}

// Administrator: Block a user
export function blockUser(userId: number): Promise<AdminUser> {
  return apiRequest<AdminUser>(`/admin/users/${userId}/block`, {
    method: "PATCH",
  });
}
