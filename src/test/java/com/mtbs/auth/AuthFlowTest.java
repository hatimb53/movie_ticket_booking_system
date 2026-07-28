package com.mtbs.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Auth + RBAC behavior at the HTTP seam: register a customer, log in for a JWT, and verify role
 * gating (customer reaches customer endpoints, is 403'd from admin endpoints; missing/invalid
 * tokens are 401). A test-only secured controller stands in for real endpoints (added in later
 * slices).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(AuthFlowTest.SecuredProbeController.class)
class AuthFlowTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  private String register(String email, String password) throws Exception {
    return "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
  }

  @Test
  void registerCreatesCustomerAndLoginReturnsToken() throws Exception {
    mockMvc.perform(post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(register("alice@example.com", "s3cret-pass")))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.email", is("alice@example.com")))
        .andExpect(jsonPath("$.role", is("CUSTOMER")));

    mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(register("alice@example.com", "s3cret-pass")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token", is(notNullValue())))
        .andExpect(jsonPath("$.role", is("CUSTOMER")));
  }

  @Test
  void registrationIsRejectedForInvalidInput() throws Exception {
    mockMvc.perform(post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"not-an-email\",\"password\":\"x\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status", is(400)));
  }

  @Test
  void loginWithWrongPasswordIs401() throws Exception {
    mockMvc.perform(post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(register("bob@example.com", "correct-pass")))
        .andExpect(status().isCreated());

    mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(register("bob@example.com", "wrong-pass")))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void customerCanReachCustomerEndpointButNotAdminEndpoint() throws Exception {
    String token = tokenFor("carol@example.com", "pw-carol-123");

    mockMvc.perform(get("/test-secure/customer").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.user", is("carol@example.com")));

    mockMvc.perform(get("/test-secure/admin").header("Authorization", "Bearer " + token))
        .andExpect(status().isForbidden());
  }

  @Test
  void missingTokenIs401AndGarbageTokenIs401() throws Exception {
    mockMvc.perform(get("/test-secure/customer"))
        .andExpect(status().isUnauthorized());

    mockMvc.perform(get("/test-secure/customer").header("Authorization", "Bearer not.a.jwt"))
        .andExpect(status().isUnauthorized());
  }

  private String tokenFor(String email, String password) throws Exception {
    mockMvc.perform(post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(register(email, password)))
        .andExpect(status().isCreated());
    MvcResult result = mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(register(email, password)))
        .andExpect(status().isOk())
        .andReturn();
    JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
    return node.get("token").asText();
  }

  @RestController
  @RequestMapping("/test-secure")
  static class SecuredProbeController {

    @GetMapping("/customer")
    @PreAuthorize("hasRole('CUSTOMER')")
    java.util.Map<String, String> customer(Authentication auth) {
      return java.util.Map.of("user", auth.getName());
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    java.util.Map<String, String> admin(Authentication auth) {
      return java.util.Map.of("user", auth.getName());
    }
  }
}
