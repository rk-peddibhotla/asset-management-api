
import { useState } from "react";
import type { FormEvent } from "react";

import AuthLayout from "../components/AuthLayout";

import { login } from "../api/auth";
import { saveRole, saveToken } from "../auth/authStorage";

interface LoginPageProps {
  onLogin: () => void;
  onRegister: () => void;
}

function LoginPage({ onLogin, onRegister }: LoginPageProps) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    setError("");
    setLoading(true);

    try {
      const response = await login({
        email,
        password,
      });

      saveToken(response.token);
      saveRole(response.role);

      onLogin();
    } catch (error) {
      setError(
        error instanceof Error
          ? error.message
          : "Unable to sign in. Please try again."
      );
    } finally {
      setLoading(false);
    }
  }

  return (
    <AuthLayout>
      <h1>Sign in</h1>

      <p className="auth-panel-description">
        Enter your account credentials to continue.
      </p>

      <form onSubmit={handleSubmit} className="login-form">
        <div className="form-field">
          <label htmlFor="email">Email address</label>

          <input
            id="email"
            type="email"
            placeholder="you@example.com"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            autoComplete="email"
            aria-invalid={Boolean(error)}
            aria-describedby={
              error ? "login-error" : undefined
            }
            required
          />
        </div>

        <div className="form-field">
          <label htmlFor="password">Password</label>

          <input
            id="password"
            type="password"
            placeholder="Enter your password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            autoComplete="current-password"
            aria-invalid={Boolean(error)}
            aria-describedby={
              error ? "login-error" : undefined
            }
            required
          />
        </div>

        {error && (
          <p
            id="login-error"
            className="error-message"
            role="alert"
          >
            {error}
          </p>
        )}

        <button
          type="submit"
          className="primary-button login-button"
          disabled={loading}
        >
          {loading ? "Signing in..." : "Sign in"}
        </button>
      </form>

      <p className="register-footer">
        Don't have an account?{" "}
        <button
          type="button"
          className="text-button"
          onClick={onRegister}
          disabled={loading}
        >
          Register
        </button>
      </p>
    </AuthLayout>
  );
}

export default LoginPage;
