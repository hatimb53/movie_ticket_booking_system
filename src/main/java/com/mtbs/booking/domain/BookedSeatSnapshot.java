package com.mtbs.booking.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;

/**
 * Immutable record of a seat as it was at hold time. {@link com.mtbs.show.domain.ShowSeat}'s
 * {@code booking_id} FK can move to a different booking later (a lapsed hold gets reclaimed by
 * another customer), so a booking's own history must not depend on that live association — this
 * snapshot is what {@code GET /bookings} actually reads.
 */
@Embeddable
public class BookedSeatSnapshot {

  @Column(name = "show_seat_id", nullable = false)
  private Long showSeatId;

  @Column(name = "seat_label", nullable = false)
  private String label;

  @Column(name = "seat_price", nullable = false)
  private BigDecimal price;

  protected BookedSeatSnapshot() {
  }

  public BookedSeatSnapshot(Long showSeatId, String label, BigDecimal price) {
    this.showSeatId = showSeatId;
    this.label = label;
    this.price = price;
  }

  public Long getShowSeatId() {
    return showSeatId;
  }

  public String getLabel() {
    return label;
  }

  public BigDecimal getPrice() {
    return price;
  }
}
