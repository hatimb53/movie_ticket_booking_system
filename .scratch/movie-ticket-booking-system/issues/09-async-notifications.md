# 09 — Async notifications

**What to build:** Booking confirmation, cancellation/refund, and pre-show reminders are delivered
as notifications without blocking the booking flow. A slow or failing notification never blocks or
fails the booking transaction. Notifications are persisted as history.

**Blocked by:** 06. (Cancellation/refund notification wires in as ticket 08 lands.)

**Status:** done

- [x] `BookingConfirmedEvent` / `BookingCancelledEvent` published via `ApplicationEventPublisher` on confirmation and cancellation.
- [x] `@Async("notificationExecutor")` `@TransactionalEventListener(AFTER_COMMIT)` dispatches through the `NotificationService` interface (`LoggingNotificationService`: log + persist `Notification`).
- [x] Decoupled from the booking transaction — fires AFTER_COMMIT on a separate thread; listener failures are caught/logged, never affecting the booking.
- [x] `@Scheduled` `ReminderScheduler` scans confirmed bookings with imminent shows and emits a one-time SHOW_REMINDER.
- [x] Tests: async confirmation delivered after commit (booking CONFIRMED synchronously, notification persisted, awaited); reminder emitted exactly once (idempotent).

**Note:** Events live in `com.mtbs.booking.event` so `booking` doesn't depend on `notification`
(notification → booking only, no cycle). `notify` runs `REQUIRES_NEW`. Failure isolation is
structural (AFTER_COMMIT + async + try/catch), so no separate rollback test was needed.
