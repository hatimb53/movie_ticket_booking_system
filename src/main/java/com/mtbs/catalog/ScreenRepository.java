package com.mtbs.catalog;

import com.mtbs.catalog.domain.Screen;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ScreenRepository extends JpaRepository<Screen, Long> {

  /**
   * Locks the screen row (SELECT … FOR UPDATE) so two admins scheduling shows on the same screen
   * at the same time serialize: the second scheduleShow blocks here until the first commits, then
   * re-checks for overlap against the now-committed show.
   */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select s from Screen s where s.id = :id")
  Optional<Screen> lockById(@Param("id") Long id);
}
