package com.mtbs.catalog;

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
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Admin-only catalog writes. */
@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCatalogController {

  private final CatalogService catalogService;

  public AdminCatalogController(CatalogService catalogService) {
    this.catalogService = catalogService;
  }

  @PostMapping("/cities")
  public ResponseEntity<CityResponse> createCity(@Valid @RequestBody CreateCityRequest request) {
    return created(catalogService.createCity(request));
  }

  @PostMapping("/theaters")
  public ResponseEntity<TheaterResponse> createTheater(
      @Valid @RequestBody CreateTheaterRequest request) {
    return created(catalogService.createTheater(request));
  }

  @PostMapping("/screens")
  public ResponseEntity<ScreenResponse> createScreen(
      @Valid @RequestBody CreateScreenRequest request) {
    return created(catalogService.createScreen(request));
  }

  @PostMapping("/screens/{id}/layout")
  public ResponseEntity<ScreenLayoutResponse> defineLayout(
      @PathVariable Long id, @Valid @RequestBody LayoutRequest request) {
    return ResponseEntity.ok(catalogService.defineLayout(id, request));
  }

  @PostMapping("/movies")
  public ResponseEntity<MovieResponse> createMovie(@Valid @RequestBody MovieRequest request) {
    return created(catalogService.createMovie(request));
  }

  @PutMapping("/movies/{id}")
  public ResponseEntity<MovieResponse> updateMovie(
      @PathVariable Long id, @Valid @RequestBody MovieRequest request) {
    return ResponseEntity.ok(catalogService.updateMovie(id, request));
  }

  private <T> ResponseEntity<T> created(T body) {
    return ResponseEntity.status(HttpStatus.CREATED).body(body);
  }
}
