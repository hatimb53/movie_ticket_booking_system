package com.mtbs.booking;

import com.mtbs.booking.domain.Booking;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {

  List<Booking> findByOwnerEmailOrderByIdDesc(String email);
}
