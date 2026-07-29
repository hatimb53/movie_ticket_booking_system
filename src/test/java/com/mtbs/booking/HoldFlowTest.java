package com.mtbs.booking;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtbs.auth.UserRepository;
import com.mtbs.auth.domain.Role;
import com.mtbs.auth.domain.User;
import com.mtbs.show.ShowSeatRepository;
import com.mtbs.show.domain.ShowSeat;
import com.mtbs.show.domain.ShowSeatStatus;
import java.time.Instant;
import java.util.List;
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
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Hold lifecycle at the HTTP seam plus the expiry mechanics: a hold marks seats HELD, a second
 * attempt on a held seat is rejected (409), an expired hold is reclaimable, and the sweeper frees
 * lapsed holds.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class HoldFlowTest {

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
  @Autowired
  private BookingService bookingService;

  private String adminToken;
  private String customerToken;
  private long showId;
  private long firstSeatId;

  @BeforeEach
  void setUp() throws Exception {
    userRepository.save(new User("admin@mtbs.com", passwordEncoder.encode("admin-pass"), Role.ADMIN));
    adminToken = login("admin@mtbs.com", "admin-pass");
    mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
        .content("{\"email\":\"cust@mtbs.com\",\"password\":\"cust-pass\"}"));
    customerToken = login("cust@mtbs.com", "cust-pass");

    long cityId = id(adminPost("/admin/cities", "{\"name\":\"Mumbai\",\"state\":\"MH\"}"));
    long theaterId = id(adminPost("/admin/theaters",
        "{\"cityId\":" + cityId + ",\"name\":\"PVR\",\"address\":\"Parel\"}"));
    long screenId = id(adminPost("/admin/screens",
        "{\"theaterId\":" + theaterId + ",\"name\":\"S1\"}"));
    mockMvc.perform(post("/admin/screens/" + screenId + "/layout")
        .header("Authorization", "Bearer " + adminToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"rows\":1,\"seatsPerRow\":2,\"premiumRows\":[]}")).andExpect(status().isOk());
    long movieId = id(adminPost("/admin/movies",
        "{\"title\":\"Dune\",\"durationMinutes\":155,\"language\":\"English\",\"rating\":\"UA\"}"));
    showId = id(adminPost("/admin/shows",
        "{\"movieId\":" + movieId + ",\"screenId\":" + screenId
            + ",\"startTime\":\"2026-12-01T18:30:00\",\"regularPrice\":200,\"premiumPrice\":400}"));
    firstSeatId = showSeatRepository.findByShowIdOrderByIdAsc(showId).get(0).getId();
  }

  @Test
  void holdMarksSeatsHeldAndReturnsPendingBooking() throws Exception {
    mockMvc.perform(post("/bookings/hold")
            .header("Authorization", "Bearer " + customerToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"showId\":" + showId + ",\"showSeatIds\":[" + firstSeatId + "]}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status", is("PENDING_PAYMENT")))
        .andExpect(jsonPath("$.total", is(200.00)))
        .andExpect(jsonPath("$.seats.length()", is(1)));

    assertThat(showSeatRepository.findById(firstSeatId).orElseThrow().getStatus())
        .isEqualTo(ShowSeatStatus.HELD);
  }

  @Test
  void holdingAnAlreadyHeldSeatIsRejected() throws Exception {
    hold(customerToken, firstSeatId).andExpect(status().isCreated());

    // A second customer tries the same seat.
    mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
        .content("{\"email\":\"other@mtbs.com\",\"password\":\"other-pass\"}"));
    String otherToken = login("other@mtbs.com", "other-pass");

    hold(otherToken, firstSeatId).andExpect(status().isConflict());
  }

  @Test
  void expiredHoldIsReclaimable() throws Exception {
    hold(customerToken, firstSeatId).andExpect(status().isCreated());

    // Force the hold to have lapsed.
    ShowSeat seat = showSeatRepository.findById(firstSeatId).orElseThrow();
    seat.hold(Instant.now().minusSeconds(60));
    showSeatRepository.saveAndFlush(seat);

    // Another customer can now reclaim it.
    mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
        .content("{\"email\":\"late@mtbs.com\",\"password\":\"late-pass\"}"));
    String lateToken = login("late@mtbs.com", "late-pass");
    hold(lateToken, firstSeatId).andExpect(status().isCreated());
  }

  @Test
  void sweeperReleasesExpiredHolds() throws Exception {
    hold(customerToken, firstSeatId).andExpect(status().isCreated());
    ShowSeat seat = showSeatRepository.findById(firstSeatId).orElseThrow();
    seat.hold(Instant.now().minusSeconds(60));
    showSeatRepository.saveAndFlush(seat);

    bookingService.releaseExpiredHolds();

    assertThat(showSeatRepository.findById(firstSeatId).orElseThrow().getStatus())
        .isEqualTo(ShowSeatStatus.AVAILABLE);
  }

  // --- helpers ---

  private org.springframework.test.web.servlet.ResultActions hold(String token, long seatId)
      throws Exception {
    return mockMvc.perform(post("/bookings/hold")
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"showId\":" + showId + ",\"showSeatIds\":[" + seatId + "]}"));
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
