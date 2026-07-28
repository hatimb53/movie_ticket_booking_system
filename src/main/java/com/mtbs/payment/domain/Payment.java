package com.mtbs.payment.domain;

import com.mtbs.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/** A payment attempt against a booking. A booking may accrue multiple attempts (retries). */
@Entity
@Table(name = "payments")
public class Payment extends BaseEntity {

  @Column(nullable = false)
  private Long bookingId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PaymentStatus status;

  @Column(nullable = false)
  private BigDecimal amount;

  @Column
  private String reference;

  protected Payment() {
  }

  public Payment(Long bookingId, PaymentStatus status, BigDecimal amount, String reference) {
    this.bookingId = bookingId;
    this.status = status;
    this.amount = amount;
    this.reference = reference;
  }

  public Long getBookingId() {
    return bookingId;
  }

  public PaymentStatus getStatus() {
    return status;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public String getReference() {
    return reference;
  }
}
