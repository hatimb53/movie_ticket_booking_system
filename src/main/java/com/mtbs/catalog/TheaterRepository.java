package com.mtbs.catalog;

import com.mtbs.catalog.domain.Theater;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TheaterRepository extends JpaRepository<Theater, Long> {

  List<Theater> findByCityId(Long cityId);
}
