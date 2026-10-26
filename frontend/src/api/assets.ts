import { apiRequest } from "./client";

export interface AssetCategory {
  id: number;
  category: string;
  subCategory: string;
}

export interface Asset {
  id: number;
  name: string;
  manufacturer: string | null;
  price: number;
  category: AssetCategory;
}

export interface CreateAssetRequest {
  name: string;
  manufacturer: string;
  price: number;
  categoryId: number;
}

export function getAssets(): Promise<Asset[]> {
  return apiRequest<Asset[]>("/assets");
}

export function createAsset(
  request: CreateAssetRequest
): Promise<Asset> {
  return apiRequest<Asset>("/assets", {
    method: "POST",
    body: JSON.stringify(request),
  });
}