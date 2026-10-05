
import { apiRequest } from "./client";

export interface AdminUser {
  id: number;
  fullName: string;
  email: string;
  mobileNumber: string | null;
  role: string;
  accountStatus: string;
}

export function getPendingUsers(): Promise<AdminUser[]> {
  return apiRequest<AdminUser[]>("/admin/users/pending");
}

export function approveUser(userId: number): Promise<AdminUser> {
  return apiRequest<AdminUser>(`/admin/users/${userId}/approve`, {
    method: "PATCH",
  });
}

export function blockUser(userId: number): Promise<AdminUser> {
  return apiRequest<AdminUser>(`/admin/users/${userId}/block`, {
    method: "PATCH",
  });
}

