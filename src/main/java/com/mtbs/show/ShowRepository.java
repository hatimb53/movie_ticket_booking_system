package com.mtbs.show;

import com.mtbs.show.domain.Show;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShowRepository extends JpaRepository<Show, Long> {

  /** Fetches the movie eagerly — the overlap check needs each show's runtime. */
  @Query("select s from Show s join fetch s.movie where s.screen.id = :screenId")
  List<Show> findByScreenId(@Param("screenId") Long screenId);

  /**
   * Browse shows with all filters optional: city, movie, and a date window. A null filter matches
   * everything for that dimension.
   */
  @Query("""
      select s from Show s
      where (:cityId is null or s.screen.theater.city.id = :cityId)
        and (:movieId is null or s.movie.id = :movieId)
        and (:from is null or s.startTime >= :from)
        and (:to is null or s.startTime < :to)
      order by s.startTime asc
      """)
  Page<Show> browse(
      @Param("cityId") Long cityId,
      @Param("movieId") Long movieId,
      @Param("from") LocalDateTime from,
      @Param("to") LocalDateTime to,
      Pageable pageable);
}
