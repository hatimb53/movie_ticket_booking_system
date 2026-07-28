package com.mtbs.booking.domain;

import com.mtbs.auth.domain.User;
import com.mtbs.common.domain.BaseEntity;
import com.mtbs.show.domain.Show;
import com.mtbs.show.domain.ShowSeat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * A customer's reservation of specific show seats. Owns the {@code booking_id} FK on the held seats
 * (via {@code @JoinColumn}) so a seat points back to its holding booking without ShowSeat depending
 * on this package.
 */
@Entity
@Table(name = "bookings")
public class Booking extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id")
  private User owner;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "show_id")
  private Show show;

  @OneToMany(fetch = FetchType.LAZY)
  @JoinColumn(name = "booking_id")
  private List<ShowSeat> seats = new ArrayList<>();

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private BookingStatus status;

  @Column(nullable = false)
  private BigDecimal total;

  protected Booking() {
  }

  public Booking(User owner, Show show, List<ShowSeat> seats, BigDecimal total) {
    this.owner = owner;
    this.show = show;
    this.seats = new ArrayList<>(seats);
    this.total = total;
    this.status = BookingStatus.PENDING_PAYMENT;
  }

  public User getOwner() {
    return owner;
  }

  public Show getShow() {
    return show;
  }

  public List<ShowSeat> getSeats() {
    return seats;
  }

  public BookingStatus getStatus() {
    return status;
  }

  public BigDecimal getTotal() {
    return total;
  }

  public void markConfirmed() {
    this.status = BookingStatus.CONFIRMED;
  }

  public void markPaymentFailed() {
    this.status = BookingStatus.PAYMENT_FAILED;
  }

  public void markCancelled() {
    this.status = BookingStatus.CANCELLED;
  }

  public void setTotal(BigDecimal total) {
    this.total = total;
  }
}
