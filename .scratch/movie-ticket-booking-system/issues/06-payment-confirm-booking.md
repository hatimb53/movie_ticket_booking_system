# 06 — Payment → confirmed booking + history

**What to build:** A customer pays for their held seats; on success the booking is confirmed and
the seats become BOOKED, on failure the booking is left unconfirmed (PAYMENT_FAILED) and the seats
release on hold expiry. The customer can view their booking history.

**Blocked by:** 05.

**Status:** done

- [x] `PaymentGateway` interface + deterministic `MockPaymentGateway` (token `"fail"` forces failure).
- [x] `POST /bookings/{id}/pay`: records a `Payment`, transitions PENDING_PAYMENT → CONFIRMED (seats HELD→BOOKED, 200) or → PAYMENT_FAILED (402).
- [x] HELD→BOOKED happens transactionally on successful payment, under the same pessimistic lock as holds.
- [x] `GET /bookings/me` returns the authenticated customer's bookings (seats, total, status).
- [x] Booking total computed server-side at hold (tier × weekend) and stored on the booking.
- [x] Integration tests: pay→CONFIRMED/seats BOOKED; forced failure→PAYMENT_FAILED/seat still HELD; double-pay→409; other customer→403.

**Note:** Pay re-locks and re-validates the hold (expired hold → 409) so payment can't race a
re-hold. Failed payment records a FAILED `Payment` and leaves seats HELD to expire via the sweeper.
