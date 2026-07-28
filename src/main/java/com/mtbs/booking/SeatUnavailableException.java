package com.mtbs.booking;

/** Thrown when one or more requested seats are already held or booked. Mapped to HTTP 409. */
public class SeatUnavailableException extends RuntimeException {

  public SeatUnavailableException(String message) {
    super(message);
  }
}
