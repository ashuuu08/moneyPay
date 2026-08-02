# 📋 MoneyPay — Phase Implementation Report

> **Project**: MoneyPay — Production-grade Event-Driven Payment Platform  
> **Stack**: Java 21 · Spring Boot 3.3 · Apache Kafka · PostgreSQL · Redis · Eureka  
> **Author**: Ashish · [GitHub](https://github.com/ashuuu08) · [Portfolio](https://ashuuu08.netlify.app)

---

## ✅ Phase 1 — Foundation (COMPLETE)

**Goal**: Set up the entire project skeleton, shared library, auth, gateway, and local infra.

### What was built

#### `common-lib` — Shared Library
| File | Purpose |
|---|---|
| `ApiResponse<T>` | Standard JSON envelope (`success`, `message`, `data`, `errorCode`, `timestamp`) used by all services |
| `MoneyPayException` | Custom runtime exception carrying HTTP status + error code |
| `GlobalExceptionHandler` | `@RestControllerAdvice` that maps exceptions → consistent `ApiResponse` error responses |

#### `auth-service` (port 8081)
| Component | Details |
|---|---|
| `User` entity | UUID PK, roles (`CUSTOMER / MERCHANT / ADMIN`), status (`ACTIVE / SUSPENDED / PENDING_KYC`) |
| `RefreshToken` entity | Rotation-based refresh tokens with expiry |
| `AuthService` | Register, login, token refresh, logout with full refresh token rotation |
| `JwtService` | HS256 JWT issuance and validation (15 min access, 7 day refresh) |
| `SecurityConfig` | Spring Security config — public auth routes, stateless session |
| `AuthController` | `POST /api/v1/auth/register`, `/login`, `/refresh`, `/logout` |
| Flyway migration | `V1__init_auth_schema.sql` — users + refresh_tokens tables |

#### `api-gateway` (port 8080)
| Component | Details |
|---|---|
| `JwtAuthFilter` | Spring Cloud Gateway filter — validates JWT on all protected routes |
| `application.yml` | Routes: auth (no auth), payment/wallet/ledger/merchant (JWT required + rate limit) |
| Redis rate limiting | Token bucket: 20 req/s replenish, 40 burst capacity per API key |

#### `eureka-server` (port 8761)
- Netflix Eureka service registry
- All microservices register on startup, discovery is automatic

#### `infra/docker-compose.yml`
| Service | Image | Purpose |
|---|---|---|
| `zookeeper` | `confluentinc/cp-zookeeper:7.6.1` | Kafka coordination |
| `kafka` | `confluentinc/cp-kafka:7.6.1` | Event streaming broker |
| `kafdrop` | `obsidiandynamics/kafdrop:4.0.2` | Kafka UI at `:9000` |
| `redis` | `redis:7.2-alpine` | Idempotency keys + rate limiting |
| `postgres-auth/payment/wallet/ledger/merchant` | `postgres:16-alpine` | Database-per-service isolation |

---

## ✅ Phase 2 — Core Payment Flow (COMPLETE)

**Goal**: Implement the main payment pipeline — initiation, idempotency, Kafka events, wallet balance updates, and double-entry bookkeeping.

### `common-lib` — `PaymentEvent` added
| Field | Type | Description |
|---|---|---|
| `paymentId` | `String` | Unique payment UUID |
| `orderId` | `String` | Caller's order reference |
| `userId` | `String` | Buyer UUID |
| `merchantId` | `String` | Seller UUID |
| `amount` | `BigDecimal` | Transaction amount |
| `currency` | `String` | ISO 4217 code (e.g. `INR`) |
| `eventType` | `String` | `INITIATED \| SUCCESS \| FAILED \| REFUNDED` |
| `timestamp` | `Instant` | Event emission time |

Shared by payment-service (producer), wallet-service, ledger-service, notification-service, alert-service (consumers).

---

### `payment-service` (port 8082)

**Flow**: `POST /api/v1/payments` → idempotency check → save INITIATED → publish event → mock gateway → update status → publish result → cache response

| Component | Details |
|---|---|
| `Payment` entity | UUID PK, `status` enum (INITIATED/SUCCESS/FAILED/REFUNDED), `idempotencyKey` unique index, `providerRef` |
| `PaymentRepository` | `findByIdempotencyKey`, `findByOrderId` |
| `IdempotencyService` | Redis-backed, 7-day TTL — prevents duplicate charges on retried requests |
| `MockPaymentGatewayService` | Simulates Razorpay/Stripe sandbox; returns `MOCK-XXXXXXXX` providerRef; always succeeds in dev |
| `PaymentEventPublisher` | Routes `PaymentEvent` to correct Kafka topic using `paymentId` as partition key (ordering guarantee) |
| `PaymentService` | Full orchestration: idempotency → persist → publish INITIATED → gateway charge → update status → publish SUCCESS/FAILED → cache |
| `KafkaConfig` | Declares `payment.initiated`, `payment.success`, `payment.failed` topics (3 partitions, 1 replica) |
| `RedisConfig` | `StringRedisSerializer` — stores idempotency key → JSON response |
| `JpaConfig` | Enables `@CreatedDate` / `@LastModifiedDate` auditing |
| `PaymentController` | `POST /api/v1/payments` (initiate), `GET /api/v1/payments/{id}` (status) |
| Flyway V1 | `payments` table with `UNIQUE (idempotency_key)`, CHECK constraint on status, indexes on userId, merchantId, status, createdAt |

**Kafka Topics Published**:
- `payment.initiated` — immediately after DB save
- `payment.success` — after successful gateway charge
- `payment.failed` — after gateway failure

---

### `wallet-service` (port 8083)

**Flow**: Consumes `payment.success` → credits merchant + debits buyer (best-effort)

| Component | Details |
|---|---|
| `Wallet` entity | `userId + currency` unique constraint, `@Version` optimistic locking, balance with CHECK >= 0 |
| `WalletTransaction` entity | Immutable audit record: CREDIT/DEBIT, amount, `referenceId`, `balanceAfter` snapshot |
| `WalletRepository` | Includes `PESSIMISTIC_WRITE` lock query for concurrent balance safety |
| `WalletTransactionRepository` | `existsByReferenceId` — idempotency guard for Kafka redelivery |
| `WalletService` | `credit()` — auto-creates wallet on first use; `debit()` — throws `INSUFFICIENT_FUNDS` if balance < amount |
| `PaymentEventConsumer` | Listens `payment.success` + `payment.failed`; credits merchant; best-effort debits buyer |
| `WalletController` | `GET /api/v1/wallets/{userId}` (balance), `GET /api/v1/wallets/{userId}/transactions` (paginated history) |
| Flyway V1 | `wallets` + `wallet_transactions` tables with CHECK constraints |

---

### `ledger-service` (port 8084)

**Flow**: Consumes `payment.success` → posts exactly two ledger entries atomically

| Component | Details |
|---|---|
| `LedgerEntry` entity | `paymentId`, `accountId`, `entryType` (DEBIT/CREDIT), amount, currency |
| `LedgerEntryRepository` | `findByPaymentId`, `findByAccountId`, `existsByPaymentIdAndEntryType` (idempotency guard) |
| `LedgerService` | `postDoubleEntry()` — saves DEBIT (user) + CREDIT (merchant) in one transaction; idempotent |
| `PaymentEventConsumer` | Listens `payment.success`; triggers double-entry posting |
| `LedgerController` | `GET /api/v1/ledger/payments/{id}` (audit trail), `GET /api/v1/ledger/accounts/{id}` (statement) |
| Flyway V1 | `ledger_entries` table with `UNIQUE (payment_id, entry_type)` — enforces double-entry invariant at DB level |

---

## ✅ Phase 3 — Supporting Services (COMPLETE)

### `notification-service` (port 8085)

**Goal**: Notify users (email + SMS) when their payment succeeds or fails.

| Component | Details |
|---|---|
| `NotificationMessage` | Internal value object: recipient, channel, subject, body, paymentId, eventType |
| `NotificationChannel` | Enum: `EMAIL \| SMS \| WEBHOOK` |
| `EmailNotificationService` | Spring Mail / JavaMailSender; graceful error handling — never crashes consumer |
| `SmsNotificationService` | Mock in dev (logs to console); replace with Twilio/AWS SNS in production |
| `NotificationService` | Orchestrates email + SMS dispatch; Redis deduplication with 7-day TTL — prevents double-notify on Kafka redelivery |
| `PaymentEventConsumer` | Listens `payment.success` + `payment.failed`; triggers `NotificationService` |
| `RedisConfig` | StringRedisSerializer for dedup key storage |
| `application.yml` | Kafka consumer, Spring Mail (SMTP), Redis, Eureka — `test-connection: false` so local startup works without SMTP |

**Email Templates**:
- **Success**: "Your payment of ₹{amount} for order #{orderId} was successful."
- **Failure**: "Your payment of ₹{amount} for order #{orderId} could not be processed."

---

### `merchant-service` (port 8086)

**Goal**: Onboard merchants, track KYC status, and issue/revoke API keys.

| Component | Details |
|---|---|
| `Merchant` entity | `userId` (FK to auth-service), `businessName`, `businessEmail`, `gstin`, `pan`, `kycStatus` (PENDING/UNDER_REVIEW/APPROVED/REJECTED), `webhookUrl` |
| `MerchantApiKey` entity | `keyPrefix` (first 17 chars, safe to display), `keyHash` (BCrypt — plain key never stored), `active`, `expiresAt` |
| `MerchantRepository` | `findByUserId`, `findByBusinessEmail`, `existsByUserId` |
| `MerchantApiKeyRepository` | `findByMerchantIdAndActiveTrue`, `findByKeyPrefix` |
| `MerchantService` | `onboard()`, `getMerchant()`, `updateKycStatus()`, `generateApiKey()` (returns plain key ONCE), `listApiKeys()`, `revokeApiKey()` |
| `MerchantController` | Full REST API for onboarding, KYC management, and API key lifecycle |
| `MerchantConfig` | JPA auditing + `BCryptPasswordEncoder` bean |
| Flyway V1 | `merchants` + `merchant_api_keys` tables with all constraints and indexes |

**API Endpoints**:
| Method | Path | Description |
|---|---|---|
| `POST` | `/api/v1/merchants` | Onboard new merchant |
| `GET` | `/api/v1/merchants/{id}` | Get merchant profile |
| `PATCH` | `/api/v1/merchants/{id}/kyc` | Update KYC status (admin) |
| `POST` | `/api/v1/merchants/{id}/api-keys` | Generate new API key |
| `GET` | `/api/v1/merchants/{id}/api-keys` | List active API keys |
| `DELETE` | `/api/v1/merchants/api-keys/{keyId}` | Revoke an API key |

---

### `alert-service` (port 8087)

**Goal**: Notify the ops team immediately when payment failures or anomalies occur.

| Component | Details |
|---|---|
| `AlertService` | Sends ops alerts via email (Spring Mail) and Slack webhook (mock in dev, configurable via `ALERT_SLACK_WEBHOOK_URL`) |
| `PaymentAlertConsumer` | Listens `payment.failed`; triggers `alertPaymentFailure()` with full context |
| `application.yml` | Kafka consumer, Spring Mail, Eureka; configurable `alert.ops-email` and `alert.slack-webhook-url` |

**Alert content includes**: paymentId, orderId, userId, merchantId, amount, currency, timestamp — with a pointer to dashboards.

---

## 🏗 Overall Architecture — Service Map

```
Client (Web/Mobile)
        │ HTTPS
        ▼
  API Gateway :8080  ──── JWT Filter + Rate Limit (Redis)
        │
  Eureka :8761  ──── Service Discovery
        │
  ┌─────┼─────────────────────────────────┐
  │     │                                 │
Auth  Payment   Wallet   Ledger  Merchant  Notification  Alert
:8081  :8082    :8083    :8084   :8086      :8085        :8087
  │     │         │        │       │
 PG    PG        PG       PG      PG         Redis        ─
        │
      Kafka Broker
  ┌────┼──────────────────────┐
  │    │                      │
payment.success  payment.failed  payment.initiated
  │         │         │
Wallet   Ledger   Notif    Alert
```

---

## 📊 Kafka Event Flow

| Topic | Producer | Consumers |
|---|---|---|
| `payment.initiated` | payment-service | alert-service (future anomaly detection) |
| `payment.success` | payment-service | wallet-service, ledger-service, notification-service |
| `payment.failed` | payment-service | wallet-service (log), notification-service, alert-service |

---

## 🗄 Database Isolation (Per-Service)

| Service | DB Name | Tables |
|---|---|---|
| auth-service | `moneypay_auth` | `users`, `refresh_tokens` |
| payment-service | `moneypay_payment` | `payments` |
| wallet-service | `moneypay_wallet` | `wallets`, `wallet_transactions` |
| ledger-service | `moneypay_ledger` | `ledger_entries` |
| merchant-service | `moneypay_merchant` | `merchants`, `merchant_api_keys` |

---

## 🔒 Security Highlights

| Feature | Implementation |
|---|---|
| JWT auth | HS256 Bearer tokens, 15-min access + 7-day refresh rotation |
| API key hashing | BCrypt — plain key shown once at generation, never stored |
| Idempotency | Redis 7-day TTL per `idempotencyKey` — no duplicate charges |
| Rate limiting | Redis token bucket at gateway — 20 req/s per API key |
| Notification dedup | Redis 7-day TTL per `paymentId:eventType` — no double emails |
| DB constraints | CHECK + UNIQUE constraints enforce business rules at DB level |

---

## 🚀 How to Run Locally

```bash
# 1. Start all infra (Postgres x5, Kafka, Redis, Zookeeper, Kafdrop)
docker-compose -f infra/docker-compose.yml up -d

# 2. Build everything
mvn clean install -DskipTests

# 3. Start services individually (each in a separate terminal)
cd eureka-server   && mvn spring-boot:run   # :8761
cd api-gateway     && mvn spring-boot:run   # :8080
cd auth-service    && mvn spring-boot:run   # :8081
cd payment-service && mvn spring-boot:run   # :8082
cd wallet-service  && mvn spring-boot:run   # :8083
cd ledger-service  && mvn spring-boot:run   # :8084
cd notification-service && mvn spring-boot:run  # :8085
cd merchant-service && mvn spring-boot:run  # :8086
cd alert-service   && mvn spring-boot:run   # :8087

# OR — start everything with Docker Compose full profile
docker-compose -f infra/docker-compose.yml --profile full up --build
```

### Quick Smoke Test

```bash
# Register user
POST http://localhost:8080/api/v1/auth/register
{ "fullName": "Test User", "email": "test@demo.com", "password": "Pass@123" }

# Login → get JWT
POST http://localhost:8080/api/v1/auth/login
{ "email": "test@demo.com", "password": "Pass@123" }

# Initiate payment (with JWT in Authorization header)
POST http://localhost:8080/api/v1/payments
{
  "orderId": "ORD-001",
  "userId": "<userId>",
  "merchantId": "<merchantId>",
  "amount": 500.00,
  "currency": "INR",
  "idempotencyKey": "unique-key-001"
}

# Check wallet balance
GET http://localhost:8080/api/v1/wallets/<userId>

# Check ledger audit trail
GET http://localhost:8080/api/v1/ledger/payments/<paymentId>
```

---

## 📌 Roadmap Status

| Phase | Status | Description |
|---|---|---|
| Phase 1 — Foundation | ✅ Complete | Multi-module Maven, common-lib, auth, gateway, eureka, docker-compose |
| Phase 2 — Core Payment | ✅ Complete | payment, wallet, ledger services + Kafka event pipeline |
| Phase 3 — Supporting Services | ✅ Complete | notification, merchant, alert services |
| Phase 4 — Reliability | 🔜 Next | Resilience4j circuit breakers, DLQ handling, distributed tracing |
| Phase 5 — Observability | 🔜 Next | Prometheus + Grafana dashboards, Alertmanager, structured logging |
| Phase 6 — Containerization | 🔜 Next | Dockerfiles per service, full Docker Compose, Kubernetes Helm charts |
| Phase 7 — CI/CD | 🔜 Next | GitHub Actions CI/CD, SonarQube, contract testing |
| Phase 8 — Production | 🔜 Next | Vault secrets, DB backups, PCI-DSS review, runbooks |
