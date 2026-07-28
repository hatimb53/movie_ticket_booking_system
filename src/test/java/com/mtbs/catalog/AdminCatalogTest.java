package com.mtbs.catalog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtbs.auth.UserRepository;
import com.mtbs.auth.domain.Role;
import com.mtbs.auth.domain.User;
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

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Admin catalog management at the HTTP seam: an admin builds the venue tree (city → theater →
 * screen → generated seat layout) and manages movies; a customer is forbidden from writing.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional // each test rolls back so the shared in-memory H2 stays isolated between methods
class AdminCatalogTest {

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
    customerToken = registerAndLogin("cust@mtbs.com", "cust-pass");
  }

  @Test
  void adminBuildsVenueTreeAndLayoutGeneratesCategorizedSeats() throws Exception {
    long cityId = idOf(adminPost("/admin/cities", "{\"name\":\"Mumbai\",\"state\":\"MH\"}"));

    long theaterId = idOf(adminPost("/admin/theaters",
        "{\"cityId\":" + cityId + ",\"name\":\"PVR Phoenix\",\"address\":\"Lower Parel\"}"));

    long screenId = idOf(adminPost("/admin/screens",
        "{\"theaterId\":" + theaterId + ",\"name\":\"Screen 1\"}"));

    // rows=2, seatsPerRow=3, row 1 premium -> A1..A3 PREMIUM, B1..B3 REGULAR
    mockMvc.perform(post("/admin/screens/" + screenId + "/layout")
            .header("Authorization", "Bearer " + adminToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"rows\":2,\"seatsPerRow\":3,\"premiumRows\":[1]}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.seats.length()", is(6)))
        .andExpect(jsonPath("$.seats[0].label", is("A1")))
        .andExpect(jsonPath("$.seats[0].category", is("PREMIUM")))
        .andExpect(jsonPath("$.seats[3].label", is("B1")))
        .andExpect(jsonPath("$.seats[3].category", is("REGULAR")));
  }

  @Test
  void adminCreatesMovieAndItIsListed() throws Exception {
    adminPost("/admin/movies",
        "{\"title\":\"Inception\",\"durationMinutes\":148,\"language\":\"English\",\"rating\":\"UA\"}");

    // Assert by content, not position — other tests may have committed movies into shared H2.
    mockMvc.perform(get("/movies").header("Authorization", "Bearer " + customerToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*].title", org.hamcrest.Matchers.hasItem("Inception")))
        .andExpect(jsonPath("$[?(@.title=='Inception')].durationMinutes",
            org.hamcrest.Matchers.hasItem(148)));
  }

  @Test
  void customerCannotCreateCity() throws Exception {
    mockMvc.perform(post("/admin/cities")
            .header("Authorization", "Bearer " + customerToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"Delhi\",\"state\":\"DL\"}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void creatingTheaterInMissingCityIs404() throws Exception {
    mockMvc.perform(post("/admin/theaters")
            .header("Authorization", "Bearer " + adminToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"cityId\":99999,\"name\":\"Ghost\",\"address\":\"nowhere\"}"))
        .andExpect(status().isNotFound());
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

  private long idOf(MvcResult result) throws Exception {
    JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
    return node.get("id").asLong();
  }

  private String registerAndLogin(String email, String password) throws Exception {
    mockMvc.perform(post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
        .andExpect(status().isCreated());
    return login(email, password);
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
