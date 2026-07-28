package com.mtbs.booking.dto;

import java.math.BigDecimal;

public record CancellationResponse(
    Long bookingId, String status, BigDecimal refundAmount, BigDecimal refundPercent) {
}
