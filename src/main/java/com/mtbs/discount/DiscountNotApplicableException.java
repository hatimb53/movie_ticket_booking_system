package com.mtbs.discount;

/** Thrown when a discount code cannot be applied (unknown, inactive, expired, below min, exhausted). Mapped to HTTP 422. */
public class DiscountNotApplicableException extends RuntimeException {

  public DiscountNotApplicableException(String message) {
    super(message);
  }
}
