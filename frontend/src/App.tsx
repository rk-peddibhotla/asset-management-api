
import { useState } from "react";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import AssetsPage from "./pages/AssetsPage";
import MyOrdersPage from "./pages/MyOrdersPage";
import AdminUsersPage from "./pages/AdminUsersPage";
import AdminOrdersPage from "./pages/AdminOrdersPage";
import {
  getRole,
  getToken,
  clearToken,
} from "./auth/authStorage";
import "./App.css";

type Page = "assets" | "orders" | "admin-users" | "admin-orders";

function App() {
  const [authenticated, setAuthenticated] = useState(
    getToken() !== null
  );

  const [role, setRole] = useState<string | null>(getRole());

  const [page, setPage] = useState<Page>("assets");

  const [showRegister, setShowRegister] = useState(false);

  function handleLogin() {
    setRole(getRole());
    setAuthenticated(true);
    setShowRegister(false);
  }

  function handleLogout() {
    clearToken();
    setRole(null);
    setAuthenticated(false);
    setShowRegister(false);
    setPage("assets");
  }

  if (!authenticated) {
    if (showRegister) {
      return (
        <RegisterPage
          onBackToLogin={() => setShowRegister(false)}
        />
      );
    }

    return (
      <LoginPage
        onLogin={handleLogin}
        onRegister={() => setShowRegister(true)}
      />
    );
  }

  return (
    <div className="app">
      <header className="app-header">
        <h1>Asset Management</h1>

        <nav className="app-nav">
          <button
            className={page === "assets" ? "active" : ""}
            onClick={() => setPage("assets")}
          >
            Assets
          </button>

          <button
            className={page === "orders" ? "active" : ""}
            onClick={() => setPage("orders")}
          >
            My Orders
          </button>

          {role === "ADMIN" && (
            <>
              <button
                className={page === "admin-users" ? "active" : ""}
                onClick={() => setPage("admin-users")}
              >
                Pending Users
              </button>

              <button
                className={page === "admin-orders" ? "active" : ""}
                onClick={() => setPage("admin-orders")}
              >
                Pending Orders
              </button>
            </>
          )}

          <button onClick={handleLogout}>
            Logout
          </button>
        </nav>
      </header>

      <main className="app-content">
        {page === "assets" && <AssetsPage />}

        {page === "orders" && <MyOrdersPage />}

        {page === "admin-users" && role === "ADMIN" && (
          <AdminUsersPage />
        )}

        {page === "admin-orders" && role === "ADMIN" && (
          <AdminOrdersPage />
        )}
      </main>
    </div>
  );
}

export default App;
