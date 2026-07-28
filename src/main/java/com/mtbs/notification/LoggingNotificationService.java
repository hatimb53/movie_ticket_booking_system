package com.mtbs.notification;

import com.mtbs.notification.domain.Notification;
import com.mtbs.notification.domain.NotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Logs the notification and persists it as history. Runs in its own transaction. */
@Service
public class LoggingNotificationService implements NotificationService {

  private static final Logger log = LoggerFactory.getLogger(LoggingNotificationService.class);
  private static final String CHANNEL = "LOG";

  private final NotificationRepository repository;

  public LoggingNotificationService(NotificationRepository repository) {
    this.repository = repository;
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void notify(NotificationType type, String recipientEmail, Long bookingId, String message) {
    log.info("[NOTIFY:{}] to={} booking={} : {}", type, recipientEmail, bookingId, message);
    repository.save(new Notification(recipientEmail, type, CHANNEL, message, bookingId));
  }
}
