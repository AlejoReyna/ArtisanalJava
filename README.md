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
  .../application/service/       service ports (+DefaultOrderPricingService impl)
  .../application/service/blockchain/  ports + pure helpers (Keccak/Solana codecs)
  .../application/service/rewards|wallet/  ports + RewardCalculator
  .../application/config/        chain model + registry + fail-closed options
  .../application/validation/    hand-rolled validators (no FluentValidation)
  .../application/common/        SlugGenerator
```

Phase 2b (next): use-case implementations (`ProductService`, `OrderService`,
`CouponService`, `ProfileService`, staking/ledger/loyalty services),
`BlockchainManifestLoader`, `AgenticCommerceReconciliationApplicator`.

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

## Fidelity checks

- `Order.place` starts in `PROCESSING` (quirk preserved); `transitionTo` enforces
  `PENDING→PROCESSING|CANCELLED`, `PROCESSING→COMPLETED|CANCELLED`.
- `AvatarCatalog.normalize` returns a **new** instance, never mutates input.
- `AvatarSeed` lowercases `0x`-hex (EVM) only; Base58 untouched.
- Enum codes match C# ints (`OrderStatus.Pending=0`, `SmartAccountType.SimpleAccount=0`, …).
