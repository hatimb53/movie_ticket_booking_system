package com.mtbs.notification.domain;

import com.mtbs.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;

/** A delivered notification, persisted as history. The mock "channel" is a log line. */
@Entity
@Table(name = "notifications")
public class Notification extends BaseEntity {

  @Getter
  @Column(nullable = false)
  private String recipientEmail;

  @Getter
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private NotificationType type;

  @Column(nullable = false)
  private String channel;

  @Getter
  @Column(nullable = false, length = 1000)
  private String message;

  @Getter
  @Column
  private Long bookingId;

  @Getter
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

}
