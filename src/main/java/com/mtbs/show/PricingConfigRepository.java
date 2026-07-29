package com.mtbs.show;

import com.mtbs.show.domain.PricingConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PricingConfigRepository extends JpaRepository<PricingConfig, Long> {
}
