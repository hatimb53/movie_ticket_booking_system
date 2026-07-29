# Graph Report - .  (2026-07-28)

## Corpus Check
- cluster-only mode — file stats not available

## Summary
- 890 nodes · 2226 edges · 40 communities (38 shown, 2 thin omitted)
- Extraction: 90% EXTRACTED · 10% INFERRED · 0% AMBIGUOUS · INFERRED: 218 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `c4246d08`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- DataSeeder.java
- DiscountCode
- UserRepository
- ShowSeat
- NotificationType
- RefundPolicy
- SecurityConfig.java
- .build
- CancellationTest
- PaymentFlowTest
- AuthFlowTest
- BookingResponse
- PaymentService.java
- Booking
- ShowService
- AdminCatalogTest
- SeatCategory
- ErrorContractTest.java
- DiscountBookingTest
- Show
- CancellationService.java
- BaseEntity
- BookingController.java
- PaymentGateway
- BookingRepository
- Screen
- Movie
- Theater
- Seat
- AdminShowController.java
- JpaRepository
- AsyncConfig.java
- OpenApiConfig.java
- BookingStatus
- MovieTicketBookingSystemApplicationTests.java
- hitl-loop.template.sh
- PersistenceConfig.java
- SchedulingConfig.java
- MovieTicketBookingSystemApplication
- com.mtbs:movie-ticket-booking-system

## God Nodes (most connected - your core abstractions)
1. `DiscountCode` - 39 edges
2. `ShowSeat` - 39 edges
3. `BaseEntity` - 34 edges
4. `ShowSeatRepository` - 34 edges
5. `UserRepository` - 33 edges
6. `Booking` - 33 edges
7. `CatalogService` - 31 edges
8. `User` - 28 edges
9. `ShowService` - 25 edges
10. `Show` - 25 edges

## Surprising Connections (you probably didn't know these)
- `AuthService` --references--> `JwtService`  [EXTRACTED]
  src/main/java/com/mtbs/auth/AuthService.java → src/main/java/com/mtbs/auth/JwtService.java
- `HoldService` --references--> `UserRepository`  [EXTRACTED]
  src/main/java/com/mtbs/booking/HoldService.java → src/main/java/com/mtbs/auth/UserRepository.java
- `DataSeeder` --references--> `UserRepository`  [EXTRACTED]
  src/main/java/com/mtbs/common/seed/DataSeeder.java → src/main/java/com/mtbs/auth/UserRepository.java
- `CancellationTest` --references--> `UserRepository`  [EXTRACTED]
  src/test/java/com/mtbs/booking/CancellationTest.java → src/main/java/com/mtbs/auth/UserRepository.java
- `HoldConcurrencyTest` --references--> `UserRepository`  [EXTRACTED]
  src/test/java/com/mtbs/booking/HoldConcurrencyTest.java → src/main/java/com/mtbs/auth/UserRepository.java

## Import Cycles
- None detected.

## Communities (40 total, 2 thin omitted)

### Community 0 - "DataSeeder.java"
Cohesion: 0.08
Nodes (43): CommandLineRunner, ConditionalOnProperty, PutMapping, AdminCatalogController, PostMapping, PreAuthorize, RequestMapping, ResponseEntity (+35 more)

### Community 1 - "DiscountCode"
Cohesion: 0.06
Nodes (28): AdminDiscountController, DiscountAdminService, GetMapping, PostMapping, PreAuthorize, RequestMapping, ResponseEntity, RestController (+20 more)

### Community 2 - "UserRepository"
Cohesion: 0.06
Nodes (38): AuthController, PostMapping, RequestMapping, ResponseEntity, RestController, AuthService, PasswordEncoder, Service (+30 more)

### Community 3 - "ShowSeat"
Cohesion: 0.07
Nodes (29): BookingMapper, HoldSweeper, Component, Logger, Scheduled, Transactional, Entity, Table (+21 more)

### Community 4 - "NotificationType"
Cohesion: 0.08
Nodes (25): Async, BookingCancelledEvent, BookingConfirmedEvent, Entity, Table, Notification, NotificationType, BOOKING_CANCELLED (+17 more)

### Community 5 - "RefundPolicy"
Cohesion: 0.09
Nodes (22): Embeddable, AdminRefundPolicyController, GetMapping, PostMapping, PreAuthorize, RequestMapping, ResponseEntity, RestController (+14 more)

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

### Community 11 - "BookingResponse"
Cohesion: 0.15
Nodes (13): BookedSeat, BookingDtos, BookingResponse, HoldRequest, HoldController, PostMapping, PreAuthorize, Principal (+5 more)

### Community 12 - "PaymentService.java"
Cohesion: 0.15
Nodes (11): ApplicationEventPublisher, Service, Transactional, PaymentService, Entity, Table, Payment, PaymentStatus (+3 more)

### Community 13 - "Booking"
Cohesion: 0.23
Nodes (4): AccessDeniedException, Booking, Entity, Table

### Community 14 - "ShowService"
Cohesion: 0.21
Nodes (11): PageResponse, SeatMapResponse, ShowDtos, ShowResponse, GetMapping, RestController, ShowQueryController, Pageable (+3 more)

