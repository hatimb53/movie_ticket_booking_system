# 05 — Seat holds with pessimistic locking + expiry (centerpiece)

**What to build:** A customer places a time-bound hold on one or more seats for a show. When two
customers race for the same seat, exactly one wins and the other gets a clean "seat unavailable"
error — a seat is never double-allocated. Holds auto-release after a configurable TTL so abandoned
carts free up. This is the graded correctness centerpiece.

**Blocked by:** 04.

**Status:** ready-for-agent

- [ ] `POST /shows/{id}/holds` places an all-or-nothing hold on the requested seats, returning a booking in PENDING_PAYMENT.
- [ ] AVAILABLE→HELD transition uses a pessimistic write lock (`SELECT … FOR UPDATE`); multi-seat holds lock rows in deterministic id order to avoid deadlocks.
- [ ] Losing concurrent attempts fail cleanly (409-style "seat unavailable"), never double-allocate.
- [ ] Lazy expiry: an expired HELD seat is treated as AVAILABLE and reclaimable under the lock.
- [ ] `@Scheduled` sweeper flips lapsed HELD rows back to AVAILABLE; TTL configurable (default 5 min).
- [ ] H2 configured so `FOR UPDATE` genuinely serializes (no MVCC short-circuit).
- [ ] **Concurrency integration test:** N threads race one seat → exactly one CONFIRMED-eligible hold, rest rejected, no double-allocation.
- [ ] **Expiry test:** a hold lapses → the seat becomes bookable again.
