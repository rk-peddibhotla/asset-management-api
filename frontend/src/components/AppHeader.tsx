
import type { ReactNode } from "react";

interface AppHeaderProps {
  children?: ReactNode;
  subtitle?: string;
}

function AppHeader({
  children,
  subtitle = "Resource Management Portal",
}: AppHeaderProps) {
  return (
    <header className="app-header">
      <div className="app-brand">
        <span className="app-brand-title">
          Asset Management System
        </span>

        <span className="app-brand-subtitle">
          {subtitle}
        </span>
      </div>

      {children && (
        <nav
          className="app-nav"
          aria-label="Main navigation"
        >
          {children}
        </nav>
      )}
    </header>
  );
}

export default AppHeader;
