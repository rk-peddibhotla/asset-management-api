import { apiRequest } from "./client";

export interface AssetCategory {
  id: number;
  category: string;
  subCategory: string;
}

export interface CreateCategoryRequest {
  category: string;
  subCategory: string;
}

export function getCategories(): Promise<AssetCategory[]> {
  return apiRequest<AssetCategory[]>("/categories");
}

export function createCategory(
  request: CreateCategoryRequest
): Promise<AssetCategory> {
  return apiRequest<AssetCategory>("/categories", {
    method: "POST",
    body: JSON.stringify(request),
  });
}