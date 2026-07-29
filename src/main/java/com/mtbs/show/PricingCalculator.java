package com.mtbs.show;

import com.mtbs.catalog.domain.SeatCategory;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

/**
 * Computes a seat's price from two axes: the seat-category base price and a weekend surcharge
 * applied when the show falls on Saturday or Sunday. Prices are always scaled to 2 decimals.
 *
 * <p>Stateless on purpose — the weekend multiplier is admin-editable at runtime (see
 * {@link PricingConfigService}), so it's read once per {@code scheduleShow} call and passed in
 * here rather than cached on this component.
 */
@Component
public class PricingCalculator {

  public BigDecimal priceFor(
      SeatCategory category, BigDecimal regularBase, BigDecimal premiumBase,
      LocalDateTime startTime, BigDecimal weekendMultiplier) {
    BigDecimal base = category == SeatCategory.PREMIUM ? premiumBase : regularBase;
    BigDecimal price = isWeekend(startTime) ? base.multiply(weekendMultiplier) : base;
    return price.setScale(2, RoundingMode.HALF_UP);
  }

  public boolean isWeekend(LocalDateTime startTime) {
    DayOfWeek day = startTime.getDayOfWeek();
    return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
  }
}
