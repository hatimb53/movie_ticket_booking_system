# 07 — Discount codes

**What to build:** An admin creates and manages discount codes (percentage or flat, with an
optional cap, validity window, minimum booking amount, and global usage limit). A customer applies
a code at checkout and pays less when eligible; ineligible codes are rejected with a clear reason,
and the usage limit can never be over-redeemed under concurrency.

**Blocked by:** 06.

**Status:** ready-for-agent

- [ ] `DiscountCode` entity + admin CRUD (code, type, value, maxDiscountAmount, validFrom/Until, minBookingAmount, usageLimit, usedCount, active).
- [ ] Apply-at-checkout: validate exists/active/in-window/min-met/limit-not-exhausted; percentage capped by maxDiscountAmount.
- [ ] `usedCount` increment happens inside the booking-confirmation transaction so it cannot over-redeem.
- [ ] Clear, distinct errors for each rejection reason (expired, inactive, below minimum, exhausted, unknown code).
- [ ] Unit tests for discount validation/application (incl. cap and exhaustion); integration test for a discounted booking paying less.
