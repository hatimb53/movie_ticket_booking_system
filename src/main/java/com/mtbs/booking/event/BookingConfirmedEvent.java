package com.mtbs.booking.event;

import java.time.LocalDateTime;

/** Published after a booking is confirmed and its transaction commits. */
public record BookingConfirmedEvent(
    Long bookingId, String recipientEmail, String movieTitle, LocalDateTime showStart) {
}
