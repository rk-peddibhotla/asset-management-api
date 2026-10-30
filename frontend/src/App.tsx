
import { useEffect, useState } from "react";

import AppHeader from "./components/AppHeader";

import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import AssetsPage from "./pages/AssetsPage";
import MyOrdersPage from "./pages/MyOrdersPage";
import ManageAssetsPage from "./pages/ManageAssetsPage";
import AdminUsersPage from "./pages/AdminUsersPage";
import AdminOrdersPage from "./pages/AdminOrdersPage";
import AllUsersPage from "./pages/AllUsersPage";
import AllOrdersPage from "./pages/AllOrdersPage";

import { getCurrentUser } from "./api/users";

import {
  getRole,
  getToken,
  clearToken,
} from "./auth/authStorage";

import "./App.css";

type Page =
  | "assets"
  | "orders"
  | "manage-assets"
  | "admin-users"
  | "all-users"
  | "admin-orders"
  | "all-orders";

type AuthPage = "login" | "register";

function App() {
  const [authenticated, setAuthenticated] = useState(
    getToken() !== null
  );

  const [role, setRole] = useState<string | null>(
    getRole()
  );

  const [fullName, setFullName] = useState<string | null>(
    null
  );

  const [page, setPage] = useState<Page>("assets");

  const [authPage, setAuthPage] =
    useState<AuthPage>("login");

  const isAdmin = role === "ADMIN";
  const isStudent = role === "STUDENT";

  useEffect(() => {
    if (!authenticated || !isStudent) {
      return;
    }

    let cancelled = false;

    async function loadCurrentUser() {
      try {
        const user = await getCurrentUser();

        if (!cancelled) {
          setFullName(user.fullName);
        }
      } catch (error) {
        if (!cancelled) {
          console.error(
            "Failed to load current user:",
            error
          );
          setFullName(null);
        }
      }
    }

    void loadCurrentUser();

    return () => {
      cancelled = true;
    };
  }, [authenticated, isStudent]);

  const portalSubtitle = isAdmin
    ? "Admin Portal"
    : isStudent
      ? fullName
        ? `${fullName}'s Portal`
        : "Student Portal"
      : "Resource Management Portal";

  function handleLogin() {
    setRole(getRole());
    setFullName(null);
    setAuthenticated(true);
    setAuthPage("login");
    setPage("assets");
  }

  function handleLogout() {
    clearToken();
    setRole(null);
    setFullName(null);
    setAuthenticated(false);
    setAuthPage("login");
    setPage("assets");
  }

  if (!authenticated) {
    if (authPage === "register") {
      return (
        <RegisterPage
          onBackToLogin={() => setAuthPage("login")}
        />
      );
    }

    return (
      <LoginPage
        onLogin={handleLogin}
        onRegister={() => setAuthPage("register")}
      />
    );
  }

  return (
    <div className="app">
      <AppHeader subtitle={portalSubtitle} />

      <nav
        className="portal-nav"
        aria-label="Main navigation"
      >
        <div className="portal-nav-inner">
          <div className="portal-nav-links">
            <button
              type="button"
              className={
                page === "assets" ? "active" : ""
              }
              aria-current={
                page === "assets" ? "page" : undefined
              }
              onClick={() => setPage("assets")}
            >
              Assets
            </button>

            {isStudent && (
              <button
                type="button"
                className={
                  page === "orders" ? "active" : ""
                }
                aria-current={
                  page === "orders" ? "page" : undefined
                }
                onClick={() => setPage("orders")}
              >
                My Orders
              </button>
            )}

            {isAdmin && (
              <>
                <button
                  type="button"
                  className={
                    page === "manage-assets"
                      ? "active"
                      : ""
                  }
                  aria-current={
                    page === "manage-assets"
                      ? "page"
                      : undefined
                  }
                  onClick={() =>
                    setPage("manage-assets")
                  }
                >
                  Manage Assets
                </button>

                <button
                  type="button"
                  className={
                    page === "admin-users"
                      ? "active"
                      : ""
                  }
                  aria-current={
                    page === "admin-users"
                      ? "page"
                      : undefined
                  }
                  onClick={() =>
                    setPage("admin-users")
                  }
                >
                  Pending Users
                </button>

                <button
                  type="button"
                  className={
                    page === "all-users"
                      ? "active"
                      : ""
                  }
                  aria-current={
                    page === "all-users"
                      ? "page"
                      : undefined
                  }
                  onClick={() =>
                    setPage("all-users")
                  }
                >
                  All Users
                </button>

                <button
                  type="button"
                  className={
                    page === "admin-orders"
                      ? "active"
                      : ""
                  }
                  aria-current={
                    page === "admin-orders"
                      ? "page"
                      : undefined
                  }
                  onClick={() =>
                    setPage("admin-orders")
                  }
                >
                  Pending Orders
                </button>

                <button
                  type="button"
                  className={
                    page === "all-orders"
                      ? "active"
                      : ""
                  }
                  aria-current={
                    page === "all-orders"
                      ? "page"
                      : undefined
                  }
                  onClick={() =>
                    setPage("all-orders")
                  }
                >
                  All Orders
                </button>
              </>
            )}
          </div>

          <button
            type="button"
            className="portal-logout"
            onClick={handleLogout}
          >
            Logout
          </button>
        </div>
      </nav>

      <main className="app-content">
        {page === "assets" && <AssetsPage />}

        {page === "orders" &&
          isStudent && (
            <MyOrdersPage />
          )}

        {page === "manage-assets" &&
          isAdmin && (
            <ManageAssetsPage />
          )}

        {page === "admin-users" &&
          isAdmin && (
            <AdminUsersPage />
          )}

        {page === "all-users" &&
          isAdmin && (
            <AllUsersPage />
          )}

        {page === "admin-orders" &&
          isAdmin && (
            <AdminOrdersPage />
          )}

        {page === "all-orders" &&
          isAdmin && (
            <AllOrdersPage />
          )}
      </main>
    </div>
  );
}

export default App;
