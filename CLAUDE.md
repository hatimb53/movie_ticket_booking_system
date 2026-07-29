
# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this repo is

Assignment: a Movie Ticket Booking System REST API. Full requirements are in
`docs/Movie Ticket Booking System.pdf`. Scaffolded as a Maven + Spring Boot 3.3.4 project
(Java 17, base package `com.mtbs`), with H2 as the default dev/test datasource — swap to a real
DB before calling persistence "done" if that matters for the submission. Build/test:
`mvn package` / `mvn test` (requires `JAVA_HOME` pointed at a JDK 17, not the system default).

## Assignment constraints (do not violate)

- **Stack is fixed: Spring Boot.** Do not substitute another framework.
- **In scope**: REST APIs for the core flows, persistence to a DB of choice, basic role-based
  access control (`admin`, `customer`), input validation/error handling, unit + integration tests
  for the core flows.
- **Out of scope — do not spend time on these**: frontend/UI, deployment/containerization/CI-CD,
  distributed systems/microservices, OAuth/SSO/MFA, production-grade observability/monitoring.
- Domain: multiple cities → theaters → shows → seat-level booking. Time-bound seat holds that
  auto-release on expiry. Pricing tiers (regular/premium/weekend) + discount codes. Payment,
  confirmation, cancellation with configurable refund policies. Concurrent booking attempts on
  the same seat must serialize correctly (no double-allocation). Confirmation/reminder
  notifications must not block the booking request.
- Scoping (which entities/APIs/edge cases to include) is intentionally left to the implementer —
  document meaningful assumptions in `README.md` as they're made, not at the end.

## Deliverable requirements

The submission is graded on the repo contents, so these are not optional:
- `README.md` documenting assumptions and design decisions.
- This `CLAUDE.md` file and everything under `.claude/skills/` must be committed as-is — they are
  part of the submission (evidence of the AI workflow used).
- All raw files used during development (specs, tickets, prototypes) should be kept in the repo
  rather than deleted once used.
- Multiple commits during development are expected — don't squash the whole build into one commit.

## Workflow skills available

This repo has `.claude/skills/` populated with Matt Pocock's engineering skill set. Typical flow
for a feature: `/grill-me` or `/grilling` to pressure-test scope decisions → `/to-spec` to turn
the discussion into a spec (PRD) → `/to-tickets` to break the spec into vertical-slice tickets →
`/tdd` / `/implement` to build → `/code-review` before calling something done. Run
`/setup-matt-pocock-skills` once before first use of the engineering skills — it configures
tickets to write to local files under `.scratch/` (there is no external issue tracker for this
assignment) and the domain doc layout. `/domain-modeling` and `/codebase-design` are for pinning
down the booking-domain glossary and module boundaries as they emerge.

## Concurrency

Seat holds and booking confirmation are the one place correctness (not just feature coverage) is
explicitly graded — the concurrent-booking-serialization requirement needs a real mechanism
(DB-level locking/constraints, optimistic locking, or similar), not an assumption that requests
arrive one at a time. Cover this with a concurrent integration test, not just unit tests.

## graphify

This project has a knowledge graph at graphify-out/ with god nodes, community structure, and cross-file relationships.
When the user types `/graphify`, use the installed graphify skill (`.claude/skills/graphify/SKILL.md`) before doing anything else.

Rules:
- For codebase questions, first run `graphify query "<question>"` when graphify-out/graph.json exists. Use `graphify path "<A>" "<B>"` for relationships and `graphify explain "<concept>"` for focused concepts. These return a scoped subgraph, usually much smaller than GRAPH_REPORT.md or raw grep output.
- If graphify-out/wiki/index.md exists, use it for broad navigation instead of raw source browsing.
- Read graphify-out/GRAPH_REPORT.md only for broad architecture review or when query/path/explain do not surface enough context.
- After modifying code, run `graphify update .` to keep the graph current (AST-only, no API cost).
