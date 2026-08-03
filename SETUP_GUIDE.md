# MoneyPay — Complete Setup & Run Guide

> **Java 22 detected ✅ | Docker ❌ Not installed | Maven ❌ Not on PATH**
> This guide covers everything from scratch.

---

## STEP 1 — Install Docker Desktop

Docker runs **PostgreSQL, Kafka, Redis, Zookeeper** — all required infra.

1. Download: https://www.docker.com/products/docker-desktop/
2. Install (keep all defaults, enable WSL2 if asked)
3. **Restart your PC** after install
4. Open Docker Desktop and wait for it to show **"Engine running"**
5. Verify:
   ```powershell
   docker --version
   docker compose version
   ```

---

## STEP 2 — Install Maven

1. Download: https://maven.apache.org/download.cgi→ Pick: `apache-maven-3.9.x-bin.zip`
2. Extract to `C:\maven`
3. Add to System PATH:
   - Search **"Environment Variables"** in Start
   - Under System Variables → `Path` → Edit → New → `C:\maven\bin`
   - Click OK on all dialogs
4. **Open a new PowerShell window** and verify:
   ```powershell
   mvn -version
   ```

---

## STEP 3 — Create the .env file

Copy and paste this into a new file at:
`C:\Users\pc\Downloads\moneyPay\moneyPay\infra\.env`

```env
# ── Spring ───────────────────────────────────────
SPRING_PROFILES_ACTIVE=local

# ── Database ──────────────────────────────────────
DB_HOST=localhost
DB_PORT=5432
DB_USER=postgres
DB_PASSWORD=changeme

# ── Kafka ─────────────────────────────────────────
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# ── Redis ─────────────────────────────────────────
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=redispass

# ── Eureka ────────────────────────────────────────
EUREKA_URI=http://admin:admin@localhost:8761/eureka
EUREKA_USER=admin
EUREKA_PASSWORD=admin

# ── JWT ───────────────────────────────────────────
JWT_SECRET=moneypay_super_secret_key_must_be_32_chars_min
JWT_EXPIRY_MINUTES=15
JWT_REFRESH_EXPIRY_DAYS=7

# ── Email (optional - leave blank for local dev) ──
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=
MAIL_PASSWORD=

# ── Alerts (optional) ─────────────────────────────
ALERT_OPS_EMAIL=ops@moneypay.io
ALERT_SLACK_WEBHOOK_URL=
NOTIFICATION_FROM_EMAIL=noreply@moneypay.io
```

---

## STEP 4 — Start Infrastructure (Postgres + Kafka + Redis)

Open PowerShell and run:

```powershell
cd C:\Users\pc\Downloads\moneyPay\moneyPay\infra
docker compose up -d
```

Wait ~60 seconds, then verify everything is healthy:

```powershell
docker compose ps
```

You should see all these as **healthy/running**:

| Container                  | Port | Status  |
| -------------------------- | ---- | ------- |
| moneypay-postgres-auth     | 5432 | healthy |
| moneypay-postgres-payment  | 5432 | healthy |
| moneypay-postgres-wallet   | 5432 | healthy |
| moneypay-postgres-ledger   | 5432 | healthy |
| moneypay-postgres-merchant | 5432 | healthy |
| moneypay-kafka             | 9092 | healthy |
| moneypay-zookeeper         | 2181 | healthy |
| moneypay-redis             | 6379 | healthy |
| moneypay-kafdrop           | 9000 | running |

> **Kafka UI**: Open http://localhost:9000 in browser to see topics.

---

## STEP 5 — Build All Services

```powershell
cd C:\Users\pc\Downloads\moneyPay\moneyPay
mvn clean install -DskipTests
```

Expected output at the end:

```
[INFO] BUILD SUCCESS
```

---

## STEP 6 — Run Services (each in separate PowerShell tab)

Open **9 separate PowerShell windows** and run one command each:

**Window 1 — Eureka (start this FIRST)**

```powershell
cd C:\Users\pc\Downloads\moneyPay\moneyPay\eureka-server
mvn spring-boot:run
```

Wait until you see: `Eureka Server is started`
Then open: http://localhost:8761

---

**Window 2 — Auth Service**

```powershell
cd C:\Users\pc\Downloads\moneyPay\moneyPay\auth-service
mvn spring-boot:run
```

---

**Window 3 — API Gateway**

```powershell
cd C:\Users\pc\Downloads\moneyPay\moneyPay\api-gateway
mvn spring-boot:run
```

---

**Window 4 — Payment Service**

```powershell
cd C:\Users\pc\Downloads\moneyPay\moneyPay\payment-service
mvn spring-boot:run
```

