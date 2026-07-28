package com.mtbs.notification;

import com.mtbs.auth.UserRepository;
import com.mtbs.auth.domain.Role;
import com.mtbs.auth.domain.User;
import com.mtbs.booking.HoldService;
import com.mtbs.booking.PaymentService;
import com.mtbs.booking.dto.BookingDtos.BookingResponse;
import com.mtbs.catalog.CatalogService;
import com.mtbs.catalog.dto.CatalogDtos.CreateCityRequest;
import com.mtbs.catalog.dto.CatalogDtos.CreateScreenRequest;
import com.mtbs.catalog.dto.CatalogDtos.CreateTheaterRequest;
import com.mtbs.catalog.dto.CatalogDtos.LayoutRequest;
import com.mtbs.catalog.dto.CatalogDtos.MovieRequest;
import com.mtbs.notification.domain.NotificationType;
import com.mtbs.show.ShowSeatRepository;
import com.mtbs.show.ShowService;
import com.mtbs.show.dto.ShowDtos.ScheduleShowRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.awaitility.Awaitility;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Proves confirmation notifications are delivered asynchronously after the booking commits: the
 * booking is CONFIRMED immediately and a BOOKING_CONFIRMED notification is persisted shortly after,
 * off the request thread. Non-transactional so the AFTER_COMMIT async listener actually fires.
 */
@SpringBootTest
class NotificationDeliveryTest {

  @Autowired
  private CatalogService catalogService;
  @Autowired
  private ShowService showService;
  @Autowired
  private HoldService holdService;
  @Autowired
  private PaymentService paymentService;
  @Autowired
  private ShowSeatRepository showSeatRepository;
  @Autowired
  private NotificationRepository notificationRepository;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private PasswordEncoder passwordEncoder;

  @Test
  void confirmingABookingDeliversAConfirmationNotificationAsynchronously() {
    var city = catalogService.createCity(new CreateCityRequest("NotifCity", "NC"));
    var theater = catalogService.createTheater(
        new CreateTheaterRequest(city.id(), "NotifPlex", "St"));
    var screen = catalogService.createScreen(new CreateScreenRequest(theater.id(), "S1"));
    catalogService.defineLayout(screen.id(), new LayoutRequest(1, 1, List.of()));
    var movie = catalogService.createMovie(new MovieRequest("NotifMovie", 120, "English", "UA"));
    var show = showService.scheduleShow(new ScheduleShowRequest(
        movie.id(), screen.id(), LocalDateTime.of(2026, 12, 2, 19, 0),
        new BigDecimal("200"), new BigDecimal("400")));
    long seatId = showSeatRepository.findByShowIdOrderByIdAsc(show.id()).get(0).getId();

    userRepository.save(
        new User("notif@mtbs.com", passwordEncoder.encode("pw-notif-1"), Role.CUSTOMER));
    BookingResponse held = holdService.hold("notif@mtbs.com", show.id(), List.of(seatId));

    BookingResponse paid = paymentService.pay("notif@mtbs.com", held.id(), "visa");
    assertThat(paid.status()).isEqualTo("CONFIRMED"); // booking confirmed synchronously

    // Notification arrives asynchronously, after the commit.
    Awaitility.await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->
        assertThat(notificationRepository.existsByBookingIdAndType(
            held.id(), NotificationType.BOOKING_CONFIRMED)).isTrue());
  }
}
