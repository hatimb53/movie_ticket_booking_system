package com.mtbs.show;

import com.mtbs.show.domain.ShowSeat;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShowSeatRepository extends JpaRepository<ShowSeat, Long> {

  List<ShowSeat> findByShowIdOrderByIdAsc(Long showId);
}
