package com.mtbs.common.error;

import java.time.Instant;
import java.util.List;

/**
 * The single, consistent error body returned for every failed request.
 *
 * @param timestamp   when the error was produced
 * @param status      HTTP status code
 * @param error       HTTP reason phrase (e.g. "Bad Request")
 * @param message     human-readable summary safe to expose to clients
 * @param path        request path that produced the error
 * @param fieldErrors per-field validation messages, when applicable (otherwise null)
 */
public record ErrorResponse(
    Instant timestamp,
    int status,
    String error,
    String message,
    String path,
    List<FieldError> fieldErrors) {

  public record FieldError(String field, String message) {
  }
}
