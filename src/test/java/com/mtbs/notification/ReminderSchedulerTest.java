package com.mtbs.notification;

import com.mtbs.auth.UserRepository;
import com.mtbs.auth.domain.Role;
import com.mtbs.auth.domain.User;
import com.mtbs.booking.BookingService;
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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/** The reminder job emits a one-time SHOW_REMINDER for confirmed bookings whose show starts soon. */
@SpringBootTest
@Transactional
class ReminderSchedulerTest {

  @Autowired
  private CatalogService catalogService;
  @Autowired
  private ShowService showService;
  @Autowired
  private BookingService bookingService;
  @Autowired
  private ShowSeatRepository showSeatRepository;
  @Autowired
  private NotificationRepository notificationRepository;
  @Autowired
  private ReminderScheduler reminderScheduler;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private PasswordEncoder passwordEncoder;

  @Test
  void remindsConfirmedBookingForAnImminentShowOnlyOnce() {
    var city = catalogService.createCity(new CreateCityRequest("RemCity", "RC"));
    var theater = catalogService.createTheater(new CreateTheaterRequest(city.id(), "RemPlex", "St"));
    var screen = catalogService.createScreen(new CreateScreenRequest(theater.id(), "S1"));
    catalogService.defineLayout(screen.id(), new LayoutRequest(1, 1, List.of()));
    var movie = catalogService.createMovie(new MovieRequest("RemMovie", 120, "English", "UA"));
    // Show starts within the 120-minute reminder window.
    var show = showService.scheduleShow(new ScheduleShowRequest(
        movie.id(), screen.id(), LocalDateTime.now().plusMinutes(30),
        new BigDecimal("200"), new BigDecimal("400")));
    long seatId = showSeatRepository.findByShowIdOrderByIdAsc(show.id()).get(0).getId();

    userRepository.save(new User("rem@mtbs.com", passwordEncoder.encode("pw-rem-1"), Role.CUSTOMER));
    BookingResponse held = bookingService.hold("rem@mtbs.com", show.id(), List.of(seatId));
    bookingService.pay("rem@mtbs.com", held.id(), "visa");

    reminderScheduler.sendReminders();
    reminderScheduler.sendReminders(); // idempotent

    long reminders = notificationRepository.findByRecipientEmailOrderByIdDesc("rem@mtbs.com")
        .stream()
        .filter(n -> n.getType() == NotificationType.SHOW_REMINDER && held.id().equals(n.getBookingId()))
        .count();
    assertThat(reminders).isEqualTo(1);
  }
}
