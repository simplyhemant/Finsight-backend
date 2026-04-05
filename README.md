# Finsight — Finance Data Processing and Access Control Backend

> **Backend Developer Intern — Assignment Submission**

A robust, production-grade backend system for a financial dashboard, built with **Spring Boot 3.4.1**, **PostgreSQL 15**, and **JWT-based stateless authentication**. The system provides complete CRUD operations for financial records, role-based access control across three user tiers, and real-time aggregated dashboard analytics for enterprise data viewing.

### 🌟 Live API Documentation
Explore the complete, fully-documented API endpoints on Postman:
👉 **[Finsight Postman API Collection](https://documenter.getpostman.com/view/39898850/2sBXiqE8qq)**

---

## 📑 Table of Contents

- [Tech Stack](#-tech-stack)
- [Architecture Overview](#-architecture-overview)
- [Project Structure](#-project-structure)
- [Data Model (ER Diagram)](#-data-model)
- [Features Implemented](#-features-implemented)
- [Setup & Run Instructions](#-setup--run-instructions)
- [API Documentation](#-api-documentation)
- [Role-Based Access Control Matrix](#-role-based-access-control-matrix)
- [Sample API Responses](#-sample-api-responses)
- [Validation & Error Handling](#-validation--error-handling)
- [Assumptions & Design Decisions](#-assumptions--design-decisions)
- [Optional Enhancements Implemented](#-optional-enhancements-implemented)
- [Future Improvements](#-future-improvements)

---

## 🛠 Tech Stack

| Layer | Technology |
|-------|-----------|
| **Language** | Java 21 |
| **Framework** | Spring Boot 3.4.1 |
| **Database** | PostgreSQL 15 (Dockerized via Docker Compose) |
| **ORM** | Spring Data JPA + Hibernate |
| **Security** | Spring Security + JWT (jjwt library) |
| **Validation** | Hibernate Validator (`@Valid`, `@NotBlank`, `@NotNull`) |
| **API Docs** | SpringDoc OpenAPI 3 (Swagger UI) |
| **Build Tool** | Maven |
| **Utilities** | Lombok, SLF4J Logging |
| **Containerization** | Docker Compose (PostgreSQL + PgAdmin) |

---

## 🏗 Architecture Overview

The application follows a **layered architecture** with clear separation of concerns:

```
┌──────────────────────────────────────────────────────────────────────┐
│                        CLIENT (Postman / Frontend)                   │
└────────────────────────────────┬─────────────────────────────────────┘
                                 │ HTTP + JWT Bearer Token
┌────────────────────────────────▼─────────────────────────────────────┐
│  SECURITY LAYER                                                      │
│  ┌─────────────────┐  ┌──────────────────┐  ┌────────────────────┐  │
│  │ JwtTokenValidator│→ │ SecurityConfig   │→ │ @PreAuthorize      │  │
│  │ (OncePerRequest) │  │ (FilterChain)    │  │ (Method Security)  │  │
│  └─────────────────┘  └──────────────────┘  └────────────────────┘  │
└────────────────────────────────┬─────────────────────────────────────┘
                                 │
┌────────────────────────────────▼─────────────────────────────────────┐
│  CONTROLLER LAYER                                                    │
│  AuthController │ UserController │ FinancialRecordController         │
│  CategoryController │ DashboardController                            │
└────────────────────────────────┬─────────────────────────────────────┘
                                 │
┌────────────────────────────────▼─────────────────────────────────────┐
│  SERVICE LAYER (Business Logic + Validation)                         │
│  AuthService │ UserService │ FinancialRecordService                   │
│  CategoryService │ DashboardService                                  │
└────────────────────────────────┬─────────────────────────────────────┘
                                 │
┌────────────────────────────────▼─────────────────────────────────────┐
│  REPOSITORY LAYER (Spring Data JPA + Custom JPQL/Native Queries)     │
│  UserRepository │ FinancialRecordRepository │ CategoryRepository      │
└────────────────────────────────┬─────────────────────────────────────┘
                                 │
┌────────────────────────────────▼─────────────────────────────────────┐
│  DATABASE: PostgreSQL 15 (Docker)                                    │
│  Tables: users, financial_records, categories                        │
│  Indexes on: email, role, status, type, category_id, date, user_id   │
└──────────────────────────────────────────────────────────────────────┘
```

**Key Design Principles:**
- **Stateless Authentication** — JWT tokens; no server-side sessions.
- **Enterprise Dashboard** — Dashboard queries aggregate system-wide enterprise data, accessible according to RBAC.
- **Soft Deletion** — Financial records use a `deleted` flag instead of hard deletes to preserve audit trails.
- **DTO Pattern** — Separate Request and Response DTOs to decouple API contracts from database entities.

---

## 📁 Project Structure

```
src/main/java/simply/Finsight_backend/
├── config/                          # Application configurations
│   ├── AdminInitializer.java        # Seeds default admin on first startup
│   ├── AppConfig.java               # Password encoder + general beans
│   └── SwaggerConfig.java           # OpenAPI/Swagger configuration
├── controller/                      # REST API endpoints
│   ├── AuthController.java          # Register, Login, Profile, Password
│   ├── UserController.java          # Admin user management
│   ├── FinancialRecordController.java  # CRUD + filtering for transactions
│   ├── CategoryController.java      # Category management
│   └── DashboardController.java     # Aggregated analytics endpoints
├── dto/
│   ├── request/                     # Input DTOs with validation
│   │   ├── RegisterRequest.java
│   │   ├── LoginRequest.java
│   │   ├── CreateRecordRequest.java
│   │   ├── UpdateRecordRequest.java
│   │   ├── CreateCategoryRequest.java
│   │   └── ... (10 request DTOs)
│   └── response/                    # Output DTOs
│       ├── ApiResponse.java         # Standardized wrapper for all responses
│       ├── DashboardSummaryResponse.java
│       ├── MonthlyTrendResponse.java
│       ├── WeeklyTrendResponse.java
│       └── ... (12 response DTOs)
├── entity/                          # JPA Entities
│   ├── User.java
│   ├── FinancialRecord.java
│   └── Category.java
├── enums/                           # Enumerations
│   ├── Role.java                    # ADMIN, ANALYST, VIEWER
│   ├── TransactionType.java         # INCOME, EXPENSE
│   └── UserStatus.java              # ACTIVE, INACTIVE
├── exception/                       # Custom exceptions + global handler
│   ├── GlobalExceptionHandler.java  # Centralized @ControllerAdvice
│   ├── BusinessException.java
│   ├── ResourceNotFoundException.java
│   ├── DuplicateResourceException.java
│   ├── AuthenticationException.java
│   ├── AuthorizationException.java
│   └── ValidationException.java
├── mapper/                          # Entity ↔ DTO mappers
│   └── UserMapper.java
├── repository/                      # Spring Data JPA repositories
│   ├── UserRepository.java
│   ├── FinancialRecordRepository.java  # Custom JPQL + native queries
│   └── CategoryRepository.java
├── security/                        # JWT + Spring Security
│   ├── SecurityConfig.java          # HTTP security filter chain
│   ├── JwtTokenProvider.java        # Token generation
│   ├── JwtTokenValidator.java       # Token validation filter
│   ├── JwtConstants.java            # Secret key + expiration
│   └── CustomAccessDeniedHandler.java  # 403 response handler
└── service/                         # Business logic layer
    ├── AuthService.java
    ├── UserService.java
    ├── FinancialRecordService.java
    ├── CategoryService.java
    ├── DashboardService.java
    ├── CustomUserDetails.java       # Spring Security UserDetails impl
    └── impl/                        # Service implementations
        ├── AuthServiceImpl.java
        ├── UserServiceImpl.java
        ├── FinancialRecordServiceImpl.java
        ├── CategoryServiceImpl.java
        └── DashboardServiceImpl.java
```

---

## 📊 Data Model

```
┌─────────────────────┐       ┌──────────────────────────┐       ┌─────────────────────┐
│       users          │       │    financial_records      │       │     categories       │
├─────────────────────┤       ├──────────────────────────┤       ├─────────────────────┤
│ id (PK)             │──┐    │ id (PK)                  │    ┌──│ id (PK)             │
│ name                │  │    │ amount (DECIMAL 15,2)    │    │  │ name (UNIQUE)       │
│ email (UNIQUE, IDX) │  │    │ type (INCOME/EXPENSE)    │    │  │ type (INCOME/EXPENSE)│
│ password (hashed)   │  │    │ category_id (FK) ────────│────┘  │ active (BOOLEAN)    │
│ role (ENUM, IDX)    │  └───→│ created_by_id (FK)       │       │ created_by_id (FK)  │
│ status (ENUM, IDX)  │       │ date                     │       │ created_at          │
│ created_at          │       │ description              │       └─────────────────────┘
│ updated_at          │       │ deleted (BOOLEAN, IDX)   │
└─────────────────────┘       │ created_at               │
                              │ updated_at               │
                              └──────────────────────────┘
```

**Key Relationships:**
- `User` → `FinancialRecord` (One-to-Many): A user creates many records.
- `Category` → `FinancialRecord` (One-to-Many): Each record belongs to a category.
- `User` → `Category` (One-to-Many): Admins create categories.

**Indexing Strategy:**
Database indexes are defined on frequently queried columns (`email`, `role`, `status`, `type`, `category_id`, `date`, `deleted`, `created_by_id`) to optimize read-heavy dashboard queries.

---

## ✅ Features Implemented

### 1. User & Role Management ✔
- Secure registration with password hashing (BCrypt).
- JWT-based login returning a Bearer token.
- Three distinct roles: **ADMIN**, **ANALYST**, **VIEWER**.
- Admin can: list all users, search users, filter by role/status, update roles, activate/deactivate accounts.
- User status management (`ACTIVE` / `INACTIVE`) — inactive users are blocked from performing operations.
- Default admin account is auto-seeded on first startup (`admin@finsight.com` / `admin123`).

### 2. Financial Records Management ✔
- Full CRUD operations (Create, Read, Update, Soft-Delete).
- Each record includes: `amount` (BigDecimal), `type` (INCOME/EXPENSE), `category`, `date`, `description`.
- Advanced filtering by: `keyword`, `type`, `categoryId`, `date range` — all with pagination.
- Records are owned by the user who created them (`createdBy` relationship).
- Admin audit endpoint to view all system-wide records.

### 3. Category Management ✔
- Admin can create and toggle (activate/deactivate) categories.
- Categories are typed as either `INCOME` or `EXPENSE`.
- All users can view active categories filtered by type.

### 4. Dashboard Summary APIs ✔
- **Total income, total expenses, net balance** — system-wide enterprise reporting.
- **Category-wise totals** — grouped aggregation with record counts.
- **Monthly trends** — month-by-month income vs. expense with net balance.
- **Weekly trends** — week-by-week breakdown.
- **Recent activity** — most recent transactions (configurable limit).
- **Top expense/income categories** — ranked by total amount.
- **Date-range summary** — custom period aggregation.

### 5. Access Control ✔
- JWT-based stateless authentication via `JwtTokenValidator` filter.
- Method-level authorization with `@PreAuthorize` annotations.
- Custom `AccessDeniedHandler` for proper 403 JSON responses.

### 6. Validation & Error Handling ✔
- Input validation using Hibernate Validator (`@Valid`, `@NotBlank`, `@NotNull`, `@Positive`, `@Email`).
- Global exception handler (`@ControllerAdvice`) returning structured JSON error responses.
- Proper HTTP status codes (201, 400, 401, 403, 404, 409, 500).
- Custom exception hierarchy: `BusinessException`, `ResourceNotFoundException`, `DuplicateResourceException`, `AuthenticationException`, `AuthorizationException`, `ValidationException`.

---

## 🏁 Setup & Run Instructions

### Prerequisites
- **Docker** & **Docker Compose** (for database)
- **Java 21+**
- **Maven 3.8+**

### Step 1: Clone the Repository
```bash
git clone https://github.com/SimplyHemant/Finsight-backend.git
cd Finsight-backend
```

### Step 2: Start the Database
```bash
docker-compose up -d
```
This starts:
- **PostgreSQL 15** on port `5434` (DB: `finsight`, User: `postgres`, Password: `root123`)
- **PgAdmin** on port `5051` (Email: `admin@admin.com`, Password: `root`)

### Step 3: Build & Run the Application
```bash
mvn clean install
mvn spring-boot:run
```

The application starts at: **`http://localhost:8080`**

### Step 4: Access Swagger UI
Open in browser: **[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)**

### Default Admin Credentials
On first startup, the system auto-seeds an admin account:
| Field | Value |
|-------|-------|
| Email | `admin@finsight.com` |
| Password | `admin123` |

---

## 📡 API Documentation

### API Documentation Endpoints

- **Postman Online Documentation**: [https://documenter.getpostman.com/view/39898850/2sBXiqE8qq](https://documenter.getpostman.com/view/39898850/2sBXiqE8qq)
*(Includes request bodies, query parameters, constraints, and descriptions for all 30 routes).*

To view the Swagger interface locally, run the application and visit `http://localhost:8080/swagger-ui/index.html`.

---

### Authentication APIs

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| `POST` | `/api/auth/register` | Register a new user | Public |
| `POST` | `/api/auth/login` | Login and receive JWT token | Public |
| `GET` | `/api/auth/me` | Get current user profile | Bearer Token |
| `PUT` | `/api/auth/change-password` | Change own password | Bearer Token |
| `POST` | `/api/auth/logout` | Logout (client discards token) | Bearer Token |

### User Management APIs (Admin Only)

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| `GET` | `/api/users` | List all users (paginated, sortable) | ADMIN |
| `GET` | `/api/users/{id}` | Get user by ID | ADMIN |
| `PATCH` | `/api/users/{id}/status` | Activate/Deactivate user | ADMIN |
| `PATCH` | `/api/users/{id}/role` | Change user role | ADMIN |
| `GET` | `/api/users/status/{status}` | Filter users by status | ADMIN |
| `GET` | `/api/users/role/{role}` | Filter users by role | ADMIN |
| `GET` | `/api/users/search?keyword=` | Search by name/email | ADMIN |
| `GET` | `/api/users/profile` | Get own profile | Any |
| `PUT` | `/api/users/profile` | Update own profile | Any |

### Financial Records APIs

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| `POST` | `/api/records` | Create a new record | ADMIN |
| `GET` | `/api/records` | Get own records (paginated) | ALL |
| `GET` | `/api/records/{id}` | Get record by ID | ADMIN, ANALYST |
| `PATCH` | `/api/records/{id}` | Update a record | ADMIN |
| `DELETE` | `/api/records/{id}` | Soft delete a record | ADMIN |
| `GET` | `/api/records/filter` | Advanced filtering | ADMIN, ANALYST |
| `GET` | `/api/records/admin/all` | System-wide audit log | ADMIN |

### Category APIs

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| `POST` | `/api/categories` | Create a category | ADMIN |
| `GET` | `/api/categories/{id}` | Get category by ID | Any |
| `PATCH` | `/api/categories/{id}/toggle` | Activate/Deactivate | ADMIN |
| `GET` | `/api/categories/all` | List all categories | ADMIN |
| `GET` | `/api/categories/active` | List active categories | Any |
| `GET` | `/api/categories/type/{type}` | Filter by INCOME/EXPENSE | Any |

### Dashboard & Analytics APIs

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| `GET` | `/api/dashboard/summary` | Total income, expense, net balance | ALL |
| `GET` | `/api/dashboard/categories` | Category-wise totals | ALL |
| `GET` | `/api/dashboard/categories/{type}` | Category totals by type | ALL |
| `GET` | `/api/dashboard/trends/monthly?year=` | Monthly trends | ALL |
| `GET` | `/api/dashboard/trends/weekly?year=` | Weekly trends | ALL |
| `GET` | `/api/dashboard/recent?limit=` | Recent activity | ALL |
| `GET` | `/api/dashboard/top-categories/expense?limit=` | Top expense categories | ALL |
| `GET` | `/api/dashboard/top-categories/income?limit=` | Top income categories | ALL |
| `GET` | `/api/dashboard/summary/date-range?startDate=&endDate=` | Custom date range summary | ALL |

---

## 🔐 Role-Based Access Control Matrix

| Action | VIEWER | ANALYST | ADMIN |
|--------|:------:|:-------:|:-----:|
| Register / Login | ✅ | ✅ | ✅ |
| View own profile | ✅ | ✅ | ✅ |
| View dashboard summaries | ✅ | ✅ | ✅ |
| View trends & analytics | ✅ | ✅ | ✅ |
| View own records | ✅ | ✅ | ✅ |
| View record by ID | ❌ | ✅ | ✅ |
| Filter/search records | ❌ | ✅ | ✅ |
| Create records | ❌ | ❌ | ✅ |
| Update records | ❌ | ❌ | ✅ |
| Delete records (soft) | ❌ | ❌ | ✅ |
| Manage categories | ❌ | ❌ | ✅ |
| Manage users | ❌ | ❌ | ✅ |
| View all system records | ❌ | ❌ | ✅ |

---

## 📋 Sample API Responses

### Successful Response (`GET /api/dashboard/summary`)
```json
{
  "success": true,
  "status": 200,
  "message": "Dashboard summary retrieved successfully",
  "data": {
    "totalIncome": 75000.00,
    "totalExpense": 32500.00,
    "netBalance": 42500.00,
    "totalRecords": 24,
    "totalIncomeRecords": 10,
    "totalExpenseRecords": 14,
    "lastUpdated": "2026-04-05T01:20:00"
  }
}
```

### Validation Error (`POST /api/records` — missing fields)
```json
{
  "success": false,
  "status": 400,
  "message": "Validation failed",
  "errors": {
    "amount": "Amount is required and must be positive",
    "type": "Transaction type is required",
    "categoryId": "Category is required"
  }
}
```

### Unauthorized (`GET /api/dashboard/summary` — no token)
```json
{
  "success": false,
  "status": 401,
  "message": "Authentication required. Please provide a valid JWT token."
}
```

### Forbidden (`POST /api/records` — VIEWER trying to create)
```json
{
  "success": false,
  "status": 403,
  "message": "Access Denied: You do not have permission to perform this action."
}
```

---

## ⚙ Validation & Error Handling

### Input Validation
All request DTOs are validated using Jakarta Bean Validation:
- `@NotBlank` — for required string fields (name, email)
- `@NotNull` — for required references (categoryId, type)
- `@Positive` — for monetary amounts (must be > 0)
- `@Email` — for email format validation

### Global Exception Handler
A centralized `@ControllerAdvice` (`GlobalExceptionHandler`) catches all exceptions and returns structured JSON responses with appropriate HTTP status codes:

| Exception | HTTP Status | When |
|-----------|:-----------:|------|
| `ValidationException` | 400 | Invalid input data |
| `AuthenticationException` | 401 | Missing/invalid JWT token |
| `AuthorizationException` / `AccessDenied` | 403 | Insufficient permissions |
| `ResourceNotFoundException` | 404 | Entity not found by ID |
| `DuplicateResourceException` | 409 | Duplicate email/category |
| `BusinessException` | 400 | Business rule violation |
| `Exception` (fallback) | 500 | Unexpected server error |

---

## 📝 Assumptions & Design Decisions

1. **User Registration Role**: New users register as `VIEWER` by default. Only an Admin can promote them to `ANALYST` or `ADMIN`.
2. **Soft Deletion**: Financial records are soft-deleted (`deleted = true`) instead of permanently removed. All queries explicitly filter out deleted records using `deleted = false`.
3. **Enterprise Level Data**: Dashboard aggregations (totals, trends, category breakdowns) represent system-wide data, acting as a global company dashboard for viewers and analysts.
4. **BigDecimal for Currency**: All monetary values use `BigDecimal(15,2)` to avoid floating-point precision errors.
5. **Category-Type Binding**: Each category is typed as either `INCOME` or `EXPENSE`. Records must use a category that matches their transaction type.
6. **Admin Seeding**: A default admin account (`admin@finsight.com` / `admin123`) is auto-created on first startup via `CommandLineRunner`.
7. **Stateless JWT**: No server-side session storage. Tokens are validated on every request via a custom `OncePerRequestFilter`. Logout is handled client-side by discarding the token.
8. **Pagination**: List endpoints support pagination with configurable `page`, `size`, and `sort` parameters to handle large datasets efficiently.
9. **Environment Variables**: Database connection uses environment variables with sensible defaults, making the app deployable across different environments without code changes.

---

## 🌟 Optional Enhancements Implemented

| Enhancement | Status | Details |
|-------------|:------:|---------|
| JWT Authentication | ✅ | Full token-based auth with custom filters |
| Pagination | ✅ | All list endpoints support paginated responses |
| Soft Delete | ✅ | Records use a `deleted` flag for audit preservation |
| Search Support | ✅ | User search by keyword; record filtering by multiple criteria |
| API Documentation | ✅ | SpringDoc OpenAPI 3 / Swagger UI |
| Structured Error Responses | ✅ | Global exception handler with typed exceptions |
| Database Indexing | ✅ | Indexes on frequently queried columns |
| Docker Compose | ✅ | One-command database + PgAdmin setup |
| Admin Auto-Seeding | ✅ | Default admin created on first run |
| Global Dashboard | ✅ | Dashboard aggregates system-wide records |

---

## 🔮 Future Improvements

- **Caching (Redis)**: Cache frequently accessed dashboard summaries to reduce database load.
- **Unit & Integration Tests**: Add JUnit 5 + Mockito test coverage for services and controllers.
- **Rate Limiting**: Implement API rate limiting using Bucket4j to prevent abuse.
- **Refresh Tokens**: Add a refresh token mechanism for seamless token renewal.
- **Audit Logging**: Track all CRUD operations with timestamps and actor information.
- **Export**: Allow exporting financial records to CSV/PDF.

---

## 👨‍💻 Author

**Hemant**
*Backend Developer Intern*

---

> **Note**: This project was built as an assignment submission for the Backend Developer Intern position. The focus was on clean architecture, correct business logic, and demonstrating sound backend engineering practices rather than building a production-ready system.
