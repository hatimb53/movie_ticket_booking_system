# 05 — Seat holds with pessimistic locking + expiry (centerpiece)

**What to build:** A customer places a time-bound hold on one or more seats for a show. When two
customers race for the same seat, exactly one wins and the other gets a clean "seat unavailable"
error — a seat is never double-allocated. Holds auto-release after a configurable TTL so abandoned
carts free up. This is the graded correctness centerpiece.

**Blocked by:** 04.

**Status:** done

- [x] `POST /shows/{id}/holds` places an all-or-nothing hold on the requested seats, returning a booking in PENDING_PAYMENT.
- [x] AVAILABLE→HELD transition uses a pessimistic write lock (`SELECT … FOR UPDATE`, `ShowSeatRepository.lockByIds`); multi-seat holds lock rows in deterministic id order.
- [x] Losing concurrent attempts fail cleanly (409 SeatUnavailableException), never double-allocate.
- [x] Lazy expiry: an expired HELD seat is treated as AVAILABLE and reclaimable under the lock.
- [x] `@Scheduled` `HoldSweeper` flips lapsed HELD rows back to AVAILABLE; TTL configurable (`app.hold.ttl-seconds`, default 300).
- [x] H2 configured with `LOCK_TIMEOUT=10000` so `FOR UPDATE` blocks (serializes) instead of erroring.
- [x] **Concurrency integration test:** 8 threads race one seat → exactly 1 win, 7 clean rejections, seat HELD, one booking.
- [x] **Expiry tests:** expired hold is reclaimable; sweeper releases lapsed holds.

**Note:** `Booking` entity introduced (PENDING_PAYMENT), owns `booking_id` FK on held seats via
`@JoinColumn` (no ShowSeat→booking code dependency). Concurrency test runs at the service seam
(non-transactional) so it commits data — list-based assertions elsewhere now assert by content, not
position.
