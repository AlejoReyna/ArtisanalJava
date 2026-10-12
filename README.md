# ArtisanalBrew — Spring Boot port (Phase 1: Domain, Phase 2a: Application ports)

Sibling of `../ArtisanalBrew` (the .NET 10 source of truth). Phase 1 ports
`src/ThisCafeteria.Domain` to pure Java 21 with zero Spring/JPA dependencies.
Phase 2a ports the framework-free half of `src/ThisCafeteria.Application`.

## Layout

```
pom.xml                          parent (mirrors ThisCafeteria.sln)
artisanalbrew-domain/            mirrors ThisCafeteria.Domain
  src/main/.../domain/entities/  24 classes + 2 records (OrderPricing, StakingOperationIdentity)
  src/main/.../domain/enums/     5 enums with explicit C# int codes
  src/main/.../domain/avatars/   AvatarCatalog/Seed/RobotAvatar/Slot/Item/Kind
artisanalbrew-application/       mirrors ThisCafeteria.Application (Phase 2a: ports)
  .../application/dto/           20 records/classes (+3 schema records)
  .../application/repository/    13 ports + snapshot + StakingLedgerWriteResult + UnitOfWork
  .../application/service/       service ports (+Default* use-case implementations)
  .../application/service/blockchain/  ports + pure helpers (Keccak/Solana codecs)
  .../application/service/rewards|wallet/  ports + RewardCalculator
  .../application/service/commerce/  reconciliation applicator
  .../application/config/        chain model + registry + manifest loader + fail-closed options
  .../application/validation/    hand-rolled validators (no FluentValidation)
  .../application/common/        SlugGenerator
artisanalbrew-infrastructure/    mirrors ThisCafeteria.Infrastructure persistence (Phase 3)
  src/main/resources/db/migration/V1__baseline.sql        PG-exact squash of 33 EF migrations
  src/main/resources/db/migration-h2/V1__baseline.sql    H2 twin (partial-index WHERE dropped)
  .../persistence/entity/        20 flat JPA rows, EF table/column names preserved
  .../persistence/repo/          Spring Data interfaces (incl. atomic tryConsume UPDATE)
  .../persistence/adapter/       all 13 repository ports + JPA UnitOfWork
```

Phase 2b (next): ~~use-case implementations (`ProductService`, `OrderService`,
`CouponService`, `ProfileService`, staking/ledger/loyalty services),
`BlockchainManifestLoader`, `AgenticCommerceReconciliationApplicator`.~~ DONE —
all `Default*` use-cases, `BlockchainManifestLoader` (Jackson, verified against
the real committed manifests), and the reconciliation applicator live in this
module; 42 application tests green.

## Build / test

```bash
mvn -q -f pom.xml test
```

Requires Java 21+ (`mvn -v` must show a 21+ runtime; Homebrew `maven` + `openjdk@21` is fine).

## C# → Java notes (the point of the exercise)

| .NET | Java here |
|---|---|
| `sealed class` + private ctor + static factory (`Order.Place`, `Coupon.Create`, `StakingLedgerEntry.Create`) | `public class` + `private` implied by factory-only docs; same validation messages |
| `sealed record OrderPricing(...)` | `record OrderPricing(...)` — same value equality |
| `decimal.Round(x,2,AwayFromZero)` | `setScale(2, HALF_UP)` — identical for non-negative money |
| `decimal` money | `BigDecimal`; never `double` |
| `DateTime.UtcNow` / `DateTimeOffset` | `Instant.now()` |
| `Guid.NewGuid()` | `UUID.randomUUID()` |
| `FrozenDictionary` in `AvatarSlot`/`AvatarCatalog` | Unmodifiable `LinkedHashMap` built once |
| C# indexer `avatar[slot]` | `get(slot)` / `set(slot)`; unknown slot throws `IllegalArgumentException` (was `ArgumentOutOfRangeException`) |
| `ulong` FNV-1a + `%` in `AvatarSeed` | `long` (same bit wrap) + `Long.remainderUnsigned` — `%` would be **signed** and diverge for large hashes |
| `ToUpperInvariant()` | `toUpperCase(Locale.ROOT)` |
| `IsHexDigit` via `Uri.IsHexDigit` | Manual `0-9/a-f/A-F` check |
| `[ConcurrencyCheck]` tokens | Plain `int`/`long` fields; become `@Version` in Phase 3 (JPA) |
| `[MaxLength(n)]` | Documented only; becomes `@Column(length=n)` in Phase 3 |

