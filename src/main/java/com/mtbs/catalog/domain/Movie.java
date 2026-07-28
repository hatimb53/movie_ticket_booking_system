package com.mtbs.catalog.domain;

import com.mtbs.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "movies")
public class Movie extends BaseEntity {

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private int durationMinutes;

  @Column(nullable = false)
  private String language;

  @Column(nullable = false)
  private String rating;

  protected Movie() {
  }

  public Movie(String title, int durationMinutes, String language, String rating) {
    this.title = title;
    this.durationMinutes = durationMinutes;
    this.language = language;
    this.rating = rating;
  }

  public String getTitle() {
    return title;
  }

  public int getDurationMinutes() {
    return durationMinutes;
  }

  public String getLanguage() {
    return language;
  }

  public String getRating() {
    return rating;
  }

  public void update(String title, int durationMinutes, String language, String rating) {
    this.title = title;
    this.durationMinutes = durationMinutes;
    this.language = language;
    this.rating = rating;
  }
}
