package com.mtbs.show;

import com.mtbs.catalog.MovieRepository;
import com.mtbs.catalog.ScreenRepository;
import com.mtbs.catalog.domain.Movie;
import com.mtbs.catalog.domain.Screen;
import com.mtbs.catalog.domain.Seat;
import com.mtbs.common.error.ResourceNotFoundException;
import com.mtbs.show.domain.Show;
import com.mtbs.show.domain.ShowSeat;
import com.mtbs.show.domain.ShowSeatStatus;
import com.mtbs.show.dto.ShowDtos.PageResponse;
import com.mtbs.show.dto.ShowDtos.ScheduleShowRequest;
import com.mtbs.show.dto.ShowDtos.SeatMapEntry;
import com.mtbs.show.dto.ShowDtos.SeatMapResponse;
import com.mtbs.show.dto.ShowDtos.ShowResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Schedules shows (materializing per-seat inventory) and serves browse/seat-map reads. */
@Service
public class ShowService {

  private final MovieRepository movieRepository;
  private final ScreenRepository screenRepository;
  private final ShowRepository showRepository;
  private final ShowSeatRepository showSeatRepository;
  private final PricingCalculator pricingCalculator;

  public ShowService(
      MovieRepository movieRepository,
      ScreenRepository screenRepository,
      ShowRepository showRepository,
      ShowSeatRepository showSeatRepository,
      PricingCalculator pricingCalculator) {
    this.movieRepository = movieRepository;
    this.screenRepository = screenRepository;
    this.showRepository = showRepository;
    this.showSeatRepository = showSeatRepository;
    this.pricingCalculator = pricingCalculator;
  }

  @Transactional
  public ShowResponse scheduleShow(ScheduleShowRequest request) {
    Movie movie = movieRepository.findById(request.movieId())
        .orElseThrow(() -> new ResourceNotFoundException("Movie " + request.movieId() + " not found"));
    Screen screen = screenRepository.findById(request.screenId())
        .orElseThrow(() ->
            new ResourceNotFoundException("Screen " + request.screenId() + " not found"));
    if (screen.getSeats().isEmpty()) {
      throw new IllegalStateException("Screen " + screen.getId() + " has no seat layout");
    }

    Show show = showRepository.save(new Show(
        movie, screen, request.startTime(), request.regularPrice(), request.premiumPrice()));

    List<ShowSeat> showSeats = new ArrayList<>();
    for (Seat seat : screen.getSeats()) {
      var price = pricingCalculator.priceFor(
          seat.getCategory(), request.regularPrice(), request.premiumPrice(), request.startTime());
      showSeats.add(new ShowSeat(show, seat, price));
    }
    showSeatRepository.saveAll(showSeats);

    return ShowMapper.toShow(show);
  }

  @Transactional(readOnly = true)
  public PageResponse<ShowResponse> browse(Long cityId, Long movieId, LocalDate date, Pageable pageable) {
    LocalDateTime from = date != null ? date.atStartOfDay() : null;
    LocalDateTime to = date != null ? date.plusDays(1).atStartOfDay() : null;
    Page<Show> page = showRepository.browse(cityId, movieId, from, to, pageable);
    List<ShowResponse> content = page.getContent().stream().map(ShowMapper::toShow).toList();
    return new PageResponse<>(
        content, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
  }

  @Transactional(readOnly = true)
  public SeatMapResponse seatMap(Long showId) {
    Show show = showRepository.findById(showId)
        .orElseThrow(() -> new ResourceNotFoundException("Show " + showId + " not found"));
    Instant now = Instant.now();
    List<SeatMapEntry> seats = showSeatRepository.findByShowIdOrderByIdAsc(showId).stream()
        .map(ss -> presentEntry(ss, now))
        .toList();
    return new SeatMapResponse(show.getId(), show.getMovie().getTitle(), show.getStartTime(), seats);
  }

  /** An expired hold is presented as available, matching how a booking attempt would treat it. */
  private SeatMapEntry presentEntry(ShowSeat ss, Instant now) {
    SeatMapEntry entry = ShowMapper.toSeatMapEntry(ss);
    if (ss.isHoldExpired(now)) {
      return new SeatMapEntry(
          entry.showSeatId(), entry.seatLabel(), entry.category(), entry.price(),
          ShowSeatStatus.AVAILABLE.name());
    }
    return entry;
  }
}
