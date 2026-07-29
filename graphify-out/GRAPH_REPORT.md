# Graph Report - movie_ticket_booking_system  (2026-07-29)

## Corpus Check
- 156 files · ~44,337 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 1150 nodes · 2406 edges · 68 communities (53 shown, 15 thin omitted)
- Extraction: 91% EXTRACTED · 9% INFERRED · 0% AMBIGUOUS · INFERRED: 220 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `c4246d08`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- CatalogService
- DiscountCode
- User
- HoldFlowTest.java
- NotificationType
- DataSeeder.java
- SecurityConfig.java
- .build
- CancellationTest
- PaymentFlowTest
- AuthFlowTest
- Issue tracker: GitHub
- Payment
- Booking
- ShowResponse
- AdminCatalogTest
- BaseEntity
- ErrorContractTest.java
- DiscountBookingTest
- Show
- UserRepository
- What You Must Do When Invoked
- Implementation Decisions
- Codebase Design
- During the session
- ShowSeat
- Movie
- Diagnosing Bugs
- Test-Driven Development
- ShowService
- Process
- AsyncConfig.java
- OpenApiConfig.java
- Movie Ticket Booking System
- MovieTicketBookingSystemApplicationTests.java
- hitl-loop.template.sh
- PersistenceConfig.java
- SchedulingConfig.java
- MovieTicketBookingSystemApplication
- com.mtbs:movie-ticket-booking-system
- Ask Matt
- ShowService.java
- graphify reference: extra exports and benchmark
- to-spec/SKILL.md
- CLAUDE.md
- Process
- graphify reference: query, path, explain
- ShowRepository.java
- graphify reference: add a URL and watch a folder
- graphify reference: commit hook and native CLAUDE.md integration
- graphify reference: incremental update and cluster-only
- graphify reference: GitHub clone and cross-repo merge
- graphify reference: transcribe video and audio
- graphify
- extraction-spec.md
- 01-foundation-error-contract.md
- 02-auth-rbac.md
- 03-admin-catalog.md
- 04-show-scheduling-pricing-browse.md
- 05-seat-holds-concurrency-expiry.md
- 06-payment-confirm-booking.md
- 07-discount-codes.md
- 09-async-notifications.md
- 10-readme-seed-h2-caveat.md

## God Nodes (most connected - your core abstractions)
1. `DiscountCode` - 39 edges
2. `ShowSeat` - 36 edges
3. `BaseEntity` - 34 edges
4. `UserRepository` - 33 edges
5. `Booking` - 31 edges
6. `CatalogService` - 31 edges
7. `User` - 28 edges
8. `BookingService` - 25 edges
9. `ShowSeatRepository` - 25 edges
10. `ShowService` - 25 edges

## Surprising Connections (you probably didn't know these)
- `AuthService` --references--> `JwtService`  [EXTRACTED]
  src/main/java/com/mtbs/auth/AuthService.java → src/main/java/com/mtbs/auth/JwtService.java
- `AuthService` --references--> `UserRepository`  [EXTRACTED]
  src/main/java/com/mtbs/auth/AuthService.java → src/main/java/com/mtbs/auth/UserRepository.java
- `UserRepository` --references--> `User`  [EXTRACTED]
  src/main/java/com/mtbs/auth/UserRepository.java → src/main/java/com/mtbs/auth/domain/User.java
- `DataSeeder` --references--> `UserRepository`  [EXTRACTED]
  src/main/java/com/mtbs/common/seed/DataSeeder.java → src/main/java/com/mtbs/auth/UserRepository.java
- `CancellationTest` --references--> `UserRepository`  [EXTRACTED]
  src/test/java/com/mtbs/booking/CancellationTest.java → src/main/java/com/mtbs/auth/UserRepository.java

## Import Cycles
- None detected.

## Communities (68 total, 15 thin omitted)

### Community 0 - "CatalogService"
Cohesion: 0.09
Nodes (35): PutMapping, AdminCatalogController, PostMapping, PreAuthorize, RequestMapping, ResponseEntity, RestController, CatalogQueryController (+27 more)

### Community 1 - "DiscountCode"
Cohesion: 0.06
Nodes (25): AdminDiscountController, DiscountAdminService, GetMapping, PostMapping, PreAuthorize, RequestMapping, ResponseEntity, RestController (+17 more)

