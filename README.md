# ArtisanalBrew — Spring Boot port (Phase 1: Domain)

Sibling of `../ArtisanalBrew` (the .NET 10 source of truth). Phase 1 ports
`src/ThisCafeteria.Domain` to pure Java 21 with zero Spring/JPA dependencies.

## Layout

```
pom.xml                          parent (mirrors ThisCafeteria.sln)
artisanalbrew-domain/            mirrors ThisCafeteria.Domain
  src/main/.../domain/entities/  24 classes + 2 records (OrderPricing, StakingOperationIdentity)
  src/main/.../domain/enums/     5 enums with explicit C# int codes
  src/main/.../domain/avatars/   AvatarCatalog/Seed/RobotAvatar/Slot/Item/Kind
  src/test/...                   5 parity test classes
```

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

## Fidelity checks

- `Order.place` starts in `PROCESSING` (quirk preserved); `transitionTo` enforces
  `PENDING→PROCESSING|CANCELLED`, `PROCESSING→COMPLETED|CANCELLED`.
- `AvatarCatalog.normalize` returns a **new** instance, never mutates input.
- `AvatarSeed` lowercases `0x`-hex (EVM) only; Base58 untouched.
- Enum codes match C# ints (`OrderStatus.Pending=0`, `SmartAccountType.SimpleAccount=0`, …).
