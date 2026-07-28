    # 08 — Cancellation & refunds

**What to build:** An admin defines time-tiered refund policies (per theater, plus a system
default). A customer cancels a confirmed booking before showtime and receives a refund computed
from the applicable policy tier; their seats are released, any discount usage is rolled back, and a
refund record is created.

**Blocked by:** 07.

**Status:** ready-for-agent

- [ ] `RefundPolicy` (ordered tiers {hoursBeforeShow → percent}), admin CRUD, theater-owned + a system default.
- [ ] `POST /bookings/{id}/cancel` on a CONFIRMED booking before showtime → CANCELLED.
- [ ] Policy resolution: booking → show → screen → theater, falling back to system default; compute hours-until-show → tier percent.
- [ ] Refund `total × percent` via the mock gateway; create a `Refund` record.
- [ ] Seats released (BOOKED→AVAILABLE); discount `usedCount` rolled back.
- [ ] Cancellation blocked after showtime (or per policy) with a clear error.
- [ ] Integration tests: cancel at different lead times → different refund %, seats freed, discount usage restored.
