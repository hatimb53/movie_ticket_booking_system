package com.mtbs.show.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/** Request/response records for admin pricing-tier configuration. */
public final class PricingConfigDtos {

  private PricingConfigDtos() {
  }

  public record UpdatePricingConfigRequest(@NotNull @Positive BigDecimal weekendMultiplier) {
  }

  public record PricingConfigResponse(BigDecimal weekendMultiplier) {
  }
}
