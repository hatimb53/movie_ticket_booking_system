package com.mtbs.show.domain;

import com.mtbs.catalog.domain.Seat;
import com.mtbs.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Per-(show × seat) inventory row — the unit of booking. Carries its frozen price and the
 * lifecycle status guarded, under concurrency, by pessimistic locking (ticket 05). The
 * {@code (show, seat)} pair is unique. An optimistic {@code version} guards against lost updates.
 */
@Entity
@Table(name = "show_seats",
    uniqueConstraints = @UniqueConstraint(columnNames = {"show_id", "seat_id"}))
public class ShowSeat extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "show_id")
  private Show show;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "seat_id")
  private Seat seat;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ShowSeatStatus status;

  @Column
  private Instant heldUntil;

  @Column(nullable = false)
  private BigDecimal price;

  @Version
  private Long version;

  protected ShowSeat() {
  }

  public ShowSeat(Show show, Seat seat, BigDecimal price) {
    this.show = show;
    this.seat = seat;
    this.price = price;
    this.status = ShowSeatStatus.AVAILABLE;
  }

  public Show getShow() {
    return show;
  }

  public Seat getSeat() {
    return seat;
  }

  public ShowSeatStatus getStatus() {
    return status;
  }

  public Instant getHeldUntil() {
    return heldUntil;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public void hold(Instant until) {
    this.status = ShowSeatStatus.HELD;
    this.heldUntil = until;
  }

  public void book() {
    this.status = ShowSeatStatus.BOOKED;
    this.heldUntil = null;
  }

  public void release() {
    this.status = ShowSeatStatus.AVAILABLE;
    this.heldUntil = null;
  }

  public boolean isHoldExpired(Instant now) {
    return status == ShowSeatStatus.HELD && heldUntil != null && heldUntil.isBefore(now);
  }
}
