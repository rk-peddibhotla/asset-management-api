
import { useState } from "react";
import type { FormEvent } from "react";

import AuthLayout from "../components/AuthLayout";
import { register } from "../api/auth";

interface RegisterPageProps {
  onBackToLogin: () => void;
}

function RegisterPage({ onBackToLogin }: RegisterPageProps) {
  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [mobileNumber, setMobileNumber] = useState("");

  const [error, setError] = useState("");
  const [success, setSuccess] = useState(false);
  const [loading, setLoading] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    setError("");
    setSuccess(false);

    if (password !== confirmPassword) {
      setError("Passwords do not match.");
      return;
    }

    setLoading(true);

    try {
      await register({
        fullName: fullName.trim(),
        email: email.trim(),
        password,
        mobileNumber: mobileNumber.trim(),
      });

      setPassword("");
      setConfirmPassword("");
      setSuccess(true);
    } catch (error) {
      setError(
        error instanceof Error
          ? error.message
          : "Registration failed. Please try again."
      );
    } finally {
      setLoading(false);
    }
  }

  if (success) {
    return (
      <AuthLayout>
        <h1>Registration successful</h1>

        <p className="success-message" role="status">
          Your account has been created and is waiting for
          administrator approval.
        </p>

        <p className="auth-panel-description">
          You can sign in once your account has been approved.
        </p>

        <button
          type="button"
          className="primary-button login-button"
          onClick={onBackToLogin}
        >
          Back to sign in
        </button>
      </AuthLayout>
    );
  }

  return (
    <AuthLayout>
      <h1>Create an account</h1>

      <p className="auth-panel-description">
        Complete the form below to register.
        New accounts require administrator approval.
      </p>

      <form onSubmit={handleSubmit} className="login-form">
        <div className="form-field">
          <label htmlFor="fullName">Full name</label>

          <input
            id="fullName"
            type="text"
            placeholder="Enter your full name"
            value={fullName}
            onChange={(event) => setFullName(event.target.value)}
            autoComplete="name"
            maxLength={100}
            required
          />
        </div>

        <div className="form-field">
          <label htmlFor="registerEmail">Email address</label>

          <input
            id="registerEmail"
            type="email"
            placeholder="you@example.com"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            autoComplete="email"
            maxLength={255}
            required
          />
        </div>

        <div className="form-field">
          <label htmlFor="mobileNumber">Mobile number</label>

          <input
            id="mobileNumber"
            type="tel"
            placeholder="Enter your mobile number"
            value={mobileNumber}
            onChange={(event) => setMobileNumber(event.target.value)}
            autoComplete="tel"
            maxLength={20}
            required
          />
        </div>

        <div className="form-field">
          <label htmlFor="registerPassword">Password</label>

          <input
            id="registerPassword"
            type="password"
            placeholder="Create a password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            autoComplete="new-password"
            aria-invalid={Boolean(error)}
            aria-describedby={
              error ? "register-error" : undefined
            }
            required
          />
        </div>

        <div className="form-field">
          <label htmlFor="confirmPassword">
            Confirm password
          </label>

          <input
            id="confirmPassword"
            type="password"
            placeholder="Confirm your password"
            value={confirmPassword}
            onChange={(event) =>
              setConfirmPassword(event.target.value)
            }
            autoComplete="new-password"
            aria-invalid={Boolean(error)}
            aria-describedby={
              error ? "register-error" : undefined
            }
            required
          />
        </div>

        {error && (
          <p
            id="register-error"
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
          {loading ? "Creating account..." : "Create account"}
        </button>
      </form>

      <p className="register-footer">
        Already have an account?{" "}
        <button
          type="button"
          className="text-button"
          onClick={onBackToLogin}
          disabled={loading}
        >
          Sign in
        </button>
      </p>
    </AuthLayout>
  );
}

export default RegisterPage;
