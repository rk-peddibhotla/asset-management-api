
import { useEffect, useState, type FormEvent } from "react";

import { createAsset } from "../api/assets";

import {
  createCategory,
  getCategories,
  type AssetCategory,
} from "../api/categories";

function ManageAssetsPage() {
  const [categories, setCategories] = useState<AssetCategory[]>([]);
  const [loading, setLoading] = useState(true);

  const [category, setCategory] = useState("");
  const [subCategory, setSubCategory] = useState("");

  const [name, setName] = useState("");
  const [manufacturer, setManufacturer] = useState("");
  const [price, setPrice] = useState("");
  const [categoryId, setCategoryId] = useState("");

  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const [creatingCategory, setCreatingCategory] = useState(false);
  const [creatingAsset, setCreatingAsset] = useState(false);

  useEffect(() => {
    async function loadCategories() {
      try {
        const response = await getCategories();
        setCategories(response);
      } catch (error) {
        setError(
          error instanceof Error
            ? error.message
            : "Failed to load categories."
        );
      } finally {
        setLoading(false);
      }
    }

    void loadCategories();
  }, []);

  async function handleCreateCategory(
    event: FormEvent<HTMLFormElement>
  ) {
    event.preventDefault();

    setError("");
    setSuccess("");
    setCreatingCategory(true);

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
          : "Failed to create category."
      );
    } finally {
      setCreatingCategory(false);
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

    setCreatingAsset(true);

    try {
      await createAsset({
        name: name.trim(),
        manufacturer: manufacturer.trim(),
        price: Number(price),
        categoryId: Number(categoryId),
      });

      setName("");
      setManufacturer("");
      setPrice("");

      setSuccess("Asset created successfully.");
    } catch (error) {
      setError(
        error instanceof Error
          ? error.message
          : "Failed to create asset."
      );
    } finally {
      setCreatingAsset(false);
    }
  }

  return (
    <section>
      <h2 className="page-title">Manage Assets</h2>

      <p className="page-description">
        Add asset categories and register new assets.
      </p>

      {error && (
        <p className="error-message" role="alert">
          {error}
        </p>
      )}

      {success && (
        <p className="success-message" role="status">
          {success}
        </p>
      )}

      {loading ? (
        <p className="empty-message">
          Loading categories...
        </p>
      ) : (
        <div className="management-grid">
          <section className="management-panel">
            <h3>Add Category</h3>

            <form
              className="management-form"
              onSubmit={handleCreateCategory}
            >
              <div className="form-field">
                <label htmlFor="category">
                  Category
                </label>

                <input
                  id="category"
                  type="text"
                  value={category}
                  onChange={(event) =>
                    setCategory(event.target.value)
                  }
                  placeholder="e.g. Electronics"
                  maxLength={100}
                  required
                />
              </div>

              <div className="form-field">
                <label htmlFor="subCategory">
                  Subcategory
                </label>

                <input
                  id="subCategory"
                  type="text"
                  value={subCategory}
                  onChange={(event) =>
                    setSubCategory(event.target.value)
                  }
                  placeholder="e.g. Laptops"
                  maxLength={100}
                  required
                />
              </div>

              <button
                type="submit"
                className="primary-button"
                disabled={creatingCategory}
              >
                {creatingCategory
                  ? "Adding..."
                  : "Add Category"}
              </button>
            </form>
          </section>

          <section className="management-panel">
            <h3>Add Asset</h3>

            <form
              className="management-form"
              onSubmit={handleCreateAsset}
            >
              <div className="form-field">
                <label htmlFor="assetName">
                  Asset Name
                </label>

                <input
                  id="assetName"
                  type="text"
                  value={name}
                  onChange={(event) =>
                    setName(event.target.value)
                  }
                  placeholder="e.g. ThinkPad T14"
                  maxLength={200}
                  required
                />
              </div>

              <div className="form-field">
                <label htmlFor="manufacturer">
                  Manufacturer
                </label>

                <input
                  id="manufacturer"
                  type="text"
                  value={manufacturer}
                  onChange={(event) =>
                    setManufacturer(event.target.value)
                  }
                  placeholder="e.g. Lenovo"
                  maxLength={100}
                />
              </div>

              <div className="form-field">
                <label htmlFor="price">
                  Price (₹)
                </label>

                <input
                  id="price"
                  type="number"
                  min="0"
                  step="0.01"
                  value={price}
                  onChange={(event) =>
                    setPrice(event.target.value)
                  }
                  required
                />
              </div>

              <div className="form-field">
                <label htmlFor="assetCategory">
                  Category
                </label>

                <select
                  id="assetCategory"
                  value={categoryId}
                  onChange={(event) =>
                    setCategoryId(event.target.value)
                  }
                  required
                >
                  <option value="">
                    Select a category
                  </option>

                  {categories.map((item) => (
                    <option
                      key={item.id}
                      value={item.id}
                    >
                      {item.category} / {item.subCategory}
                    </option>
                  ))}
                </select>

                {categories.length === 0 && (
                  <p className="form-hint">
                    Add a category before creating an asset.
                  </p>
                )}
              </div>

              <button
                type="submit"
                className="primary-button"
                disabled={
                  categories.length === 0 || creatingAsset
                }
              >
                {creatingAsset
                  ? "Adding..."
                  : "Add Asset"}
              </button>
            </form>
          </section>
        </div>
      )}
    </section>
  );
}

export default ManageAssetsPage;
