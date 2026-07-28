package com.mtbs.notification.domain;

import com.mtbs.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;

/** A delivered notification, persisted as history. The mock "channel" is a log line. */
@Entity
@Table(name = "notifications")
public class Notification extends BaseEntity {

  @Column(nullable = false)
  private String recipientEmail;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private NotificationType type;

  @Column(nullable = false)
  private String channel;

  @Column(nullable = false, length = 1000)
  private String message;

  @Column
  private Long bookingId;

  @Column(nullable = false)
  private Instant sentAt;

  protected Notification() {
  }

  public Notification(String recipientEmail, NotificationType type, String channel,
      String message, Long bookingId) {
    this.recipientEmail = recipientEmail;
    this.type = type;
    this.channel = channel;
    this.message = message;
    this.bookingId = bookingId;
    this.sentAt = Instant.now();
  }

  public String getRecipientEmail() {
    return recipientEmail;
  }

  public NotificationType getType() {
    return type;
  }

  public String getMessage() {
    return message;
  }

  public Long getBookingId() {
    return bookingId;
  }
}
