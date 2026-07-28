package com.mtbs.booking;

import com.mtbs.booking.domain.BookingStatus;
import com.mtbs.booking.dto.BookingDtos.BookingResponse;
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

/** Payment and booking history for the authenticated customer. */
@RestController
@RequestMapping("/bookings")
@PreAuthorize("hasRole('CUSTOMER')")
public class BookingController {

  private final PaymentService paymentService;
  private final BookingQueryService bookingQueryService;
  private final CancellationService cancellationService;

  public BookingController(
      PaymentService paymentService,
      BookingQueryService bookingQueryService,
      CancellationService cancellationService) {
    this.paymentService = paymentService;
    this.bookingQueryService = bookingQueryService;
    this.cancellationService = cancellationService;
  }

  @PostMapping("/{id}/pay")
  public ResponseEntity<BookingResponse> pay(
      @PathVariable Long id, @Valid @RequestBody PayRequest request, Principal principal) {
    BookingResponse response = paymentService.pay(principal.getName(), id, request.token());
    HttpStatus status = response.status().equals(BookingStatus.PAYMENT_FAILED.name())
        ? HttpStatus.PAYMENT_REQUIRED
        : HttpStatus.OK;
    return ResponseEntity.status(status).body(response);
  }

  @PostMapping("/{id}/cancel")
  public CancellationResponse cancel(@PathVariable Long id, Principal principal) {
    return cancellationService.cancel(principal.getName(), id);
  }

  @GetMapping("/me")
  public List<BookingResponse> myBookings(Principal principal) {
    return bookingQueryService.myBookings(principal.getName());
  }
}
