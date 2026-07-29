package com.mtbs.booking;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtbs.auth.UserRepository;
import com.mtbs.auth.domain.Role;
import com.mtbs.auth.domain.User;
import com.mtbs.show.ShowSeatRepository;
import com.mtbs.show.domain.ShowSeatStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Payment → confirmation/failure state machine and booking history at the HTTP seam. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PaymentFlowTest {

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
  private long showId;
  private long seatId;

  @BeforeEach
  void setUp() throws Exception {
    userRepository.save(new User("admin@mtbs.com", passwordEncoder.encode("admin-pass"), Role.ADMIN));
    adminToken = login("admin@mtbs.com", "admin-pass");
    register("buyer@mtbs.com", "buyer-pass");
    customerToken = login("buyer@mtbs.com", "buyer-pass");

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
    seatId = showSeatRepository.findByShowIdOrderByIdAsc(showId).get(0).getId();
  }

  @Test
  void payingAHeldBookingConfirmsItAndBooksSeats() throws Exception {
    long bookingId = id(hold(customerToken, seatId).andExpect(status().isCreated()).andReturn());

    mockMvc.perform(pay(bookingId, "visa-1234"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status", is("CONFIRMED")));

    assertThat(showSeatRepository.findById(seatId).orElseThrow().getStatus())
        .isEqualTo(ShowSeatStatus.BOOKED);

    mockMvc.perform(get("/bookings").header("Authorization", "Bearer " + customerToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*].status", hasItem("CONFIRMED")));
  }

  @Test
  void failedPaymentLeavesBookingUnconfirmedAndSeatStillHeld() throws Exception {
    long bookingId = id(hold(customerToken, seatId).andExpect(status().isCreated()).andReturn());

    mockMvc.perform(pay(bookingId, "fail"))
        .andExpect(status().isPaymentRequired())
        .andExpect(jsonPath("$.status", is("PAYMENT_FAILED")));

    assertThat(showSeatRepository.findById(seatId).orElseThrow().getStatus())
        .isEqualTo(ShowSeatStatus.HELD);
  }

  @Test
  void payingAlreadyConfirmedBookingIsConflict() throws Exception {
    long bookingId = id(hold(customerToken, seatId).andExpect(status().isCreated()).andReturn());
    mockMvc.perform(pay(bookingId, "visa")).andExpect(status().isOk());

    mockMvc.perform(pay(bookingId, "visa")).andExpect(status().isConflict());
  }

  @Test
  void cannotPayAnotherCustomersBooking() throws Exception {
    long bookingId = id(hold(customerToken, seatId).andExpect(status().isCreated()).andReturn());

    register("intruder@mtbs.com", "intruder-pass");
    String intruderToken = login("intruder@mtbs.com", "intruder-pass");
    mockMvc.perform(post("/bookings/" + bookingId + "/pay")
            .header("Authorization", "Bearer " + intruderToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"token\":\"visa\"}"))
        .andExpect(status().isForbidden());
  }

  // --- helpers ---

  private ResultActions hold(String token, long sId) throws Exception {
    return mockMvc.perform(post("/bookings/hold")
        .header("Authorization", "Bearer " + token)
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"showId\":" + showId + ",\"showSeatIds\":[" + sId + "]}"));
  }

  private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder pay(
      long bookingId, String cardToken) {
    return post("/bookings/" + bookingId + "/pay")
        .header("Authorization", "Bearer " + customerToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"token\":\"" + cardToken + "\"}");
  }

  private void register(String email, String password) throws Exception {
    mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"));
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
