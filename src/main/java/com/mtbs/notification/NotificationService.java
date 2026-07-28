package com.mtbs.notification;

import com.mtbs.notification.domain.NotificationType;

/**
 * Delivers and records notifications. The shipped implementation logs and persists; a real one
 * would send email/SMS. Kept as an interface so the channel can be swapped without touching callers.
 */
public interface NotificationService {

  void notify(NotificationType type, String recipientEmail, Long bookingId, String message);
}
