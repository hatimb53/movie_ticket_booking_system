package com.mtbs.booking;

import com.mtbs.booking.dto.BookingDtos.BookingResponse;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Read side of bookings — a customer's own history. */
@Service
public class BookingQueryService {

  private final BookingRepository bookingRepository;

  public BookingQueryService(BookingRepository bookingRepository) {
    this.bookingRepository = bookingRepository;
  }

  @Transactional(readOnly = true)
  public List<BookingResponse> myBookings(String userEmail) {
    return bookingRepository.findByOwnerEmailOrderByIdDesc(userEmail).stream()
        .map(BookingMapper::toBooking)
        .toList();
  }
}
