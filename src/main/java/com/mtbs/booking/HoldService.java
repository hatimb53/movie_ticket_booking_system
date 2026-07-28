package com.mtbs.booking;

import com.mtbs.auth.UserRepository;
import com.mtbs.auth.domain.User;
import com.mtbs.booking.domain.Booking;
import com.mtbs.booking.dto.BookingDtos.BookingResponse;
import com.mtbs.common.error.ResourceNotFoundException;
import com.mtbs.discount.DiscountService;
import com.mtbs.discount.DiscountService.DiscountApplication;
import com.mtbs.show.ShowSeatRepository;
import com.mtbs.show.domain.ShowSeat;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Places time-bound seat holds. The AVAILABLE→HELD transition happens under a pessimistic write
 * lock on the target {@link ShowSeat} rows, so concurrent attempts on the same seat serialize:
 * exactly one wins, the rest see HELD/BOOKED and are rejected. An expired hold is reclaimable.
 */
@Service
public class HoldService {

  private final ShowSeatRepository showSeatRepository;
  private final BookingRepository bookingRepository;
  private final UserRepository userRepository;
  private final DiscountService discountService;
  private final long ttlSeconds;

  public HoldService(
      ShowSeatRepository showSeatRepository,
      BookingRepository bookingRepository,
      UserRepository userRepository,
      DiscountService discountService,
      @Value("${app.hold.ttl-seconds:300}") long ttlSeconds) {
    this.showSeatRepository = showSeatRepository;
    this.bookingRepository = bookingRepository;
    this.userRepository = userRepository;
    this.discountService = discountService;
    this.ttlSeconds = ttlSeconds;
  }

  @Transactional
  public BookingResponse hold(String userEmail, Long showId, List<Long> showSeatIds) {
    return hold(userEmail, showId, showSeatIds, null);
  }

  @Transactional
  public BookingResponse hold(String userEmail, Long showId, List<Long> showSeatIds, String discountCode) {
    User user = userRepository.findByEmail(userEmail)
        .orElseThrow(() -> new ResourceNotFoundException("User " + userEmail + " not found"));

    // Deterministic ordering for lock acquisition (defence-in-depth; the query also orders).
    List<Long> orderedIds = showSeatIds.stream().distinct().sorted().toList();
    List<ShowSeat> seats = showSeatRepository.lockByIds(orderedIds);

    if (seats.size() != orderedIds.size()) {
      throw new ResourceNotFoundException("One or more seats do not exist");
    }

    Instant now = Instant.now();
    for (ShowSeat seat : seats) {
      if (!seat.getShow().getId().equals(showId)) {
        throw new SeatUnavailableException("Seat " + seat.getId() + " is not part of show " + showId);
      }
      boolean bookable = seat.getStatus() == com.mtbs.show.domain.ShowSeatStatus.AVAILABLE
          || seat.isHoldExpired(now);
      if (!bookable) {
        throw new SeatUnavailableException("Seat " + seat.getSeat().getLabel() + " is not available");
      }
    }

    Instant holdUntil = now.plusSeconds(ttlSeconds);
    BigDecimal subtotal = BigDecimal.ZERO;
    for (ShowSeat seat : seats) {
      seat.hold(holdUntil);
      subtotal = subtotal.add(seat.getPrice());
    }
    showSeatRepository.saveAll(seats);

    Booking booking = new Booking(user, seats.get(0).getShow(), seats, subtotal);
    if (StringUtils.hasText(discountCode)) {
      DiscountApplication applied = discountService.evaluate(discountCode, subtotal);
      booking.applyDiscount(applied.code(), applied.discountAmount(), applied.total());
    }
    return BookingMapper.toBooking(bookingRepository.save(booking));
  }
}
