package com.mtbs.notification;

import com.mtbs.notification.dto.NotificationDtos.NotificationResponse;
import java.security.Principal;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * A user's own notification feed — confirmations, cancellations, and show reminders — latest
 * first. Any authenticated user sees only their own notifications (matched by recipient email),
 * so no role restriction is needed beyond being logged in.
 */
@RestController
@RequestMapping("/notifications")
public class NotificationController {

  private final NotificationRepository notificationRepository;

  public NotificationController(NotificationRepository notificationRepository) {
    this.notificationRepository = notificationRepository;
  }

  @GetMapping
  public List<NotificationResponse> myNotifications(Principal principal) {
    return notificationRepository.findByRecipientEmailOrderBySentAtDesc(principal.getName())
        .stream()
        .map(n -> new NotificationResponse(
            n.getId(), n.getType().name(), n.getMessage(), n.getBookingId(), n.getSentAt()))
        .toList();
  }
}
