package com.mtbs.notification.dto;

import java.time.Instant;

/** Response records for the customer-facing notification feed. */
public final class NotificationDtos {

  private NotificationDtos() {
  }

  public record NotificationResponse(
      Long id, String type, String message, Long bookingId, Instant sentAt) {
  }
}
