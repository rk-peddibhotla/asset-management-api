
import { useEffect, useState } from "react";

import { getAllOrders } from "../api/orders";
import type { Order } from "../api/orders";

function AllOrdersPage() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    async function loadAllOrders() {
      try {
        const response = await getAllOrders();
        setOrders(response);
      } catch (error) {
        setError(
          error instanceof Error
            ? error.message
            : "Failed to load orders."
        );
      } finally {
        setLoading(false);
      }
    }

    void loadAllOrders();
  }, []);

  function formatDate(value: string | null): string {
    if (!value) {
      return "-";
    }

    return new Date(value).toLocaleString();
  }

  function formatFine(value: number | null): string {
    return `₹${(value ?? 0).toFixed(2)}`;
  }

  return (
    <section>
      <h2 className="page-title">All Orders</h2>

      <p className="page-description">
        View all student borrowing requests, active loans,
        returns, and fines.
      </p>

      {error && (
        <p className="error-message" role="alert">
          {error}
        </p>
      )}

      {loading ? (
        <p className="empty-message">
          Loading orders...
        </p>
      ) : orders.length === 0 ? (
        !error && (
          <p className="empty-message">
            No orders found.
          </p>
        )
      ) : (
        <div className="table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th scope="col">Order ID</th>
                <th scope="col">Student Name</th>
                <th scope="col">Student Email</th>
                <th scope="col">Asset Name</th>
                <th scope="col">Requested On</th>
                <th scope="col">Status</th>
                <th scope="col">Due Date</th>
                <th scope="col">Returned On</th>
                <th scope="col">Fine</th>
              </tr>
            </thead>

            <tbody>
              {orders.map((order) => (
                <tr key={order.id}>
                  <td>#{order.id}</td>

                  <td>{order.userName}</td>

                  <td>{order.userEmail}</td>

                  <td>{order.assetName}</td>

                  <td>{formatDate(order.requestedAt)}</td>

                  <td>
                    <span
                      className={`status status-${order.status.toLowerCase()}`}
                    >
                      {order.status}
                    </span>
                  </td>

                  <td>{formatDate(order.dueAt)}</td>

                  <td>{formatDate(order.returnedAt)}</td>

                  <td>{formatFine(order.fineAmount)}</td>
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
