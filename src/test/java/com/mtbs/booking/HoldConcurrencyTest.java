package com.mtbs.booking;

import com.mtbs.auth.UserRepository;
import com.mtbs.auth.domain.Role;
import com.mtbs.auth.domain.User;
import com.mtbs.catalog.CatalogService;
import com.mtbs.catalog.dto.CatalogDtos.CreateCityRequest;
import com.mtbs.catalog.dto.CatalogDtos.CreateScreenRequest;
import com.mtbs.catalog.dto.CatalogDtos.CreateTheaterRequest;
import com.mtbs.catalog.dto.CatalogDtos.LayoutRequest;
import com.mtbs.catalog.dto.CatalogDtos.MovieRequest;
import com.mtbs.show.ShowSeatRepository;
import com.mtbs.show.ShowService;
import com.mtbs.show.domain.ShowSeat;
import com.mtbs.show.domain.ShowSeatStatus;
import com.mtbs.show.dto.ShowDtos.ScheduleShowRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The graded centerpiece: many customers race to hold the SAME seat. Correct serialization means
 * exactly one hold succeeds and every other attempt is cleanly rejected — the seat is never
 * double-allocated. Runs at the service seam so real threads race real committed transactions
 * (MockMvc's single-request model can't express this).
 */
@SpringBootTest
class HoldConcurrencyTest {

  @Autowired
  private CatalogService catalogService;
  @Autowired
  private ShowService showService;
  @Autowired
  private HoldService holdService;
  @Autowired
  private ShowSeatRepository showSeatRepository;
  @Autowired
  private BookingRepository bookingRepository;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private PasswordEncoder passwordEncoder;

  @Test
  void concurrentHoldsOnOneSeatSerializeToExactlyOneWinner() throws Exception {
    // --- arrange: a show with a single seat, and one customer, all committed ---
    var city = catalogService.createCity(new CreateCityRequest("RaceCity", "RC"));
    var theater = catalogService.createTheater(
        new CreateTheaterRequest(city.id(), "RacePlex", "Main St"));
    var screen = catalogService.createScreen(new CreateScreenRequest(theater.id(), "S1"));
    catalogService.defineLayout(screen.id(), new LayoutRequest(1, 1, List.of()));
    var movie = catalogService.createMovie(new MovieRequest("RaceMovie", 120, "English", "UA"));
    var show = showService.scheduleShow(new ScheduleShowRequest(
        movie.id(), screen.id(), LocalDateTime.of(2026, 12, 1, 19, 0),
        new BigDecimal("200"), new BigDecimal("400")));

    Long seatId = showSeatRepository.findByShowIdOrderByIdAsc(show.id()).get(0).getId();

    userRepository.save(
        new User("racer@mtbs.com", passwordEncoder.encode("pw-racer-1"), Role.CUSTOMER));

    // --- act: N threads fire the same hold simultaneously ---
    int threads = 8;
    ExecutorService pool = Executors.newFixedThreadPool(threads);
    CountDownLatch ready = new CountDownLatch(threads);
    CountDownLatch go = new CountDownLatch(1);
    AtomicInteger wins = new AtomicInteger();
    AtomicInteger rejections = new AtomicInteger();
    AtomicInteger unexpected = new AtomicInteger();

    for (int i = 0; i < threads; i++) {
      pool.submit(() -> {
        ready.countDown();
        try {
          go.await();
          holdService.hold("racer@mtbs.com", show.id(), List.of(seatId));
          wins.incrementAndGet();
        } catch (SeatUnavailableException e) {
          rejections.incrementAndGet();
        } catch (Exception e) {
          unexpected.incrementAndGet();
        }
        return null;
      });
    }
    ready.await();
    go.countDown();
    pool.shutdown();
    assertThat(pool.awaitTermination(30, TimeUnit.SECONDS)).isTrue();

    // --- assert: exactly one winner, no double-allocation ---
    assertThat(wins.get()).isEqualTo(1);
    assertThat(rejections.get()).isEqualTo(threads - 1);
    assertThat(unexpected.get()).isZero();

    ShowSeat seat = showSeatRepository.findById(seatId).orElseThrow();
    assertThat(seat.getStatus()).isEqualTo(ShowSeatStatus.HELD);
    assertThat(bookingRepository.findByOwnerEmailOrderByIdDesc("racer@mtbs.com")).hasSize(1);
  }
}
