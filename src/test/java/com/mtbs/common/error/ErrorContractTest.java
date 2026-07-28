package com.mtbs.common.error;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies the global error contract at the HTTP seam: every failure mode returns the same
 * structured JSON body ({timestamp, status, error, message, path}) with the correct status code.
 * Uses a test-only controller that deliberately throws each error type.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(ErrorContractTest.ThrowingController.class)
class ErrorContractTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void validationFailureReturns400WithStructuredBody() throws Exception {
    mockMvc.perform(post("/test-errors/validate")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status", is(400)))
        .andExpect(jsonPath("$.error", is("Bad Request")))
        .andExpect(jsonPath("$.path", is("/test-errors/validate")))
        .andExpect(jsonPath("$.timestamp", is(notNullValue())))
        .andExpect(jsonPath("$.message", is(notNullValue())));
  }

  @Test
  void notFoundReturns404WithStructuredBody() throws Exception {
    mockMvc.perform(get("/test-errors/missing"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status", is(404)))
        .andExpect(jsonPath("$.error", is("Not Found")))
        .andExpect(jsonPath("$.path", is("/test-errors/missing")))
        .andExpect(jsonPath("$.message", is("Widget 42 not found")))
        .andExpect(jsonPath("$.timestamp", is(notNullValue())));
  }

  @Test
  void uncaughtExceptionReturns500WithoutLeakingDetails() throws Exception {
    mockMvc.perform(get("/test-errors/boom"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.status", is(500)))
        .andExpect(jsonPath("$.error", is("Internal Server Error")))
        .andExpect(jsonPath("$.path", is("/test-errors/boom")))
        .andExpect(jsonPath("$.message", is("An unexpected error occurred")))
        .andExpect(jsonPath("$.timestamp", is(notNullValue())));
  }

  @RestController
  @RequestMapping("/test-errors")
  static class ThrowingController {

    @PostMapping("/validate")
    String validate(@Valid @RequestBody EchoRequest request) {
      return request.name();
    }

    @GetMapping("/missing")
    String missing() {
      throw new ResourceNotFoundException("Widget 42 not found");
    }

    @GetMapping("/boom")
    String boom() {
      throw new IllegalStateException("internal detail that must not leak");
    }

    record EchoRequest(@NotBlank String name) {
    }
  }
}
