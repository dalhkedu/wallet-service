# Architectural Design Record & Software Development Plan: `ms-wallet-digital`

---

## 1. Executive Summary & Quality Framework Alignment

This document serves as the official **Architectural Design Record (ADR)** and **Development Plan** for the Digital Wallet Microservice (`ms-wallet-digital`). This service represents a mission-critical component within the payment ecosystem ; its zero-downtime availability, strict transaction correctness, and absolute traceability are foundational to the platform’s business continuity.

The architectural blueprint is engineered by cross-referencing industry standard frameworks:

*

**ISO/IEC 25010 Quality Model:** Maximizing *Functional Correctness* (zero overdraft) , *Reliability* (fault tolerance and maturity via transactional ledgers) , *Security* (non-repudiation and identity isolation) , and *Performance Efficiency* (asynchronous scale).

*

**AWS Well-Architected Framework:** Implementing *Operational Excellence* through comprehensive telemetry , *Security* via defense-in-depth and least-privilege service interactions , and *Reliability* by implementing automated recovery and asynchronous loose-coupling.

*

**iSAQB Core Principles:** Ensuring clean separation of concerns, explicit definition of system boundaries, domain-driven boundaries, and context-specific architectural views.

---

## 2. Core Assumptions & Business Boundaries

The system design relies on the following structural constraints and boundaries:

### Upstream/Satelite System Boundaries

The `ms-carteira-digital` isolates core financial value movements. It explicitly delegates out-of-domain functions to satelite systems managed by separate specialized teams:

*

**Customer Auth/Login Management:** Handles human identity federation, access tokens, and credential rotation.

*

**Fraud Detection Engine (Antifraud):** Evaluates behavioral transaction risks synchronously before financial settlement occurs.

*

**Data Protection & Privacy Vault:** Mandates zero exposure of Personally Identifiable Information (PII) within the transaction ledger.

*

**Core Account Management Engine:** Manages physical banking or deposit account status and external ledger funding.

*

**Notification Engine:** Assynchronously handles user alerts via Push, SMS, and Email.

### Structural Business Restrictions

*

**Currency Constriction:** The service processes transactions exclusively using the Brazilian Real (BRL) currency.

*

**Cardinality Rules:** A single natural person (*Pessoa Física - PF*) can map to only one active account. Consequently, an account maps directly to **exactly one digital wallet instance**.

*

**Movement Limitations:** The wallet functions strictly as an append-only balance manager linking values to account structures. Overdraft configurations are explicitly forbidden; transactions cannot cause balances to fall below zero ($0.00$).

---

## 3. Transaction Flow & Orchestration Engine

To safeguard the core ledger against distributed race conditions, all transactional requests pass through a centralized **Transaction Orchestrator**. The orchestrator functions as a state machine backed by Amazon DynamoDB for rapid distributed lock management and idempotency checks.

### The Fail-Fast Pipeline Validation

Before executing any state alteration within the relational database, the transaction orchestrator enforces a sequential **Fail-Fast** screening pipeline:

```
[BFF / Client Request]
          │
          ▼
1. Idempotency Check ───────> [DynamoDB lookup via uuidv5_timestamp]
          [cite_start]│                   * Abort instantly if duplicate request is active[cite: 277].
          ▼
2. PF Status Validation ────> [Query Profile Vault]
          [cite_start]│                   * Block if CPF or account is inactive/frozen[cite: 300].
          ▼
3. Risk Assessment ─────────> [Query Antifraud Service]
          [cite_start]│                   * Reject if behavioral flag is raised[cite: 303].
          ▼
4. Funding Source Check ────> [Query Core Account Management]
          [cite_start]│                   * Verify structural account connectivity[cite: 306].
          ▼
5. Asynchronous Core Dispatch 

```

### Unified Idempotency Matrix

To prevent duplicate financial settlements, every state change request must feature a deterministic idempotency handle generated via a SHA-1 `UUID v5` hash appended with a timestamp:

$$\text{IdempotencyKey} = \text{uuidv5}(\text{from} + \text{to} + \text{valor}) + \text{"\_"} + \text{timestamp}$$

| Transaction Type | Context | Source Identity (`from`) | Destination Identity (`to`) | Internal Operational Mechanics |
| --- | --- | --- | --- | --- |
| **UC06: Deposit** | Funding wallet | `accountId` (Core Bank Account) | `walletId` (Digital Wallet) | Captures value from external core checking account into the digital wallet.

|
| **UC07: Withdraw** | Defunding wallet | `walletId` (Digital Wallet) | `accountId` (Core Bank Account) | Retains and transfers value out of digital wallet to personal bank accounts.

|
| **UC08: Transfer** | Wallet Peer Shift | `walletId` (Origin Wallet) | `walletId` (Target Wallet) | Executes peer-to-peer balance adjustments inside the internal microservice ledger .

|

---

## 4. Dual-State Resilience & Messaging Topology

The system handles write performance limits and network partitions through an asynchronous messaging model complemented by a synchronous fallback override loop.

### Asynchronous Event Topography

Transactions route through dedicated AWS SQS queue networks and SNS topics to ensure structural decoupling :

```
                                  [Transaction Requests]
                                            │
                                            ▼
                           [cite_start]Tópico: wallet-transaction-requests-topic [cite: 96]
                                            │
                                            ▼
                           [cite_start]Fila: wallet-ms-transfer-commands-queue [cite: 97]
                                            │
                                            ▼
                                  [MS Carteira Digital]
                                            │
                                            ▼
                            [cite_start]Tópico: wallet-transaction-results-topic [cite: 106]
                                            │
                                            ▼
                            [cite_start]Fila: orchestrator-wallet-callback-queue [cite: 108]
                                            │
                                            ▼
                                [Update DynamoDB Engine]

```

