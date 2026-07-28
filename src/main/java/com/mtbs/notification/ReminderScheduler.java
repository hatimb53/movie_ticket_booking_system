package com.mtbs.notification;

import com.mtbs.booking.BookingRepository;
import com.mtbs.booking.domain.Booking;
import com.mtbs.notification.domain.NotificationType;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Periodically sends a one-time reminder for confirmed bookings whose show is starting soon. Idempotent:
 * a booking that already has a SHOW_REMINDER notification is skipped, so reminders aren't duplicated.
 */
@Component
public class ReminderScheduler {

  private final BookingRepository bookingRepository;
  private final NotificationRepository notificationRepository;
  private final NotificationService notificationService;
  private final long windowMinutes;

  public ReminderScheduler(
      BookingRepository bookingRepository,
      NotificationRepository notificationRepository,
      NotificationService notificationService,
      @Value("${app.reminder.window-minutes:120}") long windowMinutes) {
    this.bookingRepository = bookingRepository;
    this.notificationRepository = notificationRepository;
    this.notificationService = notificationService;
    this.windowMinutes = windowMinutes;
  }

  @Scheduled(fixedDelayString = "${app.reminder.interval-ms:60000}")
  @Transactional(readOnly = true)
  public void sendReminders() {
    LocalDateTime now = LocalDateTime.now();
    List<Booking> upcoming =
        bookingRepository.findConfirmedWithShowStartBetween(now, now.plusMinutes(windowMinutes));
    for (Booking b : upcoming) {
      if (notificationRepository.existsByBookingIdAndType(b.getId(), NotificationType.SHOW_REMINDER)) {
        continue;
      }
      notificationService.notify(
          NotificationType.SHOW_REMINDER,
          b.getOwner().getEmail(),
          b.getId(),
          "Reminder: '" + b.getShow().getMovie().getTitle() + "' starts at "
              + b.getShow().getStartTime() + ".");
    }
  }
}
