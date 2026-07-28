# 10 — README, demo seed data & H2 caveat

**What to build:** The submission's documentation and demonstrability. A reviewer can read the
README to understand the assumptions and design decisions, run the app against rich seed data, and
understand the H2-vs-Postgres locking caveat. Supports the recorded video.

**Blocked by:** 06 (minimum demoable). Ideally finalized after all feature slices.

**Status:** ready-for-agent

- [ ] README documenting assumptions and design decisions (venue model, concurrency mechanism, pricing, refunds, auth, notifications).
- [ ] Documented H2-vs-Postgres locking caveat and how the concurrency test proves serialization on H2; Postgres as a config swap.
- [ ] Rich demo seed data (cities/theaters/screens/movies/shows + admin + a discount code + refund policy) via the seed harness.
- [ ] Instructions to build/run/test, including the `JAVA_HOME`→JDK 17 note.
- [ ] Stretch items listed as explicitly optional/out-of-scope-unless-time.
