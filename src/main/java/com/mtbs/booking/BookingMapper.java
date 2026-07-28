package com.mtbs.booking;

import com.mtbs.booking.domain.Booking;
import com.mtbs.booking.dto.BookingDtos.BookedSeat;
import com.mtbs.booking.dto.BookingDtos.BookingResponse;
import com.mtbs.show.domain.ShowSeat;

/** Manual entity → DTO mapping for bookings. */
final class BookingMapper {

  private BookingMapper() {
  }

  static BookingResponse toBooking(Booking b) {
    return new BookingResponse(
        b.getId(),
        b.getShow().getId(),
        b.getStatus().name(),
        b.getTotal(),
        b.getSeats().stream().map(BookingMapper::toBookedSeat).toList());
  }

  private static BookedSeat toBookedSeat(ShowSeat ss) {
    return new BookedSeat(ss.getId(), ss.getSeat().getLabel(), ss.getPrice());
  }
}
