package com.mtbs.booking;

import com.mtbs.booking.dto.BookingDtos.BookingResponse;
import com.mtbs.booking.dto.BookingDtos.HoldRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Customer seat holds. */
@RestController
public class HoldController {

  private final HoldService holdService;

  public HoldController(HoldService holdService) {
    this.holdService = holdService;
  }

  @PostMapping("/shows/{id}/holds")
  @PreAuthorize("hasRole('CUSTOMER')")
  public ResponseEntity<BookingResponse> hold(
      @PathVariable Long id, @Valid @RequestBody HoldRequest request, Principal principal) {
    BookingResponse response = holdService.hold(principal.getName(), id, request.showSeatIds());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
}
