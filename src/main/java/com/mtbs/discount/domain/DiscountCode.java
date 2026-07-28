package com.mtbs.discount.domain;

import com.mtbs.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * A promotional code applied to a booking total. Percentage discounts may be capped by
 * {@code maxDiscountAmount}; a global {@code usageLimit} bounds total redemptions and is enforced
 * under a pessimistic lock at booking confirmation so it can never be over-redeemed.
 */
@Entity
@Table(name = "discount_codes")
public class DiscountCode extends BaseEntity {

  @Column(nullable = false, unique = true)
  private String code;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private DiscountType type;

  @Column(name = "discount_value", nullable = false)
  private BigDecimal value;

  @Column
  private BigDecimal maxDiscountAmount;

  @Column
  private BigDecimal minBookingAmount;

  @Column(nullable = false)
  private LocalDateTime validFrom;

  @Column(nullable = false)
  private LocalDateTime validUntil;

  @Column
  private Integer usageLimit;

  @Column(nullable = false)
  private int usedCount;

  @Column(nullable = false)
  private boolean active;

  protected DiscountCode() {
  }

  public DiscountCode(String code, DiscountType type, BigDecimal value, BigDecimal maxDiscountAmount,
      BigDecimal minBookingAmount, LocalDateTime validFrom, LocalDateTime validUntil,
      Integer usageLimit, boolean active) {
    this.code = code;
    this.type = type;
    this.value = value;
    this.maxDiscountAmount = maxDiscountAmount;
    this.minBookingAmount = minBookingAmount;
    this.validFrom = validFrom;
    this.validUntil = validUntil;
    this.usageLimit = usageLimit;
    this.usedCount = 0;
    this.active = active;
  }

  public String getCode() {
    return code;
  }

  public DiscountType getType() {
    return type;
  }

  public BigDecimal getValue() {
    return value;
  }

  public BigDecimal getMaxDiscountAmount() {
    return maxDiscountAmount;
  }

  public BigDecimal getMinBookingAmount() {
    return minBookingAmount;
  }

  public LocalDateTime getValidFrom() {
    return validFrom;
  }

  public LocalDateTime getValidUntil() {
    return validUntil;
  }

  public Integer getUsageLimit() {
    return usageLimit;
  }

  public int getUsedCount() {
    return usedCount;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public boolean isWithinWindow(LocalDateTime now) {
    return !now.isBefore(validFrom) && !now.isAfter(validUntil);
  }

  public boolean isExhausted() {
    return usageLimit != null && usedCount >= usageLimit;
  }

  public void incrementUsed() {
    this.usedCount++;
  }

  public void decrementUsed() {
    if (this.usedCount > 0) {
      this.usedCount--;
    }
  }

  /** Discount amount for a given pre-discount subtotal, never exceeding the subtotal. */
  public BigDecimal discountFor(BigDecimal subtotal) {
    BigDecimal raw = type == DiscountType.PERCENTAGE
        ? subtotal.multiply(value).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
        : value;
    if (maxDiscountAmount != null && raw.compareTo(maxDiscountAmount) > 0) {
      raw = maxDiscountAmount;
    }
    if (raw.compareTo(subtotal) > 0) {
      raw = subtotal;
    }
    return raw.setScale(2, RoundingMode.HALF_UP);
  }
}
