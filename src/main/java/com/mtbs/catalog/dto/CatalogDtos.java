package com.mtbs.catalog.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/** Request/response records for the admin catalog. Grouped to keep the boundary types together. */
public final class CatalogDtos {

  private CatalogDtos() {
  }

  // --- City ---
  public record CreateCityRequest(@NotBlank String name, @NotBlank String state) {
  }

  public record CityResponse(Long id, String name, String state) {
  }

  // --- Theater ---
  public record CreateTheaterRequest(
      @NotNull Long cityId, @NotBlank String name, @NotBlank String address) {
  }

  public record TheaterResponse(Long id, Long cityId, String name, String address) {
  }

  // --- Screen ---
  public record CreateScreenRequest(@NotNull Long theaterId, @NotBlank String name) {
  }

  public record ScreenResponse(Long id, Long theaterId, String name, int seatCount) {
  }

  // --- Seat layout ---
  public record LayoutRequest(
      @Min(1) int rows,
      @Min(1) int seatsPerRow,
      @NotNull List<@Min(1) Integer> premiumRows) {
  }

  public record SeatResponse(Long id, String label, String rowLabel, int seatNumber, String category) {
  }

  public record ScreenLayoutResponse(Long screenId, String name, List<SeatResponse> seats) {
  }

  // --- Movie ---
  public record MovieRequest(
      @NotBlank String title,
      @Min(1) int durationMinutes,
      @NotBlank String language,
      @NotBlank String rating) {
  }

  public record MovieResponse(
      Long id, String title, int durationMinutes, String language, String rating) {
  }
}
