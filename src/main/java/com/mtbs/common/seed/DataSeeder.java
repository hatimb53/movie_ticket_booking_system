package com.mtbs.common.seed;

import com.mtbs.auth.UserRepository;
import com.mtbs.auth.domain.Role;
import com.mtbs.auth.domain.User;
import com.mtbs.catalog.CatalogService;
import com.mtbs.catalog.dto.CatalogDtos.CityResponse;
import com.mtbs.catalog.dto.CatalogDtos.CreateCityRequest;
import com.mtbs.catalog.dto.CatalogDtos.CreateScreenRequest;
import com.mtbs.catalog.dto.CatalogDtos.CreateTheaterRequest;
import com.mtbs.catalog.dto.CatalogDtos.LayoutRequest;
import com.mtbs.catalog.dto.CatalogDtos.MovieResponse;
import com.mtbs.catalog.dto.CatalogDtos.ScreenResponse;
import com.mtbs.catalog.dto.CatalogDtos.TheaterResponse;
import com.mtbs.catalog.dto.CatalogDtos.MovieRequest;
import com.mtbs.discount.DiscountCodeRepository;
import com.mtbs.discount.domain.DiscountCode;
import com.mtbs.discount.domain.DiscountType;
import com.mtbs.refund.RefundPolicyRepository;
import com.mtbs.refund.domain.RefundPolicy;
import com.mtbs.refund.domain.RefundTier;
import com.mtbs.show.ShowService;
import com.mtbs.show.dto.ShowDtos.ScheduleShowRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds demonstrable data on startup when {@code app.seed.enabled=true} (off by default so tests
 * start clean). Idempotent: if the admin already exists, seeding is skipped. Credentials below are
 * for local demo/dev only.
 */
@Component
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
public class DataSeeder implements CommandLineRunner {

  private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final CatalogService catalogService;
  private final ShowService showService;
  private final DiscountCodeRepository discountCodeRepository;
  private final RefundPolicyRepository refundPolicyRepository;

  public DataSeeder(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      CatalogService catalogService,
      ShowService showService,
      DiscountCodeRepository discountCodeRepository,
      RefundPolicyRepository refundPolicyRepository) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.catalogService = catalogService;
    this.showService = showService;
    this.discountCodeRepository = discountCodeRepository;
    this.refundPolicyRepository = refundPolicyRepository;
  }

  @Override
  @Transactional
  public void run(String... args) {
    if (userRepository.existsByEmail("admin@mtbs.com")) {
      log.info("Seed data already present — skipping.");
      return;
    }
    log.info("Seeding demo data...");

    userRepository.save(new User("admin@mtbs.com", passwordEncoder.encode("admin123"), Role.ADMIN));
    userRepository.save(
        new User("customer@mtbs.com", passwordEncoder.encode("customer123"), Role.CUSTOMER));

    CityResponse mumbai = catalogService.createCity(new CreateCityRequest("Mumbai", "Maharashtra"));
    TheaterResponse pvr = catalogService.createTheater(
        new CreateTheaterRequest(mumbai.id(), "PVR Phoenix", "Lower Parel"));
    ScreenResponse screen = catalogService.createScreen(
        new CreateScreenRequest(pvr.id(), "Audi 1"));
    catalogService.defineLayout(screen.id(), new LayoutRequest(8, 12, List.of(1, 2)));

    MovieResponse dune = catalogService.createMovie(
        new MovieRequest("Dune: Part Two", 166, "English", "UA"));
    MovieResponse oppen = catalogService.createMovie(
        new MovieRequest("Oppenheimer", 180, "English", "A"));

    showService.scheduleShow(new ScheduleShowRequest(
        dune.id(), screen.id(), LocalDateTime.now().plusDays(1).withHour(18).withMinute(30),
        new BigDecimal("250"), new BigDecimal("450")));
    showService.scheduleShow(new ScheduleShowRequest(
        oppen.id(), screen.id(), LocalDateTime.now().plusDays(2).withHour(21).withMinute(0),
        new BigDecimal("250"), new BigDecimal("450")));

    discountCodeRepository.save(new DiscountCode(
        "WELCOME10", DiscountType.PERCENTAGE, new BigDecimal("10"), new BigDecimal("150"),
        new BigDecimal("300"), LocalDateTime.now().minusDays(1), LocalDateTime.now().plusMonths(6),
        100, true));

    // System default policy + a theater-specific policy.
    refundPolicyRepository.save(new RefundPolicy(null, List.of(
        new RefundTier(48, new BigDecimal("100")),
        new RefundTier(6, new BigDecimal("50")),
        new RefundTier(0, new BigDecimal("0")))));
    refundPolicyRepository.save(new RefundPolicy(pvr.id(), List.of(
        new RefundTier(24, new BigDecimal("100")),
        new RefundTier(2, new BigDecimal("50")),
        new RefundTier(0, new BigDecimal("0")))));

    log.info("Seed complete: admin@mtbs.com/admin123, customer@mtbs.com/customer123, "
        + "2 shows, discount WELCOME10, refund policies.");
  }
}
