package com.mtbs.booking;

import com.mtbs.booking.domain.Booking;
import com.mtbs.booking.domain.BookingStatus;
import com.mtbs.booking.dto.BookingDtos.BookingResponse;
import com.mtbs.common.error.ResourceNotFoundException;
import com.mtbs.discount.DiscountService;
import com.mtbs.payment.PaymentGateway;
import com.mtbs.payment.PaymentGateway.ChargeRequest;
import com.mtbs.payment.PaymentGateway.PaymentOutcome;
import com.mtbs.payment.PaymentRepository;
import com.mtbs.payment.domain.Payment;
import com.mtbs.payment.domain.PaymentStatus;
import com.mtbs.show.ShowSeatRepository;
import com.mtbs.show.domain.ShowSeat;
import com.mtbs.show.domain.ShowSeatStatus;
import java.time.Instant;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Confirms a held booking by charging the (mock) gateway. On success the seats move HELD→BOOKED and
 * the booking is CONFIRMED; on failure a FAILED payment is recorded and the booking is left
 * PAYMENT_FAILED with its seats still held (they release on hold expiry). The seat transition runs
 * under the same pessimistic lock as holds, so payment cannot race a re-hold of an expired seat.
 */
@Service
public class PaymentService {

  private final BookingRepository bookingRepository;
  private final ShowSeatRepository showSeatRepository;
  private final PaymentRepository paymentRepository;
  private final PaymentGateway paymentGateway;
  private final DiscountService discountService;

  public PaymentService(
      BookingRepository bookingRepository,
      ShowSeatRepository showSeatRepository,
      PaymentRepository paymentRepository,
      PaymentGateway paymentGateway,
      DiscountService discountService) {
    this.bookingRepository = bookingRepository;
    this.showSeatRepository = showSeatRepository;
    this.paymentRepository = paymentRepository;
    this.paymentGateway = paymentGateway;
    this.discountService = discountService;
  }

  @Transactional
  public BookingResponse pay(String userEmail, Long bookingId, String token) {
    Booking booking = bookingRepository.findById(bookingId)
        .orElseThrow(() -> new ResourceNotFoundException("Booking " + bookingId + " not found"));
    if (!booking.getOwner().getEmail().equals(userEmail)) {
      throw new AccessDeniedException("Booking does not belong to the caller");
    }
    if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
      throw new InvalidBookingStateException(
          "Booking " + bookingId + " is " + booking.getStatus() + ", not payable");
    }

    // Lock the seats and confirm the hold is still valid (not expired/swept out from under us).
    List<Long> seatIds = booking.getSeats().stream().map(ShowSeat::getId).sorted().toList();
    List<ShowSeat> seats = showSeatRepository.lockByIds(seatIds);
    Instant now = Instant.now();
    for (ShowSeat seat : seats) {
      if (seat.getStatus() != ShowSeatStatus.HELD || seat.isHoldExpired(now)) {
        throw new SeatUnavailableException(
            "Hold on seat " + seat.getSeat().getLabel() + " has expired");
      }
    }

    // If a discount is attached, fail fast if it's no longer redeemable — before charging.
    boolean hasDiscount = booking.getDiscount() != null;
    if (hasDiscount) {
      discountService.assertRedeemable(booking.getDiscount().getId());
    }

    PaymentOutcome outcome =
        paymentGateway.charge(new ChargeRequest(booking.getTotal(), token));

    if (outcome.success()) {
      seats.forEach(ShowSeat::book);
      showSeatRepository.saveAll(seats);
      booking.markConfirmed();
      if (hasDiscount) {
        discountService.redeem(booking.getDiscount().getId());
      }
      paymentRepository.save(new Payment(
          bookingId, PaymentStatus.SUCCESS, booking.getTotal(), outcome.reference()));
    } else {
      booking.markPaymentFailed();
      paymentRepository.save(new Payment(
          bookingId, PaymentStatus.FAILED, booking.getTotal(), null));
    }
    return BookingMapper.toBooking(booking);
  }
}
