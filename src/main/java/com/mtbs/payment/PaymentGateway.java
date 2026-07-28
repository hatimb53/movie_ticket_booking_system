package com.mtbs.payment;

import java.math.BigDecimal;

/**
 * Payment provider seam. A real gateway (Stripe, etc.) would implement this; the app ships a
 * deterministic mock. Kept as an interface so the provider can be swapped without touching the
 * booking flow.
 */
public interface PaymentGateway {

  PaymentOutcome charge(ChargeRequest request);

  record ChargeRequest(BigDecimal amount, String token) {
  }

  record PaymentOutcome(boolean success, String reference) {
  }
}
