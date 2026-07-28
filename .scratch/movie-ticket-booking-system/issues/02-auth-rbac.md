# 02 — Auth & RBAC

**What to build:** A visitor can register as a customer and log in to receive a JWT. Authenticated
requests are authorized by role: customers can reach customer endpoints, admin-only endpoints
reject customers with 403, and missing/invalid/expired tokens yield 401.

**Blocked by:** 01.

**Status:** done

- [x] `User` entity (email, BCrypt password, role ∈ {ADMIN, CUSTOMER}).
- [x] `POST /auth/register` (customer) and `POST /auth/login` returning a signed JWT.
- [x] Stateless Spring Security filter chain that authenticates the JWT and populates roles.
- [x] Method-level `@PreAuthorize` distinguishing ADMIN vs CUSTOMER.
- [x] 401 for missing/invalid/expired token; 403 for authenticated-but-wrong-role.
- [x] Integration tests: register→login happy path, customer→admin endpoint → 403, no-token → 401.
- [~] An admin user is seedable for later slices/demos — `User(ADMIN)` constructor + `Role.ADMIN` exist; actual seed row wired in ticket 10.

**Note:** JWT via jjwt 0.12.6 (HS256, secret + TTL from `app.jwt.*`). 401/403 responses reuse the
`ErrorResponse` shape (security entry-point/denied-handler + an `AccessDeniedException` advice for
method-level denials). `@EnableMethodSecurity` enabled.
