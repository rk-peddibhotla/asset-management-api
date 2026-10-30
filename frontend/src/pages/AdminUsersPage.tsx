
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
  const [success, setSuccess] = useState("");
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
            : "Failed to load pending users."
        );
      } finally {
        setLoading(false);
      }
    }

    void loadPendingUsers();
  }, []);

  async function handleApprove(userId: number) {
    setError("");
    setSuccess("");
    setProcessingUserId(userId);

    try {
      await approveUser(userId);

      setUsers((currentUsers) =>
        currentUsers.filter((user) => user.id !== userId)
      );

      setSuccess("User approved successfully.");
    } catch (error) {
      setError(
        error instanceof Error
          ? error.message
          : "Failed to approve user."
      );
    } finally {
      setProcessingUserId(null);
    }
  }

  async function handleBlock(userId: number) {
    setError("");
    setSuccess("");
    setProcessingUserId(userId);

    try {
      await blockUser(userId);

      setUsers((currentUsers) =>
        currentUsers.filter((user) => user.id !== userId)
      );

      setSuccess("User blocked successfully.");
    } catch (error) {
      setError(
        error instanceof Error
          ? error.message
          : "Failed to block user."
      );
    } finally {
      setProcessingUserId(null);
    }
  }

  return (
    <section>
      <h2 className="page-title">Pending Users</h2>

      <p className="page-description">
        Review new user registrations and approve or block accounts.
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
          Loading pending users...
        </p>
      ) : users.length === 0 ? (
        !error && (
          <p className="empty-message">
            No pending users found.
          </p>
        )
      ) : (
        <div className="table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th scope="col">Full Name</th>
                <th scope="col">Email Address</th>
                <th scope="col">Mobile Number</th>
                <th scope="col">Role</th>
                <th scope="col">Status</th>
                <th scope="col">Actions</th>
              </tr>
            </thead>

            <tbody>
              {users.map((user) => (
                <tr key={user.id}>
                  <td>{user.fullName}</td>

                  <td>{user.email}</td>

                  <td>{user.mobileNumber ?? "N/A"}</td>

                  <td>{user.role}</td>

                  <td>
                    <span
                      className={`status status-${user.accountStatus.toLowerCase()}`}
                    >
                      {user.accountStatus}
                    </span>
                  </td>

                  <td>
                    <div className="table-actions">
                      <button
                        type="button"
                        className="secondary-button"
                        onClick={() => handleApprove(user.id)}
                        disabled={processingUserId !== null}
                      >
                        {processingUserId === user.id
                          ? "Processing..."
                          : "Approve"}
                      </button>

                      <button
                        type="button"
                        className="danger-button"
                        onClick={() => handleBlock(user.id)}
                        disabled={processingUserId !== null}
                      >
                        Block
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

export default AdminUsersPage;
