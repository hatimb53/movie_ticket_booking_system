# 01 — Foundation & error contract

**What to build:** The shared conventions every later slice reuses, proven by a working error
response. A client hitting an endpoint with bad input or a not-found resource gets a consistent,
structured JSON error with the correct HTTP status. The app boots with a seed-data harness in
place (even if it seeds nothing yet).

**Blocked by:** None — can start immediately.

**Status:** done

- [x] Base auditing `@MappedSuperclass` (`id`, `createdAt`, `updatedAt`) available for entities to extend.
- [x] Global `@RestControllerAdvice` producing the error body `{timestamp, status, error, message, path}`.
- [x] Validation failures (`@Valid`) map to 400 with field-level messages; not-found → 404; generic fallback → 500.
- [x] DTO-at-boundary + manual-mapper convention established (entities never serialized directly).
- [x] `CommandLineRunner` seed harness wired (no-op or minimal), toggleable via config/profile.
- [x] An error-contract test asserts the JSON shape and status codes on a representative endpoint.
- [x] `mvn test` green; existing `contextLoads` still passes.

**Note:** Added a permissive `SecurityFilterChain` as a prefactor (starter-security otherwise
locks every endpoint behind a 401); ticket 02 replaces it with JWT + role rules. `@EnableJpaAuditing`
wired so `BaseEntity` timestamps populate.
