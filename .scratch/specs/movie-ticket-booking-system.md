---
title: Movie Ticket Booking System
status: ready-for-agent
type: spec
---

# Movie Ticket Booking System — Spec

## Problem Statement

Moviegoers in multiple cities want to browse what's playing and reserve specific seats
for a specific show, pay, and receive confirmation — with the confidence that the exact
seat they picked is theirs and will never be sold to someone else who clicked at the same
moment. If plans change, they want to cancel and get a fair, predictable refund. Theater
operators (admins) want to set up their cities, theaters, screens, seat layouts, shows,
prices, discounts, and refund rules, and trust that the system enforces them correctly.

The hard part — the part that ruins the experience if it's wrong — is two customers racing
for the last seat. A booking system that occasionally double-allocates a seat is worse than
useless. Everything else is table stakes; correct seat serialization is the product.

## Solution

A REST API, built on Spring Boot, that models the full venue hierarchy (city → theater →
screen → show), exposes seat-level booking with **time-bound holds** that auto-release on
expiry, and serializes concurrent bookings on the same seat via database-level pessimistic
locking so a seat is allocated to exactly one booking, ever.

Customers register/log in (JWT), browse shows filtered by city/movie/date, see a live seat
map with per-seat prices, place a hold on chosen seats, pay (mock gateway), and receive a
confirmed booking plus a non-blocking notification. They can view booking history and cancel
for a refund computed from a configurable, time-tiered policy. Admins manage the catalog,
seat layouts, per-show pricing, discount codes, and refund policies behind role-based access
control.

Pricing is computed server-side from a two-axis model (seat category × weekend surcharge),
with discount codes applied on the total. Notifications (confirmation, cancellation, reminder)
are dispatched asynchronously and never block or fail the booking transaction.

## User Stories

### Authentication & Access Control
1. As a visitor, I want to register as a customer, so that I can book tickets under my own account.
2. As a registered user, I want to log in and receive a JWT, so that I can make authenticated requests.
3. As a customer, I want my bookings tied to my account, so that I can see only my own history.
4. As an admin, I want endpoints that only admins can call, so that customers cannot mutate the catalog.
5. As a customer, I want a clear 403 when I hit an admin-only endpoint, so that boundaries are obvious.
6. As any client, I want a clear 401 when my token is missing/invalid/expired, so that auth failures are unambiguous.

### Admin — Catalog Management
7. As an admin, I want to create/list/update cities, so that I can define where theaters operate.
8. As an admin, I want to create theaters under a city, so that I can model each venue.
9. As an admin, I want to create screens (auditoriums) under a theater, so that I can model where shows play.
10. As an admin, I want to define a screen's seat layout by rows × seats-per-row with designated premium rows, so that seats are generated without per-seat tedium.
11. As an admin, I want to create/list/update movies (title, duration, language, rating), so that shows can reference them.
12. As an admin, I want to schedule a show (movie + screen + start time + per-category base prices), so that customers can book it.
13. As an admin, I want ShowSeat inventory generated automatically when I schedule a show, so that I don't manage seats per show manually.
14. As an admin, I want per-show base prices frozen at scheduling time, so that later price changes don't alter existing shows.

### Admin — Pricing, Discounts, Refund Policies
15. As an admin, I want to set regular and premium base prices per show, so that seating tiers are priced correctly.
16. As an admin, I want a configurable weekend surcharge (multiplier or flat), so that weekend shows are priced higher.
17. As an admin, I want to create discount codes (percentage or flat, with an optional cap), so that I can run promotions.
18. As an admin, I want discount codes to have a validity window, minimum booking amount, and global usage limit, so that promotions are bounded.
19. As an admin, I want to activate/deactivate a discount code, so that I can pull a promotion immediately.
20. As an admin, I want to define a time-tiered refund policy per theater (e.g. >24h → 100%, 2–24h → 50%, <2h → 0%), so that cancellations refund fairly.
21. As an admin, I want a system-default refund policy, so that theaters without an explicit policy still behave predictably.

