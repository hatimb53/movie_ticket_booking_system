package com.mtbs.booking;

import com.mtbs.booking.domain.Booking;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<Booking, Long> {

  List<Booking> findByOwnerEmailOrderByIdDesc(String email);

  @Query("""
      select b from Booking b
      where b.status = com.mtbs.booking.domain.BookingStatus.CONFIRMED
        and b.show.startTime between :from and :to
      """)
  List<Booking> findConfirmedWithShowStartBetween(
      @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
