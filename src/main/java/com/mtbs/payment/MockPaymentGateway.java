package com.mtbs.payment;

import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Deterministic mock gateway: every charge succeeds EXCEPT when the token is {@code "fail"}
 * (case-insensitive), which lets tests and demos exercise the payment-failure path predictably.
 */
@Component
public class MockPaymentGateway implements PaymentGateway {

  static final String FAIL_TOKEN = "fail";

  @Override
  public PaymentOutcome charge(ChargeRequest request) {
    if (FAIL_TOKEN.equalsIgnoreCase(request.token())) {
      return new PaymentOutcome(false, null);
    }
    return new PaymentOutcome(true, "MOCK-" + UUID.randomUUID());
  }

  @Override
  public RefundOutcome refund(RefundRequest request) {
    // The mock always refunds successfully.
    return new RefundOutcome(true, "MOCK-REFUND-" + UUID.randomUUID());
  }
}
