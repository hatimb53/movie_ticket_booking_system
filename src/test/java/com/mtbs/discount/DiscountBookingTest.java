package com.mtbs.discount;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtbs.auth.UserRepository;
import com.mtbs.auth.domain.Role;
import com.mtbs.auth.domain.User;
import com.mtbs.show.ShowSeatRepository;
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

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Discount codes applied at checkout and redeemed at confirmation, at the HTTP seam. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DiscountBookingTest {

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
  private long premiumSeatId;

  @BeforeEach
  void setUp() throws Exception {
    userRepository.save(new User("admin@mtbs.com", passwordEncoder.encode("admin-pass"), Role.ADMIN));
    adminToken = login("admin@mtbs.com", "admin-pass");
    mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
        .content("{\"email\":\"buyer@mtbs.com\",\"password\":\"buyer-pass\"}"));
    customerToken = login("buyer@mtbs.com", "buyer-pass");

    long cityId = id(adminPost("/admin/cities", "{\"name\":\"Mumbai\",\"state\":\"MH\"}"));
    long theaterId = id(adminPost("/admin/theaters",
        "{\"cityId\":" + cityId + ",\"name\":\"PVR\",\"address\":\"Parel\"}"));
    long screenId = id(adminPost("/admin/screens",
        "{\"theaterId\":" + theaterId + ",\"name\":\"S1\"}"));
    mockMvc.perform(post("/admin/screens/" + screenId + "/layout")
        .header("Authorization", "Bearer " + adminToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"rows\":1,\"seatsPerRow\":1,\"premiumRows\":[1]}")).andExpect(status().isOk());
    long movieId = id(adminPost("/admin/movies",
        "{\"title\":\"Dune\",\"durationMinutes\":155,\"language\":\"English\",\"rating\":\"UA\"}"));
    // Weekday show, premium seat = 400.
    showId = id(adminPost("/admin/shows",
        "{\"movieId\":" + movieId + ",\"screenId\":" + screenId
            + ",\"startTime\":\"2026-12-01T18:30:00\",\"regularPrice\":200,\"premiumPrice\":400}"));
    premiumSeatId = showSeatRepository.findByShowIdOrderByIdAsc(showId).get(0).getId();
  }

  @Test
  void percentageCodeLowersTotalAndIsRedeemedOnConfirmation() throws Exception {
    // 10% off, min 100, cap 1000, valid now.
    adminPost("/admin/discounts",
        "{\"code\":\"SAVE10\",\"type\":\"PERCENTAGE\",\"value\":10,\"minBookingAmount\":100,"
            + "\"validFrom\":\"2026-01-01T00:00:00\",\"validUntil\":\"2027-01-01T00:00:00\","
            + "\"usageLimit\":5,\"active\":true}");

    // Hold with the code: 400 - 10% = 360.
    long bookingId = id(mockMvc.perform(post("/shows/" + showId + "/holds")
            .header("Authorization", "Bearer " + customerToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"showSeatIds\":[" + premiumSeatId + "],\"discountCode\":\"SAVE10\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.subtotal", is(400.00)))
        .andExpect(jsonPath("$.discountAmount", is(40.00)))
        .andExpect(jsonPath("$.total", is(360.00)))
        .andExpect(jsonPath("$.discountCode", is("SAVE10")))
        .andReturn());

    mockMvc.perform(post("/bookings/" + bookingId + "/pay")
            .header("Authorization", "Bearer " + customerToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"token\":\"visa\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status", is("CONFIRMED")))
        .andExpect(jsonPath("$.total", is(360.00)));

    // usedCount incremented on confirmation.
    mockMvc.perform(get("/admin/discounts").header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.code=='SAVE10')].usedCount", hasItem(1)));
  }

  @Test
  void belowMinimumIsRejected() throws Exception {
    adminPost("/admin/discounts",
        "{\"code\":\"BIG\",\"type\":\"FLAT\",\"value\":50,\"minBookingAmount\":100000,"
            + "\"validFrom\":\"2026-01-01T00:00:00\",\"validUntil\":\"2027-01-01T00:00:00\","
            + "\"active\":true}");

    mockMvc.perform(post("/shows/" + showId + "/holds")
            .header("Authorization", "Bearer " + customerToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"showSeatIds\":[" + premiumSeatId + "],\"discountCode\":\"BIG\"}"))
        .andExpect(status().isUnprocessableEntity());
  }

  @Test
  void unknownCodeIsRejected() throws Exception {
    mockMvc.perform(post("/shows/" + showId + "/holds")
            .header("Authorization", "Bearer " + customerToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"showSeatIds\":[" + premiumSeatId + "],\"discountCode\":\"NOPE\"}"))
        .andExpect(status().isUnprocessableEntity());
  }

  // --- helpers ---

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
