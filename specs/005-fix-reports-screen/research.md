# Research: Fix Reports Screen UI

## Decision: Currency formatting uses user preferences + locale
- **Rationale**: Tutors may set a currency different from device locale. Using `NumberFormat.getCurrencyInstance(locale)` with an explicit `Currency.getInstance(userCurrencyCode)` ensures both symbol and grouping follow expectations. Injecting `LocalUserPreferences` keeps Clean Architecture boundaries.
- **Alternatives considered**:
  - Hardcoding symbols from currency code map — rejected (duplication, localization risk).
  - Formatting solely via locale without overriding currency — rejected (fails when locale ≠ selected currency).

## Decision: Responsive summary cards via flexible Column layout
- **Rationale**: Allow `Text` components to wrap by using `Modifier.fillMaxWidth()` with `softWrap = true`, `maxLines = 2`, and `Arrangement.spacedBy`. Optionally add `FlowRow` for metrics if content grows. Prevents German strings from overflowing.
- **Alternatives considered**:
  - Fixed height with `TextScale` — rejected (still prone to clipping on extreme translations).
  - Truncation with ellipsis — rejected (loses information).

## Decision: Unified bar chart styling with shared constants
- **Rationale**: Define consistent width, color, and corner radius via `remember`ed parameters. Remove per-bar conditionals. Month labels derived from `YearMonth` using `DateTimeFormatter.ofPattern("LLL")` to honor locale.
- **Alternatives considered**:
  - Material chart libraries — rejected (extra dependencies for small tweak).
  - Hardcoded month abbreviations — rejected (breaks localization).
