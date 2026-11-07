
import { useEffect, useState } from "react";

import {
  getMyBorrowings,
  returnBorrowing,
} from "../api/orders";
import type { Borrowing } from "../api/orders";

function MyOrdersPage() {
  const [borrowings, setBorrowings] = useState<Borrowing[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [returningBorrowingId, setReturningBorrowingId] = useState<
    number | null
  >(null);

  useEffect(() => {
    async function loadBorrowings() {
      try {
        const response = await getMyBorrowings();
        setBorrowings(response);
      } catch (error) {
        setError(
          error instanceof Error
            ? error.message
            : "Failed to load borrowing records."
        );
      } finally {
        setLoading(false);
      }
    }

    void loadBorrowings();
  }, []);

  async function handleReturn(borrowingId: number) {
    setError("");
    setSuccess("");
    setReturningBorrowingId(borrowingId);

    try {
      const updatedBorrowing = await returnBorrowing(borrowingId);

      setBorrowings((currentBorrowings) =>
        currentBorrowings.map((borrowing) =>
          borrowing.id === borrowingId
            ? updatedBorrowing
            : borrowing
        )
      );

      setSuccess("Resource returned successfully.");
    } catch (error) {
      setError(
        error instanceof Error
          ? error.message
          : "Failed to return resource."
      );
    } finally {
      setReturningBorrowingId(null);
    }
  }

  function formatDate(value: string | null | undefined): string {
    return value ? new Date(value).toLocaleString() : "—";
  }

  return (
    <section>
      <h2 className="page-title">My Borrowings</h2>

      <p className="page-description">
        View your borrowing requests and return records.
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
        <p className="empty-message">Loading borrowings...</p>
      ) : borrowings.length === 0 && !error ? (
        <p className="empty-message">No borrowing records found.</p>
      ) : borrowings.length > 0 ? (
        <div className="table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th scope="col">Borrowing ID</th>
                <th scope="col">Resource Name</th>
                <th scope="col">Status</th>
                <th scope="col">Requested On</th>
                <th scope="col">Due Date</th>
                <th scope="col">Returned On</th>
                <th scope="col">Late Fee</th>
                <th scope="col">Action</th>
              </tr>
            </thead>

            <tbody>
              {borrowings.map((borrowing) => (
                <tr key={borrowing.id}>
                  <td>#{borrowing.id}</td>

                  <td>{borrowing.resourceName}</td>

                  <td>
                    <span
                      className={`status status-${borrowing.status.toLowerCase()}`}
                    >
                      {borrowing.status}
                    </span>
                  </td>

                  <td>{formatDate(borrowing.requestedAt)}</td>

                  <td>{formatDate(borrowing.dueAt)}</td>

                  <td>{formatDate(borrowing.returnedAt)}</td>

                  <td>₹{borrowing.lateFeeAmount.toFixed(2)}</td>

                  <td>
                    {borrowing.status === "ACTIVE" ? (
                      <button
                        type="button"
                        className="secondary-button"
                        onClick={() => handleReturn(borrowing.id)}
                        disabled={returningBorrowingId !== null}
                      >
                        {returningBorrowingId === borrowing.id
                          ? "Returning..."
                          : "Return"}
                      </button>
                    ) : (
                      "—"
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : null}
    </section>
  );
}

export default MyOrdersPage;
