import { useEffect, useState } from "react";
import {
  approveUser,
  blockUser,
  getPendingUsers,
} from "../api/adminUsers";
import type { AdminUser } from "../api/adminUsers";

function AdminUsersPage() {
  const [users, setUsers] = useState<AdminUser[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [processingUserId, setProcessingUserId] = useState<number | null>(
    null
  );

  useEffect(() => {
    async function loadPendingUsers() {
      try {
        const response = await getPendingUsers();
        setUsers(response);
      } catch (error) {
        setError(
          error instanceof Error
            ? error.message
            : "Failed to load pending users"
        );
      } finally {
        setLoading(false);
      }
    }

    loadPendingUsers();
  }, []);

  async function handleApprove(userId: number) {
    setError("");
    setProcessingUserId(userId);

    try {
      await approveUser(userId);

      setUsers((currentUsers) =>
        currentUsers.filter((user) => user.id !== userId)
      );
    } catch (error) {
      setError(
        error instanceof Error
          ? error.message
          : "Failed to approve user"
      );
    } finally {
      setProcessingUserId(null);
    }
  }

  async function handleBlock(userId: number) {
    setError("");
    setProcessingUserId(userId);

    try {
      await blockUser(userId);

      setUsers((currentUsers) =>
        currentUsers.filter((user) => user.id !== userId)
      );
    } catch (error) {
      setError(
        error instanceof Error
          ? error.message
          : "Failed to block user"
      );
    } finally {
      setProcessingUserId(null);
    }
  }

  if (loading) {
    return <p>Loading pending users...</p>;
  }

  return (
    <section>
      <h2 className="page-title">Pending Users</h2>

      {error && (
        <p className="error-message" role="alert">
          {error}
        </p>
      )}

      {users.length === 0 ? (
        <p className="empty-message">No pending users.</p>
      ) : (
        <ul className="item-list">
          {users.map((user) => (
            <li className="item" key={user.id}>
              <h3 className="item-title">{user.fullName}</h3>

              <p className="item-detail">
                <strong>Email:</strong> {user.email}
              </p>

              <p className="item-detail">
                <strong>Mobile:</strong> {user.mobileNumber ?? "N/A"}
              </p>

              <p className="item-detail">
                <strong>Role:</strong> {user.role}
              </p>

              <p className="item-detail">
                <strong>Status:</strong>{" "}
                <span className="status">{user.accountStatus}</span>
              </p>

              <div className="item-actions">
                <button
                  className="primary-button"
                  onClick={() => handleApprove(user.id)}
                  disabled={processingUserId === user.id}
                >
                  {processingUserId === user.id
                    ? "Processing..."
                    : "Approve"}
                </button>

                <button
                  className="danger-button"
                  onClick={() => handleBlock(user.id)}
                  disabled={processingUserId === user.id}
                >
                  {processingUserId === user.id
                    ? "Processing..."
                    : "Block"}
                </button>
              </div>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}

export default AdminUsersPage;