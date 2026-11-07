
import { apiRequest } from "./client";

export interface ResourceCategory {
  id: number;
  category: string;
  subCategory: string;
}

export interface CreateCategoryRequest {
  category: string;
  subCategory: string;
}

export function getCategories(): Promise<ResourceCategory[]> {
  return apiRequest<ResourceCategory[]>("/resource-categories");
}

export function createCategory(
  request: CreateCategoryRequest
): Promise<ResourceCategory> {
  return apiRequest<ResourceCategory>("/resource-categories", {
    method: "POST",
    body: JSON.stringify(request),
  });
}
