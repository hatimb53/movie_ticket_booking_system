package com.mtbs.show;

import com.mtbs.catalog.CatalogService;
import com.mtbs.catalog.dto.CatalogDtos.CreateCityRequest;
import com.mtbs.catalog.dto.CatalogDtos.CreateScreenRequest;
import com.mtbs.catalog.dto.CatalogDtos.CreateTheaterRequest;
import com.mtbs.catalog.dto.CatalogDtos.LayoutRequest;
import com.mtbs.catalog.dto.CatalogDtos.MovieRequest;
import com.mtbs.show.dto.ShowDtos.ScheduleShowRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The weekend surcharge multiplier is admin-editable at runtime (not a fixed config value) — an
 * update takes effect on the very next show scheduled, without a restart.
 */
@SpringBootTest
@Transactional
class PricingConfigTest {

  @Autowired
  private PricingConfigService pricingConfigService;
  @Autowired
  private CatalogService catalogService;
  @Autowired
  private ShowService showService;
  @Autowired
  private ShowSeatRepository showSeatRepository;

  @Test
  void updatingTheWeekendMultiplierAffectsTheNextScheduledShow() {
    assertThat(pricingConfigService.get().weekendMultiplier()).isEqualByComparingTo("1.25");

    pricingConfigService.updateWeekendMultiplier(new BigDecimal("1.50"));
    assertThat(pricingConfigService.get().weekendMultiplier()).isEqualByComparingTo("1.50");

    var city = catalogService.createCity(new CreateCityRequest("PricingCity", "PC"));
    var theater = catalogService.createTheater(
        new CreateTheaterRequest(city.id(), "PricingPlex", "St"));
    var screen = catalogService.createScreen(new CreateScreenRequest(theater.id(), "S1"));
    catalogService.defineLayout(screen.id(), new LayoutRequest(1, 1, List.of()));
    var movie = catalogService.createMovie(new MovieRequest("PricingMovie", 120, "English", "UA"));

    // 2026-08-01 is a Saturday -> regular 200 * 1.50 = 300.00 under the updated multiplier.
    var show = showService.scheduleShow(new ScheduleShowRequest(
        movie.id(), screen.id(), LocalDateTime.of(2026, 8, 1, 20, 0),
        new BigDecimal("200"), new BigDecimal("400")));

    var seat = showSeatRepository.findByShowIdOrderByIdAsc(show.id()).get(0);
    assertThat(seat.getPrice()).isEqualByComparingTo("300.00");
  }
}
