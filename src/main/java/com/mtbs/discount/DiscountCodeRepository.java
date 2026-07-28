package com.mtbs.discount;

import com.mtbs.discount.domain.DiscountCode;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DiscountCodeRepository extends JpaRepository<DiscountCode, Long> {

  Optional<DiscountCode> findByCode(String code);

  boolean existsByCode(String code);

  /** Locks the code row so the usage-limit check-and-increment can't be raced (over-redemption). */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select d from DiscountCode d where d.id = :id")
  Optional<DiscountCode> lockById(@Param("id") Long id);
}
