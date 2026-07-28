package com.mtbs.show;

import com.mtbs.show.domain.Show;
import com.mtbs.show.domain.ShowSeat;
import com.mtbs.show.dto.ShowDtos.SeatMapEntry;
import com.mtbs.show.dto.ShowDtos.ShowResponse;

/** Manual entity → DTO mapping for shows and seat maps. */
final class ShowMapper {

  private ShowMapper() {
  }

  static ShowResponse toShow(Show s) {
    return new ShowResponse(
        s.getId(),
        s.getMovie().getId(),
        s.getMovie().getTitle(),
        s.getScreen().getId(),
        s.getScreen().getTheater().getName(),
        s.getScreen().getTheater().getCity().getName(),
        s.getStartTime(),
        s.getRegularPrice(),
        s.getPremiumPrice());
  }

  static SeatMapEntry toSeatMapEntry(ShowSeat ss) {
    return new SeatMapEntry(
        ss.getId(),
        ss.getSeat().getLabel(),
        ss.getSeat().getCategory().name(),
        ss.getPrice(),
        ss.getStatus().name());
  }
}