### Customer — Browse & Discovery
22. As a customer, I want to list cities, so that I can pick where I'm watching.
23. As a customer, I want to list theaters in a city, so that I can choose a venue.
24. As a customer, I want to list/browse movies, so that I can decide what to watch.
25. As a customer, I want to filter shows by city, movie, and date, so that I can find a screening that fits.
26. As a customer, I want to see a show's seat map with live availability and per-seat price, so that I can choose seats.
27. As a customer, I want unavailable (held/booked) seats clearly marked, so that I don't try to book them.

### Customer — Booking Lifecycle
28. As a customer, I want to place a time-bound hold on one or more seats for a show, so that they're reserved while I pay.
29. As a customer, I want my hold to expire after a configurable TTL if I don't pay, so that I'm not charged for abandoned carts and seats free up.
30. As a customer, I want to see the total price (tiers + weekend + discount) before paying, so that I know what I'll be charged.
31. As a customer, I want to apply a discount code at checkout, so that I pay less when eligible.
32. As a customer, I want a clear error if my code is invalid/expired/exhausted/below minimum, so that I understand why it didn't apply.
33. As a customer, I want to pay for my held seats, so that my booking is confirmed.
34. As a customer, I want a failed payment to leave my booking unconfirmed (and seats to release on hold expiry), so that a failure doesn't strand me.
35. As a customer, I want a confirmed booking record with its seats, total, and payment, so that I have proof of purchase.
36. As a customer racing another customer for the same seat, I want exactly one of us to win cleanly, so that the seat is never double-allocated.
37. As the losing customer in a seat race, I want a clear "seat no longer available" error, so that I can pick another seat.
38. As a customer, I want to view my booking history, so that I can review past and upcoming bookings.

### Customer — Cancellation & Refunds
39. As a customer, I want to cancel a confirmed booking before showtime, so that I can get a refund.
40. As a customer, I want my refund amount computed from the applicable time-tiered policy, so that it's fair and predictable.
41. As a customer, I want my seats released on cancellation, so that others can book them.
42. As a customer, I want a refund record, so that I can see what was returned.
43. As a customer, I want cancellation blocked after showtime (or per policy), so that rules are enforced consistently.

### Notifications
44. As a customer, I want a confirmation notification after booking, so that I know it succeeded.
45. As a customer, I want a cancellation/refund notification, so that I know my cancellation processed.
46. As a customer, I want a reminder before my show, so that I don't forget.
47. As a customer, I want the booking to succeed even if notification delivery is slow/fails, so that my purchase is never blocked by messaging.

### Cross-cutting
48. As any client, I want consistent, structured JSON errors with correct HTTP status codes, so that I can handle failures programmatically.
49. As any client, I want request validation on all inputs, so that bad requests are rejected with clear messages.
50. As an operator, I want seed data on startup (cities/theaters/screens/movies/shows + an admin), so that the system is demonstrable immediately.

## Implementation Decisions

**Bias:** correctness-first, moderate breadth. The concurrent-seat-allocation guarantee is
the graded centerpiece; catalog CRUD and browse are happy-path.

### Domain model (JPA entities)
- `User` (email, bcrypt password, role ∈ {ADMIN, CUSTOMER}).
- Venue hierarchy: `City → Theater → Screen → Show`. `Show` = `Movie` + `Screen` + `startTime` + frozen per-category prices.
- `Movie` as its own entity (title, duration, language, rating).
- `Seat` belongs to a `Screen`; has `category ∈ {REGULAR, PREMIUM}` and a label (e.g. `A1`).
- `ShowSeat` is the per-(show × seat) inventory row: `status ∈ {AVAILABLE, HELD, BOOKED}`,
  `heldUntil`, `version`, and the frozen price for that seat in that show. Materialized
  **eagerly** when a show is scheduled.
