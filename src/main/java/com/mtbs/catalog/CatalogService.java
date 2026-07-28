package com.mtbs.catalog;

import com.mtbs.catalog.domain.City;
import com.mtbs.catalog.domain.Movie;
import com.mtbs.catalog.domain.Screen;
import com.mtbs.catalog.domain.Seat;
import com.mtbs.catalog.domain.SeatCategory;
import com.mtbs.catalog.domain.Theater;
import com.mtbs.catalog.dto.CatalogDtos.CityResponse;
import com.mtbs.catalog.dto.CatalogDtos.CreateCityRequest;
import com.mtbs.catalog.dto.CatalogDtos.CreateScreenRequest;
import com.mtbs.catalog.dto.CatalogDtos.CreateTheaterRequest;
import com.mtbs.catalog.dto.CatalogDtos.LayoutRequest;
import com.mtbs.catalog.dto.CatalogDtos.MovieRequest;
import com.mtbs.catalog.dto.CatalogDtos.MovieResponse;
import com.mtbs.catalog.dto.CatalogDtos.ScreenLayoutResponse;
import com.mtbs.catalog.dto.CatalogDtos.ScreenResponse;
import com.mtbs.catalog.dto.CatalogDtos.TheaterResponse;
import com.mtbs.common.error.ResourceNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Admin catalog operations: venue hierarchy, seat-layout generation, and movies. */
@Service
public class CatalogService {

  private final CityRepository cityRepository;
  private final TheaterRepository theaterRepository;
  private final ScreenRepository screenRepository;
  private final MovieRepository movieRepository;

  public CatalogService(
      CityRepository cityRepository,
      TheaterRepository theaterRepository,
      ScreenRepository screenRepository,
      MovieRepository movieRepository) {
    this.cityRepository = cityRepository;
    this.theaterRepository = theaterRepository;
    this.screenRepository = screenRepository;
    this.movieRepository = movieRepository;
  }

  // --- City ---
  @Transactional
  public CityResponse createCity(CreateCityRequest request) {
    City city = cityRepository.save(new City(request.name(), request.state()));
    return CatalogMapper.toCity(city);
  }

  @Transactional(readOnly = true)
  public List<CityResponse> listCities() {
    return cityRepository.findAll().stream().map(CatalogMapper::toCity).toList();
  }

  // --- Theater ---
  @Transactional
  public TheaterResponse createTheater(CreateTheaterRequest request) {
    City city = cityRepository.findById(request.cityId())
        .orElseThrow(() -> new ResourceNotFoundException("City " + request.cityId() + " not found"));
    Theater theater = theaterRepository.save(new Theater(city, request.name(), request.address()));
    return CatalogMapper.toTheater(theater);
  }

  @Transactional(readOnly = true)
  public List<TheaterResponse> listTheatersInCity(Long cityId) {
    if (!cityRepository.existsById(cityId)) {
      throw new ResourceNotFoundException("City " + cityId + " not found");
    }
    return theaterRepository.findByCityId(cityId).stream().map(CatalogMapper::toTheater).toList();
  }

  // --- Screen + layout ---
  @Transactional
  public ScreenResponse createScreen(CreateScreenRequest request) {
    Theater theater = theaterRepository.findById(request.theaterId())
        .orElseThrow(() ->
            new ResourceNotFoundException("Theater " + request.theaterId() + " not found"));
    Screen screen = screenRepository.save(new Screen(theater, request.name()));
    return CatalogMapper.toScreen(screen);
  }

  @Transactional
  public ScreenLayoutResponse defineLayout(Long screenId, LayoutRequest request) {
    Screen screen = screenRepository.findById(screenId)
        .orElseThrow(() -> new ResourceNotFoundException("Screen " + screenId + " not found"));
    Set<Integer> premiumRows = Set.copyOf(request.premiumRows());
    List<Seat> seats = new ArrayList<>();
    for (int r = 1; r <= request.rows(); r++) {
      String rowLabel = rowLabel(r);
      SeatCategory category = premiumRows.contains(r) ? SeatCategory.PREMIUM : SeatCategory.REGULAR;
      for (int n = 1; n <= request.seatsPerRow(); n++) {
        seats.add(new Seat(screen, rowLabel, n, category));
      }
    }
    screen.replaceSeats(seats);
    screenRepository.save(screen);
    return CatalogMapper.toLayout(screen);
  }

  /** 1 → "A", 26 → "Z", 27 → "AA", … so large screens still get unique row labels. */
  private String rowLabel(int rowNumber) {
    StringBuilder sb = new StringBuilder();
    int n = rowNumber;
    while (n > 0) {
      n--;
      sb.insert(0, (char) ('A' + (n % 26)));
      n /= 26;
    }
    return sb.toString();
  }

  // --- Movie ---
  @Transactional
  public MovieResponse createMovie(MovieRequest request) {
    Movie movie = movieRepository.save(new Movie(
        request.title(), request.durationMinutes(), request.language(), request.rating()));
    return CatalogMapper.toMovie(movie);
  }

  @Transactional
  public MovieResponse updateMovie(Long id, MovieRequest request) {
    Movie movie = movieRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Movie " + id + " not found"));
    movie.update(request.title(), request.durationMinutes(), request.language(), request.rating());
    return CatalogMapper.toMovie(movie);
  }

  @Transactional(readOnly = true)
  public List<MovieResponse> listMovies() {
    return movieRepository.findAll().stream().map(CatalogMapper::toMovie).toList();
  }

  @Transactional(readOnly = true)
  public MovieResponse getMovie(Long id) {
    return movieRepository.findById(id)
        .map(CatalogMapper::toMovie)
        .orElseThrow(() -> new ResourceNotFoundException("Movie " + id + " not found"));
  }
}
