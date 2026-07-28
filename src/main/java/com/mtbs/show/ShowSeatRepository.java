package com.mtbs.show;

import com.mtbs.show.domain.ShowSeat;
import com.mtbs.show.domain.ShowSeatStatus;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShowSeatRepository extends JpaRepository<ShowSeat, Long> {

  List<ShowSeat> findByShowIdOrderByIdAsc(Long showId);

  /**
   * Loads the requested seats under a pessimistic write lock (SELECT … FOR UPDATE), ordered by id
   * so concurrent holds acquire locks in the same order and cannot deadlock. A competing
   * transaction blocks here until the lock holder commits, then sees the updated status.
   */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select ss from ShowSeat ss where ss.id in :ids order by ss.id asc")
  List<ShowSeat> lockByIds(@Param("ids") List<Long> ids);

  List<ShowSeat> findByStatusAndHeldUntilBefore(ShowSeatStatus status, Instant now);
}
