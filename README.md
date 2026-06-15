# 💰 Intelligent Personal Finance Manager

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-25%2B-orange.svg)](#)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.5-brightgreen.svg)](#)
[![Build](https://img.shields.io/badge/build-passing-brightgreen.svg)](#)

A **production‑grade backend** for a personal finance management platform. It combines **Open Banking data ingestion**, **ML‑powered transaction categorisation**, **smart budgeting**, and **savings goal tracking** into a secure, scalable REST API.

> 🔗 **Live API**: [https://finance-api.onrender.com](https://finance-api.onrender.com)  
> 📚 **Swagger UI**: [https://finance-api.onrender.com/swagger-ui.html](https://finance-api.onrender.com/swagger-ui.html)

---

## ✨ Features

- 🔐 **JWT‑based Authentication** – register/login with encrypted passwords and role‑based access.
- 🏦 **Bank Account Management** – connect multiple accounts, track balances, and sync transactions.
- 💸 **Transaction Tracking** – store, retrieve, and categorise every income/expense.
- 🤖 **ML‑Powered Categorisation** – automatic category detection (Groceries, Transport, etc.) with fallback rules.
- 📊 **Spending Analytics** – monthly breakdowns, category‑wise summaries, and trend detection.
- 💡 **Smart Insights** – overspending alerts, budget recommendations, and savings milestones.
- 🎯 **Savings Goals** – set targets, track progress, and receive achievement notifications.
- 🧠 **Budget Alerts** – real‑time overspending detection with configurable limits.
- 📈 **Future‑Ready** – built for expansion (Open Banking integrations, push notifications, etc.).

---

## 🧱 Tech Stack

| Layer         | Technology |
|---------------|------------|
| **Backend**   | Java 25, Spring Boot 3.1.5 |
| **Security**  | Spring Security, JWT (io.jsonwebtoken) |
| **Database**  | PostgreSQL (JPA/Hibernate) |
| **ML Client** | Spring WebClient, Resilience4j Circuit Breaker |
| **Docs**      | SpringDoc OpenAPI (Swagger UI) |
| **Build**     | Maven, Lombok |
| **Deployment**| Render (Web Service + Managed PostgreSQL) |

---

## 📂 Project Structure

```
src/main/java/com/financeapp
├── config/          # Swagger, WebClient, security configs
├── controllers/     # REST endpoints (Auth, Transactions, Budgets, etc.)
├── dto/
│   ├── request/     # Inbound DTOs with validation
│   └── response/    # Outbound DTOs
├── exceptions/      # Custom exceptions & global handler
├── integration/     # External service clients (Open Banking placeholder)
├── ml/              # ML service client & DTOs
│   └── dto/
├── models/          # JPA entities & enums
│   └── enums/
├── repositories/    # Spring Data JPA repositories
├── security/        # JWT filter, token util, security config
├── services/        # Business logic layer
└── utils/           # Helper classes
```

---

## 🚀 Quick Start (Local)

### Prerequisites

- Java 25 or higher
- Maven (or use the embedded `mvnw`)
- PostgreSQL (running locally or use H2 for quick testing)

### 1. Clone the repository

```bash
git clone https://github.com/your-username/finance-manager.git
cd finance-manager
```

### 2. Configure the database

By default, the app expects PostgreSQL with these credentials:

```
Host: localhost
Port: 5432
Database: finance_db
User: finance_user
Password: password
```

You can override them using environment variables (`PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER`, `PGPASSWORD`) or edit `src/main/resources/application.yml`.

> 💡 For quick development without PostgreSQL, switch to H2 in‑memory database
> (see the H2 profile in comments).

### 3. Build & Run

```bash
./mvnw clean install -DskipTests
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`.  
**Swagger UI** → `http://localhost:8080/swagger-ui.html`

---

## 🌐 Deploying to Render

This project is ready for one‑click deployment on [Render](https://render.com).

### Steps

1. Create a **PostgreSQL** database on Render.
2. Create a **Web Service** pointing to this repository.
   - **Build Command**: `./mvnw clean install -DskipTests`
   - **Start Command**: `java -jar target/*.jar`
3. In the Web Service environment variables, link the PostgreSQL database (Render automatically provides `PGHOST`, `PGPORT`, …).
4. Add `JWT_SECRET` as an environment variable (long random string).
5. The service will build and start automatically.

A detailed deployment guide is available in [DEPLOY.md](DEPLOY.md) (you can add one).

---

## 🤖 ML Integration

The `MLServiceClient` communicates with an external machine‑learning service to:

- Categorise raw transaction descriptions (`POST /categorize-transaction`)
- Predict future expenses (`POST /predict-expense`)
- Analyse spending patterns

If the ML service is unavailable, a **rule‑based fallback** (Resilience4j circuit breaker) ensures the app remains functional.

---

## 🔒 Security

- Stateless **JWT authentication** – every secured endpoint requires a `Bearer` token.
- Password hashing with **BCrypt**.
- Role‑based authorisation (`ROLE_USER`, `ROLE_ADMIN`) using Spring Security.
- All sensitive configuration is externalised via environment variables.

---

## 🧪 API Endpoints (Sample)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login, returns JWT |
| GET | `/api/transactions` | List transactions (paginated) |
| POST | `/api/transactions` | Create transaction |
| GET | `/api/transactions/analytics` | Spending analytics |
| POST | `/api/budgets` | Create budget |
| GET | `/api/budgets` | Get budgets |
| POST | `/api/goals` | Create savings goal |
| GET | `/api/insights` | Read financial insights |

Full API documentation is available at the **Swagger UI** endpoint.

---

## 📄 License

This project is licensed under the **Apache License 2.0** – see the [LICENSE](LICENSE) file for details.

*Built with ❤️ using Spring Boot – happy budgeting!*