## Phase 2a notes (Application ports)

| .NET | Java here |
|---|---|
| `Task<T>` + `CancellationToken` on every port | Blocking return values, no cancellation token — served from virtual threads in Phase 5 (`spring.threads.virtual.enabled=true`) instead of `async/await` |
| `IReadOnlyCollection/List<T>` | `java.util.List` (immutable copies at boundaries) |
| `DateTime` (UTC) / `DateTimeOffset` | `Instant` / `OffsetDateTime` |
| Nethereum `Sha3Keccack` / `AddressUtil` (EIP-55, calldata selectors) | Bouncy Castle `KeccakDigest(256)` + hand-rolled EIP-55 (`WalletAddressRules`, `StakingCalldataDecoder`) |
| `System.Security.Cryptography.SHA256` (Anchor discriminators) | JDK `MessageDigest("SHA-256")` (`SolanaAnchorEventCodec`) |
| `BinaryPrimitives` little-endian reads | `ByteBuffer.LITTLE_ENDIAN` (`SolanaAccountCodec`) |
| `JsonElement` log array in Anchor codec | `List<String>` — keeps the application layer JSON-free |
| FluentValidation `AbstractValidator<T>` | Hand-rolled static validators throwing `ValidationException` (same rules, same order, e.g. cascade-stop in `ProfileValidators`) |
| C# `init` records with `required` members | Java records with compact-canonical defaults where needed |
| `decimal` APR math (`apr/365/100`) | `BigDecimal` divide by `36500`, scale 18, `HALF_UP` |
| `decimal.Round(usd/rate, 18, ToZero)` (checkout) | `divide(rate, 18, DOWN)` (`DefaultOrderPricingService`) |

## Phase 3 notes (persistence)

| .NET | Java here |
|---|---|
| 33 EF migrations + snapshot | One Flyway `V1__baseline.sql`, machine-generated from the snapshot (same tables/columns/indexes/FKs); `migration-h2` twin drops partial-index `WHERE` (H2 lacks them — NULLs stay distinct in `UNIQUE` on both engines, verified by `MigrationParityTest`) |
| Domain classes as EF entities (private ctor + private setters) | Separate flat JPA rows + explicit `rehydrate` factories on `Order`/`Coupon` (added to domain — the honest Java equivalent) |
| String enums (`HasConversion<string>`) | Explicit `AttributeConverter`s preserving C# member names (`Pending`, `Beans`, …) — `EnumType.STRING` would store `PENDING`/`BEANS` and break existing rows |
| Owned `Avatar` → one `jsonb` column | Same PascalCase JSON object as `VARCHAR` (PG treats both identically; H2-validatable) |
| `ExecuteUpdateAsync` conditional consume | `@Modifying` JPQL `UPDATE … WHERE consumed IS NULL` returning row count + `refresh()` after (JPA bulk updates, unlike EF, never sync the context) |
| `DbUpdateException` catch → detach entry | Catch `PersistenceException`/`DataAccessException` (translation happens at the repo proxy, so both shapes escape) + `clear()` — `detach()` does not remove a failed insert from Hibernate's action queue |
| `IDbContextFactory` per-operation contexts | Stateless singleton adapters (an `EntityManager` is never shared, so the Blazor-circuit rationale disappears) |
| EF change tracking across applicator staging | Explicit `stageJob` write-back of every mutated projection |
| Spring Boot snake-case physical naming | Must pin `PhysicalNamingStrategyStandardImpl` — Boot's strategy rewrites even quoted identifiers, destroying the EF naming parity |

