    # 08 — Cancellation & refunds

**What to build:** An admin defines time-tiered refund policies (per theater, plus a system
default). A customer cancels a confirmed booking before showtime and receives a refund computed
from the applicable policy tier; their seats are released, any discount usage is rolled back, and a
refund record is created.

**Blocked by:** 07.

**Status:** done

- [x] `RefundPolicy` (tiers {hoursBeforeShow → percent} as an @ElementCollection), admin upsert + list, theater-owned + a system default (theaterId null).
- [x] `POST /bookings/{id}/cancel` on a CONFIRMED booking before showtime → CANCELLED.
- [x] Policy resolution: theater policy → system default → built-in 100% fallback; hours-until-show → highest matching tier percent.
- [x] Refund `total × percent` via the mock gateway (`PaymentGateway.refund`); create a `Refund` record.
- [x] Seats released (BOOKED→AVAILABLE) under the pessimistic lock; discount `usedCount` rolled back.
- [x] Cancellation blocked after showtime (409) and on non-CONFIRMED bookings (409); other customer → 403.
- [x] Integration tests: 100% (>24h) with discount rollback + seat release, 50% (~5h) partial, unpaid → 409.
