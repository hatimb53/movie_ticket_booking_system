# 07 — Discount codes

**What to build:** An admin creates and manages discount codes (percentage or flat, with an
optional cap, validity window, minimum booking amount, and global usage limit). A customer applies
a code at checkout and pays less when eligible; ineligible codes are rejected with a clear reason,
and the usage limit can never be over-redeemed under concurrency.

**Blocked by:** 06.

**Status:** done

- [x] `DiscountCode` entity + admin CRUD (create/list/deactivate; code, type, value, maxDiscountAmount, validFrom/Until, minBookingAmount, usageLimit, usedCount, active).
- [x] Apply-at-checkout (at hold): validate exists/active/in-window/min-met/not-exhausted; percentage capped by maxDiscountAmount; discount never exceeds subtotal.
- [x] `usedCount` incremented inside the confirmation transaction under a pessimistic row lock (assertRedeemable before charge, redeem after success) — can't over-redeem.
- [x] Distinct 422 errors per rejection reason (unknown, inactive, expired, below minimum, exhausted).
- [x] Unit tests (percentage/flat/cap/clamp) + integration tests (discounted booking pays less & redeems; below-min and unknown rejected).

**Note:** `Booking` gained subtotal/discountAmount/total + discount FK; `BookingResponse` now
exposes all three plus `discountCode`. `value` column renamed to `discount_value` (reserved word in
H2). Failed payment does not redeem (assert-then-increment split around the charge).
