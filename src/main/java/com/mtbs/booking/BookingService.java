package com.mtbs.booking;

import com.mtbs.auth.UserRepository;
import com.mtbs.auth.domain.User;
import com.mtbs.booking.domain.Booking;
import com.mtbs.booking.domain.BookingStatus;
import com.mtbs.booking.dto.BookingDtos.BookingResponse;
import com.mtbs.booking.dto.CancellationResponse;
import com.mtbs.booking.event.BookingCancelledEvent;
import com.mtbs.booking.event.BookingConfirmedEvent;
import com.mtbs.common.error.ResourceNotFoundException;
import com.mtbs.discount.DiscountService;
import com.mtbs.discount.DiscountService.DiscountApplication;
import com.mtbs.payment.PaymentService;
import com.mtbs.payment.PaymentService.ChargeResult;
import com.mtbs.payment.PaymentService.RefundResult;
import com.mtbs.refund.RefundPolicyResolver;
import com.mtbs.refund.RefundRepository;
import com.mtbs.refund.domain.Refund;
import com.mtbs.show.ShowSeatRepository;
import com.mtbs.show.domain.ShowSeat;
import com.mtbs.show.domain.ShowSeatStatus;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * The whole booking lifecycle in one place: hold seats, pay to confirm, cancel with a refund, read
 * a customer's history, and sweep lapsed holds. All money movement is delegated to the payment
 * module's {@link PaymentService}, so this service depends on payment and payment never depends on
 * booking.
 *
 * <p>The concurrency guarantee lives here: the AVAILABLE→HELD (and HELD→BOOKED) transitions run
 * under a pessimistic write lock on the target {@link ShowSeat} rows, so concurrent attempts on the
 * same seat serialize — exactly one wins.
 */
@Service
public class BookingService {

  private static final Logger log = LoggerFactory.getLogger(BookingService.class);

  private final ShowSeatRepository showSeatRepository;
  private final BookingRepository bookingRepository;
  private final UserRepository userRepository;
  private final DiscountService discountService;
  private final PaymentService paymentService;
  private final RefundRepository refundRepository;
  private final RefundPolicyResolver refundPolicyResolver;
  private final ApplicationEventPublisher eventPublisher;
  private final long ttlSeconds;

  public BookingService(
      ShowSeatRepository showSeatRepository,
      BookingRepository bookingRepository,
      UserRepository userRepository,
      DiscountService discountService,
      PaymentService paymentService,
      RefundRepository refundRepository,
      RefundPolicyResolver refundPolicyResolver,
      ApplicationEventPublisher eventPublisher,
      @Value("${app.hold.ttl-seconds:300}") long ttlSeconds) {
    this.showSeatRepository = showSeatRepository;
    this.bookingRepository = bookingRepository;
    this.userRepository = userRepository;
    this.discountService = discountService;
    this.paymentService = paymentService;
    this.refundRepository = refundRepository;
    this.refundPolicyResolver = refundPolicyResolver;
    this.eventPublisher = eventPublisher;
    this.ttlSeconds = ttlSeconds;
  }

  // --- Hold -----------------------------------------------------------------

  @Transactional
  public BookingResponse hold(String userEmail, Long showId, List<Long> showSeatIds) {
    return hold(userEmail, showId, showSeatIds, null);
  }

  @Transactional
  public BookingResponse hold(String userEmail, Long showId, List<Long> showSeatIds, String discountCode) {
    User user = userRepository.findByEmail(userEmail)
        .orElseThrow(() -> new ResourceNotFoundException("User " + userEmail + " not found"));

    // Deterministic ordering for lock acquisition (defence-in-depth; the query also orders).
    List<Long> orderedIds = showSeatIds.stream().distinct().sorted().toList();
    List<ShowSeat> seats = showSeatRepository.lockByIds(orderedIds);

    if (seats.size() != orderedIds.size()) {
      throw new ResourceNotFoundException("One or more seats do not exist");
    }

    Instant now = Instant.now();
    for (ShowSeat seat : seats) {
      if (!seat.getShow().getId().equals(showId)) {
        throw new SeatUnavailableException("Seat " + seat.getId() + " is not part of show " + showId);
      }
      boolean bookable = seat.getStatus() == ShowSeatStatus.AVAILABLE || seat.isHoldExpired(now);
      if (!bookable) {
        throw new SeatUnavailableException("Seat " + seat.getSeat().getLabel() + " is not available");
      }
    }

    Instant holdUntil = now.plusSeconds(ttlSeconds);
    BigDecimal subtotal = BigDecimal.ZERO;
    for (ShowSeat seat : seats) {
      seat.hold(holdUntil);
      subtotal = subtotal.add(seat.getPrice());
    }
    showSeatRepository.saveAll(seats);

    Booking booking = new Booking(user, seats.get(0).getShow(), seats, subtotal);
    if (StringUtils.hasText(discountCode)) {
      DiscountApplication applied = discountService.evaluate(discountCode, subtotal);
      booking.applyDiscount(applied.code(), applied.discountAmount(), applied.total());
    }
    return BookingMapper.toBooking(bookingRepository.save(booking));
  }

  // --- Pay ------------------------------------------------------------------

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

    ChargeResult result = paymentService.charge(bookingId, booking.getTotal(), token);

    if (result.success()) {
      seats.forEach(ShowSeat::book);
      showSeatRepository.saveAll(seats);
      booking.markConfirmed();
      if (hasDiscount) {
        discountService.redeem(booking.getDiscount().getId());
      }
      eventPublisher.publishEvent(new BookingConfirmedEvent(
          bookingId,
          booking.getOwner().getEmail(),
          booking.getShow().getMovie().getTitle(),
          booking.getShow().getStartTime()));
    } else {
      booking.markPaymentFailed();
    }
    return BookingMapper.toBooking(booking);
  }

  // --- Cancel ---------------------------------------------------------------

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

    RefundResult outcome = paymentService.refund(bookingId, refundAmount, "booking-" + bookingId);
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
    eventPublisher.publishEvent(new BookingCancelledEvent(
        bookingId,
        booking.getOwner().getEmail(),
        booking.getShow().getMovie().getTitle(),
        refundAmount));
    return new CancellationResponse(bookingId, booking.getStatus().name(), refundAmount, percent);
  }

  // --- Read -----------------------------------------------------------------

  @Transactional(readOnly = true)
  public List<BookingResponse> myBookings(String userEmail) {
    return bookingRepository.findByOwnerEmailOrderByIdDesc(userEmail).stream()
        .map(BookingMapper::toBooking)
        .toList();
  }

  // --- Sweeper --------------------------------------------------------------

  /**
   * Periodically releases lapsed holds so freed inventory shows up in listings promptly.
   * Correctness never depends on this — a booking attempt reclaims an expired hold lazily under the
   * lock — but it keeps the data presentable between collisions.
   */
  @Scheduled(fixedDelayString = "${app.hold.sweeper-interval-ms:30000}")
  @Transactional
  public void releaseExpiredHolds() {
    List<ShowSeat> expired =
        showSeatRepository.findByStatusAndHeldUntilBefore(ShowSeatStatus.HELD, Instant.now());
    if (expired.isEmpty()) {
      return;
    }
    expired.forEach(ShowSeat::release);
    showSeatRepository.saveAll(expired);
    log.debug("Released {} expired seat hold(s)", expired.size());
  }
}
