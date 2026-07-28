# 03 — Admin catalog: venue hierarchy + movies

**What to build:** An admin can build out the full venue tree — create cities, theaters under a
city, and screens under a theater — and define each screen's seat layout by rows × seats-per-row
with designated premium rows, which generates the screen's seats automatically. The admin can also
manage movies. All of it is admin-only.

**Blocked by:** 02.

**Status:** done

- [x] CRUD (create + list + read; update where sensible) for City, Theater (under a city), Screen (under a theater).
- [x] Screen seat-layout endpoint: `{rows, seatsPerRow, premiumRows}` → generates `Seat` rows with labels (e.g. `A1`) and `category ∈ {REGULAR, PREMIUM}`.
- [x] Movie CRUD (title, duration, language, rating) — create/list/read/update.
- [x] All write endpoints admin-only (customer → 403); reads open to any authenticated user.
- [x] Validation on all inputs; consistent error contract (missing parent → 404).
- [x] Integration tests: create-tree happy path, layout categorization, RBAC negative, 404 case.

**Note:** Row labels roll over past Z (AA, AB, …) for large screens. Layout is replaceable
(regenerates seats). Reads live in `CatalogQueryController`; richer browse/filtering is ticket 04.
