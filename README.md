# 💸 MoneyPay

**A production-grade, event-driven payment processing platform built on microservices architecture** — inspired by systems like Razorpay/PayU, designed to demonstrate real-world scalable financial infrastructure.





![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen)
![Kafka](https://img.shields.io/badge/Kafka-Event%20Streaming-black)
![Docker](https://img.shields.io/badge/Docker-Containerized-blue)
![Kubernetes](https://img.shields.io/badge/Kubernetes-Orchestrated-326CE5)
![License](https://img.shields.io/badge/License-MIT-yellow)

---

## 📖 Table of Contents

- [Overview](#-overview)
- [System Architecture](#-system-architecture)
- [Tech Stack](#-tech-stack)
- [Microservices](#-microservices)
- [Folder Structure](#-folder-structure)
- [Prerequisites](#-prerequisites)
- [Getting Started (Local Dev)](#-getting-started-local-dev)
- [Environment Variables](#-environment-variables)
- [API Documentation](#-api-documentation)
- [Testing Strategy](#-testing-strategy)
- [Observability](#-observability)
- [Security](#-security)
- [Deployment](#-deployment)
- [Disaster Recovery & Backups](#-disaster-recovery--backups)
- [Roadmap / TODO](#-roadmap--todo)
- [Contributing](#-contributing)
- [License](#-license)

---

## 🧭 Overview

MoneyPay is a distributed payment gateway system that handles payment initiation, wallet management, double-entry ledger accounting, notifications, and merchant onboarding — all communicating asynchronously via Kafka and orchestrated through Kubernetes.

This project is built to reflect **real production standards**: idempotent APIs, distributed tracing, centralized logging, circuit breakers, database-per-service isolation, and CI/CD automation — not just a toy CRUD app.

---

## 🏗 System Architecture

```
                        ┌─────────────────────┐
                        │      Client Apps      │
                        │  (Web / Mobile / SDK)  │
                        └──────────┬─────────────┘
                                   │ HTTPS
                                   ▼
                        ┌─────────────────────┐
                        │     API Gateway        │
                        │ (Spring Cloud Gateway)│
                        │  Auth · Rate Limit ·   │
                        │  Routing · Logging     │
                        └──────────┬─────────────┘
                                   │
                        ┌──────────┴─────────────┐
                        │   Service Registry       │
                        │       (Eureka)           │
                        └──────────┬─────────────┘
        ┌──────────┬───────────────┼───────────────┬────────────┬──────────────┐
        ▼          ▼               ▼               ▼            ▼              ▼
   ┌────────┐ ┌──────────┐  ┌─────────────┐ ┌───────────┐ ┌────────────┐ ┌───────────┐
   │  Auth  │ │ Payment  │  │   Wallet    │ │  Ledger   │ │Notification│ │  Merchant │
   │Service │ │ Service  │  │  Service    │ │  Service  │ │  Service   │ │  Service  │
   └───┬────┘ └────┬─────┘  └──────┬──────┘ └─────┬─────┘ └─────┬──────┘ └─────┬─────┘
       │           │               │              │             │              │
       ▼           ▼               ▼              ▼             │              ▼
    PGSQL       PGSQL           PGSQL          PGSQL          Redis          PGSQL
       │           │               │              │
       └───────────┴───────┬───────┴──────────────┘
                            ▼
                  ┌───────────────────┐
                  │   Kafka Broker      │  topics: payment.initiated,
                  │  (Event Backbone)   │  payment.success, payment.failed,
                  └─────────┬──────────┘  refund.requested, ledger.posted
                            │
              ┌─────────────┴──────────────┐
              ▼                             ▼
     ┌─────────────────┐          ┌──────────────────────┐
     │  Alert Service    │          │ Centralized Logging   │
     │ (Slack/Email/SMS) │          │  (ELK / Grafana Loki)│
     └─────────────────┘          └──────────────────────┘

     Cross-cutting: Prometheus + Grafana (metrics) · Micrometer Tracing (distributed tracing)
```

---

## 🧰 Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.1.0, Spring Cloud Gateway, Spring Data JPA |
| Messaging | Apache Kafka |
| Cache / Rate Limiting | Redis |
| Database | PostgreSQL (database-per-service) |
| Service Discovery | Netflix Eureka |
| Auth | JWT (Bearer Flow), Spring Security |
| Containerization | Docker, Docker Compose |
| Orchestration | Kubernetes (Helm charts) |
| CI/CD | GitHub Actions |
| Logging | ELK Stack / Grafana Loki + Promtail |
| Metrics & Tracing | Prometheus, Grafana, Micrometer + OpenTelemetry |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Testing | JUnit 5, Mockito, Testcontainers |

---




## 🧩 Microservices

| Service | Responsibility | DB |
|---|---|---|
| `api-gateway` | Routing, JWT validation, rate limiting | – |
| `auth-service` | User/merchant login, JWT issuance & refresh | PostgreSQL |
| `payment-service` | Payment initiation, gateway integration, idempotency | PostgreSQL |
| `wallet-service` | Balance management, credit/debit operations | PostgreSQL |
| `ledger-service` | Double-entry bookkeeping, audit trail | PostgreSQL |
| `notification-service` | Email/SMS/webhook dispatch to merchants | Redis |
| `merchant-service` | Merchant onboarding, KYC status, API key management | PostgreSQL |
| `alert-service` | Internal ops alerting on failures/anomalies | – |

---

## 📂 Folder Structure

```
moneyPay/
├── api-gateway/
├── auth-service/
├── payment-service/
├── wallet-service/
├── ledger-service/
├── notification-service/
├── merchant-service/
├── alert-service/
├── common-lib/                 # shared DTOs, exceptions, utils
├── infra/
│   ├── docker-compose.yml       # local orchestration
│   ├── k8s/                     # Kubernetes manifests / Helm charts
│   │   ├── base/
│   │   └── overlays/
│   │       ├── dev/
│   │       └── prod/
│   └── monitoring/
│       ├── prometheus/
│       └── grafana/
├── docs/
│   ├── architecture.md
│   ├── api-contracts/
│   └── er-diagrams/
├── .github/
│   └── workflows/
│       ├── ci.yml
│       └── cd.yml
├── .env.example
└── README.md
```

---

## ✅ Prerequisites

- Java 21 (JDK)
- Maven 3.9+
- Docker & Docker Compose
- kubectl + a local cluster (Minikube / Kind) for K8s testing
- Apache Kafka (via Docker) — no local install needed

---

## 🚀 Getting Started (Local Dev)

```bash
# 1. Clone the repo
git clone https://github.com/ashuuu08/moneyPay.git
cd moneyPay

# 2. Copy environment template
cp .env.example .env

# 3. Spin up infra (Postgres, Kafka, Redis, Zookeeper, Eureka)
docker-compose -f infra/docker-compose.yml up -d

# 4. Build all services
mvn clean install -DskipTests

# 5. Run a service (repeat per service, or use the compose profile below)
cd payment-service
mvn spring-boot:run

# OR run everything via Docker Compose
docker-compose -f infra/docker-compose.yml --profile full up --build
```

Once up, verify:
- Eureka dashboard → `http://localhost:8761`
- API Gateway → `http://localhost:8080`
- Swagger UI (per service) → `http://localhost:<port>/swagger-ui.html`
- Kafka UI (if added, e.g. Kafdrop) → `http://localhost:9000`

---

## 🔑 Environment Variables

See `.env.example` for the full list. Key variables per service:

```env
# Common
SPRING_PROFILES_ACTIVE=local
EUREKA_URI=http://localhost:8761/eureka

# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=moneypay_payment
DB_USER=postgres
DB_PASSWORD=changeme

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# JWT
JWT_SECRET=change_this_in_production
JWT_EXPIRY_MINUTES=15
JWT_REFRESH_EXPIRY_DAYS=7
```

> ⚠️ Never commit `.env` files. Use a secrets manager (Vault / AWS Secrets Manager / K8s Secrets) in staging & production.

---

## 📜 API Documentation

Each service exposes OpenAPI docs at `/v3/api-docs` and Swagger UI at `/swagger-ui.html`. A consolidated Postman collection is maintained at `docs/api-contracts/moneypay.postman_collection.json`.

---

## 🧪 Testing Strategy

| Test Type | Tool | Scope |
|---|---|---|
| Unit tests | JUnit 5 + Mockito | Service/business logic |
| Slice tests | `@WebMvcTest`, `@DataJpaTest` | Layer-specific |
| Integration tests | Testcontainers (Postgres, Kafka, Redis) | Cross-component, real infra in Docker |
| Contract tests | Spring Cloud Contract | Inter-service API contracts |
| Load tests | k6 / Gatling | Payment endpoint throughput & latency |

```bash
mvn test                 # unit + slice tests
mvn verify -Pintegration  # integration tests with Testcontainers
```

---

## 📊 Observability

- **Logging**: Structured JSON logs (Spring Boot's built-in ECS/Logstash encoder) shipped via Promtail → Loki, visualized in Grafana. Every request carries a `X-Correlation-Id` header propagated across services.
- **Tracing**: Micrometer Tracing + OpenTelemetry exporter → Grafana Tempo/Jaeger, so a single payment request can be traced end-to-end across gateway → payment → ledger → notification.
- **Metrics**: Actuator + Micrometer → Prometheus → Grafana dashboards (latency, error rate, throughput, Kafka consumer lag).
- **Alerting**: Prometheus Alertmanager rules (e.g. error rate > 2%, Kafka consumer lag > 1000) feed into `alert-service`, which notifies Slack/PagerDuty/Email.

---

## 🔐 Security

- JWT Bearer Flow with short-lived access tokens + refresh token rotation
- All inter-service calls authenticated (mTLS in production, or signed service tokens)
- Idempotency keys (stored in Redis) on all payment-mutating endpoints to prevent duplicate charges
- Input validation via `spring-boot-starter-validation` on every DTO
- Secrets never hardcoded — pulled from Vault/K8s Secrets at runtime
- Rate limiting at the gateway (Redis-backed token bucket) per API key
- PCI-DSS–aligned practices: no raw card data stored (tokenization via payment processor), encrypted data at rest (DB-level encryption) and in transit (TLS 1.2+)
- Audit logging on all financial mutations (who/what/when) in `ledger-service`
- Dependency vulnerability scanning (OWASP Dependency-Check / Snyk) in CI

---

## ☸️ Deployment

- **Local**: Docker Compose (see above)
- **Staging/Prod**: Kubernetes via Helm charts in `infra/k8s/`
  - Horizontal Pod Autoscaler on CPU/memory + custom Kafka-lag metric
  - Readiness/liveness probes on every service
  - Rolling updates with zero downtime
  - Ingress + TLS termination (cert-manager)
- **CI/CD**: GitHub Actions
  - `ci.yml`: build → test → static analysis (SonarQube) → Docker image build → push to registry
  - `cd.yml`: deploy to staging on merge to `main`, manual approval gate for production

---

## 🛡 Disaster Recovery & Backups

- Automated daily PostgreSQL backups (point-in-time recovery enabled) per service DB
- Kafka topic retention configured for replay-based recovery of consumer state
- Multi-AZ deployment for DB and Kafka brokers in production
- Documented runbook for incident response in `docs/runbooks/`

---

## 🗺 Roadmap / TODO

### Phase 1 — Foundation
- [ ] Set up multi-module Maven parent project + `common-lib`
- [ ] `auth-service`: user/merchant registration, login, JWT issuance & refresh
- [ ] `api-gateway`: routing config, JWT validation filter
- [ ] `eureka-server`: service registry setup
- [ ] Docker Compose for local infra (Postgres, Kafka, Redis, Zookeeper, Eureka)

### Phase 2 — Core Payment Flow
- [ ] `payment-service`: payment initiation API + idempotency key handling (Redis)
- [ ] Integrate a mock/sandbox payment processor (Razorpay/Stripe test mode)
- [ ] Publish Kafka events: `payment.initiated`, `payment.success`, `payment.failed`
- [ ] `wallet-service`: consume payment events, update balances
- [ ] `ledger-service`: double-entry bookkeeping, consume payment/wallet events

### Phase 3 — Supporting Services
- [ ] `notification-service`: email/SMS/webhook on payment status change
- [ ] `merchant-service`: onboarding, KYC status, API key generation
- [ ] `alert-service`: consume failure/anomaly events, notify ops via Slack

### Phase 4 — Reliability & Resilience
- [ ] Add circuit breakers (Resilience4j) on all inter-service calls
- [ ] Add retry + dead-letter-queue handling for Kafka consumers
- [ ] Distributed tracing (Micrometer + OpenTelemetry)
- [ ] Structured JSON logging across all services
- [ ] Centralized log aggregation (ELK/Loki)

### Phase 5 — Observability & Ops
- [ ] Prometheus + Grafana dashboards per service
- [ ] Alertmanager rules (error rate, latency, Kafka lag)
- [ ] Health checks + readiness/liveness probes
- [ ] Load testing (k6/Gatling) on payment endpoint

### Phase 6 — Containerization & Orchestration
- [ ] Dockerfile per service (multi-stage builds, minimal base images)
- [ ] Full Docker Compose profile for local end-to-end run
- [ ] Kubernetes manifests / Helm charts
- [ ] Horizontal Pod Autoscaler configuration
- [ ] Ingress + TLS setup

### Phase 7 — CI/CD & Quality
- [ ] GitHub Actions CI pipeline (build, test, lint, SonarQube)
- [ ] GitHub Actions CD pipeline (staging auto-deploy, prod manual approval)
- [ ] Dependency vulnerability scanning (OWASP/Snyk)
- [ ] Contract testing between services (Spring Cloud Contract)
- [ ] Testcontainers-based integration test suite

### Phase 8 — Production Hardening
- [ ] Secrets management via Vault/K8s Secrets (remove all hardcoded values)
- [ ] Database backup & PITR strategy
- [ ] Rate limiting + WAF rules at gateway
- [ ] PCI-DSS checklist review (tokenization, encryption at rest/in transit)
- [ ] Incident response runbooks
- [ ] API documentation (Swagger/OpenAPI) finalized for all services
- [ ] Postman collection + architecture docs published in `docs/`

---

## 🤝 Contributing

1. Fork the repo
2. Create a feature branch: `git checkout -b feature/short-description`
3. Commit using [Conventional Commits](https://www.conventionalcommits.org/) (`feat:`, `fix:`, `chore:`)
4. Ensure `mvn verify` passes locally
5. Open a PR against `main`

---

## 📄 License

This project is licensed under the MIT License — see [LICENSE](./LICENSE) for details.

---

**Author**: Ashish · [Portfolio](https://ashuuu08.netlify.app) · [GitHub](https://github.com/ashuuu08) · [LinkedIn](https://linkedin.com/in/itzashu08)
