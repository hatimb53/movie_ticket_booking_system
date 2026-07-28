# 03 — Admin catalog: venue hierarchy + movies

**What to build:** An admin can build out the full venue tree — create cities, theaters under a
city, and screens under a theater — and define each screen's seat layout by rows × seats-per-row
with designated premium rows, which generates the screen's seats automatically. The admin can also
manage movies. All of it is admin-only.

**Blocked by:** 02.

**Status:** ready-for-agent

- [ ] CRUD (create + list + read; update where sensible) for City, Theater (under a city), Screen (under a theater).
- [ ] Screen seat-layout endpoint: `{rows, seatsPerRow, premiumRows}` → generates `Seat` rows with labels (e.g. `A1`) and `category ∈ {REGULAR, PREMIUM}`.
- [ ] Movie CRUD (title, duration, language, rating).
- [ ] All write endpoints admin-only (customer → 403); reads follow the browse slice's rules later.
- [ ] Validation on all inputs; consistent error contract from ticket 01.
- [ ] Integration tests covering the create-tree happy path and an RBAC negative case.
