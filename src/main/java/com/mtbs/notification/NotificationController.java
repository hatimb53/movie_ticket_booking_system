package com.mtbs.notification;

import com.mtbs.notification.dto.NotificationDtos.NotificationResponse;
import java.security.Principal;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * A user's own notification feed — confirmations, cancellations, and show reminders — latest
 * first, matched by recipient email. Notifications are only ever sent to booking owners
 * (customers), so this is CUSTOMER-only, matching {@code BookingController}'s pattern.
 */
@RestController
@RequestMapping("/notifications")
@PreAuthorize("hasRole('CUSTOMER')")
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
