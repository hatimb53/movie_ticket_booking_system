package com.mtbs.booking;

import com.mtbs.booking.domain.BookedSeatSnapshot;
import com.mtbs.booking.domain.Booking;
import com.mtbs.booking.domain.BookingStatus;
import com.mtbs.booking.dto.BookingDtos.BookedSeat;
import com.mtbs.booking.dto.BookingDtos.BookingResponse;
import java.time.Instant;

/** Manual entity → DTO mapping for bookings. */
final class BookingMapper {

  private BookingMapper() {
  }

  static BookingResponse toBooking(Booking b) {
    return new BookingResponse(
        b.getId(),
        b.getShow().getId(),
        b.getStatus().name(),
        b.getSubtotal(),
        b.getDiscountAmount(),
        b.getTotal(),
        b.getDiscount() != null ? b.getDiscount().getCode() : null,
        b.getBookedSeats().stream().map(BookingMapper::toBookedSeat).toList());
  }

  /**
   * Same as {@link #toBooking(Booking)}, but reports EXPIRED for a PENDING_PAYMENT booking whose
   * hold has already lapsed, even if the sweeper hasn't swept it yet. Read-only — never writes to
   * the DB; the sweeper (or the next competing hold attempt) still owns the actual state
   * transition. Checked against the live {@code seats} association (still intact at this point —
   * reclaim by another booking, which would move it, only happens once this booking is marked
   * EXPIRED in the first place).
   */
  static BookingResponse toBooking(Booking b, Instant now) {
    boolean lapsed = b.getStatus() == BookingStatus.PENDING_PAYMENT
        && b.getSeats().stream().anyMatch(seat -> seat.isHoldExpired(now));
    String status = lapsed ? BookingStatus.EXPIRED.name() : b.getStatus().name();
    return new BookingResponse(
        b.getId(),
        b.getShow().getId(),
        status,
        b.getSubtotal(),
        b.getDiscountAmount(),
        b.getTotal(),
        b.getDiscount() != null ? b.getDiscount().getCode() : null,
        b.getBookedSeats().stream().map(BookingMapper::toBookedSeat).toList());
  }

  private static BookedSeat toBookedSeat(BookedSeatSnapshot snapshot) {
    return new BookedSeat(snapshot.getShowSeatId(), snapshot.getLabel(), snapshot.getPrice());
  }
}
