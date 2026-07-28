package com.mtbs.catalog;

import com.mtbs.catalog.dto.CatalogDtos.CityResponse;
import com.mtbs.catalog.dto.CatalogDtos.MovieResponse;
import com.mtbs.catalog.dto.CatalogDtos.TheaterResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/** Catalog reads available to any authenticated user. Richer browse/filtering lands in ticket 04. */
@RestController
public class CatalogQueryController {

  private final CatalogService catalogService;

  public CatalogQueryController(CatalogService catalogService) {
    this.catalogService = catalogService;
  }

  @GetMapping("/cities")
  public List<CityResponse> listCities() {
    return catalogService.listCities();
  }

  @GetMapping("/cities/{id}/theaters")
  public List<TheaterResponse> listTheaters(@PathVariable Long id) {
    return catalogService.listTheatersInCity(id);
  }

  @GetMapping("/movies")
  public List<MovieResponse> listMovies() {
    return catalogService.listMovies();
  }

  @GetMapping("/movies/{id}")
  public MovieResponse getMovie(@PathVariable Long id) {
    return catalogService.getMovie(id);
  }
}
