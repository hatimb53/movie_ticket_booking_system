# 10 — README, demo seed data & H2 caveat

**What to build:** The submission's documentation and demonstrability. A reviewer can read the
README to understand the assumptions and design decisions, run the app against rich seed data, and
understand the H2-vs-Postgres locking caveat. Supports the recorded video.

**Blocked by:** 06 (minimum demoable). Ideally finalized after all feature slices.

**Status:** done

- [x] README documenting assumptions and design decisions (venue model, concurrency mechanism, pricing, refunds, auth, notifications).
- [x] Documented H2-vs-Postgres locking caveat and how the concurrency test proves serialization on H2; Postgres as a config swap.
- [x] Rich idempotent demo seed data (city/theater/screen+layout/movies/shows + admin + customer + WELCOME10 discount + theater & default refund policies) via `DataSeeder` (`app.seed.enabled=true`).
- [x] Build/run/test instructions incl. `JAVA_HOME`→JDK 17 and the seed-enabled run command.
- [x] Stretch/possible-extensions listed as explicitly out-of-scope-unless-time.
- [x] `DataSeederSmokeTest` boots the app with seeding on (isolated DB) and asserts data + idempotency.
