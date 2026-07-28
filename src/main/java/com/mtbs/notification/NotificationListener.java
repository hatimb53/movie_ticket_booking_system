package com.mtbs.notification;

import com.mtbs.booking.event.BookingCancelledEvent;
import com.mtbs.booking.event.BookingConfirmedEvent;
import com.mtbs.notification.domain.NotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Sends booking notifications asynchronously, AFTER the booking transaction commits — so delivery
 * never blocks or rolls back the booking. A failure here is logged and swallowed.
 */
@Component
public class NotificationListener {

  private static final Logger log = LoggerFactory.getLogger(NotificationListener.class);

  private final NotificationService notificationService;

  public NotificationListener(NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  @Async("notificationExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onBookingConfirmed(BookingConfirmedEvent e) {
    safely(() -> notificationService.notify(
        NotificationType.BOOKING_CONFIRMED, e.recipientEmail(), e.bookingId(),
        "Your booking for '" + e.movieTitle() + "' at " + e.showStart() + " is confirmed."));
  }

  @Async("notificationExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onBookingCancelled(BookingCancelledEvent e) {
    safely(() -> notificationService.notify(
        NotificationType.BOOKING_CANCELLED, e.recipientEmail(), e.bookingId(),
        "Your booking for '" + e.movieTitle() + "' was cancelled. Refund: " + e.refundAmount()));
  }

  private void safely(Runnable task) {
    try {
      task.run();
    } catch (Exception ex) {
      log.error("Notification delivery failed (booking flow unaffected)", ex);
    }
  }
}
