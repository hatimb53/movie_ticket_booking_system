package com.mtbs.catalog.domain;

import com.mtbs.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "seats")
public class Seat extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "screen_id")
  private Screen screen;

  @Column(nullable = false)
  private String rowLabel;

  @Column(nullable = false)
  private int seatNumber;

  @Column(nullable = false)
  private String label;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private SeatCategory category;

  protected Seat() {
  }

  public Seat(Screen screen, String rowLabel, int seatNumber, SeatCategory category) {
    this.screen = screen;
    this.rowLabel = rowLabel;
    this.seatNumber = seatNumber;
    this.label = rowLabel + seatNumber;
    this.category = category;
  }

  public Screen getScreen() {
    return screen;
  }

  public String getRowLabel() {
    return rowLabel;
  }

  public int getSeatNumber() {
    return seatNumber;
  }

  public String getLabel() {
    return label;
  }

  public SeatCategory getCategory() {
    return category;
  }
}
