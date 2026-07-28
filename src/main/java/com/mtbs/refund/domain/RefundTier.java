package com.mtbs.refund.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;

/**
 * One tier of a refund policy: if the booking is cancelled at least {@code hoursBeforeShow} hours
 * before showtime, {@code refundPercent} of the total is refunded.
 */
@Embeddable
public class RefundTier {

  @Column(nullable = false)
  private int hoursBeforeShow;

  @Column(nullable = false)
  private BigDecimal refundPercent;

  protected RefundTier() {
  }

  public RefundTier(int hoursBeforeShow, BigDecimal refundPercent) {
    this.hoursBeforeShow = hoursBeforeShow;
    this.refundPercent = refundPercent;
  }

  public int getHoursBeforeShow() {
    return hoursBeforeShow;
  }

  public BigDecimal getRefundPercent() {
    return refundPercent;
  }
}
