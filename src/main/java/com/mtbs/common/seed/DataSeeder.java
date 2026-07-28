package com.mtbs.common.seed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Seed-data harness. Runs on startup only when {@code app.seed.enabled=true} (off by default, so
 * tests get a clean database). Later slices populate demo data (cities, theaters, screens, movies,
 * shows, an admin user, a discount code, a refund policy) here.
 */
@Component
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
public class DataSeeder implements CommandLineRunner {

  private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

  @Override
  public void run(String... args) {
    log.info("Data seeding enabled — no demo data wired yet (populated in later slices).");
  }
}
