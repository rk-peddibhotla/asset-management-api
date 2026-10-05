
import { useEffect, useState } from "react";
import { getAssets } from "../api/assets";
import type { Asset } from "../api/assets";
import { createOrder } from "../api/orders";

function AssetsPage() {
  const [assets, setAssets] = useState<Asset[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [requestingAssetId, setRequestingAssetId] = useState<number | null>(
    null
  );

  useEffect(() => {
    async function loadAssets() {
      try {
        const response = await getAssets();
        setAssets(response);
      } catch (error) {
        setError(
          error instanceof Error
            ? error.message
            : "Failed to load assets"
        );
      } finally {
        setLoading(false);
      }
    }

    loadAssets();
  }, []);

  async function handleRequest(assetId: number) {
    setError("");
    setRequestingAssetId(assetId);

    try {
      await createOrder({
        assetId,
      });

      alert("Asset requested successfully.");
    } catch (error) {
      setError(
        error instanceof Error
          ? error.message
          : "Failed to request asset"
      );
    } finally {
      setRequestingAssetId(null);
    }
  }

  if (loading) {
    return <p>Loading assets...</p>;
  }

  return (
    <section>
      <h2 className="page-title">Available Assets</h2>

      {error && (
        <p className="error-message" role="alert">
          {error}
        </p>
      )}

      {assets.length === 0 ? (
        <p className="empty-message">
          No assets available.
        </p>
      ) : (
        <ul className="item-list">
          {assets.map((asset) => (
            <li className="item" key={asset.id}>
              <h3 className="item-title">
                {asset.name}
              </h3>

              <p className="item-detail">
                <strong>Manufacturer:</strong>{" "}
                {asset.manufacturer ?? "N/A"}
              </p>

              <p className="item-detail">
                <strong>Price:</strong>{" "}
                ₹{asset.price.toFixed(2)}
              </p>

              <p className="item-detail">
                <strong>Category:</strong>{" "}
                {asset.category.category}
                {" / "}
                {asset.category.subCategory}
              </p>

              <div className="item-actions">
                <button
                  className="primary-button"
                  onClick={() => handleRequest(asset.id)}
                  disabled={requestingAssetId === asset.id}
                >
                  {requestingAssetId === asset.id
                    ? "Requesting..."
                    : "Request Asset"}
                </button>
              </div>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}

export default AssetsPage;

