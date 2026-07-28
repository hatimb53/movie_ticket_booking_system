package com.mtbs.booking;

import com.mtbs.show.ShowSeatRepository;
import com.mtbs.show.domain.ShowSeat;
import com.mtbs.show.domain.ShowSeatStatus;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Periodically releases lapsed holds so freed inventory shows up in listings promptly. Correctness
 * never depends on this — a booking attempt reclaims an expired hold lazily under the lock — but it
 * keeps the data presentable between collisions.
 */
@Component
public class HoldSweeper {

  private static final Logger log = LoggerFactory.getLogger(HoldSweeper.class);

  private final ShowSeatRepository showSeatRepository;

  public HoldSweeper(ShowSeatRepository showSeatRepository) {
    this.showSeatRepository = showSeatRepository;
  }

  @Scheduled(fixedDelayString = "${app.hold.sweeper-interval-ms:30000}")
  @Transactional
  public void releaseExpiredHolds() {
    List<ShowSeat> expired =
        showSeatRepository.findByStatusAndHeldUntilBefore(ShowSeatStatus.HELD, Instant.now());
    if (expired.isEmpty()) {
      return;
    }
    expired.forEach(ShowSeat::release);
    showSeatRepository.saveAll(expired);
    log.debug("Released {} expired seat hold(s)", expired.size());
  }
}
