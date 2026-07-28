package com.mtbs.catalog;

import com.mtbs.catalog.domain.City;
import com.mtbs.catalog.domain.Movie;
import com.mtbs.catalog.domain.Screen;
import com.mtbs.catalog.domain.Seat;
import com.mtbs.catalog.domain.Theater;
import com.mtbs.catalog.dto.CatalogDtos.CityResponse;
import com.mtbs.catalog.dto.CatalogDtos.MovieResponse;
import com.mtbs.catalog.dto.CatalogDtos.ScreenLayoutResponse;
import com.mtbs.catalog.dto.CatalogDtos.ScreenResponse;
import com.mtbs.catalog.dto.CatalogDtos.SeatResponse;
import com.mtbs.catalog.dto.CatalogDtos.TheaterResponse;

/** Manual entity → DTO mapping (no entities cross the HTTP boundary). */
final class CatalogMapper {

  private CatalogMapper() {
  }

  static CityResponse toCity(City c) {
    return new CityResponse(c.getId(), c.getName(), c.getState());
  }

  static TheaterResponse toTheater(Theater t) {
    return new TheaterResponse(t.getId(), t.getCity().getId(), t.getName(), t.getAddress());
  }

  static ScreenResponse toScreen(Screen s) {
    return new ScreenResponse(s.getId(), s.getTheater().getId(), s.getName(), s.getSeats().size());
  }

  static SeatResponse toSeat(Seat seat) {
    return new SeatResponse(
        seat.getId(), seat.getLabel(), seat.getRowLabel(), seat.getSeatNumber(),
        seat.getCategory().name());
  }

  static ScreenLayoutResponse toLayout(Screen s) {
    return new ScreenLayoutResponse(
        s.getId(), s.getName(), s.getSeats().stream().map(CatalogMapper::toSeat).toList());
  }

  static MovieResponse toMovie(Movie m) {
    return new MovieResponse(
        m.getId(), m.getTitle(), m.getDurationMinutes(), m.getLanguage(), m.getRating());
  }
}
