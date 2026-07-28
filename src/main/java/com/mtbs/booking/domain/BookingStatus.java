package com.mtbs.booking.domain;

/** Lifecycle of a booking. Payment/cancellation transitions land in tickets 06 and 08. */
public enum BookingStatus {
  PENDING_PAYMENT,
  CONFIRMED,
  PAYMENT_FAILED,
  CANCELLED
}
