
import { useEffect, useState } from "react";
import {
  getMyOrders,
  returnOrder,
} from "../api/orders";
import type { Order } from "../api/orders";

function MyOrdersPage() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [returningOrderId, setReturningOrderId] = useState<number | null>(
    null
  );

  async function loadOrders() {
    setError("");

    try {
      const response = await getMyOrders();
      setOrders(response);
    } catch (error) {
      setError(
        error instanceof Error
          ? error.message
          : "Failed to load orders"
      );
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadOrders();
  }, []);

  async function handleReturn(orderId: number) {
    setError("");
    setReturningOrderId(orderId);

    try {
      const updatedOrder = await returnOrder(orderId);

      setOrders((currentOrders) =>
        currentOrders.map((order) =>
          order.id === orderId ? updatedOrder : order
        )
      );
    } catch (error) {
      setError(
        error instanceof Error
          ? error.message
          : "Failed to return order"
      );
    } finally {
      setReturningOrderId(null);
    }
  }

  function formatDate(value: string): string {
    return new Date(value).toLocaleString();
  }

  if (loading) {
    return <p>Loading orders...</p>;
  }

  if (error && orders.length === 0) {
    return (
      <section>
        <h2 className="page-title">My Orders</h2>

        <p className="error-message" role="alert">
          {error}
        </p>
      </section>
    );
  }

  return (
    <section>
      <h2 className="page-title">My Orders</h2>

      {error && (
        <p className="error-message" role="alert">
          {error}
        </p>
      )}

      {orders.length === 0 ? (
        <p className="empty-message">
          You have no orders.
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
                <strong>Status:</strong>{" "}
                <span className="status">
                  {order.status}
                </span>
              </p>

              <p className="item-detail">
                <strong>Requested:</strong>{" "}
                {formatDate(order.requestedAt)}
              </p>

              {order.dueAt && (
                <p className="item-detail">
                  <strong>Due:</strong>{" "}
                  {formatDate(order.dueAt)}
                </p>
              )}

              {order.returnedAt && (
                <p className="item-detail">
                  <strong>Returned:</strong>{" "}
                  {formatDate(order.returnedAt)}
                </p>
              )}

              <p className="item-detail">
                <strong>Fine:</strong>{" "}
                ₹{order.fineAmount.toFixed(2)}
              </p>

              {order.status === "ACTIVE" && (
                <div className="item-actions">
                  <button
                    className="primary-button"
                    onClick={() => handleReturn(order.id)}
                    disabled={returningOrderId === order.id}
                  >
                    {returningOrderId === order.id
                      ? "Returning..."
                      : "Return Asset"}
                  </button>
                </div>
              )}
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}

export default MyOrdersPage;

