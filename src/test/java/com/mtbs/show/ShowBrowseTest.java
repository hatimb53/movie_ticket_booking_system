package com.mtbs.show;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtbs.auth.UserRepository;
import com.mtbs.auth.domain.Role;
import com.mtbs.auth.domain.User;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
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
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Show scheduling, eager seat materialization with frozen prices, and the customer browse +
 * seat-map read path, verified end-to-end at the HTTP seam.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ShowBrowseTest {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private PasswordEncoder passwordEncoder;

  private String adminToken;
  private String customerToken;

  @BeforeEach
  void setUp() throws Exception {
    userRepository.save(new User("admin@mtbs.com", passwordEncoder.encode("admin-pass"), Role.ADMIN));
    adminToken = login("admin@mtbs.com", "admin-pass");
    mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
        .content("{\"email\":\"c@mtbs.com\",\"password\":\"cust-pass\"}"));
    customerToken = login("c@mtbs.com", "cust-pass");
  }

  @Test
  void scheduleMaterializesSeatsWithFrozenPricesAndIsBrowsableWithSeatMap() throws Exception {
    long cityId = id(adminPost("/admin/cities", "{\"name\":\"Mumbai\",\"state\":\"MH\"}"));
    long theaterId = id(adminPost("/admin/theaters",
        "{\"cityId\":" + cityId + ",\"name\":\"PVR\",\"address\":\"Parel\"}"));
    long screenId = id(adminPost("/admin/screens",
        "{\"theaterId\":" + theaterId + ",\"name\":\"Screen 1\"}"));
    // rows=2, seatsPerRow=2, row 1 premium -> A1,A2 PREMIUM; B1,B2 REGULAR
    mockMvc.perform(post("/admin/screens/" + screenId + "/layout")
            .header("Authorization", "Bearer " + adminToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"rows\":2,\"seatsPerRow\":2,\"premiumRows\":[1]}"))
        .andExpect(status().isOk());
    long movieId = id(adminPost("/admin/movies",
        "{\"title\":\"Dune\",\"durationMinutes\":155,\"language\":\"English\",\"rating\":\"UA\"}"));

    // Weekday show -> no weekend surcharge: premium 400, regular 200. Computed relative to "now"
    // (not a hardcoded calendar date) so this test doesn't rot as real time passes it by.
    LocalDateTime weekday = nextWeekday();
    long showId = id(adminPost("/admin/shows",
        "{\"movieId\":" + movieId + ",\"screenId\":" + screenId
            + ",\"startTime\":\"" + weekday.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            + "\",\"regularPrice\":200,\"premiumPrice\":400}"));

    // Browse by city + movie + date finds the show.
    mockMvc.perform(get("/shows")
            .header("Authorization", "Bearer " + customerToken)
            .param("city", String.valueOf(cityId))
            .param("movieId", String.valueOf(movieId))
            .param("date", weekday.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements", is(1)))
        .andExpect(jsonPath("$.content[0].id", is((int) showId)))
        .andExpect(jsonPath("$.content[0].movieTitle", is("Dune")))
        .andExpect(jsonPath("$.content[0].cityName", is("Mumbai")));

    // Seat map: 4 seats, all AVAILABLE, frozen prices by category.
    MvcResult seatMap = mockMvc.perform(get("/shows/" + showId + "/seats")
            .header("Authorization", "Bearer " + customerToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.seats.length()", is(4)))
        .andReturn();

    JsonNode seats = objectMapper.readTree(seatMap.getResponse().getContentAsString()).get("seats");
    JsonNode a1 = seats.get(0);
    JsonNode b1 = seats.get(2);
    assertThat(a1.get("seatLabel").asText()).isEqualTo("A1");
    assertThat(a1.get("category").asText()).isEqualTo("PREMIUM");
    assertThat(a1.get("status").asText()).isEqualTo("AVAILABLE");
    assertThat(new BigDecimal(a1.get("price").asText())).isEqualByComparingTo("400.00");
    assertThat(b1.get("seatLabel").asText()).isEqualTo("B1");
    assertThat(b1.get("category").asText()).isEqualTo("REGULAR");
    assertThat(new BigDecimal(b1.get("price").asText())).isEqualByComparingTo("200.00");
  }

  @Test
  void weekendShowFreezesSurchargedPrices() throws Exception {
    long cityId = id(adminPost("/admin/cities", "{\"name\":\"Pune\",\"state\":\"MH\"}"));
    long theaterId = id(adminPost("/admin/theaters",
        "{\"cityId\":" + cityId + ",\"name\":\"INOX\",\"address\":\"FC Rd\"}"));
    long screenId = id(adminPost("/admin/screens",
        "{\"theaterId\":" + theaterId + ",\"name\":\"Audi 2\"}"));
    mockMvc.perform(post("/admin/screens/" + screenId + "/layout")
        .header("Authorization", "Bearer " + adminToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"rows\":1,\"seatsPerRow\":1,\"premiumRows\":[]}")).andExpect(status().isOk());
    long movieId = id(adminPost("/admin/movies",
        "{\"title\":\"Tenet\",\"durationMinutes\":150,\"language\":\"English\",\"rating\":\"UA\"}"));

    // A Saturday -> regular 200 * 1.25 = 250.00. Computed relative to "now" for the same reason.
    long showId = id(adminPost("/admin/shows",
        "{\"movieId\":" + movieId + ",\"screenId\":" + screenId
            + ",\"startTime\":\"" + nextWeekend().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            + "\",\"regularPrice\":200,\"premiumPrice\":400}"));

    MvcResult seatMap = mockMvc.perform(get("/shows/" + showId + "/seats")
            .header("Authorization", "Bearer " + customerToken))
        .andExpect(status().isOk())
        .andReturn();
    JsonNode price = objectMapper.readTree(seatMap.getResponse().getContentAsString())
        .get("seats").get(0).get("price");
    assertThat(new BigDecimal(price.asText())).isEqualByComparingTo("250.00");
  }

  // --- helpers ---

  /** A weekday evening far enough in the future that it stays "future" for a long time. */
  private static LocalDateTime nextWeekday() {
    LocalDate date = LocalDate.now().plusMonths(3);
    while (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
      date = date.plusDays(1);
    }
    return date.atTime(18, 30);
  }

  /** A Saturday evening far enough in the future that it stays "future" for a long time. */
  private static LocalDateTime nextWeekend() {
    LocalDate date = LocalDate.now().plusMonths(3);
    while (date.getDayOfWeek() != DayOfWeek.SATURDAY) {
      date = date.plusDays(1);
    }
    return date.atTime(20, 0);
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
    return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
  }
}
