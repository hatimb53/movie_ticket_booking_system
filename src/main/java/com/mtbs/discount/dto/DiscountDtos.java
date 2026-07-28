package com.mtbs.discount.dto;

import com.mtbs.discount.domain.DiscountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Request/response records for admin discount management. */
public final class DiscountDtos {

  private DiscountDtos() {
  }

  public record CreateDiscountRequest(
      @NotBlank String code,
      @NotNull DiscountType type,
      @NotNull @Positive BigDecimal value,
      BigDecimal maxDiscountAmount,
      BigDecimal minBookingAmount,
      @NotNull LocalDateTime validFrom,
      @NotNull LocalDateTime validUntil,
      Integer usageLimit,
      boolean active) {
  }

  public record DiscountResponse(
      Long id,
      String code,
      String type,
      BigDecimal value,
      BigDecimal maxDiscountAmount,
      BigDecimal minBookingAmount,
      LocalDateTime validFrom,
      LocalDateTime validUntil,
      Integer usageLimit,
      int usedCount,
      boolean active) {
  }
}
