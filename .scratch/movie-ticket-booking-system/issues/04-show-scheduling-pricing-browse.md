# 04 — Show scheduling, pricing & browse/seat-map

**What to build:** An admin schedules a show (movie + screen + start time + per-category base
prices), which eagerly materializes the per-seat inventory for that show with frozen prices. A
customer can browse shows filtered by city, movie, and date, and open a show's seat map showing
each seat's live availability and its server-computed price (seat-category base × weekend surcharge).

**Blocked by:** 03.

**Status:** done

- [x] `POST` schedule show: movie + screen + startTime + regular/premium base prices; prices frozen onto the show.
- [x] Eager `ShowSeat` materialization at scheduling: one row per screen seat, status AVAILABLE, frozen per-seat price.
- [x] Pricing calculator (unit-tested): seat-category base × weekend surcharge (configurable multiplier), server-side only.
- [x] `GET /shows?city=&movieId=&date=` with pagination (`page`/`size` → `PageResponse`).
- [x] `GET /shows/{id}/seats` returns the seat map with live availability + per-seat price; expired holds presented AVAILABLE.
- [x] Unit tests for pricing (regular/premium × weekday/weekend); integration tests for schedule→browse→seat-map and weekend surcharge.

**Note:** `ShowSeat` carries status/heldUntil/@Version now (all AVAILABLE); the hold transitions +
pessimistic lock land in ticket 05. Weekend = Sat/Sun on the show's `startTime`; multiplier from
`app.pricing.weekend-multiplier` (default 1.25). Browse param is `city` (cityId).
