package com.mtbs.booking.domain;

import com.mtbs.auth.domain.User;
import com.mtbs.common.domain.BaseEntity;
import com.mtbs.discount.domain.DiscountCode;
import com.mtbs.show.domain.Show;
import com.mtbs.show.domain.ShowSeat;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

  /**
   * Immutable snapshot of the seats as held/booked, independent of {@link #seats}'s live FK (which
   * can move to a different booking once this one's hold lapses and the seat is reclaimed). This
   * is what displays a booking's history correctly regardless of what happens afterward.
   */
  @ElementCollection
  @CollectionTable(name = "booking_seat_snapshots", joinColumns = @JoinColumn(name = "booking_id"))
  @OrderColumn(name = "seat_order")
  private List<BookedSeatSnapshot> bookedSeats = new ArrayList<>();

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private BookingStatus status;

  @Column(nullable = false)
  private BigDecimal subtotal;

  @Column(nullable = false)
  private BigDecimal discountAmount;

  @Column(nullable = false)
  private BigDecimal total;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "discount_code_id")
  private DiscountCode discount;

  protected Booking() {
  }

  public Booking(User owner, Show show, List<ShowSeat> seats, BigDecimal subtotal) {
    this.owner = owner;
    this.show = show;
    this.seats = new ArrayList<>(seats);
    this.bookedSeats = seats.stream()
        .map(s -> new BookedSeatSnapshot(s.getId(), s.getSeat().getLabel(), s.getPrice()))
        .collect(Collectors.toCollection(ArrayList::new));
    this.subtotal = subtotal;
    this.discountAmount = BigDecimal.ZERO;
    this.total = subtotal;
    this.status = BookingStatus.PENDING_PAYMENT;
  }

  /** Applies a discount, lowering the payable total. The code is redeemed only at confirmation. */
  public void applyDiscount(DiscountCode discount, BigDecimal discountAmount, BigDecimal total) {
    this.discount = discount;
    this.discountAmount = discountAmount;
    this.total = total;
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

  public List<BookedSeatSnapshot> getBookedSeats() {
    return bookedSeats;
  }

  public BookingStatus getStatus() {
    return status;
  }

  public BigDecimal getSubtotal() {
    return subtotal;
  }

  public BigDecimal getDiscountAmount() {
    return discountAmount;
  }

  public BigDecimal getTotal() {
    return total;
  }

  public DiscountCode getDiscount() {
    return discount;
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

  public void markExpired() {
    this.status = BookingStatus.EXPIRED;
  }

  public void setTotal(BigDecimal total) {
    this.total = total;
  }
}
