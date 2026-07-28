package com.mtbs.refund.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.List;

/** Request/response records for admin refund-policy management. */
public final class RefundPolicyDtos {

  private RefundPolicyDtos() {
  }

  public record TierDto(
      @PositiveOrZero int hoursBeforeShow,
      @NotNull @PositiveOrZero BigDecimal refundPercent) {
  }

  /** {@code theaterId} null defines/updates the system default policy. */
  public record CreateRefundPolicyRequest(Long theaterId, @NotEmpty List<TierDto> tiers) {
  }

  public record RefundPolicyResponse(Long id, Long theaterId, List<TierDto> tiers) {
  }
}
