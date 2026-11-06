# Asset Management System

A full-stack resource management and borrowing application built with **Java, Spring Boot, PostgreSQL, React, and TypeScript**. The application provides account management, resource cataloguing, borrowing workflows, configurable organization policies, and administrative controls.

## Features

### Authentication and account management

- User registration and login.
- JWT-based stateless authentication.
- Password hashing with BCrypt.
- Role-based access control for users and administrators.
- Configurable account approval.
- Administrator controls for reviewing, approving, and blocking accounts.

### Resource management

- Create and browse resource categories.
- Create and browse resources.
- Associate resources with categories.
- Manage resource availability.
- Prevent resources with active borrowings from being marked unavailable.

### Borrowing management

- Submit borrowing requests.
- Support configurable borrowing approval.
- Approve or reject pending requests as an administrator.
- View personal borrowing records.
- View and manage borrowing requests as an administrator.
- Return borrowed resources.
- Calculate late fees using overdue days and the applicable daily fee.

### Organization settings

Administrators can configure:

- Organization name.
- Resource label.
- Maximum active borrowings per user.
- Borrowing duration in days.
- Daily late fee.
- Whether borrowing approval is required.
- Whether account approval is required.
- Whether email notifications are enabled.

Settings are validated by the backend before being updated.

### Email notifications

- Email notifications for supported account and borrowing workflows.
- Mailpit integration for local email testing.
- Email notification behavior can be configured through organization settings.

## Technology stack

| Area | Technologies |
|---|---|
| Backend | Java 21, Spring Boot 4 |
| API | Spring Web, REST |
| Security | Spring Security, JWT, BCrypt |
| Persistence | Spring Data JPA, Hibernate |
| Database | PostgreSQL 18 |
| Database migrations | Flyway |
| Frontend | React, TypeScript |
| Frontend tooling | Vite, npm |
| Web server | Nginx |
| Email testing | Mailpit |
| Containerization | Docker, Docker Compose |
| Testing | JUnit, Spring Boot Test, MockMvc, Mockito |

## Architecture

The application is organized into a frontend, backend API, relational database, and email testing service.

```text
Browser
   |
   v
React + TypeScript
   |
   v
Nginx
   |
   |---- /       --> React application
   |
   |---- /api/*  --> Spring Boot backend
                         |
              +----------+----------+
              |          |          |
              v          v          v
          PostgreSQL   Security   Mailpit
                       (JWT)
```

In the Docker setup, Nginx serves the frontend and forwards API requests to the backend. The backend communicates with PostgreSQL and Mailpit through the Docker network.

## Project structure

```text
asset-management-api/
|
+-- src/
|   +-- main/
|   |   +-- java/com/ravikanth/asset_management/
|   |   |   +-- auth/
|   |   |   +-- borrowing/
|   |   |   +-- common/
|   |   |   +-- config/
|   |   |   +-- organization/
|   |   |   +-- resource/
|   |   |   +-- security/
|   |   |   +-- user/
|   |   |
|   |   +-- resources/
|   |       +-- db/migration/
|   |       +-- application.yaml
|   |
|   +-- test/
|       +-- java/com/ravikanth/asset_management/
|       +-- resources/
|
+-- frontend/
|   +-- src/
|   |   +-- api/
|   |   +-- assets/
|   |   +-- auth/
|   |   +-- components/
|   |   +-- pages/
|   +-- Dockerfile
|   +-- nginx.conf
|   +-- package.json
|
+-- Dockerfile
+-- docker-compose.yml
+-- .dockerignore
+-- .env.example
+-- .gitignore
+-- mvnw
+-- mvnw.cmd
+-- pom.xml
+-- README.md
```

## Getting started with Docker

### Prerequisites

Install:

- Docker Desktop with Docker Compose.
- Git.

### 1. Clone the repository

```bash
git clone https://github.com/rk-peddibhotla/asset-management-api.git
cd asset-management-api
```

### 2. Configure environment variables

Copy the example environment file.

**PowerShell**

```powershell
Copy-Item .env.example .env
```

Update `.env` with your local database credentials and a sufficiently long, randomly generated JWT secret.

Example:

```dotenv
DB_USERNAME=postgres
DB_PASSWORD=your_database_password
JWT_SECRET=replace_with_a_long_random_secret
```

The other variables in `.env.example` provide the local application and email configuration used outside the Docker Compose overrides.

Do not commit `.env` or put real credentials in `.env.example`.

### 3. Build and start the application

```bash
docker compose up --build
```

Docker Compose starts:

- PostgreSQL
- Mailpit
- Spring Boot backend
- React frontend served through Nginx

The backend waits for PostgreSQL's health check before starting.

### 4. Access the application

| Service | URL |
|---|---|
| Frontend | http://localhost:5173 |
| Backend API | http://localhost:8080 |
| Mailpit inbox | http://localhost:8025 |

