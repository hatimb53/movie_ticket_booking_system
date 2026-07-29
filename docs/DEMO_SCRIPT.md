# Demo Video Script (target: 10 minutes, Loom)

Per the assignment brief, the recording must cover four things: **approach & solution at a high
level**, **tech stack & reasoning**, **the AI workflow used**, and **the testing approach**. This
script time-boxes each section and lists what to show on screen. Adjust pacing live — the numbers
are a budget, not a script to read verbatim.

---

## 1. Approach & solution at a high level (~2.5 min)

**Say:**
- This is a Movie Ticket Booking System REST API: `City → Theater → Screen → Show`, with seat-level
  booking. The brief was intentionally open-ended (SDE-2 take-home, "you own the scoping
  decisions") — so a meaningful part of the work was *deciding* scope, not just building.
- Walk the core flow: register/login → browse shows → hold seats (time-bound) → pay → CONFIRMED
  booking, with auto-expiry if unpaid and tiered refunds on cancellation.
- Call out the one place correctness (not just features) is explicitly graded: **concurrent booking
  attempts on the same seat must serialize with no double-allocation.** This shaped the whole
  design — seat holds use pessimistic locking (`SELECT ... FOR UPDATE`), not an optimistic
  assumption that requests arrive one at a time.
- Two features built beyond the literal brief, documented as deliberate assumptions in
  `README.md`: **show-cancellation** (admin can cancel a scheduled show, refunding confirmed
  bookings and expiring unpaid holds) and a **screen double-booking guard** (a new show can't
  overlap an existing one on the same screen within a 30-minute buffer, itself
  concurrency-safe).

**Show on screen:**
- `README.md` — the core-flow diagram and the "Design decisions & assumptions" section.
- Swagger UI (`http://localhost:8080/swagger-ui.html`) — log in as the seeded customer, browse a
  show, hold a seat, pay, view booking history. This is the fastest way to prove the whole flow
  works end-to-end live.

---

## 2. Tech stack & reasoning (~2 min)

**Say, from the README's tech-stack table:**
- **Spring Boot 3.3 / Java 17** — the fixed required stack.
- **Spring Data JPA / Hibernate** — declarative persistence, and critically, first-class
  **pessimistic locking** support, which is the mechanism the concurrency guarantee is built on.
- **H2 (in-memory)** for dev/test — zero setup, and it honors `SELECT ... FOR UPDATE` so the
  concurrency test is provable offline without spinning up a real database. Explicitly **not**
  the production target: the datasource is a config swap to Postgres, no JPA/locking code changes
  needed. Flag the caveat: H2's locking semantics differ from Postgres in real deployments, so
  `LOCK_TIMEOUT=10000` is set to make `FOR UPDATE` block rather than error, matching the intended
  production behavior closely enough to trust the test.
- **Spring Security + JWT (jjwt)** — stateless role-based access (admin/customer) without pulling
  in OAuth/SSO, which the brief explicitly puts out of scope.
- **JUnit 5 + MockMvc + Awaitility** — HTTP-seam integration tests, a real multi-threaded
  concurrency test, and `Awaitility` for asserting on the async notification path without sleeps.

**Show on screen:** `pom.xml` briefly, or just the tech-stack table in `README.md`.

---

## 3. The AI workflow used (~2.5 min)

**Say:**
- Built with Claude Code using a structured skill pipeline (`.claude/skills/`), not ad-hoc
  prompting: `/grilling` or `/grill-me` to interview and pressure-test scope decisions against the
  PDF brief → `/to-spec` to turn that discussion into a written PRD → `/to-tickets` to break the
  spec into 10 vertical-slice tickets (each a complete, demoable slice: foundation, auth, admin
  catalog, show scheduling, seat holds, payment, discounts, cancellation/refunds, async
  notifications, README/seed data) → `/tdd` to implement each ticket red-green → `/code-review`
  before calling a change done.
- All of this is preserved as evidence: `.scratch/specs/movie-ticket-booking-system.md` (the spec),
  `.scratch/movie-ticket-booking-system/issues/01..10` (the tickets in dependency order), and
  `CLAUDE.md` (the persistent project instructions the AI followed every session — assignment
  constraints, the concurrency requirement, deliverable checklist).
- Also used `graphify` mid-project to build a knowledge graph of the codebase
  (`graphify-out/graph.json`, `.claude/skills/graphify/`) for faster codebase orientation on
  later features (e.g., checking module dependencies before adding show-cancellation).
- Ran `/code-review` (two-axis: standards conformance + spec conformance, via parallel sub-agents)
  against the whole diff before finalizing, to catch design smells and confirm every ticket
  requirement actually landed.
- Note the honest gap: 5 skills that were installed (`ask-matt`, `codebase-design`,
  `domain-modeling`, `diagnosing-bugs`, `implement`) were never actually exercised and were removed
  rather than left as unused clutter — the skill set reflects what was really used, not what was
  available.

**Show on screen:**
- `.claude/skills/` directory listing (8 skills remaining).
- `.scratch/movie-ticket-booking-system/issues/` — the 10 ticket files.
- `git log --oneline` — one commit per ticket, plus feature commits after the initial build
  (payment/booking decoupling, seat-expiry sync, show-overlap guard, pricing config, show
  cancellation), each on `develop` then merged to `master`.

---

## 4. Testing approach (~2.5 min)

**Say:**
- Tests live at the HTTP seam (`MockMvc`) for most flows — the public contract, not
  implementation details — plus a couple of tests at the service seam where MockMvc's
  single-request model can't express what's being tested.
- **The concurrency centerpiece:** `HoldConcurrencyTest` — 8 real threads race to hold the *same*
  seat simultaneously via `CountDownLatch`-synchronized start. Assertion: exactly one hold wins,
  the other 7 get a clean rejection, and the seat is never double-allocated. This runs against
  real committed transactions on H2's `SELECT ... FOR UPDATE`, not a mock. There's a second
  concurrency test the same shape for the show-overlap guard (`ShowOverlapTest`) — 8 threads
  racing to schedule overlapping shows on one screen, exactly one wins.
- Async notification delivery is tested with `Awaitility` (`NotificationDeliveryTest`) — asserts
  the booking is confirmed *synchronously* on the response, then the confirmation notification
  arrives *after* commit, off the request thread — proving notifications never block booking.
- Coverage across every domain module: auth/RBAC, admin catalog, show scheduling + pricing +
  browse, seat holds + expiry, payment + booking confirmation, discount codes, cancellation +
  refunds, show cancellation, pricing config. 45 tests total, all green.
- One deliberate testing gap, documented rather than hidden: no Testcontainers/Postgres-backed
  integration test — no Docker available in this environment — so the concurrency guarantee is
  proven on H2 with the `LOCK_TIMEOUT` caveat explained above, and the datasource swap to Postgres
  is a config change, not a code change.

**Show on screen:**
- `mvn test` running live, or the summary output (45 tests, 0 failures).
- Open `HoldConcurrencyTest.java` — walk the thread-pool/latch setup and the final assertions.

---

## Wrap (~30 sec)

- One-line summary: core flows + the graded concurrency guarantee are built and tested; scope
  decisions and known gaps are documented in `README.md` rather than left implicit; the AI workflow
  used is fully preserved in `.scratch/` and `.claude/skills/` as required.
