# Asset Management System

A full-stack asset lending application for managing users, assets, categories, and borrowing workflows.

The project is built with **Java, Spring Boot, PostgreSQL, React, and TypeScript**. It demonstrates backend engineering fundamentals including REST API design, JWT authentication, role-based authorization, validation, transactional business logic, database migrations, relational data modeling, and automated integration testing.

## Features

### Authentication and Users

* Student registration
* Password hashing with BCrypt
* JWT-based authentication
* Role-based authorization
* Admin approval of new accounts
* Admin account blocking
* Protected user and administrative endpoints

### Asset Management

* Asset category management
* Asset creation and retrieval
* Category and sub-category relationships
* Database-level validation and constraints
* Admin-only asset and category creation

### Asset Lending

* Borrowing requests
* Maximum of 3 active orders per user
* Admin approval and rejection
* 10-day loan period
* Asset returns
* Automatic overdue fine calculation
* User ownership checks
* Order status management

### Engineering

* Layered Controller → Service → Repository architecture
* DTOs at the REST API boundary
* Request validation
* Global exception handling
* PostgreSQL persistence
* Flyway database migrations
* Transactional service methods
* Lazy JPA relationships
* Targeted `JOIN FETCH` queries
* Database constraints and indexes
* Automated Spring integration tests

---

## Tech Stack

### Backend

* Java 21
* Spring Boot 4
* Spring Web MVC
* Spring Security
* JWT / OAuth2 Resource Server
* Spring Data JPA
* Hibernate
* PostgreSQL
* Flyway
* Maven

### Frontend

* React
* TypeScript
* Vite

### Testing

* JUnit
* Spring Boot Test
* MockMvc
* Spring Security Test

---

## Architecture

The backend follows a simple layered architecture:

```text
HTTP Request
     ↓
Controller
     ↓
Service
     ↓
Repository
     ↓
PostgreSQL
```

The main responsibilities are separated as follows:

* **Controller** — handles HTTP requests and API responses
* **Service** — contains application and business logic
* **Repository** — handles database access
* **Entity** — represents persisted database data
* **DTO** — defines data exchanged through the API
* **Security configuration** — handles JWT authentication and authorization

The project intentionally avoids unnecessary distributed-system complexity. The goal is to demonstrate solid backend engineering fundamentals in a maintainable application.

---

## Project Structure

```text
asset-management-api/
│
├── src/
│   ├── main/
│   │   ├── java/com/ravikanth/asset_management/
│   │   │
│   │   ├── auth/
│   │   │   ├── AuthController
│   │   │   ├── AuthService
│   │   │   ├── JwtService
│   │   │   └── request/response DTOs
│   │   │
│   │   ├── user/
│   │   │   ├── User
│   │   │   ├── UserRepository
│   │   │   ├── UserService
│   │   │   ├── AdminUserController
│   │   │   └── UserController
│   │   │
│   │   ├── asset/
│   │   │   ├── Asset
│   │   │   ├── AssetCategory
│   │   │   ├── AssetRepository
│   │   │   ├── AssetCategoryRepository
│   │   │   ├── AssetService
│   │   │   ├── AssetCategoryService
│   │   │   └── controllers/DTOs
│   │   │
│   │   ├── order/
│   │   │   ├── Order
│   │   │   ├── OrderStatus
│   │   │   ├── OrderRepository
│   │   │   ├── OrderService
│   │   │   ├── OrderController
│   │   │   └── DTOs
│   │   │
│   │   ├── config/
│   │   │   ├── SecurityConfig
│   │   │   ├── JwtConfig
│   │   │   └── PasswordConfig
│   │   │
│   │   └── common/
│   │       └── GlobalExceptionHandler
│   │
│   ├── main/resources/
│   │   ├── db/migration/
│   │   └── application.yml
│   │
│   └── test/
│       └── java/
│
├── frontend/
│   ├── src/
│   ├── package.json
│   └── vite.config.ts
│
├── pom.xml
└── README.md
```

---

## Authentication

Users register through:

```text
POST /api/auth/register
```

New student accounts start with:

```text
UNAPPROVED
```

An administrator can approve the account.

Once approved, the user can authenticate through:

```text
POST /api/auth/login
```

The API returns a signed JWT containing the user's identity and role.

Protected requests use:

```text
Authorization: Bearer <token>
```

Administrative endpoints require the `ADMIN` role.

Passwords are never stored as plaintext. They are encoded using BCrypt before being persisted.

---

## Order Lifecycle

Orders follow a simple workflow:

```text
PENDING
   │
   ├──→ ACTIVE ──→ RETURNED
   │
   └──→ REJECTED
```

When an administrator approves a pending order:

1. The order becomes `ACTIVE`
2. The approval timestamp is recorded
3. A 10-day due date is assigned

When an active order is returned after the due date, the API calculates an overdue fine.

Current fine:

```text
₹50 per overdue day
```

A user can only return an order that belongs to them.

---

## API Endpoints

### Authentication

| Method | Endpoint             | Description                    |
| ------ | -------------------- | ------------------------------ |
| POST   | `/api/auth/register` | Register a student             |
| POST   | `/api/auth/login`    | Authenticate and receive a JWT |

### Users — Admin

| Method | Endpoint                            | Description        |
| ------ | ----------------------------------- | ------------------ |
| GET    | `/api/admin/users/pending`          | View pending users |
| PATCH  | `/api/admin/users/{userId}/approve` | Approve a user     |
| PATCH  | `/api/admin/users/{userId}/block`   | Block a user       |

### Users

| Method | Endpoint        | Description                           |
| ------ | --------------- | ------------------------------------- |
| GET    | `/api/users/me` | Get the authenticated user's identity |

### Categories