### The Synchronous Fallback Mechanism (*Force Exec*)

If the downstream consumer experiences latency and the processing state remains `PENDING` during a user check-status poll, the system routes through an explicit **Force Exec** desynchronization path:

```
[cite_start]BFF/Canal ──(Get Status)──> Transaction Orchestrator (DynamoDB Status: PENDING) [cite: 30, 202]
                                │
                        (POST /balance [Force Sync Mode]) [cite_start][cite: 83, 204]
                                │
                                ▼
                      MS Carteira Digital (SQL Core)

```

> ⚠️ **Critical Double-Spending Counter-Measure:**
> When executing a withdrawal or transfer request, `ms-carteira-digital` utilizes a relational `SELECT ... FOR UPDATE` isolation block or row-level pessimistic locking to isolate the user's available balance. The balance required for the transaction is moved to a temporary `LOCKED` state within the relational database row before initiating external actions. If the asynchronous queue successfully handles the movement later, or if a *Force Exec* call completes the request synchronously, the orchestrator clears the lock. If the operation reaches a final error state or exhausts all 3 execution retries, the orchestrator issues an internal command to release the temporary lock and revert the balance.
>
>

---

## 5. API Contracts & Interface Specifications

The service surfaces its interfaces via standard REST endpoints utilizing structured JSON payloads:

```
# [cite_start]Matrix of API Status Codes & System Boundary Responses [cite: 250]
[cite_start]200 OK          -> successful execution of get query operations[cite: 37, 250].
[cite_start]201 Created     -> structural finalization of write workflows[cite: 38, 250].
[cite_start]400 Bad Request -> failure of contract payload validation, or zero-overdraft violation[cite: 36, 250].
[cite_start]404 Not Found   -> digital wallet identity matching constraints do not exist[cite: 35, 250].
[cite_start]500 Error       -> critical platform issues; triggers automatic engineering infrastructure alerts[cite: 34, 250].

```

### GET `/wallets`

Returns the active wallet identifier linked to the target customer identifier.

*

**Query Parameters:** `clientId` (Required), `accountId` (Required)

* **Response Payload (`200 OK`):**

```json
{
  "data": {
    "wallets": [
      {
        "walletId": "wlt_91a0b3c2-d4e5-6f7g-8h9i-0j1k2l3m4n5o",
        "walletName": "Main Digital Wallet"
      }
    ]
  }
}

```

### GET `/wallets/{walletId}`

Retrieves balance information for a specific wallet. It supports a time travel parameter to reconstruct historic transaction records.

*

**Query Parameters:** `atDate` (Optional, ISO-8601 Timestamp)

* **Response Payload (`200 OK`):**

```json
{
  "data": {
    "walletName": "Main Digital Wallet",
    "walletBalance": 1500.50
  }
}

```

### POST `/wallet`

Initializes a single wallet structure with a baseline balance of zero.

* **Request Body Payload:**

```json
{
  "walletName": "Main Digital Wallet",
  "accountId": "acc_88772211",
  "clientId": "cli_33445566"
}

```

### POST `/wallet/{walletId}/balance`

Direct synchronous bypass used by the orchestrator engine during a fallback event.

* **Request Body Payload:**

```json
{
  "transferId": "tx_77665544",
  "from": {
    "walletId": "wlt_0j1k2l3m4n5o",
    "balance": 250.00
  },
  "to": {
    "walletId": "wlt_91a0b3c2-d4e5"
  }
}

```

---

## 6. Governance, Observability & Quality Assurance

To fulfill the technical and operational readiness metrics required for high-availability deployments, the microservice implements automated quality checks throughout its delivery cycle .

### Unified Telemetry Logging Standard

To ensure audit compliance and visibility, every operational log must output in structural JSON format and include four required tracing variables:

```json
{
  "timestamp": "2026-05-27T18:07:49.000Z",
  "log_level": "INFO",
  "correlation_id": "corr_bf889c22-e19a-4712",
  "flow": "p2p_wallet_transfer_v1",
  "client_id": "cli_33445566",
  "account_id": "acc_88772211",
  "message": "Financial atomic transfer successfully committed within SQL ledger boundary."
}

```

### Continuous Integration & Testing Guardrails

Deployment pipelines are automated using a git-based layout and adhere to strict quality rules :

```
[cite_start][Code Push] ──> [SonarQube Verification] ──> [Test Pyramid Run] ──> [Canary Rollout] [cite: 112, 113, 258, 260]

```

*

**SonarQube Gates:** Rejects code changes if code smells are discovered, static code patterns breach alignment rules, or hardcoded sensitive variables are exposed.

* **The Code Quality Pyramid:**
*

**Unit Tests:** Enforces a minimum **80% line test coverage requirement** across all domain logic modules.

*

**Mutation Testing:** Enforces a **80% mutation target score** via PITest. This guarantees that unit test suites are robust enough to catch logic mutations, rather than just hitting code coverage metrics.

*

**E2E Contract Specifications:** Enforces automated API schema checks to protect against breaking changes in interface models.

*

**Production Release Profile:** Relies on **Canary Deployments** on Amazon EKS. Traffic shifts progressively to the new build while monitoring platform exception metrics. Any increase in HTTP `500` status codes automatically stops the deployment and triggers an immediate rollback.
