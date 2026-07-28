package com.mtbs.show;

import static org.assertj.core.api.Assertions.assertThat;

import com.mtbs.catalog.domain.SeatCategory;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

/**
 * Pure unit tests for the two-axis pricing model: seat-category base price × weekend surcharge.
 * No Spring context — the calculator is a plain component.
 */
class PricingCalculatorTest {

  private final PricingCalculator calculator = new PricingCalculator(new BigDecimal("1.25"));

  private static final BigDecimal REGULAR_BASE = new BigDecimal("200.00");
  private static final BigDecimal PREMIUM_BASE = new BigDecimal("400.00");

  private static final LocalDateTime WEEKDAY = LocalDateTime.of(2026, 7, 29, 18, 30); // Wednesday
  private static final LocalDateTime WEEKEND = LocalDateTime.of(2026, 8, 1, 18, 30);   // Saturday

  @Test
  void regularSeatOnWeekdayIsBasePrice() {
    assertThat(calculator.priceFor(SeatCategory.REGULAR, REGULAR_BASE, PREMIUM_BASE, WEEKDAY))
        .isEqualByComparingTo("200.00");
  }

  @Test
  void premiumSeatOnWeekdayIsPremiumBase() {
    assertThat(calculator.priceFor(SeatCategory.PREMIUM, REGULAR_BASE, PREMIUM_BASE, WEEKDAY))
        .isEqualByComparingTo("400.00");
  }

  @Test
  void regularSeatOnWeekendGetsSurcharge() {
    assertThat(calculator.priceFor(SeatCategory.REGULAR, REGULAR_BASE, PREMIUM_BASE, WEEKEND))
        .isEqualByComparingTo("250.00");
  }

  @Test
  void premiumSeatOnWeekendStacksCategoryAndSurcharge() {
    assertThat(calculator.priceFor(SeatCategory.PREMIUM, REGULAR_BASE, PREMIUM_BASE, WEEKEND))
        .isEqualByComparingTo("500.00");
  }
}
