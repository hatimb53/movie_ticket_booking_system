package com.mtbs.show.domain;

import com.mtbs.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * System-wide pricing tier configuration. A single row: the weekend surcharge multiplier applied
 * to a show's base prices (see {@link com.mtbs.show.PricingCalculator}). Admin-editable at
 * runtime via {@code /admin/pricing-config}, seeded from {@code app.pricing.weekend-multiplier} on
 * first read if no row exists yet.
 */
@Entity
@Table(name = "pricing_config")
public class PricingConfig extends BaseEntity {

  @Column(nullable = false)
  private BigDecimal weekendMultiplier;

  protected PricingConfig() {
  }

  public PricingConfig(BigDecimal weekendMultiplier) {
    this.weekendMultiplier = weekendMultiplier;
  }

  public BigDecimal getWeekendMultiplier() {
    return weekendMultiplier;
  }

  public void updateWeekendMultiplier(BigDecimal weekendMultiplier) {
    this.weekendMultiplier = weekendMultiplier;
  }
}
