# 04 — Show scheduling, pricing & browse/seat-map

**What to build:** An admin schedules a show (movie + screen + start time + per-category base
prices), which eagerly materializes the per-seat inventory for that show with frozen prices. A
customer can browse shows filtered by city, movie, and date, and open a show's seat map showing
each seat's live availability and its server-computed price (seat-category base × weekend surcharge).

**Blocked by:** 03.

**Status:** ready-for-agent

- [ ] `POST` schedule show: movie + screen + startTime + regular/premium base prices; prices frozen onto the show.
- [ ] Eager `ShowSeat` materialization at scheduling: one row per screen seat, status AVAILABLE, frozen per-seat price.
- [ ] Pricing calculator (unit-tested): seat-category base × weekend surcharge (configurable multiplier/flat), server-side only.
- [ ] `GET /shows?city=&movieId=&date=` with pagination on the list.
- [ ] `GET /shows/{id}/seats` returns the seat map with live availability + per-seat price; held/booked seats marked unavailable.
- [ ] Unit tests for pricing (regular, premium, weekend combinations); integration test for schedule→browse→seat-map.