Deferred to later phases: blockchain gateways, sponsorship/solver policies,
`AgenticJobService` impl, ~~Identity behavior (tables are already in V1)~~, Azure/
email/receipt services, workers (their tables are in V1; entities/adapters land
with Phase 8), Blazor → Thymeleaf UI.

## Phase 4 notes (security)

| .NET | Java here |
|---|---|
| `UserManager`/`SignInManager` + PBKDF2 Identity V3 hashes | `JpaIdentityAccountService` + `AspNetIdentityPasswordEncoder`: verifies V3 (`0x01\|prf\|iter\|salt\|subkey`) so existing passwords keep working, issues BCrypt for new ones (V3 has no `{id}` prefix, so a delegating encoder can't route it — hence the hand-rolled probe) |
| 5 failures → 15 min lockout | Reimplemented on the mapped `AccessFailedCount`/`LockoutEnd` columns |
| `Nethereum` `EncodeUTF8AndEcRecover` (personal_sign) | Hand-rolled secp256k1 recovery on Bouncy Castle (`EvmSignatureRecovery`); EVM challenges stay in-memory like `IMemoryCache`, Solana challenges stay persistent |
| BouncyCastle C# `Ed25519Signer` | BouncyCastle Java `Ed25519Signer` — same library, same vectors |
| `sealed` service + `@Transactional`-style interception | Proxied beans must be non-final: `sealed` is free in .NET, `final` breaks CGLIB subclassing |
| `RequireAdmin` policy, cookie + `XSRF-TOKEN` antiforgery, `/admin/login-action` 302s | `SecurityFilterChain` with method-specific matchers (order matters: `/api/orders/me` before `/api/orders/**`), `CookieCsrfTokenRepository` with matching cookie/header names |
| `sealed` everywhere else | Still `final` — only container-proxied beans pay the price |

## Phase 5a notes (web)

| .NET | Java here |
|---|---|
| Controllers + `[Authorize]`/`[ValidateAntiForgeryToken]` attributes | Same routes/statuses; authz centralized in the filter chain, validation errors via `@ControllerAdvice` (same 400 shapes) |
| Enums as int codes on the wire (System.Text.Json default) | `DotNetEnumJacksonModule` (codes both ways); the domain stays Jackson-free |
| `IMemoryCache` EVM challenges, session `WalletAddress` | Same semantics (in-memory map, `HttpSession` attrs); Solana stays persistent |
| `ResponseCache(60)` on transparency | `Cache-Control: max-age=60` |
| `"sensitive"` rate limiter (30/min/IP, 429) | `RateLimitFilter` on the same path prefixes (single-instance; note on Redis for replicas) |
| `AdminAuthController` custom endpoints | Replaced by form login (`/admin/login-action` → 302s); no controller needed |
| `XSRF-TOKEN` readable cookie + `X-CSRF-TOKEN` header | Same names in `CookieCsrfTokenRepository`; `/api/cart/**` ignored like `[IgnoreAntiforgeryToken]` |
| Anonymous API access | 401 via entry points for `/api/**`, `/staking/**`, `/rewards/**`, `/faucet/**`, `/internal/**` (no login redirect until 5b pages) |
| `dotnet run` without a database (`hasDatabase=false`) | Not ported: the web module requires PostgreSQL (documented; H2 twin only for tests) |
| Missing compiler `-parameters` | Added globally — without it Spring cannot bind `@RequestParam`/`@PathVariable` names (caught by a 400 in the first slice run) |
| `@ConditionalOnMissingBean` on scanned `@Service` stubs | Silently drops beans — must live on an explicit `@Configuration` (`UnconfiguredChainServices`) |

## Fidelity checks

- `Order.place` starts in `PROCESSING` (quirk preserved); `transitionTo` enforces
  `PENDING→PROCESSING|CANCELLED`, `PROCESSING→COMPLETED|CANCELLED`.
- `AvatarCatalog.normalize` returns a **new** instance, never mutates input.
- `AvatarSeed` lowercases `0x`-hex (EVM) only; Base58 untouched.
- Enum codes match C# ints (`OrderStatus.Pending=0`, `SmartAccountType.SimpleAccount=0`, …).
