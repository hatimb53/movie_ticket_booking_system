# 06 — Payment → confirmed booking + history

**What to build:** A customer pays for their held seats; on success the booking is confirmed and
the seats become BOOKED, on failure the booking is left unconfirmed (PAYMENT_FAILED) and the seats
release on hold expiry. The customer can view their booking history.

**Blocked by:** 05.

**Status:** ready-for-agent

- [ ] `PaymentGateway` interface + deterministic `MockPaymentGateway`, with a documented way to force failure for tests.
- [ ] `POST /bookings/{id}/pay`: records a `Payment`, transitions PENDING_PAYMENT → CONFIRMED (seats HELD→BOOKED) or → PAYMENT_FAILED.
- [ ] HELD→BOOKED happens transactionally on successful payment.
- [ ] `GET /bookings/me` returns the authenticated customer's bookings (with seats, total, payment).
- [ ] Booking total computed server-side (tier × weekend) and stored on the booking.
- [ ] Integration tests: hold→pay→CONFIRMED happy path; forced payment failure leaves booking unconfirmed and seats releasable.
