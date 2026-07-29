package com.mtbs.payment;

import com.mtbs.payment.PaymentGateway.ChargeRequest;
import com.mtbs.payment.PaymentGateway.PaymentOutcome;
import com.mtbs.payment.PaymentGateway.RefundOutcome;
import com.mtbs.payment.PaymentGateway.RefundRequest;
import com.mtbs.payment.domain.Payment;
import com.mtbs.payment.domain.PaymentStatus;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Payment-domain service: the single place that talks to the {@link PaymentGateway} and records
 * {@link Payment} attempts. It knows nothing about bookings beyond the {@code bookingId}
 * correlation, so the booking flow depends on payment and never the reverse. Both methods join the
 * caller's transaction ({@code REQUIRED}), so a payment record commits atomically with the booking
 * state change that triggered it.
 */
@Service
public class PaymentService {

  private final PaymentGateway paymentGateway;
  private final PaymentRepository paymentRepository;

  public PaymentService(PaymentGateway paymentGateway, PaymentRepository paymentRepository) {
    this.paymentGateway = paymentGateway;
    this.paymentRepository = paymentRepository;
  }

  /** Charges the gateway and records the attempt (SUCCESS or FAILED) against the booking. */
  @Transactional
  public ChargeResult charge(Long bookingId, BigDecimal amount, String token) {
    PaymentOutcome outcome = paymentGateway.charge(new ChargeRequest(amount, token));
    PaymentStatus status = outcome.success() ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
    paymentRepository.save(new Payment(bookingId, status, amount, outcome.reference()));
    return new ChargeResult(outcome.success(), outcome.reference());
  }

  /** Issues a refund through the gateway. Persistence of the refund record is the caller's concern. */
  @Transactional
  public RefundResult refund(Long bookingId, BigDecimal amount, String originalReference) {
    RefundOutcome outcome = paymentGateway.refund(new RefundRequest(amount, originalReference));
    return new RefundResult(outcome.success(), outcome.reference());
  }

  public record ChargeResult(boolean success, String reference) {
  }

  public record RefundResult(boolean success, String reference) {
  }
}