### Community 15 - "AdminCatalogTest"
Cohesion: 0.20
Nodes (10): AdminCatalogTest, AutoConfigureMockMvc, BeforeEach, MockMvc, MvcResult, ObjectMapper, PasswordEncoder, SpringBootTest (+2 more)

### Community 16 - "SeatCategory"
Cohesion: 0.18
Nodes (7): SeatCategory, PREMIUM, REGULAR, Component, PricingCalculator, Test, PricingCalculatorTest

### Community 17 - "ErrorContractTest.java"
Cohesion: 0.18
Nodes (12): EchoRequest, ErrorContractTest, AutoConfigureMockMvc, GetMapping, Import, MockMvc, PostMapping, RequestMapping (+4 more)

### Community 18 - "DiscountBookingTest"
Cohesion: 0.22
Nodes (10): DiscountBookingTest, AutoConfigureMockMvc, BeforeEach, MockMvc, MvcResult, ObjectMapper, PasswordEncoder, SpringBootTest (+2 more)

### Community 19 - "Show"
Cohesion: 0.21
Nodes (7): Page, Entity, Table, Show, Pageable, Query, ShowRepository

### Community 20 - "CancellationService.java"
Cohesion: 0.18
Nodes (8): CancellationService, ApplicationEventPublisher, Service, Transactional, Entity, Table, Refund, RefundRepository

### Community 21 - "BaseEntity"
Cohesion: 0.18
Nodes (6): EntityListeners, MappedSuperclass, City, Entity, Table, BaseEntity

### Community 22 - "BookingController.java"
Cohesion: 0.23
Nodes (10): BookingController, GetMapping, PostMapping, PreAuthorize, Principal, RequestMapping, ResponseEntity, RestController (+2 more)

### Community 23 - "PaymentGateway"
Cohesion: 0.25
Nodes (8): Component, Override, MockPaymentGateway, ChargeRequest, PaymentGateway, PaymentOutcome, RefundOutcome, RefundRequest

### Community 24 - "BookingRepository"
Cohesion: 0.24
Nodes (5): BookingQueryService, Service, Transactional, BookingRepository, Query

### Community 25 - "Screen"
Cohesion: 0.29
Nodes (4): CatalogMapper, Entity, Table, Screen

### Community 26 - "Movie"
Cohesion: 0.26
Nodes (3): Entity, Table, Movie

### Community 27 - "Theater"
Cohesion: 0.27
Nodes (4): Entity, Table, Theater, TheaterRepository

### Community 28 - "Seat"
Cohesion: 0.29
Nodes (3): Entity, Table, Seat

### Community 29 - "AdminShowController.java"
Cohesion: 0.36
Nodes (6): AdminShowController, PostMapping, PreAuthorize, RequestMapping, ResponseEntity, RestController

### Community 30 - "JpaRepository"
Cohesion: 0.43
Nodes (4): JpaRepository, CityRepository, MovieRepository, ScreenRepository

### Community 31 - "AsyncConfig.java"
Cohesion: 0.53
Nodes (4): EnableAsync, AsyncConfig, Bean, Configuration

### Community 32 - "OpenApiConfig.java"
Cohesion: 0.53
Nodes (4): OpenAPI, Bean, Configuration, OpenApiConfig

### Community 33 - "BookingStatus"
Cohesion: 0.33
Nodes (5): BookingStatus, CANCELLED, CONFIRMED, PAYMENT_FAILED, PENDING_PAYMENT

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

## Knowledge Gaps
- **19 isolated node(s):** `com.mtbs:movie-ticket-booking-system`, `ADMIN`, `CUSTOMER`, `PENDING_PAYMENT`, `CONFIRMED` (+14 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **2 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `UserRepository` connect `UserRepository` to `DataSeeder.java`, `DiscountCode`, `ShowSeat`, `CancellationTest`, `PaymentFlowTest`, `BookingResponse`, `AdminCatalogTest`, `DiscountBookingTest`, `JpaRepository`?**
  _High betweenness centrality (0.147) - this node is a cross-community bridge._
- **Why does `BaseEntity` connect `BaseEntity` to `DiscountCode`, `UserRepository`, `ShowSeat`, `NotificationType`, `RefundPolicy`, `PaymentService.java`, `Booking`, `Show`, `CancellationService.java`, `Screen`, `Movie`, `Theater`, `Seat`?**
  _High betweenness centrality (0.116) - this node is a cross-community bridge._
- **Why does `ResourceNotFoundException` connect `DataSeeder.java` to `DiscountCode`, `.build`, `BookingResponse`, `PaymentService.java`, `Booking`, `ShowService`, `CancellationService.java`?**
  _High betweenness centrality (0.102) - this node is a cross-community bridge._
- **What connects `com.mtbs:movie-ticket-booking-system`, `ADMIN`, `CUSTOMER` to the rest of the system?**
  _19 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `DataSeeder.java` be split into smaller, more focused modules?**
  _Cohesion score 0.0803921568627451 - nodes in this community are weakly interconnected._
- **Should `DiscountCode` be split into smaller, more focused modules?**
  _Cohesion score 0.05570745044429255 - nodes in this community are weakly interconnected._
- **Should `UserRepository` be split into smaller, more focused modules?**
  _Cohesion score 0.058823529411764705 - nodes in this community are weakly interconnected._