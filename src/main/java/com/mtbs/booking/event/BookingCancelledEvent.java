package com.mtbs.booking.event;

import java.math.BigDecimal;

/** Published after a booking is cancelled and its transaction commits. */
public record BookingCancelledEvent(
    Long bookingId, String recipientEmail, String movieTitle, BigDecimal refundAmount) {
}
