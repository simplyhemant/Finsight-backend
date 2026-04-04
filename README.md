# Finance Data Processing and Access Control Backend

## 📌 Project Overview
Finsight-backend is a robust and scalable backend system for a finance dashboard designed to handle financial data processing, access control, and comprehensive summary-level analytics. Built with Spring Boot and PostgreSQL, it provides a well-structured RESTful API to manage users, financial records (income/expenses), and role-based access management.

## 🚀 Features

### 1. User & Role Management
- Secure user registration and authentication using **JWT (JSON Web Tokens)**.
- Role-based Access Control (RBAC) with three distinct roles:
  - **ADMIN**: Full access to manage users, categories, and view all system records.
  - **ANALYST**: Read access to records, insights, and dashboard summaries.
  - **VIEWER**: Limited read-only access to dashboard data.
- User status management (Active / Inactive) protecting financial operations.

### 2. Financial Records Management
- Supports tracking of `Income` and `Expense` transaction types.
- Complete CRUD operations (Create, Read, Update, Delete) with **soft deletion** support.
- Advanced filtering capabilities by `date range`, `category`, `type`, and `keyword`.

### 3. Dashboard Summary APIs
- Real-time aggregated financial data:
  - Total income, total expenses, and net balance calculation.
  - Category-wise expense and income totals.
  - Weekly and Monthly trend analysis.
  - Recent activity tracking.

### 4. Advanced Security & Access Control
- Stateless authentication mechanism via JWT.
- Spring Security method-level authorization (`@PreAuthorize`).
- Customized exception handling for Authentication (401) and Access Denied (403) scenarios.

### 5. Validation & Error Handling
- Robust input validation using Hibernate Validator (`@Valid`).
- Global customized exception handling ensuring structured API error responses.

---

## 🛠️ Tech Stack
- **Framework**: Java 21, Spring Boot 3.x
- **Database**: PostgreSQL
- **ORM / Persistence**: Spring Data JPA, Hibernate
- **Security**: Spring Security, JWT (jjwt)
- **Tooling**: Maven, Lombok

---

## 🏗️ Architecture Overview
The project follows a standard layered architecture applying clean code and SOLID principles:
- **Controller Layer (`simply.Finsight_backend.controller`)**: Manages HTTP requests, input validation, and routes traffic. Restricts access based on user roles.
- **Service Layer (`simply.Finsight_backend.service.impl`)**: Encapsulates core business logic, validations (e.g., active user checks, category-type matching), and delegates DB operations.
- **Data Access Layer (`simply.Finsight_backend.repository`)**: Spring Data JPA repositories handling complex native queries and data retrieval.
- **Security (`simply.Finsight_backend.security`)**: Intercepts requests ensuring the invoker is authenticated (via custom JWT Filters) and authorized.

---

## 🏁 Setup & Run Instructions

### Prerequisites
- **Java 21+**
- **Maven**
- **PostgreSQL 14+**

### Step 1: Database Setup
1. Open PostgreSQL and create a database named `finsight`:
   ```sql
   CREATE DATABASE finsight;
   ```
2. Update the `src/main/resources/application.properties` with your credentials:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/finsight
   spring.datasource.username=postgres
   spring.datasource.password=your_password
   ```

### Step 2: Build & Run
1. Navigate to the project root directory.
2. Build the application using Maven:
   ```bash
   mvn clean install
   ```
3. Run the Spring Boot application:
   ```bash
   mvn spring-boot:run
   ```
The application will start on `http://localhost:8080`.

---

## 📡 Core API Endpoints

### Authentication
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/api/auth/register` | Register a new user | Public |
| POST | `/api/auth/login` | Login and receive JWT token | Public |

### Financial Records
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/api/records` | Create a new financial record | ADMIN |
| GET | `/api/records` | Get current user's records | ADMIN, ANALYST, VIEWER |
| GET | `/api/records/filter` | Advance filtering of records | ADMIN, ANALYST |
| PATCH | `/api/records/{id}` | Update an existing record | ADMIN |
| DELETE | `/api/records/{id}` | Soft delete a record | ADMIN |
| GET | `/api/records/admin/all`| View all system records | ADMIN |

### Dashboard Summaries
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/dashboard/summary` | General totals overview | ALL ROLES |
| GET | `/api/dashboard/categories` | Totals grouped by Category | ALL ROLES |
| GET | `/api/dashboard/trends/monthly`| Monthly income/expense trends| ALL ROLES |

### Sample Response (`GET /api/dashboard/summary`)
```json
{
  "success": true,
  "status": 200,
  "message": "Dashboard summary retrieved successfully",
  "data": {
    "totalIncome": 5400.00,
    "totalExpense": 1200.00,
    "netBalance": 4200.00,
    "totalRecords": 15,
    "totalIncomeRecords": 5,
    "totalExpenseRecords": 10,
    "lastUpdated": "2026-04-04T12:00:00"
  }
}
```

---

## 🛡️ Security Details
- **Stateless Sessions**: The server does not keep session state. Every request requires an `Authorization: Bearer <token>` header.
- **Role Enforcement at Endpoint Level**: Admin tasks cannot be executed by Analysts or Viewers, strictly enforced by Spring Security's `@PreAuthorize`.
- **Soft Deletes**: Preventing actual data loss for auditing purposes. All read/query operations explicitly filter out deleted records.

---

## 🔮 Future Improvements
1. **Caching (Redis)**: Implement caching layer for the dashboard summary endpoints since financial aggregations can be heavy on the database.
2. **API Documentation**: Integrate Swagger / OpenAPI for an interactive UI mapping of all endpoints.
3. **Automated Testing**: Expand the test suite by adding extensive Unit Test (JUnit/Mockito) and Integration tests.
4. **Rate Limiting (Bucket4j)**: Prevent brute-force attacks and abuse of analytical APIs.

---

## 👨‍💻 Author
**Hemant**
Backend Developer Intern Candidate
