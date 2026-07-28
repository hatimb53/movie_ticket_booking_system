# 02 — Auth & RBAC

**What to build:** A visitor can register as a customer and log in to receive a JWT. Authenticated
requests are authorized by role: customers can reach customer endpoints, admin-only endpoints
reject customers with 403, and missing/invalid/expired tokens yield 401.

**Blocked by:** 01.

**Status:** ready-for-agent

- [ ] `User` entity (email, BCrypt password, role ∈ {ADMIN, CUSTOMER}).
- [ ] `POST /auth/register` (customer) and `POST /auth/login` returning a signed JWT.
- [ ] Stateless Spring Security filter chain that authenticates the JWT and populates roles.
- [ ] Method-level `@PreAuthorize` distinguishing ADMIN vs CUSTOMER.
- [ ] 401 for missing/invalid/expired token; 403 for authenticated-but-wrong-role.
- [ ] Integration tests: register→login happy path, customer→admin endpoint → 403, no-token → 401.
- [ ] An admin user is seedable for later slices/demos.
