package com.mtbs.common.seed;

import com.mtbs.auth.UserRepository;
import com.mtbs.catalog.MovieRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Boots the app with seeding enabled and verifies the demo data is populated (and idempotent).
 * Uses an isolated in-memory database so the committed seed data can't collide with other tests
 * that create their own {@code admin@mtbs.com} in the shared default database.
 */
@SpringBootTest(properties = {
    "app.seed.enabled=true",
    "spring.datasource.url=jdbc:h2:mem:seedtest;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000"
})
class DataSeederSmokeTest {

  @Autowired
  private UserRepository userRepository;
  @Autowired
  private MovieRepository movieRepository;
  @Autowired
  private DataSeeder dataSeeder;

  @Test
  void seedsDemoDataAndIsIdempotent() {
    assertThat(userRepository.existsByEmail("admin@mtbs.com")).isTrue();
    assertThat(userRepository.existsByEmail("customer@mtbs.com")).isTrue();
    assertThat(movieRepository.count()).isGreaterThanOrEqualTo(2);

    long usersBefore = userRepository.count();
    dataSeeder.run(); // second run should be a no-op
    assertThat(userRepository.count()).isEqualTo(usersBefore);
  }
}
