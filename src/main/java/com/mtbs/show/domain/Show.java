package com.mtbs.show.domain;

import com.mtbs.catalog.domain.Movie;
import com.mtbs.catalog.domain.Screen;
import com.mtbs.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A screening: a movie on a screen at a start time, with per-category base prices frozen at
 * scheduling time so later price changes never affect an already-scheduled show.
 */
@Entity
@Table(name = "shows")
public class Show extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "movie_id")
  private Movie movie;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "screen_id")
  private Screen screen;

  @Column(nullable = false)
  private LocalDateTime startTime;

  @Column(nullable = false)
  private BigDecimal regularPrice;

  @Column(nullable = false)
  private BigDecimal premiumPrice;

  protected Show() {
  }

  public Show(Movie movie, Screen screen, LocalDateTime startTime,
      BigDecimal regularPrice, BigDecimal premiumPrice) {
    this.movie = movie;
    this.screen = screen;
    this.startTime = startTime;
    this.regularPrice = regularPrice;
    this.premiumPrice = premiumPrice;
  }

  public Movie getMovie() {
    return movie;
  }

  public Screen getScreen() {
    return screen;
  }

  public LocalDateTime getStartTime() {
    return startTime;
  }

  public BigDecimal getRegularPrice() {
    return regularPrice;
  }

  public BigDecimal getPremiumPrice() {
    return premiumPrice;
  }
}
