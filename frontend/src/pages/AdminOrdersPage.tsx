
import { useEffect, useState } from "react";

import {
  approveBorrowing,
  getPendingBorrowings,
  rejectBorrowing,
} from "../api/orders";

import type { Borrowing } from "../api/orders";

function AdminOrdersPage() {
  const [borrowings, setBorrowings] = useState<Borrowing[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [processingBorrowingId, setProcessingBorrowingId] = useState<
    number | null
  >(null);

  useEffect(() => {
    async function loadPendingBorrowings() {
      try {
        const response = await getPendingBorrowings();
        setBorrowings(response);
      } catch (error) {
        setError(
          error instanceof Error
            ? error.message
            : "Failed to load pending borrowings."
        );
      } finally {
        setLoading(false);
      }
    }

    void loadPendingBorrowings();
  }, []);

  async function handleApprove(borrowingId: number) {
    setError("");
    setSuccess("");
    setProcessingBorrowingId(borrowingId);

    try {
      await approveBorrowing(borrowingId);

      setBorrowings((currentBorrowings) =>
        currentBorrowings.filter(
          (borrowing) => borrowing.id !== borrowingId
        )
      );

      setSuccess("Borrowing request approved successfully.");
    } catch (error) {
      setError(
        error instanceof Error
          ? error.message
          : "Failed to approve borrowing request."
      );
    } finally {
      setProcessingBorrowingId(null);
    }
  }

  async function handleReject(borrowingId: number) {
    setError("");
    setSuccess("");
    setProcessingBorrowingId(borrowingId);

    try {
      await rejectBorrowing(borrowingId);

      setBorrowings((currentBorrowings) =>
        currentBorrowings.filter(
          (borrowing) => borrowing.id !== borrowingId
        )
      );

      setSuccess("Borrowing request rejected successfully.");
    } catch (error) {
      setError(
        error instanceof Error
          ? error.message
          : "Failed to reject borrowing request."
      );
    } finally {
      setProcessingBorrowingId(null);
    }
  }

  function formatDate(value: string): string {
    return new Date(value).toLocaleString();
  }

  return (
    <section>
      <h2 className="page-title">Pending Borrowings</h2>

      <p className="page-description">
        Review user borrowing requests and approve or reject them.
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
          Loading pending borrowings...
        </p>
      ) : borrowings.length === 0 ? (
        !error && (
          <p className="empty-message">
            No pending borrowings found.
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
                <th scope="col">Actions</th>
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

                  <td>
                    <div className="table-actions">
                      <button
                        type="button"
                        className="secondary-button"
                        onClick={() => handleApprove(borrowing.id)}
                        disabled={processingBorrowingId !== null}
                      >
                        {processingBorrowingId === borrowing.id
                          ? "Processing..."
                          : "Approve"}
                      </button>

                      <button
                        type="button"
                        className="danger-button"
                        onClick={() => handleReject(borrowing.id)}
                        disabled={processingBorrowingId !== null}
                      >
                        {processingBorrowingId === borrowing.id
                          ? "Processing..."
                          : "Reject"}
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}

export default AdminOrdersPage;
