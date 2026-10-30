
import { useEffect, useState } from "react";

import { getMyOrders, returnOrder } from "../api/orders";
import type { Order } from "../api/orders";

function MyOrdersPage() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [returningOrderId, setReturningOrderId] = useState<number | null>(
    null
  );

  useEffect(() => {
    async function loadOrders() {
      try {
        const response = await getMyOrders();
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

    void loadOrders();
  }, []);

  async function handleReturn(orderId: number) {
    setError("");
    setSuccess("");
    setReturningOrderId(orderId);

    try {
      const updatedOrder = await returnOrder(orderId);

      setOrders((currentOrders) =>
        currentOrders.map((order) =>
          order.id === orderId ? updatedOrder : order
        )
      );

      setSuccess("Asset returned successfully.");
    } catch (error) {
      setError(
        error instanceof Error
          ? error.message
          : "Failed to return asset."
      );
    } finally {
      setReturningOrderId(null);
    }
  }

  function formatDate(value: string | null | undefined): string {
    return value ? new Date(value).toLocaleString() : "—";
  }

  return (
    <section>
      <h2 className="page-title">My Orders</h2>

      <p className="page-description">
        View borrowing requests and return records.
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
        <p className="empty-message">Loading orders...</p>
      ) : orders.length === 0 && !error ? (
        <p className="empty-message">No orders found.</p>
      ) : orders.length > 0 ? (
        <div className="table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th scope="col">Order ID</th>
                <th scope="col">Asset Name</th>
                <th scope="col">Status</th>
                <th scope="col">Requested On</th>
                <th scope="col">Due Date</th>
                <th scope="col">Returned On</th>
                <th scope="col">Fine</th>
                <th scope="col">Action</th>
              </tr>
            </thead>

            <tbody>
              {orders.map((order) => (
                <tr key={order.id}>
                  <td>#{order.id}</td>

                  <td>{order.assetName}</td>

                  <td>
                    <span
                      className={`status status-${order.status.toLowerCase()}`}
                    >
                      {order.status}
                    </span>
                  </td>

                  <td>{formatDate(order.requestedAt)}</td>

                  <td>{formatDate(order.dueAt)}</td>

                  <td>{formatDate(order.returnedAt)}</td>

                  <td>₹{order.fineAmount.toFixed(2)}</td>

                  <td>
                    {order.status === "ACTIVE" ? (
                      <button
                        type="button"
                        className="secondary-button"
                        onClick={() => handleReturn(order.id)}
                        disabled={returningOrderId !== null}
                      >
                        {returningOrderId === order.id
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
