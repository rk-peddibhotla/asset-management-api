
import { useEffect, useState, type FormEvent } from "react";
import {
  createAsset,
  getAssets,
  type Asset,
} from "../api/assets";
import {
  createCategory,
  getCategories,
  type AssetCategory,
} from "../api/categories";

function ManageAssetsPage() {
  const [categories, setCategories] = useState<AssetCategory[]>([]);
  const [assets, setAssets] = useState<Asset[]>([]);
  const [loading, setLoading] = useState(true);

  const [category, setCategory] = useState("");
  const [subCategory, setSubCategory] = useState("");

  const [name, setName] = useState("");
  const [manufacturer, setManufacturer] = useState("");
  const [price, setPrice] = useState("");
  const [categoryId, setCategoryId] = useState("");

  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  useEffect(() => {
    async function loadData() {
      setError("");

      try {
        const [categoryResponse, assetResponse] = await Promise.all([
          getCategories(),
          getAssets(),
        ]);

        setCategories(categoryResponse);
        setAssets(assetResponse);
      } catch (error) {
        setError(
          error instanceof Error
            ? error.message
            : "Failed to load management data"
        );
      } finally {
        setLoading(false);
      }
    }

    void loadData();
  }, []);

  async function handleCreateCategory(
    event: FormEvent<HTMLFormElement>
  ) {
    event.preventDefault();
    setError("");
    setSuccess("");

    try {
      const newCategory = await createCategory({
        category: category.trim(),
        subCategory: subCategory.trim(),
      });

      setCategories((current) => [...current, newCategory]);
      setCategoryId(String(newCategory.id));
      setCategory("");
      setSubCategory("");
      setSuccess("Category created successfully.");
    } catch (error) {
      setError(
        error instanceof Error
          ? error.message
          : "Failed to create category"
      );
    }
  }

  async function handleCreateAsset(
    event: FormEvent<HTMLFormElement>
  ) {
    event.preventDefault();
    setError("");
    setSuccess("");

    if (!categoryId) {
      setError("Please select a category.");
      return;
    }

    try {
      const newAsset = await createAsset({
        name: name.trim(),
        manufacturer: manufacturer.trim(),
        price: Number(price),
        categoryId: Number(categoryId),
      });

      setAssets((current) => [...current, newAsset]);

      setName("");
      setManufacturer("");
      setPrice("");

      setSuccess("Asset created successfully.");
    } catch (error) {
      setError(
        error instanceof Error
          ? error.message
          : "Failed to create asset"
      );
    }
  }

  if (loading) {
    return <p>Loading asset management...</p>;
  }

  return (
    <section>
      <h2 className="page-title">Manage Assets</h2>

      {error && (
        <p className="error-message" role="alert">
          {error}
        </p>
      )}

      {success && <p role="status">{success}</p>}

      <section>
        <h3>Create Category</h3>

        <form onSubmit={handleCreateCategory}>
          <div>
            <label htmlFor="category">Category</label>
            <input
              id="category"
              value={category}
              onChange={(event) => setCategory(event.target.value)}
              placeholder="e.g. Laptop"
              maxLength={100}
              required
            />
          </div>

          <div>
            <label htmlFor="subCategory">Subcategory</label>
            <input
              id="subCategory"
              value={subCategory}
              onChange={(event) => setSubCategory(event.target.value)}
              placeholder="e.g. Business Laptop"
              maxLength={100}
              required
            />
          </div>

          <button className="primary-button" type="submit">
            Create Category
          </button>
        </form>
      </section>

      <section>
        <h3>Create Asset</h3>

        <form onSubmit={handleCreateAsset}>
          <div>
            <label htmlFor="assetName">Asset Name</label>
            <input
              id="assetName"
              value={name}
              onChange={(event) => setName(event.target.value)}
              placeholder="e.g. ThinkPad T14"
              maxLength={200}
              required
            />
          </div>

          <div>
            <label htmlFor="manufacturer">Manufacturer</label>
            <input
              id="manufacturer"
              value={manufacturer}
              onChange={(event) => setManufacturer(event.target.value)}
              placeholder="e.g. Lenovo"
              maxLength={100}
            />
          </div>

          <div>
            <label htmlFor="price">Price (₹)</label>
            <input
              id="price"
              type="number"
              min="0"
              step="0.01"
              value={price}
              onChange={(event) => setPrice(event.target.value)}
              required
            />
          </div>

          <div>
            <label htmlFor="assetCategory">Category</label>
            <select
              id="assetCategory"
              value={categoryId}
              onChange={(event) => setCategoryId(event.target.value)}
              required
            >
              <option value="">Select a category</option>

              {categories.map((item) => (
                <option key={item.id} value={item.id}>
                  {item.category} / {item.subCategory}
                </option>
              ))}
            </select>

            {categories.length === 0 && (
              <p>Create a category before creating an asset.</p>
            )}
          </div>

          <button
            className="primary-button"
            type="submit"
            disabled={categories.length === 0}
          >
            Create Asset
          </button>
        </form>
      </section>

      <section>
        <h3>Existing Assets ({assets.length})</h3>

        {assets.length === 0 ? (
          <p>No assets found.</p>
        ) : (
          <ul className="item-list">
            {assets.map((asset) => (
              <li className="item" key={asset.id}>
                <h4 className="item-title">{asset.name}</h4>

                <p className="item-detail">
                  Manufacturer: {asset.manufacturer ?? "N/A"}
                </p>

                <p className="item-detail">
                  Price: ₹{asset.price.toFixed(2)}
                </p>

                <p className="item-detail">
                  Category: {asset.category.category} /{" "}
                  {asset.category.subCategory}
                </p>
              </li>
            ))}
          </ul>
        )}
      </section>
    </section>
  );
}

export default ManageAssetsPage;
