package com.mtbs.discount;

import static org.assertj.core.api.Assertions.assertThat;

import com.mtbs.discount.domain.DiscountCode;
import com.mtbs.discount.domain.DiscountType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

/** Pure unit tests for discount amount computation: percentage, cap, flat, and subtotal clamp. */
class DiscountCalculationTest {

  private DiscountCode code(DiscountType type, String value, String cap) {
    return new DiscountCode("C", type, new BigDecimal(value),
        cap == null ? null : new BigDecimal(cap), null,
        LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1), null, true);
  }

  @Test
  void percentageDiscount() {
    assertThat(code(DiscountType.PERCENTAGE, "10", null).discountFor(new BigDecimal("500.00")))
        .isEqualByComparingTo("50.00");
  }

  @Test
  void percentageDiscountIsCappedByMaxAmount() {
    assertThat(code(DiscountType.PERCENTAGE, "50", "100").discountFor(new BigDecimal("500.00")))
        .isEqualByComparingTo("100.00");
  }

  @Test
  void flatDiscount() {
    assertThat(code(DiscountType.FLAT, "75", null).discountFor(new BigDecimal("500.00")))
        .isEqualByComparingTo("75.00");
  }

  @Test
  void discountNeverExceedsSubtotal() {
    assertThat(code(DiscountType.FLAT, "999", null).discountFor(new BigDecimal("200.00")))
        .isEqualByComparingTo("200.00");
  }
}