### Community 2 - "User"
Cohesion: 0.07
Nodes (29): AuthController, PostMapping, RequestMapping, ResponseEntity, RestController, AuthService, PasswordEncoder, Service (+21 more)

### Community 3 - "HoldFlowTest.java"
Cohesion: 0.18
Nodes (11): HoldFlowTest, AutoConfigureMockMvc, BeforeEach, MockMvc, MvcResult, ObjectMapper, PasswordEncoder, ResultActions (+3 more)

### Community 4 - "NotificationType"
Cohesion: 0.08
Nodes (25): Async, BookingCancelledEvent, BookingConfirmedEvent, Entity, Table, Notification, NotificationType, BOOKING_CANCELLED (+17 more)

### Community 5 - "DataSeeder.java"
Cohesion: 0.07
Nodes (29): CommandLineRunner, ConditionalOnProperty, Embeddable, DataSeeder, Component, Logger, PasswordEncoder, Transactional (+21 more)

### Community 6 - "SecurityConfig.java"
Cohesion: 0.10
Nodes (23): AccessDeniedHandler, AuthenticationEntryPoint, Claims, EnableMethodSecurity, FilterChain, HttpSecurity, OncePerRequestFilter, SecretKey (+15 more)

### Community 7 - ".build"
Cohesion: 0.13
Nodes (14): ExceptionHandler, MethodArgumentNotValidException, RestControllerAdvice, DuplicateEmailException, InvalidCredentialsException, InvalidBookingStateException, SeatUnavailableException, ErrorResponse (+6 more)

### Community 8 - "CancellationTest"
Cohesion: 0.17
Nodes (12): CancellationTest, AutoConfigureMockMvc, BeforeEach, MockHttpServletRequestBuilder, MockMvc, MvcResult, ObjectMapper, PasswordEncoder (+4 more)

### Community 9 - "PaymentFlowTest"
Cohesion: 0.18
Nodes (12): AutoConfigureMockMvc, BeforeEach, MockHttpServletRequestBuilder, MockMvc, MvcResult, ObjectMapper, PasswordEncoder, ResultActions (+4 more)

### Community 10 - "AuthFlowTest"
Cohesion: 0.17
Nodes (13): Authentication, AuthFlowTest, AutoConfigureMockMvc, GetMapping, Import, MockMvc, ObjectMapper, PreAuthorize (+5 more)

### Community 11 - "Issue tracker: GitHub"
Cohesion: 0.06
Nodes (30): Before exploring, read these, Domain Docs, File structure, Flag ADR conflicts, Use the glossary's vocabulary, Conventions, Issue tracker: GitHub, Pull requests as a triage surface (+22 more)

### Community 12 - "Payment"
Cohesion: 0.10
Nodes (20): Entity, Table, Payment, PaymentStatus, FAILED, SUCCESS, Component, Override (+12 more)

### Community 13 - "Booking"
Cohesion: 0.07
Nodes (24): AccessDeniedException, Principal, BookingController, GetMapping, PostMapping, PreAuthorize, ResponseEntity, RestController (+16 more)

### Community 14 - "ShowResponse"
Cohesion: 0.25
Nodes (8): PageResponse, SeatMapEntry, SeatMapResponse, ShowDtos, ShowResponse, GetMapping, RestController, ShowQueryController

### Community 15 - "AdminCatalogTest"
Cohesion: 0.20
Nodes (10): AdminCatalogTest, AutoConfigureMockMvc, BeforeEach, MockMvc, MvcResult, ObjectMapper, PasswordEncoder, SpringBootTest (+2 more)

### Community 16 - "BaseEntity"
Cohesion: 0.05
Nodes (24): EntityListeners, MappedSuperclass, CatalogMapper, CityRepository, City, Entity, Table, Entity (+16 more)

### Community 17 - "ErrorContractTest.java"
Cohesion: 0.18
Nodes (12): EchoRequest, ErrorContractTest, AutoConfigureMockMvc, GetMapping, Import, MockMvc, PostMapping, RequestMapping (+4 more)

### Community 18 - "DiscountBookingTest"
Cohesion: 0.22
Nodes (10): DiscountBookingTest, AutoConfigureMockMvc, BeforeEach, MockMvc, MvcResult, ObjectMapper, PasswordEncoder, SpringBootTest (+2 more)