- `Booking` (owner, show, status ∈ {PENDING_PAYMENT, CONFIRMED, PAYMENT_FAILED, CANCELLED},
  total, applied discount), with associated `ShowSeat`s.
- `Payment` (booking, status, amount, txn ref). `Refund` (booking, amount, reason/tier).
- `DiscountCode` (code, type ∈ {PERCENTAGE, FLAT}, value, maxDiscountAmount, validFrom,
  validUntil, minBookingAmount, usageLimit, usedCount, active).
- `RefundPolicy` (owning theater or system default) = ordered tiers {hoursBeforeShow → percent}.
- `Notification` (recipient, type, channel, payload, sentAt) — persisted history.
- Base auditing (`id`, `createdAt`, `updatedAt`) via `@MappedSuperclass`. Money as `BigDecimal`.

### Concurrency (centerpiece)
- The contended operation is the `AVAILABLE → HELD` transition on `ShowSeat` rows.
- Hold acquisition selects the target `ShowSeat` rows with a **pessimistic write lock**
  (`@Lock(PESSIMISTIC_WRITE)`, i.e. `SELECT … FOR UPDATE`) inside a single transaction,
  re-checks status (treating expired holds as available) under the lock, flips to `HELD`,
  and commits. Concurrent transactions block, then observe `HELD/BOOKED` and fail cleanly
  with a 409-style "seat unavailable."
- Multi-seat holds lock all requested rows in a deterministic order (by id) to avoid
  deadlocks; the hold is all-or-nothing.
- `HELD → BOOKED` happens on successful payment, within a transaction.
- H2 must be configured so `FOR UPDATE` genuinely blocks (URL-level lock settings, no MVCC
  short-circuit); this is validated by the concurrency test and documented as an H2-vs-Postgres
  caveat in the README.

### Hold expiry
- TTL configurable (default 5 min).
- **Lazy:** any read/hold attempt treats `HELD && heldUntil < now` as effectively AVAILABLE
  and may reclaim it under the pessimistic lock. Correctness never depends on the sweeper.
- **Sweeper:** a `@Scheduled` job (~30–60s) flips lapsed `HELD` rows back to `AVAILABLE` so
  listings stay accurate.

### Pricing & discounts
- Two-axis: seat-category base price (frozen per show) × weekend surcharge (configurable
  multiplier/flat, applied when the show date is a weekend). Computed **server-side** at
  hold/checkout; never trusted from the client.
- Discount applied to the booking total after tier pricing. Validation: exists, active,
  in-window, min met, usage limit not exhausted; percentage discounts capped by
  `maxDiscountAmount`. `usedCount` increment happens **inside** the booking-confirmation
  transaction so it can't over-redeem.

### Payment
- Explicit two-step: hold → `Booking(PENDING_PAYMENT)` → `POST pay` → `Booking(CONFIRMED)`
  or `PAYMENT_FAILED`.
- `PaymentGateway` interface with a deterministic `MockPaymentGateway` impl (a documented
  way to force failure for tests). Produces a `Payment` record.

### Refunds
- On cancel of a `CONFIRMED` booking before showtime: resolve the applicable `RefundPolicy`
  (booking → show → screen → theater, else system default), compute hours-until-show, select
  the matching tier percent, refund `total × percent` via the mock gateway (create `Refund`),
  release seats (`BOOKED → AVAILABLE`), and roll back the discount `usedCount`. Booking →
  `CANCELLED`.

### Auth & RBAC
- JWT bearer tokens; DB-persisted users; BCrypt passwords. `/auth/register`, `/auth/login`.
- Method-level `@PreAuthorize` distinguishing ADMIN vs CUSTOMER. Stateless security filter chain.

### Notifications
- Domain events published via `ApplicationEventPublisher`; `@Async` listeners on a dedicated
  `TaskExecutor` dispatch through a `NotificationService` interface (log + persist `Notification`).
