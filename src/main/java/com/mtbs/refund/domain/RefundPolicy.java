package com.mtbs.refund.domain;

import com.mtbs.common.domain.BaseEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * A refund policy: a set of time-based tiers. Owned by a theater ({@code theaterId}) or, when
 * {@code theaterId} is null, the single system default used when a theater has no policy of its own.
 */
@Entity
@Table(name = "refund_policies")
public class RefundPolicy extends BaseEntity {

  @Column(unique = true)
  private Long theaterId;

  @ElementCollection
  @CollectionTable(name = "refund_policy_tiers", joinColumns = @JoinColumn(name = "policy_id"))
  private List<RefundTier> tiers = new ArrayList<>();

  protected RefundPolicy() {
  }

  public RefundPolicy(Long theaterId, List<RefundTier> tiers) {
    this.theaterId = theaterId;
    this.tiers = new ArrayList<>(tiers);
  }

  public Long getTheaterId() {
    return theaterId;
  }

  public List<RefundTier> getTiers() {
    return tiers;
  }

  /**
   * Refund percent for a cancellation happening {@code hoursUntilShow} before the show: the highest
   * tier whose threshold is met. No matching tier → 0%.
   */
  public BigDecimal percentFor(long hoursUntilShow) {
    return tiers.stream()
        .sorted(Comparator.comparingInt(RefundTier::getHoursBeforeShow).reversed())
        .filter(t -> hoursUntilShow >= t.getHoursBeforeShow())
        .map(RefundTier::getRefundPercent)
        .findFirst()
        .orElse(BigDecimal.ZERO);
  }
}
