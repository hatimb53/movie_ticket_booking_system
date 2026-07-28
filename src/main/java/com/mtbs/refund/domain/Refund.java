package com.mtbs.refund.domain;

import com.mtbs.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/** A refund issued for a cancelled booking. */
@Entity
@Table(name = "refunds")
public class Refund extends BaseEntity {

  @Column(nullable = false)
  private Long bookingId;

  @Column(nullable = false)
  private BigDecimal amount;

  @Column(nullable = false)
  private BigDecimal refundPercent;

  @Column
  private String reference;

  protected Refund() {
  }

  public Refund(Long bookingId, BigDecimal amount, BigDecimal refundPercent, String reference) {
    this.bookingId = bookingId;
    this.amount = amount;
    this.refundPercent = refundPercent;
    this.reference = reference;
  }

  public Long getBookingId() {
    return bookingId;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public BigDecimal getRefundPercent() {
    return refundPercent;
  }

  public String getReference() {
    return reference;
  }
}
