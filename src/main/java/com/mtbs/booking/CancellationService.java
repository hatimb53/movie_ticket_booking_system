package com.mtbs.booking;

import com.mtbs.booking.domain.Booking;
import com.mtbs.booking.domain.BookingStatus;
import com.mtbs.booking.dto.CancellationResponse;
import com.mtbs.common.error.ResourceNotFoundException;
import com.mtbs.discount.DiscountService;
import com.mtbs.payment.PaymentGateway;
import com.mtbs.payment.PaymentGateway.RefundOutcome;
import com.mtbs.payment.PaymentGateway.RefundRequest;
import com.mtbs.refund.RefundPolicyResolver;
import com.mtbs.refund.RefundRepository;
import com.mtbs.refund.domain.Refund;
import com.mtbs.show.ShowSeatRepository;
import com.mtbs.show.domain.ShowSeat;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cancels a confirmed booking before showtime: computes the refund from the applicable time-tiered
 * policy, issues it via the (mock) gateway, releases the seats, rolls back any discount redemption,
 * and marks the booking cancelled.
 */
@Service
public class CancellationService {

  private final BookingRepository bookingRepository;
  private final ShowSeatRepository showSeatRepository;
  private final RefundRepository refundRepository;
  private final RefundPolicyResolver refundPolicyResolver;
  private final DiscountService discountService;
  private final PaymentGateway paymentGateway;

  public CancellationService(
      BookingRepository bookingRepository,
      ShowSeatRepository showSeatRepository,
      RefundRepository refundRepository,
      RefundPolicyResolver refundPolicyResolver,
      DiscountService discountService,
      PaymentGateway paymentGateway) {
    this.bookingRepository = bookingRepository;
    this.showSeatRepository = showSeatRepository;
    this.refundRepository = refundRepository;
    this.refundPolicyResolver = refundPolicyResolver;
    this.discountService = discountService;
    this.paymentGateway = paymentGateway;
  }

  @Transactional
  public CancellationResponse cancel(String userEmail, Long bookingId) {
    Booking booking = bookingRepository.findById(bookingId)
        .orElseThrow(() -> new ResourceNotFoundException("Booking " + bookingId + " not found"));
    if (!booking.getOwner().getEmail().equals(userEmail)) {
      throw new AccessDeniedException("Booking does not belong to the caller");
    }
    if (booking.getStatus() != BookingStatus.CONFIRMED) {
      throw new InvalidBookingStateException(
          "Only confirmed bookings can be cancelled (was " + booking.getStatus() + ")");
    }

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime showStart = booking.getShow().getStartTime();
    if (!now.isBefore(showStart)) {
      throw new InvalidBookingStateException("Cannot cancel after the show has started");
    }

    Long theaterId = booking.getShow().getScreen().getTheater().getId();
    BigDecimal percent = refundPolicyResolver.resolvePercent(theaterId, showStart, now);
    BigDecimal refundAmount = booking.getTotal()
        .multiply(percent)
        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

    RefundOutcome outcome = paymentGateway.refund(
        new RefundRequest(refundAmount, "booking-" + bookingId));
    refundRepository.save(new Refund(bookingId, refundAmount, percent, outcome.reference()));

    // Release the seats under the same pessimistic lock used for holds.
    List<Long> seatIds = booking.getSeats().stream().map(ShowSeat::getId).sorted().toList();
    List<ShowSeat> seats = showSeatRepository.lockByIds(seatIds);
    seats.forEach(ShowSeat::release);
    showSeatRepository.saveAll(seats);

    if (booking.getDiscount() != null) {
      discountService.release(booking.getDiscount().getId());
    }

    booking.markCancelled();
    return new CancellationResponse(bookingId, booking.getStatus().name(), refundAmount, percent);
  }
}
