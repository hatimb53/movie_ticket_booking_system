package com.mtbs.show.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Request/response records for show scheduling and browse. */
public final class ShowDtos {

  private ShowDtos() {
  }

  public record ScheduleShowRequest(
      @NotNull Long movieId,
      @NotNull Long screenId,
      @NotNull @Future @JsonFormat(shape = JsonFormat.Shape.STRING) LocalDateTime startTime,
      @NotNull @Positive BigDecimal regularPrice,
      @NotNull @Positive BigDecimal premiumPrice) {
  }

  public record ShowResponse(
      Long id,
      Long movieId,
      String movieTitle,
      Long screenId,
      String theaterName,
      String cityName,
      LocalDateTime startTime,
      BigDecimal regularPrice,
      BigDecimal premiumPrice) {
  }

  public record SeatMapEntry(
      Long showSeatId, String seatLabel, String category, BigDecimal price, String status) {
  }

  public record SeatMapResponse(
      Long showId, String movieTitle, LocalDateTime startTime, List<SeatMapEntry> seats) {
  }

  public record PageResponse<T>(
      List<T> content, int page, int size, long totalElements, int totalPages) {
  }
}
