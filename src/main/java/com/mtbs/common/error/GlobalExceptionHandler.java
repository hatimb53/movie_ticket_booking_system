package com.mtbs.common.error;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Translates exceptions into the consistent {@link ErrorResponse} body. Any exception not handled
 * explicitly falls through to {@link #handleUnexpected} as a 500 with a generic message, so
 * internal details never leak to clients.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
        .map(this::toFieldError)
        .toList();
    return build(HttpStatus.BAD_REQUEST, "Validation failed", request, fieldErrors);
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(
      ResourceNotFoundException ex, HttpServletRequest request) {
    return build(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
  }

  @ExceptionHandler(com.mtbs.auth.InvalidCredentialsException.class)
  public ResponseEntity<ErrorResponse> handleInvalidCredentials(
      com.mtbs.auth.InvalidCredentialsException ex, HttpServletRequest request) {
    return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), request, null);
  }

  @ExceptionHandler(com.mtbs.auth.DuplicateEmailException.class)
  public ResponseEntity<ErrorResponse> handleDuplicateEmail(
      com.mtbs.auth.DuplicateEmailException ex, HttpServletRequest request) {
    return build(HttpStatus.CONFLICT, ex.getMessage(), request, null);
  }

  @ExceptionHandler(com.mtbs.booking.SeatUnavailableException.class)
  public ResponseEntity<ErrorResponse> handleSeatUnavailable(
      com.mtbs.booking.SeatUnavailableException ex, HttpServletRequest request) {
    return build(HttpStatus.CONFLICT, ex.getMessage(), request, null);
  }

  @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handleAccessDenied(
      org.springframework.security.access.AccessDeniedException ex, HttpServletRequest request) {
    return build(HttpStatus.FORBIDDEN, "Access denied", request, null);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
    return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request, null);
  }

  private ErrorResponse.FieldError toFieldError(FieldError fieldError) {
    return new ErrorResponse.FieldError(fieldError.getField(), fieldError.getDefaultMessage());
  }

  private ResponseEntity<ErrorResponse> build(
      HttpStatus status,
      String message,
      HttpServletRequest request,
      List<ErrorResponse.FieldError> fieldErrors) {
    ErrorResponse body = new ErrorResponse(
        Instant.now(),
        status.value(),
        status.getReasonPhrase(),
        message,
        request.getRequestURI(),
        fieldErrors);
    return ResponseEntity.status(status).body(body);
  }
}
