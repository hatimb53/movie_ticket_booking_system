package com.mtbs.refund;

import com.mtbs.refund.domain.RefundPolicy;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

/**
 * Resolves the refund percentage for a cancellation: prefer the booking's theater policy, fall back
 * to the system default, and if neither is configured refund the full amount (documented default).
 */
@Component
public class RefundPolicyResolver {

  private final RefundPolicyRepository policyRepository;

  public RefundPolicyResolver(RefundPolicyRepository policyRepository) {
    this.policyRepository = policyRepository;
  }

  public BigDecimal resolvePercent(Long theaterId, LocalDateTime showStart, LocalDateTime now) {
    long hoursUntilShow = Duration.between(now, showStart).toHours();
    return policyRepository.findByTheaterId(theaterId)
        .or(policyRepository::findByTheaterIdIsNull)
        .map(policy -> policy.percentFor(hoursUntilShow))
        .orElse(new BigDecimal("100"));
  }
}
