package com.mtbs.show;

import com.mtbs.auth.UserRepository;
import com.mtbs.auth.domain.Role;
import com.mtbs.auth.domain.User;
import com.mtbs.booking.BookingService;
import com.mtbs.booking.InvalidBookingStateException;
import com.mtbs.booking.SeatUnavailableException;
import com.mtbs.booking.dto.BookingDtos.BookingResponse;
import com.mtbs.catalog.CatalogService;
import com.mtbs.catalog.dto.CatalogDtos.CreateCityRequest;
import com.mtbs.catalog.dto.CatalogDtos.CreateScreenRequest;
import com.mtbs.catalog.dto.CatalogDtos.CreateTheaterRequest;
import com.mtbs.catalog.dto.CatalogDtos.LayoutRequest;
import com.mtbs.catalog.dto.CatalogDtos.MovieRequest;
import com.mtbs.show.dto.ShowDtos.ScheduleShowRequest;
import com.mtbs.show.dto.ShowDtos.ShowCancellationResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Admin show cancellation: refunds every CONFIRMED booking in full, expires unpaid holds, and
 * blocks any further hold attempt on the now-cancelled show.
 */
@SpringBootTest
@Transactional
class ShowCancellationTest {

  @Autowired
  private CatalogService catalogService;
  @Autowired
  private ShowService showService;
  @Autowired
  private BookingService bookingService;
  @Autowired
  private ShowSeatRepository showSeatRepository;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private PasswordEncoder passwordEncoder;

  @Test
  void cancellingAShowRefundsConfirmedBookingsAndExpiresPendingHolds() {
    var city = catalogService.createCity(new CreateCityRequest("CancelCity", "CC"));
    var theater = catalogService.createTheater(
        new CreateTheaterRequest(city.id(), "CancelPlex", "St"));
    var screen = catalogService.createScreen(new CreateScreenRequest(theater.id(), "S1"));
    catalogService.defineLayout(screen.id(), new LayoutRequest(1, 2, List.of()));
    var movie = catalogService.createMovie(new MovieRequest("CancelMovie", 120, "English", "UA"));
    var show = showService.scheduleShow(new ScheduleShowRequest(
        movie.id(), screen.id(), LocalDateTime.of(2026, 12, 1, 19, 0),
        new BigDecimal("200"), new BigDecimal("400")));
    List<Long> seatIds = showSeatRepository.findByShowIdOrderByIdAsc(show.id()).stream()
        .map(s -> s.getId()).toList();

    userRepository.save(new User("paid@mtbs.com", passwordEncoder.encode("pw-paid-1"), Role.CUSTOMER));
    userRepository.save(new User("unpaid@mtbs.com", passwordEncoder.encode("pw-unpaid-1"), Role.CUSTOMER));

    BookingResponse paidBooking = bookingService.hold("paid@mtbs.com", show.id(), List.of(seatIds.get(0)));
    bookingService.pay("paid@mtbs.com", paidBooking.id(), "visa");

    BookingResponse unpaidBooking =
        bookingService.hold("unpaid@mtbs.com", show.id(), List.of(seatIds.get(1)));

    ShowCancellationResponse result = bookingService.cancelShow(show.id());

    assertThat(result.bookingsRefunded()).isEqualTo(1);
    assertThat(result.totalRefunded()).isEqualByComparingTo(paidBooking.total());
    assertThat(result.bookingsExpired()).isEqualTo(1);

    assertThat(bookingService.myBookings("paid@mtbs.com").get(0).status()).isEqualTo("CANCELLED");
    assertThat(bookingService.myBookings("unpaid@mtbs.com").get(0).status()).isEqualTo("EXPIRED");

    // Cancelling again is rejected.
    assertThatThrownBy(() -> bookingService.cancelShow(show.id()))
        .isInstanceOf(InvalidBookingStateException.class);

    // No further hold can be placed on a cancelled show.
    userRepository.save(new User("late@mtbs.com", passwordEncoder.encode("pw-late-1"), Role.CUSTOMER));
    assertThatThrownBy(() -> bookingService.hold("late@mtbs.com", show.id(), List.of(seatIds.get(0))))
        .isInstanceOf(SeatUnavailableException.class);
  }
}
