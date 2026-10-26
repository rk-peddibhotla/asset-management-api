
import { useEffect, useState } from "react";
import {
  approveOrder,
  getPendingOrders,
  rejectOrder,
} from "../api/adminOrders";
import type { Order } from "../api/orders";

function AdminOrdersPage() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
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
            : "Failed to load pending orders"
        );
      } finally {
        setLoading(false);
      }
    }

    loadPendingOrders();
  }, []);

  async function handleApprove(orderId: number) {
    setError("");
    setProcessingOrderId(orderId);

    try {
      await approveOrder(orderId);

      setOrders((currentOrders) =>
        currentOrders.filter((order) => order.id !== orderId)
      );
    } catch (error) {
      setError(
        error instanceof Error
          ? error.message
          : "Failed to approve order"
      );
    } finally {
      setProcessingOrderId(null);
    }
  }

  async function handleReject(orderId: number) {
    setError("");
    setProcessingOrderId(orderId);

    try {
      await rejectOrder(orderId);

      setOrders((currentOrders) =>
        currentOrders.filter((order) => order.id !== orderId)
      );
    } catch (error) {
      setError(
        error instanceof Error
          ? error.message
          : "Failed to reject order"
      );
    } finally {
      setProcessingOrderId(null);
    }
  }

  function formatDate(value: string): string {
    return new Date(value).toLocaleString();
  }

  if (loading) {
    return <p>Loading pending orders...</p>;
  }

  return (
    <section>
      <h2 className="page-title">Pending Orders</h2>

      {error && (
        <p className="error-message" role="alert">
          {error}
        </p>
      )}

      {orders.length === 0 ? (
        <p className="empty-message">
          No pending orders.
        </p>
      ) : (
        <ul className="item-list">
          {orders.map((order) => (
            <li className="item" key={order.id}>
              <h3 className="item-title">
                {order.assetName}
              </h3>

              <p className="item-detail">
                <strong>Order ID:</strong> {order.id}
              </p>

              <p className="item-detail">
                <strong>User ID:</strong> {order.userId}
              </p>

              <p className="item-detail">
                <strong>Asset ID:</strong> {order.assetId}
              </p>

              <p className="item-detail">
                <strong>Status:</strong>{" "}
                <span className="status">
                  {order.status}
                </span>
              </p>

              <p className="item-detail">
                <strong>Requested:</strong>{" "}
                {formatDate(order.requestedAt)}
              </p>

              <div className="item-actions">
                <button
                  className="primary-button"
                  onClick={() => handleApprove(order.id)}
                  disabled={processingOrderId === order.id}
                >
                  {processingOrderId === order.id
                    ? "Processing..."
                    : "Approve"}
                </button>

                <button
                  className="danger-button"
                  onClick={() => handleReject(order.id)}
                  disabled={processingOrderId === order.id}
                >
                  {processingOrderId === order.id
                    ? "Processing..."
                    : "Reject"}
                </button>
              </div>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}

export default AdminOrdersPage;
