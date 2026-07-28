package com.mtbs.booking.dto;

import jakarta.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.util.List;

/** Request/response records for the booking flow. */
public final class BookingDtos {

  private BookingDtos() {
  }

  public record HoldRequest(@NotEmpty List<Long> showSeatIds) {
  }

  public record BookedSeat(Long showSeatId, String label, BigDecimal price) {
  }

  public record BookingResponse(
      Long id, Long showId, String status, BigDecimal total, List<BookedSeat> seats) {
  }
}
