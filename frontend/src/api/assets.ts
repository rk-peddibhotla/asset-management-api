
import { apiRequest } from "./client";

export interface ResourceCategory {
  id: number;
  category: string;
  subCategory: string;
}

export interface Resource {
  id: number;
  name: string;
  manufacturer: string | null;
  purchasePrice: number | null;
  description: string | null;
  status: "AVAILABLE" | "UNAVAILABLE";
  categoryId: number;
  category: string;
  subCategory: string;
}

export interface CreateResourceRequest {
  name: string;
  manufacturer: string;
  purchasePrice: number;
  description: string;
  categoryId: number;
}

export function getResources(): Promise<Resource[]> {
  return apiRequest<Resource[]>("/resources");
}

export function createResource(
  request: CreateResourceRequest
): Promise<Resource> {
  return apiRequest<Resource>("/resources", {
    method: "POST",
    body: JSON.stringify(request),
  });
}
