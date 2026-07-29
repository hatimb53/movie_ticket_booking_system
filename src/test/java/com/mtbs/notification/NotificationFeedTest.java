package com.mtbs.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mtbs.auth.UserRepository;
import com.mtbs.auth.domain.Role;
import com.mtbs.auth.domain.User;
import com.mtbs.notification.domain.Notification;
import com.mtbs.notification.domain.NotificationType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** GET /notifications: a user's own feed, latest first. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class NotificationFeedTest {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private PasswordEncoder passwordEncoder;
  @Autowired
  private NotificationRepository notificationRepository;

  @Test
  void listsOwnNotificationsLatestFirst() throws Exception {
    userRepository.save(
        new User("feed@mtbs.com", passwordEncoder.encode("pw-feed-1"), Role.CUSTOMER));

    // Notification.sentAt is stamped as Instant.now() in the constructor, so creating these in
    // this order gives them strictly increasing timestamps -- exercising the sort, not relying on
    // insertion order (findByRecipientEmailOrderBySentAtDesc, not ...OrderByIdDesc).
    save("feed@mtbs.com", NotificationType.BOOKING_CONFIRMED, "oldest");
    Thread.sleep(5);
    save("feed@mtbs.com", NotificationType.BOOKING_CANCELLED, "middle");
    Thread.sleep(5);
    save("feed@mtbs.com", NotificationType.SHOW_REMINDER, "newest");
    // A different user's notification must never leak into this feed.
    save("someone-else@mtbs.com", NotificationType.BOOKING_CONFIRMED, "not mine");

    String token = login("feed@mtbs.com", "pw-feed-1");

    mockMvc.perform(get("/notifications").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(3))
        .andExpect(jsonPath("$[0].message").value("newest"))
        .andExpect(jsonPath("$[1].message").value("middle"))
        .andExpect(jsonPath("$[2].message").value("oldest"));
  }

  private void save(String email, NotificationType type, String message) {
    notificationRepository.saveAndFlush(new Notification(email, type, "LOG", message, null));
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
