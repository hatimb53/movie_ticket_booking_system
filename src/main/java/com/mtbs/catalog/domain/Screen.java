package com.mtbs.catalog.domain;

import com.mtbs.common.domain.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "screens")
public class Screen extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "theater_id")
  private Theater theater;

  @Column(nullable = false)
  private String name;

  @OneToMany(mappedBy = "screen", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("id ASC")
  private List<Seat> seats = new ArrayList<>();

  protected Screen() {
  }

  public Screen(Theater theater, String name) {
    this.theater = theater;
    this.name = name;
  }

  public Theater getTheater() {
    return theater;
  }

  public String getName() {
    return name;
  }

  public List<Seat> getSeats() {
    return seats;
  }

  /** Replaces the screen's seat layout with a freshly generated set. */
  public void replaceSeats(List<Seat> newSeats) {
    seats.clear();
    seats.addAll(newSeats);
  }
}
