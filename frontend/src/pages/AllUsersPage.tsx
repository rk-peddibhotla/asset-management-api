
import { useEffect, useState } from "react";

import { getAllUsers } from "../api/adminUsers";
import type { AdminUser } from "../api/adminUsers";

function AllUsersPage() {
  const [users, setUsers] = useState<AdminUser[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    async function loadAllUsers() {
      try {
        const response = await getAllUsers();
        setUsers(response);
      } catch (error) {
        setError(
          error instanceof Error
            ? error.message
            : "Failed to load users."
        );
      } finally {
        setLoading(false);
      }
    }

    void loadAllUsers();
  }, []);

  return (
    <section>
      <h2 className="page-title">All Users</h2>

      <p className="page-description">
        View all registered users and their account status.
      </p>

      {error && (
        <p className="error-message" role="alert">
          {error}
        </p>
      )}

      {loading ? (
        <p className="empty-message">
          Loading users...
        </p>
      ) : users.length === 0 ? (
        !error && (
          <p className="empty-message">
            No users found.
          </p>
        )
      ) : (
        <div className="table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th scope="col">User ID</th>
                <th scope="col">Full Name</th>
                <th scope="col">Email Address</th>
                <th scope="col">Mobile Number</th>
                <th scope="col">Role</th>
                <th scope="col">Status</th>
              </tr>
            </thead>

            <tbody>
              {users.map((user) => (
                <tr key={user.id}>
                  <td>#{user.id}</td>

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
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}

export default AllUsersPage;
