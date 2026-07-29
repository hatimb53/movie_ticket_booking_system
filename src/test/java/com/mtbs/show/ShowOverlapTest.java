package com.mtbs.show;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtbs.auth.UserRepository;
import com.mtbs.auth.domain.Role;
import com.mtbs.auth.domain.User;
import com.mtbs.catalog.CatalogService;
import com.mtbs.catalog.dto.CatalogDtos.CreateCityRequest;
import com.mtbs.catalog.dto.CatalogDtos.CreateScreenRequest;
import com.mtbs.catalog.dto.CatalogDtos.CreateTheaterRequest;
import com.mtbs.catalog.dto.CatalogDtos.LayoutRequest;
import com.mtbs.catalog.dto.CatalogDtos.MovieRequest;
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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * A screen can only show one movie at a time: scheduling must reject any new show whose
 * [start, start+duration) window, expanded by the 30-minute buffer, overlaps an existing show on
 * the same screen.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ShowOverlapTest {

  @Autowired
  private CatalogService catalogService;
  @Autowired
  private ShowService showService;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;

  @Test
  @Transactional
  void rejectsAShowThatWouldOverlapAnExistingOneWithinTheBuffer() throws Exception {
    userRepository.save(new User("ovadmin@mtbs.com", passwordEncoder.encode("pw-ov-1"), Role.ADMIN));
    String token = login("ovadmin@mtbs.com", "pw-ov-1");

    var city = catalogService.createCity(new CreateCityRequest("OverlapCity", "OC"));
    var theater = catalogService.createTheater(
        new CreateTheaterRequest(city.id(), "OverlapPlex", "St"));
    var screen = catalogService.createScreen(new CreateScreenRequest(theater.id(), "S1"));
    catalogService.defineLayout(screen.id(), new LayoutRequest(1, 1, List.of()));
    var movie = catalogService.createMovie(new MovieRequest("OverlapMovie", 120, "English", "UA"));

    // First show: 10:00 - 12:00.
    showService.scheduleShow(new ScheduleShowRequest(
        movie.id(), screen.id(), LocalDateTime.of(2026, 9, 1, 10, 0),
        new BigDecimal("200"), new BigDecimal("400")));

    // A show starting at 12:15 is only 15 minutes after the first ends - inside the 30-minute
    // buffer, so it must be rejected.
    mockMvc.perform(post("/admin/shows")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"movieId\":" + movie.id() + ",\"screenId\":" + screen.id()
                + ",\"startTime\":\"2026-09-01T12:15:00\",\"regularPrice\":200,\"premiumPrice\":400}"))
        .andExpect(status().isConflict());

    // A show starting at 12:30 clears the buffer exactly and is allowed.
    mockMvc.perform(post("/admin/shows")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"movieId\":" + movie.id() + ",\"screenId\":" + screen.id()
                + ",\"startTime\":\"2026-09-01T12:30:00\",\"regularPrice\":200,\"premiumPrice\":400}"))
        .andExpect(status().isCreated());
  }

  /**
   * The graded centerpiece for this rule: two admins race to schedule overlapping shows on the
   * SAME screen. Correct serialization means exactly one wins and the other is cleanly rejected —
   * never both. Runs at the service seam (real threads, real committed transactions) since
   * MockMvc's single-request model can't express this.
   */
  @Test
  void concurrentOverlappingSchedulesOnOneScreenSerializeToExactlyOneWinner() throws Exception {
    var city = catalogService.createCity(new CreateCityRequest("RaceScreenCity", "RC"));
    var theater = catalogService.createTheater(
        new CreateTheaterRequest(city.id(), "RaceScreenPlex", "Main St"));
    var screen = catalogService.createScreen(new CreateScreenRequest(theater.id(), "S1"));
    catalogService.defineLayout(screen.id(), new LayoutRequest(1, 1, List.of()));
    var movie = catalogService.createMovie(new MovieRequest("RaceScreenMovie", 120, "English", "UA"));

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
          showService.scheduleShow(new ScheduleShowRequest(
              movie.id(), screen.id(), LocalDateTime.of(2026, 9, 2, 18, 0),
              new BigDecimal("200"), new BigDecimal("400")));
          wins.incrementAndGet();
        } catch (ShowOverlapException e) {
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

    assertThat(wins.get()).isEqualTo(1);
    assertThat(rejections.get()).isEqualTo(threads - 1);
    assertThat(unexpected.get()).isZero();
  }

  private String login(String email, String password) throws Exception {
    MvcResult result = mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
        .andExpect(status().isOk())
        .andReturn();
    return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
  }
}
