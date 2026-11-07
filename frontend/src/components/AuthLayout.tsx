
import type { ReactNode } from "react";
import AppHeader from "./AppHeader";

interface AuthLayoutProps {
  children: ReactNode;
}

function AuthLayout({ children }: AuthLayoutProps) {
  return (
    <div className="auth-page">
      <AppHeader />

      <main className="auth-main">
        <section className="auth-panel">
          {children}
        </section>
      </main>

      <footer className="auth-footer">
        <p>
          Resource Management Portal
          <span aria-hidden="true"> · </span>
          User and administrator access
        </p>
      </footer>
    </div>
  );
}

export default AuthLayout;
