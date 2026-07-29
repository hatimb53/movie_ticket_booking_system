package com.mtbs.show;

import com.mtbs.show.domain.PricingConfig;
import com.mtbs.show.dto.PricingConfigDtos.PricingConfigResponse;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Owns the single {@link PricingConfig} row. Admin-editable at runtime, unlike a plain
 * {@code @Value} which would need a restart to change.
 */
@Service
public class PricingConfigService {

  private final PricingConfigRepository repository;
  private final BigDecimal defaultWeekendMultiplier;

  public PricingConfigService(
      PricingConfigRepository repository,
      @Value("${app.pricing.weekend-multiplier:1.25}") BigDecimal defaultWeekendMultiplier) {
    this.repository = repository;
    this.defaultWeekendMultiplier = defaultWeekendMultiplier;
  }

  @Transactional
  public BigDecimal getWeekendMultiplier() {
    return current().getWeekendMultiplier();
  }

  @Transactional(readOnly = true)
  public PricingConfigResponse get() {
    return toResponse(current());
  }

  @Transactional
  public PricingConfigResponse updateWeekendMultiplier(BigDecimal weekendMultiplier) {
    PricingConfig config = current();
    config.updateWeekendMultiplier(weekendMultiplier);
    return toResponse(config);
  }

  private PricingConfig current() {
    return repository.findAll().stream().findFirst()
        .orElseGet(() -> repository.save(new PricingConfig(defaultWeekendMultiplier)));
  }

  private PricingConfigResponse toResponse(PricingConfig config) {
    return new PricingConfigResponse(config.getWeekendMultiplier());
  }
}
