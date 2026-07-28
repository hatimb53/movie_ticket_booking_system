package com.mtbs.show;

import com.mtbs.show.dto.ShowDtos.PageResponse;
import com.mtbs.show.dto.ShowDtos.SeatMapResponse;
import com.mtbs.show.dto.ShowDtos.ShowResponse;
import java.time.LocalDate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Customer-facing browse and seat-map reads. */
@RestController
public class ShowQueryController {

  private final ShowService showService;

  public ShowQueryController(ShowService showService) {
    this.showService = showService;
  }

  @GetMapping("/shows")
  public PageResponse<ShowResponse> browse(
      @RequestParam(required = false) Long city,
      @RequestParam(required = false) Long movieId,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    Pageable pageable = PageRequest.of(page, size);
    return showService.browse(city, movieId, date, pageable);
  }

  @GetMapping("/shows/{id}/seats")
  public SeatMapResponse seatMap(@PathVariable Long id) {
    return showService.seatMap(id);
  }
}