Mailpit captures outgoing application emails locally. No external email account is required for the Docker Compose setup.

### 5. Stop the application

```bash
docker compose down
```

The PostgreSQL data volume is retained when the containers are stopped this way.

To remove the containers and database volume:

```bash
docker compose down -v
```

**Warning:** Removing the volume deletes the PostgreSQL data stored by this Compose project.

## Running locally without Docker

For local development outside Docker, install:

- Java 21
- PostgreSQL 18
- Node.js 22
- npm

Create the PostgreSQL database and configure the backend environment variables using the settings in `.env.example`.

Start the backend from the project root.

**PowerShell**

```powershell
.\mvnw.cmd spring-boot:run
```

Start the frontend from the `frontend` directory:

```powershell
npm ci
npm run dev
```

Vite is configured to proxy `/api` requests to `http://localhost:8080` during local development. Ensure the backend is running before using the frontend.

## API reference

All endpoints use the `/api` base path.

### Authentication

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Register an account |
| POST | `/api/auth/login` | Authenticate and obtain a JWT |

### Users

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/users/me` | Retrieve the authenticated user's details |

### Resources and categories

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/resource-categories` | Create a resource category |
| GET | `/api/resource-categories` | List resource categories |
| POST | `/api/resources` | Create a resource |
| GET | `/api/resources` | List resources |
| PATCH | `/api/resources/{id}/availability` | Update resource availability |

### Borrowings

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/borrowings` | Submit a borrowing request |
| GET | `/api/borrowings/my` | List the current user's borrowings |
| PATCH | `/api/borrowings/{borrowingId}/return` | Return a borrowed resource |

### Administrator borrowing operations

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/admin/borrowings/pending` | List pending borrowing requests |
| GET | `/api/admin/borrowings` | List borrowing records |
| PATCH | `/api/admin/borrowings/{borrowingId}/approve` | Approve a pending request |
| PATCH | `/api/admin/borrowings/{borrowingId}/reject` | Reject a pending request |

### Administrator user operations

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/admin/users` | List users |
| GET | `/api/admin/users/pending` | List accounts awaiting approval |
| PATCH | `/api/admin/users/{userId}/approve` | Approve a user account |
| PATCH | `/api/admin/users/{userId}/block` | Block a user account |

### Organization settings

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/organization-settings` | Retrieve organization settings |
| PUT | `/api/organization-settings` | Update organization settings |

### Administrator dashboard

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/admin/dashboard` | Retrieve dashboard information |

Protected endpoints require authentication. Administrator operations require the appropriate administrator role.

## Business rules

The application uses organization settings to control borrowing behavior rather than relying on fixed borrowing limits and fees.

### Borrowing workflow

1. An authenticated user submits a borrowing request.
2. The backend verifies that the user's account is active and the resource is available.
3. If borrowing approval is required, the request remains pending.
4. Otherwise, the borrowing is activated immediately, subject to the configured borrowing limit.
5. An administrator can approve or reject pending requests.
6. Approved borrowings receive a due date based on the borrowing duration.
7. The user can return an active borrowing.
8. Late fees are calculated using the applicable daily fee and overdue calendar days.

### Borrowing policy snapshots

The daily late fee and borrowing duration are captured when a borrowing request is created.

This preserves the original policy applicable to that request, even if an administrator changes organization settings before the request is approved or returned.

Pending requests do not consume an active borrowing slot. The active borrowing limit is checked when a borrowing is activated.

## Database migrations

Flyway manages database schema changes.

Migration files are located at:

```text
src/main/resources/db/migration/
```

The application uses versioned SQL migrations to evolve the database schema.

Hibernate is configured to validate the schema rather than automatically modify it.

## Testing

The backend test suite uses JUnit, Spring Boot Test, MockMvc, and Mockito.

Run the backend tests from the project root.

**PowerShell**

```powershell
.\mvnw.cmd test
```

The test configuration connects to a separate PostgreSQL database. Ensure this database is available before running the suite. Never configure tests to use your development or production database.

The complete backend test suite has passed **63 tests**, with zero failures, errors, or skipped tests in the latest verified run.

The frontend can be checked and built from the `frontend` directory:

```bash
npm ci
npm run lint
npm run build
```

## Security and configuration

- Passwords are hashed using BCrypt.
- Authentication uses JWT tokens.
- Backend authorization is enforced through Spring Security.
- Database credentials and JWT secrets are supplied through environment variables.
- `.env` is excluded from version control.
- Docker uses a separate runtime image for the backend and frontend.
- PostgreSQL data is stored in a named Docker volume.
- Mailpit is used for local email testing.

## Future improvements

Potential areas for further development include:

- Additional integration and frontend tests.
- More comprehensive API documentation.
- Production deployment and monitoring.
- Improved observability and application logging.
- Additional administrative reporting and audit capabilities.

## Author

**Ravi Kanth Peddibhotla**

GitHub: https://github.com/rk-peddibhotla