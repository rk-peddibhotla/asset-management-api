import { apiRequest } from "./client";

export interface LoginRequest {
email: string;
password: string;
}

export interface LoginResponse {
token: string;
tokenType: string;
userId: number;
email: string;
role: string;
}

export interface RegisterRequest {
fullName: string;
email: string;
password: string;
mobileNumber: string;
}

export interface RegisterResponse {
id: number;
fullName: string;
email: string;
mobileNumber: string | null;
role: string;
accountStatus: string;
}

export function login(
request: LoginRequest
): Promise<LoginResponse> {
return apiRequest<LoginResponse>("/auth/login", {
method: "POST",
body: JSON.stringify(request),
});
}

export function register(
request: RegisterRequest
): Promise<RegisterResponse> {
return apiRequest<RegisterResponse>("/auth/register", {
method: "POST",
body: JSON.stringify(request),
});
}
