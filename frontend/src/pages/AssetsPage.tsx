
import { useEffect, useState } from "react";

import { getAssets } from "../api/assets";
import type { Asset } from "../api/assets";
import { createOrder } from "../api/orders";
import { getRole } from "../auth/authStorage";

function AssetsPage() {
  const isStudent = getRole() === "STUDENT";

  const [assets, setAssets] = useState<Asset[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const [requestingAssetId, setRequestingAssetId] = useState<number | null>(
    null
  );

  const [searchQuery, setSearchQuery] = useState("");
  const [selectedCategory, setSelectedCategory] = useState("ALL");

  useEffect(() => {
    async function loadAssets() {
      try {
        const response = await getAssets();
        setAssets(response);
      } catch (error) {
        setError(
          error instanceof Error
            ? error.message
            : "Failed to load assets."
        );
      } finally {
        setLoading(false);
      }
    }

    void loadAssets();
  }, []);

  const categories = Array.from(
    new Set(
      assets
        .map((asset) => asset.category)
        .filter((category): category is string => Boolean(category))
    )
  ).sort();

  const filteredAssets = assets.filter((asset) => {
    const query = searchQuery.trim().toLowerCase();

    const searchableText = [
      asset.name,
      asset.manufacturer,
      asset.category,
      asset.subCategory,
    ]
      .map((value) => String(value ?? "").toLowerCase())
      .join(" ");

    const matchesSearch = searchableText.includes(query);

    const matchesCategory =
      selectedCategory === "ALL" ||
      asset.category === selectedCategory;

    return matchesSearch && matchesCategory;
  });

  async function handleRequest(assetId: number) {
    if (!isStudent) {
      return;
    }

    setError("");
    setSuccess("");
    setRequestingAssetId(assetId);

    try {
      await createOrder({ assetId });

      setSuccess(
        "Asset request submitted. Waiting for administrator approval."
      );
    } catch (error) {
      setError(
        error instanceof Error
          ? error.message
          : "Failed to request asset."
      );
    } finally {
      setRequestingAssetId(null);
    }
  }

  return (
    <section>
      <h2 className="page-title">Assets</h2>

      <p className="page-description">
        {isStudent
          ? "View available assets and submit borrowing requests."
          : "View all registered assets."}
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

      {!loading && assets.length > 0 && (
        <div className="asset-filters">
          <input
            type="search"
            placeholder="Search assets..."
            aria-label="Search assets"
            value={searchQuery}
            onChange={(event) => setSearchQuery(event.target.value)}
          />

          <select
            aria-label="Filter by category"
            value={selectedCategory}
            onChange={(event) =>
              setSelectedCategory(event.target.value)
            }
          >
            <option value="ALL">All Categories</option>

            {categories.map((category) => (
              <option key={category} value={category}>
                {category}
              </option>
            ))}
          </select>
        </div>
      )}

      {loading ? (
        <p className="empty-message">
          Loading assets...
        </p>
      ) : assets.length === 0 ? (
        !error && (
          <p className="empty-message">
            No assets found.
          </p>
        )
      ) : filteredAssets.length === 0 ? (
        <p className="empty-message">
          No assets match your search or filters.
        </p>
      ) : (
        <div className="table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th scope="col">Asset Name</th>
                <th scope="col">Manufacturer</th>
                <th scope="col">Category</th>
                <th scope="col">Price</th>

                {isStudent && (
                  <th scope="col">Action</th>
                )}
              </tr>
            </thead>

            <tbody>
              {filteredAssets.map((asset) => (
                <tr key={asset.id}>
                  <td>{asset.name}</td>

                  <td>{asset.manufacturer ?? "N/A"}</td>

                  <td>
                    {asset.category ?? "N/A"}
                    {" / "}
                    {asset.subCategory ?? "N/A"}
                  </td>

                  <td>₹{asset.price.toFixed(2)}</td>

                  {isStudent && (
                    <td>
                      <button
                        type="button"
                        className="secondary-button"
                        onClick={() => handleRequest(asset.id)}
                        disabled={requestingAssetId !== null}
                      >
                        {requestingAssetId === asset.id
                          ? "Requesting..."
                          : "Request"}
                      </button>
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}

export default AssetsPage;
