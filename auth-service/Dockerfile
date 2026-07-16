# ── Stage 1: Build ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace

# Copy parent pom and common-lib first (layer cache)
COPY pom.xml ./
COPY common-lib/pom.xml common-lib/
COPY auth-service/pom.xml auth-service/

# Download dependencies
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -pl common-lib,auth-service -am -q 2>/dev/null || true

# Copy source
COPY common-lib/src common-lib/src
COPY auth-service/src auth-service/src

# Build
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -pl common-lib,auth-service -am -DskipTests --no-transfer-progress

# ── Stage 2: Runtime ────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Non-root user for security
RUN addgroup -S moneypay && adduser -S moneypay -G moneypay
USER moneypay

# JVM tuning for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

COPY --from=builder /workspace/auth-service/target/*.jar app.jar

EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
