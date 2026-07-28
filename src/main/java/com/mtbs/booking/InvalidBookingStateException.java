package com.mtbs.booking;

/** Thrown when an operation is invalid for the booking's current state. Mapped to HTTP 409. */
public class InvalidBookingStateException extends RuntimeException {

  public InvalidBookingStateException(String message) {
    super(message);
  }
}