| Method | Endpoint          | Description       |
| ------ | ----------------- | ----------------- |
| POST   | `/api/categories` | Create a category |
| GET    | `/api/categories` | List categories   |

Creating categories requires the `ADMIN` role.

### Assets

| Method | Endpoint      | Description     |
| ------ | ------------- | --------------- |
| POST   | `/api/assets` | Create an asset |
| GET    | `/api/assets` | List assets     |

Creating assets requires the `ADMIN` role.

### Orders

| Method | Endpoint                       | Description                          |
| ------ | ------------------------------ | ------------------------------------ |
| POST   | `/api/orders`                  | Create a borrowing request           |
| GET    | `/api/orders/my`               | View the authenticated user's orders |
| PATCH  | `/api/orders/{orderId}/return` | Return an active order               |

### Orders — Admin

| Method | Endpoint                              | Description         |
| ------ | ------------------------------------- | ------------------- |
| GET    | `/api/admin/orders/pending`           | View pending orders |
| PATCH  | `/api/admin/orders/{orderId}/approve` | Approve an order    |
| PATCH  | `/api/admin/orders/{orderId}/reject`  | Reject an order     |

---

## Database

The application uses PostgreSQL.

Main tables:

```text
users
asset_categories
assets
orders
flyway_schema_history
```

Relationships:

```text
User
  │
  └── 1-to-many ── Order

Asset
  │
  └── 1-to-many ── Order

AssetCategory
  │
  └── 1-to-many ── Asset
```

Database schema changes are managed through Flyway migrations.

The schema also includes:

* Foreign-key constraints
* Unique constraints
* Check constraints
* Indexes for commonly queried columns

---

## Local Setup

### Prerequisites

Install:

* Java 21
* PostgreSQL
* Git
* Node.js 22+ for the frontend

### Create the Database

Create a PostgreSQL database:

```sql
CREATE DATABASE asset_management;
```

The application uses Flyway to create and validate the required tables when it starts.

### Configuration

The backend supports environment variables for database and JWT configuration.

Example:

```text
DB_URL=jdbc:postgresql://localhost:5432/asset_management
DB_USERNAME=postgres
DB_PASSWORD=<your-password>
JWT_SECRET=<your-development-secret>
SERVER_PORT=8080
```

For local development, the application also provides development defaults through `application.yml`.

For production deployments, provide the configuration through environment variables rather than committing credentials or secrets to source control.

---

## Run the Backend

From the project root:

### Windows

```powershell
.\mvnw.cmd spring-boot:run
```

The API runs on:

```text
http://localhost:8080
```

---

## Run the Frontend

From the `frontend` directory:

```powershell
npm install
npm run dev
```

The frontend development server runs on:

```text
http://localhost:5173
```

The frontend communicates with the Spring Boot REST API using JWT authentication.

---

## Run Tests

From the project root:

```powershell
.\mvnw.cmd clean test
```

The test suite covers:

* User registration
* Password hashing
* Login
* JWT authentication
* Account approval
* Account blocking
* Role-based authorization
* Asset creation
* Asset retrieval
* Category creation
* Category retrieval
* Duplicate category handling
* Order creation
* Order approval
* Order rejection
* Order ownership
* Order returns
* Overdue fine calculation
* Authentication failures
* Authorization failures
* Database persistence

The complete backend test suite currently passes.

---

## Example Workflow

A typical lending workflow is:

```text
1. Student registers
          ↓
2. Account starts as UNAPPROVED
          ↓
3. Admin approves account
          ↓
4. Student logs in
          ↓
5. Student creates borrowing request
          ↓
6. Order starts as PENDING
          ↓
7. Admin approves the order
          ↓
8. Order becomes ACTIVE
          ↓
9. Student returns the asset
          ↓
10. Order becomes RETURNED
          ↓
11. Overdue fine is calculated when applicable
```

---

## Engineering Considerations

The project intentionally keeps the architecture simple while addressing common backend engineering concerns.

### Authentication and Authorization

* Authentication identity is taken from the JWT rather than trusted from request parameters.
* Administrative operations are protected using role-based authorization.
* Passwords are stored as BCrypt hashes.
* JWT authentication is stateless.

### Persistence

* JPA relationships use lazy loading by default.
* `open-in-view` is disabled.
* Targeted `JOIN FETCH` queries load related entities when required by API responses.
* Database constraints enforce important data integrity rules.
* Indexes support frequently queried relationships and statuses.

### API Design

* DTOs are used at the API boundary.
* Persistence entities are not directly exposed as REST responses.
* Request validation is handled using Jakarta Validation.
* Common application errors are handled through a global exception handler.

### Business Logic

* Business operations are implemented in transactional service methods.
* Order ownership is checked before returns.
* Active-order limits are enforced.
* Loan due dates and overdue fines are calculated by the backend.

### Database Versioning

* Database schema changes are version-controlled through Flyway migrations.
* Hibernate uses `ddl-auto: validate`, allowing the application to verify that the entity model matches the migration-managed schema.

### Testing

The project uses Spring integration tests with MockMvc to verify API behavior, persistence, authentication, authorization, and important business rules together.

---

## Frontend

A React and TypeScript frontend provides the user-facing client for the API.

Current functionality includes:

* Student login
* Asset browsing
* Borrowing requests
* My Orders
* Asset returns
* Fine display
* Admin login
* Pending user management
* User approval/blocking
* Pending order management
* Order approval/rejection
* Logout

The frontend is intentionally kept separate from the Spring Boot backend so the REST API can also be consumed by other clients.

---

## Project Status

**Backend:** Complete and tested

**Frontend:** Implemented and tested

**Database:** PostgreSQL + Flyway

**Authentication:** JWT + RBAC

**Testing:** Automated integration/security tests passing
