
import { useEffect, useState } from "react";

import { getResources } from "../api/assets";
import type { Resource } from "../api/assets";
import { createBorrowing } from "../api/orders";
import { getRole } from "../auth/authStorage";

function AssetsPage() {
  const isUser = getRole() === "USER";

  const [resources, setResources] = useState<Resource[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const [requestingResourceId, setRequestingResourceId] = useState<
    number | null
  >(null);

  const [searchQuery, setSearchQuery] = useState("");
  const [selectedCategory, setSelectedCategory] = useState("ALL");

  useEffect(() => {
    async function loadResources() {
      try {
        const response = await getResources();
        setResources(response);
      } catch (error) {
        setError(
          error instanceof Error
            ? error.message
            : "Failed to load resources."
        );
      } finally {
        setLoading(false);
      }
    }

    void loadResources();
  }, []);

  const categories = Array.from(
    new Set(
      resources
        .map((resource) => resource.category)
        .filter((category): category is string => Boolean(category))
    )
  ).sort();

  const filteredResources = resources.filter((resource) => {
    const query = searchQuery.trim().toLowerCase();

    const searchableText = [
      resource.name,
      resource.manufacturer,
      resource.category,
      resource.subCategory,
      resource.description,
    ]
      .map((value) => String(value ?? "").toLowerCase())
      .join(" ");

    const matchesSearch = searchableText.includes(query);

    const matchesCategory =
      selectedCategory === "ALL" ||
      resource.category === selectedCategory;

    return matchesSearch && matchesCategory;
  });

  async function handleRequest(resourceId: number) {
    if (!isUser) {
      return;
    }

    setError("");
    setSuccess("");
    setRequestingResourceId(resourceId);

    try {
      await createBorrowing({ resourceId });

      setSuccess(
        "Resource request submitted. Waiting for administrator approval."
      );
    } catch (error) {
      setError(
        error instanceof Error
          ? error.message
          : "Failed to request resource."
      );
    } finally {
      setRequestingResourceId(null);
    }
  }

  return (
    <section>
      <h2 className="page-title">Resources</h2>

      <p className="page-description">
        {isUser
          ? "View available resources and submit borrowing requests."
          : "View all registered resources."}
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

      {!loading && resources.length > 0 && (
        <div className="asset-filters">
          <input
            type="search"
            placeholder="Search resources..."
            aria-label="Search resources"
            value={searchQuery}
            onChange={(event) => setSearchQuery(event.target.value)}
          />

          <select
            aria-label="Filter by category"
            value={selectedCategory}
            onChange={(event) => setSelectedCategory(event.target.value)}
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
        <p className="empty-message">Loading resources...</p>
      ) : resources.length === 0 ? (
        !error && (
          <p className="empty-message">
            No resources found.
          </p>
        )
      ) : filteredResources.length === 0 ? (
        <p className="empty-message">
          No resources match your search or filters.
        </p>
      ) : (
        <div className="table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th scope="col">Resource Name</th>
                <th scope="col">Manufacturer</th>
                <th scope="col">Category</th>
                <th scope="col">Purchase Price</th>
                <th scope="col">Status</th>

                {isUser && (
                  <th scope="col">Action</th>
                )}
              </tr>
            </thead>

            <tbody>
              {filteredResources.map((resource) => (
                <tr key={resource.id}>
                  <td>{resource.name}</td>

                  <td>{resource.manufacturer ?? "N/A"}</td>

                  <td>
                    {resource.category ?? "N/A"}
                    {" / "}
                    {resource.subCategory ?? "N/A"}
                  </td>

                  <td>
                    {resource.purchasePrice == null
                      ? "N/A"
                      : `₹${resource.purchasePrice.toFixed(2)}`}
                  </td>

                  <td>{resource.status}</td>

                  {isUser && (
                    <td>
                      <button
                        type="button"
                        className="secondary-button"
                        onClick={() => handleRequest(resource.id)}
                        disabled={
                          requestingResourceId !== null ||
                          resource.status !== "AVAILABLE"
                        }
                      >
                        {requestingResourceId === resource.id
                          ? "Requesting..."
                          : resource.status === "AVAILABLE"
                            ? "Request"
                            : "Unavailable"}
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
