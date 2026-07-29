package com.mtbs.show;

import com.mtbs.booking.BookingService;
import com.mtbs.show.dto.ShowDtos.ScheduleShowRequest;
import com.mtbs.show.dto.ShowDtos.ShowCancellationResponse;
import com.mtbs.show.dto.ShowDtos.ShowResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Admin-only show scheduling and cancellation. */
@RestController
@RequestMapping("/admin/shows")
@PreAuthorize("hasRole('ADMIN')")
public class AdminShowController {

  private final ShowService showService;
  private final BookingService bookingService;

  public AdminShowController(ShowService showService, BookingService bookingService) {
    this.showService = showService;
    this.bookingService = bookingService;
  }

  @PostMapping
  public ResponseEntity<ShowResponse> schedule(@Valid @RequestBody ScheduleShowRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(showService.scheduleShow(request));
  }

  /**
   * Cancels a scheduled show: refunds every CONFIRMED booking in full and expires every unpaid
   * PENDING_PAYMENT hold. Delegated to {@link BookingService}, which already owns the
   * payment/refund wiring this needs.
   */
  @PostMapping("/{id}/cancel")
  public ResponseEntity<ShowCancellationResponse> cancel(@PathVariable Long id) {
    return ResponseEntity.ok(bookingService.cancelShow(id));
  }
}
