
import { apiRequest } from "./client";

export interface CurrentUser {
  id: number;
  fullName: string;
  email: string;
  mobileNumber: string | null;
  role: string;
  accountStatus: string;
}

// Get the currently authenticated user's profile
export function getCurrentUser(): Promise<CurrentUser> {
  return apiRequest<CurrentUser>("/users/me");
}
