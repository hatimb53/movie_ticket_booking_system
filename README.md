# Movie Ticket Booking System

A Spring Boot REST API for seat-level movie ticket booking across multiple cities and theaters,
with time-bound seat holds, tiered pricing, discount codes, mock payments, configurable refunds,
and non-blocking notifications. Concurrent booking attempts on the same seat are serialized at the
database level so a seat is never double-allocated.

> SDE-2 take-home. Scope, entity choices, and edge-case coverage were deliberately owned by the
> implementer; the meaningful decisions are documented below.

## Tech stack & why

| Choice | Reason |
| --- | --- |
| **Spring Boot 3.3 / Java 17** | Required stack; mature ecosystem for REST + JPA + security. |
| **Spring Data JPA / Hibernate** | Declarative persistence; first-class **pessimistic locking** for the concurrency core. |
| **H2 (in-memory)** | Zero-setup dev/test DB. Honors `SELECT … FOR UPDATE`, so the concurrency guarantee is testable offline. **Postgres is a config swap** (see caveat). |
| **Spring Security + JWT (jjwt)** | Stateless role-based access without pulling in OAuth/SSO (explicitly out of scope). |
| **JUnit 5 + MockMvc + Awaitility** | HTTP-seam integration tests, real multi-threaded concurrency test, async assertions. |

## Build, run, test

Requires **JDK 17**. On this machine the `java` on `PATH` is 8, so point `JAVA_HOME` at a JDK 17:

```bash
export JAVA_HOME=/c/Users/ROG/.jdks/azul-17.0.10   # adjust to your JDK 17

mvn test          # run the full test suite
mvn spring-boot:run   # start the API on http://localhost:8080
```

Start with demo data seeded (admin + customer + shows + a discount + refund policies):

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--app.seed.enabled=true
# admin@mtbs.com / admin123     (ADMIN)
# customer@mtbs.com / customer123 (CUSTOMER)
```

H2 console (dev): `http://localhost:8080/h2-console` — JDBC URL `jdbc:h2:mem:mtbs`, user `sa`.

## Core flow

```
register/login → browse shows → hold seats (time-bound) → pay → CONFIRMED booking
                                          │                          │
                                    auto-expire if unpaid       cancel → tiered refund
```

1. **Auth** — `POST /auth/register` (creates a CUSTOMER), `POST /auth/login` → JWT. Send it as
   `Authorization: Bearer <token>`.
2. **Browse** — `GET /shows?city=&movieId=&date=`, `GET /shows/{id}/seats` (live availability + price).
3. **Hold** — `POST /shows/{id}/holds` with the chosen `showSeatIds` (+ optional `discountCode`) →
   a `PENDING_PAYMENT` booking; seats are held for a configurable TTL (default 5 min).
4. **Pay** — `POST /bookings/{id}/pay` → `CONFIRMED` (seats BOOKED) or `PAYMENT_FAILED` (402).
5. **History / cancel** — `GET /bookings/me`, `POST /bookings/{id}/cancel` (time-tiered refund).
6. **Admin** — `/admin/**` (cities, theaters, screens + seat layout, movies, shows, discounts,
   refund policies), all `ADMIN`-only.

## Design decisions & assumptions

- **Venue model.** `City → Theater → Screen → Show`. A `Screen` owns a fixed seat layout;
  a `Show` is a `Movie` on a `Screen` at a time. Seat layouts are generated from
  `{rows, seatsPerRow, premiumRows}` (labels `A1…`, rolling over to `AA` past `Z`).
- **Seat inventory.** Scheduling a show **eagerly materializes** one `ShowSeat` per seat
  (`AVAILABLE → HELD → BOOKED`) with its price **frozen** onto the row, so later price changes never
  affect an existing show.
- **Concurrency (the graded core).** The `AVAILABLE → HELD` transition happens under a
  **pessimistic write lock** (`SELECT … FOR UPDATE`, seats locked in id order to avoid deadlocks).
  Concurrent attempts on the same seat serialize: exactly one wins, the rest get a clean 409. An
  expired hold is reclaimable under the same lock; a `@Scheduled` sweeper also releases lapsed holds
  so listings stay clean. Payment re-locks and re-validates the hold before charging.
- **Pricing.** Two axes: **seat-category base** (regular/premium, per show) × **weekend surcharge**
  (Sat/Sun, `app.pricing.weekend-multiplier`, default 1.25). Always computed server-side.
- **Discounts.** Percentage/flat with optional cap, validity window, minimum, and a **global usage
  limit**. Validated at hold; **redeemed at confirmation under a row lock** so the limit can't be
  over-redeemed, and only a successful payment consumes the code.
- **Payment.** A `PaymentGateway` interface with a deterministic `MockPaymentGateway` (token
  `"fail"` forces failure) — swap in a real provider without touching the booking flow.
- **Refunds.** Time-tiered `RefundPolicy` per theater, falling back to a system default (and to
  100% if none configured). Cancelling before showtime refunds `total × tier%` via the gateway,
  releases seats, and rolls back discount usage.
- **Notifications.** Confirmation/cancellation are published as domain events and delivered by an
  `@Async` `@TransactionalEventListener(AFTER_COMMIT)` — **never blocking or failing the booking**.
  A `@Scheduled` reminder job notifies once for imminent shows. Delivery logs + persists a
  `Notification` (a real email/SMS channel would implement the same interface).
- **Access control.** JWT + BCrypt; `ADMIN` manages the catalog, `CUSTOMER` books/cancels/views own
  history. Registration only creates customers; admins are provisioned via seed data.
- **Errors.** Consistent JSON body `{timestamp, status, error, message, path, fieldErrors}` with
  appropriate codes (400 validation, 401, 403, 404, 409 seat/state conflict, 402 payment,
  422 discount-not-applicable).

### Assumptions

- Money is `BigDecimal`, 2-decimal, HALF_UP. Show times are local `LocalDateTime` (no timezone
  modeling). "Weekend" = Saturday/Sunday of the show's date.
- One booking attempt per hold; a hold is all-or-nothing across its seats.
- A failed payment leaves seats HELD to expire naturally (no immediate release).

## Concurrency test & the H2 caveat

The centerpiece test (`HoldConcurrencyTest`) races **8 threads** for a single seat and asserts
exactly one confirmed hold with no double-allocation — running at the service layer with real
committed transactions (an HTTP single-request test can't express a true race).

**H2 vs Postgres.** H2's locking semantics differ from a production database. The datasource URL
sets `LOCK_TIMEOUT=10000` so `SELECT … FOR UPDATE` **blocks** (serializes) rather than erroring, and
the concurrency test proves the guarantee on H2. The production-intent target is **PostgreSQL**,
reached by swapping the datasource (and driver dependency) — the JPA/locking code is unchanged.

## Out of scope (per the brief)

UI/frontend, deployment/containerization/CI-CD, microservices/message brokers, OAuth/SSO/MFA,
production observability, real payment/notification providers. Testcontainers was considered for
Postgres-backed tests but dropped (no Docker available); hence H2 with the caveat above.

## Possible extensions (not built)

Per-user discount redemption limits, per-show refund-policy overrides, search-by-title / "now
showing", and payment retry/idempotency keys.

## Project layout

`com.mtbs.{auth, catalog, show, booking, payment, discount, refund, notification, common}` — each a
vertical slice (domain + repository + service + controller). Feature build history is in
`.scratch/` (spec + per-ticket slices), retained as evidence of the AI-assisted workflow.