### Community 19 - "Show"
Cohesion: 0.23
Nodes (4): Entity, Table, Show, Transactional

### Community 20 - "UserRepository"
Cohesion: 0.07
Nodes (29): ApplicationEventPublisher, JpaRepository, UserRepository, BookingRepository, Query, BookingService, Logger, Scheduled (+21 more)

### Community 21 - "What You Must Do When Invoked"
Cohesion: 0.07
Nodes (26): For /graphify add and --watch, For /graphify query, For the commit hook and native CLAUDE.md integration, For --update and --cluster-only, /graphify, Honesty Rules, Interpreter guard for subcommands, Part A - Structural extraction for code files (+18 more)

### Community 22 - "Implementation Decisions"
Cohesion: 0.07
Nodes (26): Admin — Catalog Management, Admin — Pricing, Discounts, Refund Policies, API surface (representative), Auth & RBAC, Authentication & Access Control, Concurrency (centerpiece), Cross-cutting, Cross-cutting (+18 more)

### Community 23 - "Codebase Design"
Cohesion: 0.09
Nodes (21): 1. In-process, 2. Local-substitutable, 3. Remote but owned (Ports & Adapters), 4. True external (Mock), Deepening, Dependency categories, Seam discipline, Testing strategy: replace, don't layer (+13 more)

### Community 24 - "During the session"
Cohesion: 0.09
Nodes (19): ADR Format, Numbering, Optional sections, Template, What qualifies, When to offer an ADR, CONTEXT.md Format, Rules (+11 more)

### Community 25 - "ShowSeat"
Cohesion: 0.20
Nodes (4): Entity, Table, ShowSeat, ShowMapper

### Community 26 - "Movie"
Cohesion: 0.29
Nodes (3): Entity, Table, Movie

### Community 27 - "Diagnosing Bugs"
Cohesion: 0.14
Nodes (13): Completion criterion — a tight loop that goes red, Diagnosing Bugs, Minimise, Non-deterministic bugs, Phase 1 — Build a feedback loop, Phase 2 — Reproduce + minimise, Phase 3 — Hypothesise, Phase 4 — Instrument (+5 more)

### Community 28 - "Test-Driven Development"
Cohesion: 0.15
Nodes (10): Designing for Mockability, When to Mock, Anti-patterns, Rules of the loop, Seams — where tests go, Test-Driven Development, What a good test is, Bad Tests (+2 more)

### Community 29 - "ShowService"
Cohesion: 0.26
Nodes (8): AdminShowController, PostMapping, PreAuthorize, RequestMapping, ResponseEntity, RestController, Service, ShowService

### Community 30 - "Process"
Cohesion: 0.15
Nodes (12): 1. Gather context, 2. Explore the codebase (optional), 3. Draft vertical slices, 4. Quiz the user, 5. Publish the tickets to the configured tracker, Acceptance criteria, Blocked by, <NN> — <Ticket title> (+4 more)

### Community 31 - "AsyncConfig.java"
Cohesion: 0.53
Nodes (4): EnableAsync, AsyncConfig, Bean, Configuration

### Community 32 - "OpenApiConfig.java"
Cohesion: 0.53
Nodes (4): OpenAPI, Bean, Configuration, OpenApiConfig

### Community 33 - "Movie Ticket Booking System"
Cohesion: 0.18
Nodes (10): Assumptions, Build, run, test, Concurrency test & the H2 caveat, Core flow, Design decisions & assumptions, Movie Ticket Booking System, Out of scope (per the brief), Possible extensions (not built) (+2 more)

### Community 34 - "MovieTicketBookingSystemApplicationTests.java"
Cohesion: 0.60
Nodes (3): SpringBootTest, Test, MovieTicketBookingSystemApplicationTests

### Community 35 - "hitl-loop.template.sh"
Cohesion: 0.83
Nodes (3): capture(), hitl-loop.template.sh script, step()

### Community 36 - "PersistenceConfig.java"
Cohesion: 0.83
Nodes (3): EnableJpaAuditing, Configuration, PersistenceConfig

### Community 37 - "SchedulingConfig.java"
Cohesion: 0.83
Nodes (3): EnableScheduling, Configuration, SchedulingConfig

### Community 40 - "Ask Matt"
Cohesion: 0.20
Nodes (9): Ask Matt, Codebase health, Context hygiene, Crossing sessions, On-ramps, Precondition, Standalone, The main flow: idea → ship (+1 more)

