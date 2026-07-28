# 01 — Foundation & error contract

**What to build:** The shared conventions every later slice reuses, proven by a working error
response. A client hitting an endpoint with bad input or a not-found resource gets a consistent,
structured JSON error with the correct HTTP status. The app boots with a seed-data harness in
place (even if it seeds nothing yet).

**Blocked by:** None — can start immediately.

**Status:** ready-for-agent

- [ ] Base auditing `@MappedSuperclass` (`id`, `createdAt`, `updatedAt`) available for entities to extend.
- [ ] Global `@RestControllerAdvice` producing the error body `{timestamp, status, error, message, path}`.
- [ ] Validation failures (`@Valid`) map to 400 with field-level messages; not-found → 404; generic fallback → 500.
- [ ] DTO-at-boundary + manual-mapper convention established (entities never serialized directly).
- [ ] `CommandLineRunner` seed harness wired (no-op or minimal), toggleable via config/profile.
- [ ] An error-contract test asserts the JSON shape and status codes on a representative endpoint.
- [ ] `mvn test` green; existing `contextLoads` still passes.
