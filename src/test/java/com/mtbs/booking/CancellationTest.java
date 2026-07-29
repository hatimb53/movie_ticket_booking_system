package com.mtbs.booking;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtbs.auth.UserRepository;
import com.mtbs.auth.domain.Role;
import com.mtbs.auth.domain.User;
import com.mtbs.show.ShowSeatRepository;
import com.mtbs.show.domain.ShowSeatStatus;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.BeforeEach;
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
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Cancellation + time-tiered refunds at the HTTP seam: different lead times yield different refund
 * percentages, seats are released, and discount usage is rolled back.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CancellationTest {

  private static final DateTimeFormatter ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private PasswordEncoder passwordEncoder;
  @Autowired
  private ShowSeatRepository showSeatRepository;

  private String adminToken;
  private String customerToken;
  private long theaterId;
  private long screenId;
  private long movieId;

  @BeforeEach
  void setUp() throws Exception {
    userRepository.save(new User("admin@mtbs.com", passwordEncoder.encode("admin-pass"), Role.ADMIN));
    adminToken = login("admin@mtbs.com", "admin-pass");
    mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
        .content("{\"email\":\"buyer@mtbs.com\",\"password\":\"buyer-pass\"}"));
    customerToken = login("buyer@mtbs.com", "buyer-pass");

    long cityId = id(adminPost("/admin/cities", "{\"name\":\"Mumbai\",\"state\":\"MH\"}"));
    theaterId = id(adminPost("/admin/theaters",
        "{\"cityId\":" + cityId + ",\"name\":\"PVR\",\"address\":\"Parel\"}"));
    screenId = id(adminPost("/admin/screens", "{\"theaterId\":" + theaterId + ",\"name\":\"S1\"}"));
    mockMvc.perform(post("/admin/screens/" + screenId + "/layout")
        .header("Authorization", "Bearer " + adminToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"rows\":1,\"seatsPerRow\":3,\"premiumRows\":[]}")).andExpect(status().isOk());
    movieId = id(adminPost("/admin/movies",
        "{\"title\":\"Dune\",\"durationMinutes\":155,\"language\":\"English\",\"rating\":\"UA\"}"));

    // Theater refund policy: >=24h -> 100%, >=2h -> 50%, else 0.
    adminPost("/admin/refund-policies",
        "{\"theaterId\":" + theaterId + ",\"tiers\":["
            + "{\"hoursBeforeShow\":24,\"refundPercent\":100},"
            + "{\"hoursBeforeShow\":2,\"refundPercent\":50},"
            + "{\"hoursBeforeShow\":0,\"refundPercent\":0}]}");
  }

  @Test
  void cancellingWellBeforeShowRefundsFullAndReleasesSeatsAndRollsBackDiscount() throws Exception {
    adminPost("/admin/discounts",
        "{\"code\":\"HALF\",\"type\":\"PERCENTAGE\",\"value\":50,"
            + "\"validFrom\":\"2026-01-01T00:00:00\",\"validUntil\":\"2027-01-01T00:00:00\","
            + "\"usageLimit\":5,\"active\":true}");

    // Fixed weekday (Wed 2026-08-05), >24h out -> 100% tier and no weekend surcharge.
    long showId = scheduleShow(LocalDateTime.of(2026, 8, 5, 12, 0));
    long seatId = showSeatRepository.findByShowIdOrderByIdAsc(showId).get(0).getId();

    long bookingId = id(hold(seatId, "HALF").andReturn()); // subtotal 200 -> total 100
    mockMvc.perform(pay(bookingId)).andExpect(status().isOk());

    mockMvc.perform(post("/bookings/" + bookingId + "/cancel")
            .header("Authorization", "Bearer " + customerToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status", is("CANCELLED")))
        .andExpect(jsonPath("$.refundPercent", is(100)))
        .andExpect(jsonPath("$.refundAmount", is(100.00))); // 100% of discounted total 100

    assertThat(showSeatRepository.findById(seatId).orElseThrow().getStatus())
        .isEqualTo(ShowSeatStatus.AVAILABLE);

    // Discount usage rolled back to 0.
    mockMvc.perform(get("/admin/discounts").header("Authorization", "Bearer " + adminToken))
        .andExpect(jsonPath("$[?(@.code=='HALF')].usedCount", hasItem(0)));
  }

  @Test
  void cancellingCloseToShowtimeAppliesPartialRefundTier() throws Exception {
    long showId = scheduleShow(LocalDateTime.now().plusHours(5)); // >=2h, <24h -> 50%
    long seatId = showSeatRepository.findByShowIdOrderByIdAsc(showId).get(0).getId();

    long bookingId = id(hold(seatId, null).andReturn()); // total 200
    mockMvc.perform(pay(bookingId)).andExpect(status().isOk());

    mockMvc.perform(post("/bookings/" + bookingId + "/cancel")
            .header("Authorization", "Bearer " + customerToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.refundPercent", is(50)))
        .andExpect(jsonPath("$.refundAmount", is(100.00))); // 50% of 200
  }

  @Test
  void cannotCancelAnUnpaidBooking() throws Exception {
    long showId = scheduleShow(LocalDateTime.now().plusDays(3));
    long seatId = showSeatRepository.findByShowIdOrderByIdAsc(showId).get(0).getId();
    long bookingId = id(hold(seatId, null).andReturn()); // still PENDING_PAYMENT

    mockMvc.perform(post("/bookings/" + bookingId + "/cancel")
            .header("Authorization", "Bearer " + customerToken))
        .andExpect(status().isConflict());
  }

  // --- helpers ---

  private long scheduleShow(LocalDateTime startTime) throws Exception {
    return id(adminPost("/admin/shows",
        "{\"movieId\":" + movieId + ",\"screenId\":" + screenId
            + ",\"startTime\":\"" + startTime.format(ISO) + "\","
            + "\"regularPrice\":200,\"premiumPrice\":400}"));
  }

  private org.springframework.test.web.servlet.ResultActions hold(long seatId, String code)
      throws Exception {
    long showId = showBySeat(seatId);
    String body = code == null
        ? "{\"showId\":" + showId + ",\"showSeatIds\":[" + seatId + "]}"
        : "{\"showId\":" + showId + ",\"showSeatIds\":[" + seatId + "],\"discountCode\":\"" + code + "\"}";
    return mockMvc.perform(post("/bookings")
            .header("Authorization", "Bearer " + customerToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isCreated());
  }

  private long showBySeat(long seatId) {
    return showSeatRepository.findById(seatId).orElseThrow().getShow().getId();
  }

  private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder pay(long id) {
    return post("/bookings/" + id + "/pay")
        .header("Authorization", "Bearer " + customerToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"token\":\"visa\"}");
  }

  private MvcResult adminPost(String path, String body) throws Exception {
    return mockMvc.perform(post(path)
            .header("Authorization", "Bearer " + adminToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isCreated())
        .andReturn();
  }

  private long id(MvcResult result) throws Exception {
    return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
  }

  private String login(String email, String password) throws Exception {
    MvcResult result = mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
        .andExpect(status().isOk())
        .andReturn();
    JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
    return node.get("token").asText();
  }
}
