
import { useEffect, useState } from "react";

import {
  approveOrder,
  getPendingOrders,
  rejectOrder,
} from "../api/orders";

import type { Order } from "../api/orders";

function AdminOrdersPage() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [processingOrderId, setProcessingOrderId] = useState<number | null>(
    null
  );

  useEffect(() => {
    async function loadPendingOrders() {
      try {
        const response = await getPendingOrders();
        setOrders(response);
      } catch (error) {
        setError(
          error instanceof Error
            ? error.message
            : "Failed to load pending orders."
        );
      } finally {
        setLoading(false);
      }
    }

    void loadPendingOrders();
  }, []);

  async function handleApprove(orderId: number) {
    setError("");
    setSuccess("");
    setProcessingOrderId(orderId);

    try {
      await approveOrder(orderId);

      setOrders((currentOrders) =>
        currentOrders.filter((order) => order.id !== orderId)
      );

      setSuccess("Order approved successfully.");
    } catch (error) {
      setError(
        error instanceof Error
          ? error.message
          : "Failed to approve order."
      );
    } finally {
      setProcessingOrderId(null);
    }
  }

  async function handleReject(orderId: number) {
    setError("");
    setSuccess("");
    setProcessingOrderId(orderId);

    try {
      await rejectOrder(orderId);

      setOrders((currentOrders) =>
        currentOrders.filter((order) => order.id !== orderId)
      );

      setSuccess("Order rejected successfully.");
    } catch (error) {
      setError(
        error instanceof Error
          ? error.message
          : "Failed to reject order."
      );
    } finally {
      setProcessingOrderId(null);
    }
  }

  function formatDate(value: string): string {
    return new Date(value).toLocaleString();
  }

  return (
    <section>
      <h2 className="page-title">Pending Orders</h2>

      <p className="page-description">
        Review student borrowing requests and approve or reject orders.
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
          Loading pending orders...
        </p>
      ) : orders.length === 0 ? (
        !error && (
          <p className="empty-message">
            No pending orders found.
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
                <th scope="col">Actions</th>
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

                  <td>
                    <div className="table-actions">
                      <button
                        type="button"
                        className="secondary-button"
                        onClick={() => handleApprove(order.id)}
                        disabled={processingOrderId !== null}
                      >
                        {processingOrderId === order.id
                          ? "Processing..."
                          : "Approve"}
                      </button>

                      <button
                        type="button"
                        className="danger-button"
                        onClick={() => handleReject(order.id)}
                        disabled={processingOrderId !== null}
                      >
                        {processingOrderId === order.id
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
