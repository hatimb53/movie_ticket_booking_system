package com.mtbs.booking;

import com.mtbs.booking.domain.BookingStatus;
import com.mtbs.booking.dto.BookingDtos.BookingResponse;
import com.mtbs.booking.dto.BookingDtos.HoldRequest;
import com.mtbs.booking.dto.CancellationResponse;
import com.mtbs.booking.dto.PayRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The customer-facing booking surface: create a booking (hold seats on a show), pay to confirm,
 * cancel for a refund, and list your own bookings. All routes require the CUSTOMER role. Booking
 * is the resource throughout — {@code showId} is an input in the create body, not a path segment,
 * since a show isn't the parent collection a booking is created under.
 */
@RestController
@RequestMapping("/bookings")
@PreAuthorize("hasRole('CUSTOMER')")
public class BookingController {

  private final BookingService bookingService;

  public BookingController(BookingService bookingService) {
    this.bookingService = bookingService;
  }

  @PostMapping("/hold")
  public ResponseEntity<BookingResponse> hold(
      @Valid @RequestBody HoldRequest request, Principal principal) {
    BookingResponse response = bookingService.hold(
        principal.getName(), request.showId(), request.showSeatIds(), request.discountCode());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/{id}/pay")
  public ResponseEntity<BookingResponse> pay(
      @PathVariable Long id, @Valid @RequestBody PayRequest request, Principal principal) {
    BookingResponse response = bookingService.pay(principal.getName(), id, request.token());
    HttpStatus status = response.status().equals(BookingStatus.PAYMENT_FAILED.name())
        ? HttpStatus.PAYMENT_REQUIRED
        : HttpStatus.OK;
    return ResponseEntity.status(status).body(response);
  }

  @PostMapping("/{id}/cancel")
  public CancellationResponse cancel(@PathVariable Long id, Principal principal) {
    return bookingService.cancel(principal.getName(), id);
  }

  @GetMapping
  public List<BookingResponse> myBookings(Principal principal) {
    return bookingService.myBookings(principal.getName());
  }
}
