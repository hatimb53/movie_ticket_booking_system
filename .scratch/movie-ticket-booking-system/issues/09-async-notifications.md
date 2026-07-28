# 09 — Async notifications

**What to build:** Booking confirmation, cancellation/refund, and pre-show reminders are delivered
as notifications without blocking the booking flow. A slow or failing notification never blocks or
fails the booking transaction. Notifications are persisted as history.

**Blocked by:** 06. (Cancellation/refund notification wires in as ticket 08 lands.)

**Status:** ready-for-agent

- [ ] Domain events published via `ApplicationEventPublisher` on confirmation (and cancellation once 08 exists).
- [ ] `@Async` listener on a dedicated `TaskExecutor` dispatches through a `NotificationService` interface (log + persist `Notification`).
- [ ] Notification dispatch is decoupled from the booking transaction — booking commits regardless of notification outcome.
- [ ] `@Scheduled` reminder job scans upcoming shows and emits reminders.
- [ ] Test proving a failing notification does not roll back or block a booking; test asserting a `Notification` row is persisted.
