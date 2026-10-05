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

export function getAssets(): Promise<Asset[]> {
  return apiRequest<Asset[]>("/assets");
}