- Decoupled from the booking transaction — booking commits regardless of notification outcome.
- `@Scheduled` reminder job scans upcoming shows and emits reminders.

### API surface (representative)
- Auth: `POST /auth/register`, `POST /auth/login`.
- Admin: CRUD for cities, theaters, screens (+ layout), movies, shows, discount codes, refund
  policies.
- Browse: `GET /cities`, `GET /cities/{id}/theaters`, `GET /movies`, `GET /movies/{id}`,
  `GET /shows?city=&movieId=&date=`, `GET /shows/{id}/seats`.
- Booking: `POST /shows/{id}/holds`, `POST /bookings/{id}/pay`, `POST /bookings/{id}/cancel`,
  `GET /bookings/me`.
- Pagination on growable lists (shows, movies); simple elsewhere.

### Cross-cutting
- Global `@RestControllerAdvice` → consistent JSON error body
  (`timestamp, status, error, message, path`). Bean Validation (`@Valid`) on all request DTOs.
- DTOs at the boundary (never expose entities); manual mappers (avoid annotation-processor
  friction with Lombok).
- HTTP codes: 400 (validation), 401 (auth), 403 (RBAC), 404 (not found), 409 (seat conflict /
  state conflict), 422 where semantically apt.
- Seed data via `CommandLineRunner`.
- Persistence: H2 for dev + test; Postgres documented as a config swap.

## Testing Decisions

**What makes a good test here:** it asserts *external behavior* — HTTP status, response body,
persisted state transitions, and the no-double-allocation invariant — not internal method calls
or private structure. The suite is a focused pyramid weighted to the booking core, not
coverage-chasing.

**Seams (confirmed):**
- **Primary — HTTP boundary** (`@SpringBootTest` + `MockMvc` against H2): the one seam for
  full-flow tests. Covers register/login + RBAC, admin catalog setup, browse/filter, seat map,
  hold → pay → confirm, cancel → refund, discount application, and the error/validation contract.
- **Service seam — concurrency only:** the concurrent test drives the booking/hold *service*
  directly with N real threads (MockMvc's per-request model can't express a true race), asserting
  exactly one `CONFIRMED` and the rest cleanly rejected, with no seat double-allocated. Plus an
  expiry test: a hold lapses → the seat becomes bookable again.
- **Pure unit seam:** plain unit tests (no Spring context) for pricing (tiers + weekend),
  discount validation/application (incl. cap and exhaustion), and refund-tier resolution.

**Modules tested:** booking/hold service (incl. concurrency + expiry), pricing calculator,
discount service, refund service, auth/RBAC filter, and the controller flows end-to-end.

**Non-negotiable centerpieces:** the concurrency test and the hold-expiry test. RBAC negative
tests (customer → admin endpoint → 403) are required.

**Prior art:** none yet — this establishes the test conventions for the repo. The existing
`contextLoads` smoke test stays as the baseline sanity check.

## Out of Scope

- UI / frontend of any kind.
- Deployment, containerization, CI/CD. (Testcontainers considered and dropped — no Docker.)
- Distributed systems / microservices / message brokers.
- Advanced auth: OAuth, SSO, MFA, refresh-token rotation, password reset flows.
- Production observability/monitoring/alerting.
- Real payment gateway integration (mock only).
- Real notification delivery (email/SMS) — logged + persisted only.
- Exhaustive coverage; exotic admin edge cases and CRUD gold-plating.

## Further Notes

**Stretch (only if time remains, in rough priority):**
1. Per-user discount redemption tracking (one use per user).
2. Per-show refund-policy override on top of theater-level.
3. Search-by-title / "now showing / upcoming" browse enhancements.

**Known caveat to document in README:** H2's locking semantics differ from Postgres; the H2
datasource is configured so `SELECT … FOR UPDATE` genuinely serializes, and the concurrency
test proves it — but the production-intent target is Postgres.
