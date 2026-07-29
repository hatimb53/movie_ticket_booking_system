package com.mtbs.notification;

import com.mtbs.notification.domain.Notification;
import com.mtbs.notification.domain.NotificationType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

  List<Notification> findByRecipientEmailOrderByIdDesc(String recipientEmail);

  /** Latest-first: what a user should see when opening their notification feed. */
  List<Notification> findByRecipientEmailOrderBySentAtDesc(String recipientEmail);

  boolean existsByBookingIdAndType(Long bookingId, NotificationType type);
}
