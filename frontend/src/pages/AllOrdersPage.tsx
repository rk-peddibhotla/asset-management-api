
import { useEffect, useState } from "react";

import { getAllBorrowings } from "../api/orders";
import type { Borrowing } from "../api/orders";

function AllOrdersPage() {
  const [borrowings, setBorrowings] = useState<Borrowing[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    async function loadAllBorrowings() {
      try {
        const response = await getAllBorrowings();
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

    void loadAllBorrowings();
  }, []);

  function formatDate(value: string | null): string {
    if (!value) {
      return "—";
    }

    return new Date(value).toLocaleString();
  }

  function formatLateFee(value: number | null): string {
    return `₹${(value ?? 0).toFixed(2)}`;
  }

  return (
    <section>
      <h2 className="page-title">All Borrowings</h2>

      <p className="page-description">
        View all user borrowing requests, active loans,
        returns, and late fees.
      </p>

      {error && (
        <p className="error-message" role="alert">
          {error}
        </p>
      )}

      {loading ? (
        <p className="empty-message">
          Loading borrowings...
        </p>
      ) : borrowings.length === 0 ? (
        !error && (
          <p className="empty-message">
            No borrowing records found.
          </p>
        )
      ) : (
        <div className="table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th scope="col">Borrowing ID</th>
                <th scope="col">User Name</th>
                <th scope="col">User Email</th>
                <th scope="col">Resource Name</th>
                <th scope="col">Requested On</th>
                <th scope="col">Status</th>
                <th scope="col">Due Date</th>
                <th scope="col">Returned On</th>
                <th scope="col">Late Fee</th>
              </tr>
            </thead>

            <tbody>
              {borrowings.map((borrowing) => (
                <tr key={borrowing.id}>
                  <td>#{borrowing.id}</td>

                  <td>{borrowing.userName}</td>

                  <td>{borrowing.userEmail}</td>

                  <td>{borrowing.resourceName}</td>

                  <td>{formatDate(borrowing.requestedAt)}</td>

                  <td>
                    <span
                      className={`status status-${borrowing.status.toLowerCase()}`}
                    >
                      {borrowing.status}
                    </span>
                  </td>

                  <td>{formatDate(borrowing.dueAt)}</td>

                  <td>{formatDate(borrowing.returnedAt)}</td>

                  <td>{formatLateFee(borrowing.lateFeeAmount)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}

export default AllOrdersPage;
