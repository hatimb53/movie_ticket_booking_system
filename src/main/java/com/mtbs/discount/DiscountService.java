package com.mtbs.discount;

import com.mtbs.common.error.ResourceNotFoundException;
import com.mtbs.discount.domain.DiscountCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Validates and applies discount codes. {@link #evaluate} is a read-only check used at hold time;
 * {@link #redeem} performs the guarded check-and-increment at confirmation under a row lock so the
 * usage limit can't be over-redeemed. {@link #release} reverses a redemption on cancellation.
 */
@Service
public class DiscountService {

  private final DiscountCodeRepository repository;

  public DiscountService(DiscountCodeRepository repository) {
    this.repository = repository;
  }

  /** Result of applying a code to a subtotal (no state change). */
  public record DiscountApplication(DiscountCode code, BigDecimal discountAmount, BigDecimal total) {
  }

  @Transactional(readOnly = true)
  public DiscountApplication evaluate(String code, BigDecimal subtotal) {
    DiscountCode dc = repository.findByCode(code)
        .orElseThrow(() -> new DiscountNotApplicableException("Unknown discount code: " + code));
    LocalDateTime now = LocalDateTime.now();
    if (!dc.isActive()) {
      throw new DiscountNotApplicableException("Discount code is inactive");
    }
    if (!dc.isWithinWindow(now)) {
      throw new DiscountNotApplicableException("Discount code is expired or not yet valid");
    }
    if (dc.getMinBookingAmount() != null && subtotal.compareTo(dc.getMinBookingAmount()) < 0) {
      throw new DiscountNotApplicableException(
          "Booking total is below the minimum for this discount");
    }
    if (dc.isExhausted()) {
      throw new DiscountNotApplicableException("Discount code usage limit reached");
    }
    BigDecimal amount = dc.discountFor(subtotal);
    return new DiscountApplication(dc, amount, subtotal.subtract(amount));
  }

  /**
   * Locks the code row and re-validates it (no increment). Call before charging so an exhausted or
   * expired code fails fast; the lock is held for the rest of the transaction, serialising
   * redemptions of the same code.
   */
  @Transactional
  public void assertRedeemable(Long discountId) {
    lockAndValidate(discountId);
  }

  /** Increments usedCount after a successful charge (row already locked in the same transaction). */
  @Transactional
  public void redeem(Long discountId) {
    lockAndValidate(discountId).incrementUsed();
  }

  private DiscountCode lockAndValidate(Long discountId) {
    DiscountCode dc = repository.lockById(discountId)
        .orElseThrow(() -> new ResourceNotFoundException("Discount " + discountId + " not found"));
    if (!dc.isActive() || !dc.isWithinWindow(LocalDateTime.now()) || dc.isExhausted()) {
      throw new DiscountNotApplicableException("Discount code is no longer valid");
    }
    return dc;
  }

  /** Reverses a redemption (e.g. on cancellation) under a row lock. */
  @Transactional
  public void release(Long discountId) {
    repository.lockById(discountId).ifPresent(DiscountCode::decrementUsed);
  }
}