### Community 41 - "ShowService.java"
Cohesion: 0.22
Nodes (7): ShowSeatStatus, AVAILABLE, BOOKED, HELD, Lock, Query, Pageable

### Community 42 - "graphify reference: extra exports and benchmark"
Cohesion: 0.22
Nodes (8): graphify reference: extra exports and benchmark, Step 6b - Wiki (only if --wiki flag), Step 7 - Neo4j export (only if --neo4j or --neo4j-push flag), Step 7a - FalkorDB export (only if --falkordb or --falkordb-push flag), Step 7b - SVG export (only if --svg flag), Step 7c - GraphML export (only if --graphml flag), Step 7d - MCP server (only if --mcp flag), Step 8 - Token reduction benchmark (only if total_words > 5000)

### Community 43 - "to-spec/SKILL.md"
Cohesion: 0.22
Nodes (8): Further Notes, Implementation Decisions, Out of Scope, Problem Statement, Process, Solution, Testing Decisions, User Stories

### Community 44 - "CLAUDE.md"
Cohesion: 0.25
Nodes (6): Assignment constraints (do not violate), Concurrency, Deliverable requirements, graphify, What this repo is, Workflow skills available

### Community 45 - "Process"
Cohesion: 0.25
Nodes (7): 1. Pin the fixed point, 2. Identify the spec source, 3. Identify the standards sources, 4. Spawn both sub-agents in parallel, 5. Aggregate, Process, Why two axes

### Community 46 - "graphify reference: query, path, explain"
Cohesion: 0.33
Nodes (5): For /graphify explain, For /graphify path, graphify reference: query, path, explain, Step 0 — Constrained query expansion (REQUIRED before traversal), Step 1 — Traversal

### Community 47 - "ShowRepository.java"
Cohesion: 0.53
Nodes (4): Page, Pageable, Query, ShowRepository

### Community 48 - "graphify reference: add a URL and watch a folder"
Cohesion: 0.50
Nodes (3): For /graphify add, For --watch, graphify reference: add a URL and watch a folder

### Community 49 - "graphify reference: commit hook and native CLAUDE.md integration"
Cohesion: 0.50
Nodes (3): For git commit hook, For native CLAUDE.md integration, graphify reference: commit hook and native CLAUDE.md integration

### Community 50 - "graphify reference: incremental update and cluster-only"
Cohesion: 0.50
Nodes (3): For --cluster-only, For --update (incremental re-extraction), graphify reference: incremental update and cluster-only

## Knowledge Gaps
- **209 isolated node(s):** `com.mtbs:movie-ticket-booking-system`, `ADMIN`, `CUSTOMER`, `PENDING_PAYMENT`, `CONFIRMED` (+204 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **15 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `UserRepository` connect `UserRepository` to `CatalogService`, `User`, `HoldFlowTest.java`, `DataSeeder.java`, `CancellationTest`, `PaymentFlowTest`, `AdminCatalogTest`, `DiscountBookingTest`?**
  _High betweenness centrality (0.118) - this node is a cross-community bridge._
- **Why does `BaseEntity` connect `BaseEntity` to `DiscountCode`, `User`, `NotificationType`, `DataSeeder.java`, `Payment`, `Booking`, `Show`, `UserRepository`, `ShowSeat`, `Movie`?**
  _High betweenness centrality (0.090) - this node is a cross-community bridge._
- **Why does `ResourceNotFoundException` connect `CatalogService` to `DiscountCode`, `.build`, `ShowService.java`, `Booking`, `Show`, `UserRepository`?**
  _High betweenness centrality (0.061) - this node is a cross-community bridge._
- **What connects `com.mtbs:movie-ticket-booking-system`, `ADMIN`, `CUSTOMER` to the rest of the system?**
  _209 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `CatalogService` be split into smaller, more focused modules?**
  _Cohesion score 0.09192982456140351 - nodes in this community are weakly interconnected._
- **Should `DiscountCode` be split into smaller, more focused modules?**
  _Cohesion score 0.057971014492753624 - nodes in this community are weakly interconnected._
- **Should `User` be split into smaller, more focused modules?**
  _Cohesion score 0.06892230576441102 - nodes in this community are weakly interconnected._