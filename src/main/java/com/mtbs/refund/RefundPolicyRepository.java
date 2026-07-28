package com.mtbs.refund;

import com.mtbs.refund.domain.RefundPolicy;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundPolicyRepository extends JpaRepository<RefundPolicy, Long> {

  Optional<RefundPolicy> findByTheaterId(Long theaterId);

  Optional<RefundPolicy> findByTheaterIdIsNull();
}