---

**Window 5 — Wallet Service**

```powershell
cd C:\Users\pc\Downloads\moneyPay\moneyPay\wallet-service
mvn spring-boot:run
```

---

**Window 6 — Ledger Service**

```powershell
cd C:\Users\pc\Downloads\moneyPay\moneyPay\ledger-service
mvn spring-boot:run
```

---

**Window 7 — Notification Service**

```powershell
cd C:\Users\pc\Downloads\moneyPay\moneyPay\notification-service
mvn spring-boot:run
```

---

**Window 8 — Merchant Service**

```powershell
cd C:\Users\pc\Downloads\moneyPay\moneyPay\merchant-service
mvn spring-boot:run
```

---

**Window 9 — Alert Service**

```powershell
cd C:\Users\pc\Downloads\moneyPay\moneyPay\alert-service
mvn spring-boot:run
```

---

## STEP 7 — Verify Everything is Running

### Check Eureka Dashboard

Open: http://localhost:8761You should see all services registered:

- AUTH-SERVICE
- API-GATEWAY
- PAYMENT-SERVICE
- WALLET-SERVICE
- LEDGER-SERVICE
- NOTIFICATION-SERVICE
- MERCHANT-SERVICE
- ALERT-SERVICE

### Health Checks

Run these in PowerShell:

```powershell
# Gateway
curl http://localhost:8080/actuator/health

# Auth
curl http://localhost:8081/actuator/health

# Payment
curl http://localhost:8082/actuator/health

# Wallet
curl http://localhost:8083/actuator/health

# Ledger
curl http://localhost:8084/actuator/health

# Notification
curl http://localhost:8085/actuator/health

# Merchant
curl http://localhost:8086/actuator/health

# Alert
curl http://localhost:8087/actuator/health
```

All should return: `{"status":"UP"}`

---

## STEP 8 — Test the Full Payment Flow

```powershell
# 1. Register a user
curl -X POST http://localhost:8080/api/v1/auth/register `
  -H "Content-Type: application/json" `
  -d '{"fullName":"Test User","email":"test@demo.com","password":"Pass@123","role":"CUSTOMER"}'

# 2. Login and copy the accessToken
curl -X POST http://localhost:8080/api/v1/auth/login `
  -H "Content-Type: application/json" `
  -d '{"email":"test@demo.com","password":"Pass@123"}'

# 3. Initiate a payment (replace TOKEN and IDs)
curl -X POST http://localhost:8080/api/v1/payments `
  -H "Content-Type: application/json" `
  -H "Authorization: Bearer YOUR_TOKEN_HERE" `
  -d '{
    "orderId": "ORD-001",
    "userId": "USER_UUID_HERE",
    "merchantId": "MERCHANT_UUID_HERE",
    "amount": 500.00,
    "currency": "INR",
    "idempotencyKey": "unique-key-001"
  }'

# 4. Check wallet balance
curl http://localhost:8080/api/v1/wallets/USER_UUID_HERE `
  -H "Authorization: Bearer YOUR_TOKEN_HERE"

# 5. Check ledger entries
curl http://localhost:8080/api/v1/ledger/payments/PAYMENT_UUID_HERE `
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

---

## Swagger UI Links (API Docs)

| Service          | Swagger URL                           |
| ---------------- | ------------------------------------- |
| Auth Service     | http://localhost:8081/swagger-ui.html |
| Payment Service  | http://localhost:8082/swagger-ui.html |
| Wallet Service   | http://localhost:8083/swagger-ui.html |
| Ledger Service   | http://localhost:8084/swagger-ui.html |
| Merchant Service | http://localhost:8086/swagger-ui.html |

---

## Common Errors & Fixes

| Error                                | Fix                                                                                          |
| ------------------------------------ | -------------------------------------------------------------------------------------------- |
| `Connection refused :5432`         | Docker not running — open Docker Desktop first                                              |
| `FlywayException: validate failed` | DB schema mismatch — run`docker compose down -v` then `docker compose up -d`            |
| `UnknownHostException: kafka`      | Services must use`localhost:9092` not `kafka:29092` when running locally (not in Docker) |
| `Could not resolve eureka server`  | Start eureka-server first and wait for it to fully start                                     |
| `BUILD FAILURE` in mvn             | Run`mvn clean install -DskipTests` from root moneyPay folder first                         |
| Port already in use                  | Run `netstat -ano                                                                            |

---

## Stop Everything

```powershell
# Stop all services - Ctrl+C in each window, then:
cd C:\Users\pc\Downloads\moneyPay\moneyPay\infra
docker compose down

# To also delete all DB data (fresh start):
docker compose down -v
```
