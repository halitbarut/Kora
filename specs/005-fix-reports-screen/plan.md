# Implementation Plan: Fix Reports Screen UI

**Branch**: `004-refactor-reports-entry-point` | **Date**: 2025-10-31 | **Spec**: `specs/005-fix-reports-screen/spec.md`
**Input**: Bug fix specification from `/specs/005-fix-reports-screen/spec.md`

**Note**: This plan follows the updated constitution guardrails for Compose + Clean Architecture. No new backend or data layer work is required.

## Summary

Polish the Reports screen so it matches the refactored navigation flow and renders correctly across locales. We will remove any bottom navigation scaffolding, normalize the earnings bar chart styling and labels, format monetary values with the user’s configured currency symbol, and harden the summary card layout so German translations wrap without breaking alignment.

## Technical Context

**Language/Version**: Kotlin (K2) targeting JVM 17  
**Primary Dependencies**: Jetpack Compose Material 3, Hilt, Kotlin Coroutines & Flow, Navigation Compose  
**Storage**: Room database (unchanged)  
**Testing**: JUnit, kotlinx-coroutines-test, Compose UI tests  
**Target Platform**: Android (Compose-only UI)  
**Project Type**: Android mobile app using Clean Architecture  
**Performance Goals**: Maintain 60fps rendering; charts should recompute efficiently with memoization  
**Constraints**: UI must remain Material 3; no XML layouts; localization-first strings; immutable data models  
**Scale/Scope**: Single screen UI polish with localized formatting

## Constitution Check

- All code lives in Compose UI layer; no business logic bypass.  
- Strings and formatting rely on `strings.xml` and locale-aware formatters.  
- Navigation remains via `KoraDestination.Reports`; bottom navigation removal stays within UI.  
- No persistence/Room or DI changes required.  
- Compose Material 3 components only; theming consistent with existing palette.  
- Currency symbols sourced via user preferences (Clean Architecture compliance).

## Project Structure

### Documentation (this feature)

```text
specs/005-fix-reports-screen/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
└── tasks.md (to be generated later)
```

### Source Code (repository root)

```text
app/
├── src/main/java/com/barutdev/kora/ui/screens/reports/ReportsScreen.kt
├── src/main/java/com/barutdev/kora/ui/screens/reports/components/ (if new helpers needed)
├── src/main/res/values*/strings.xml
└── src/androidTest/java/com/barutdev/kora/ui/reports/...
```

**Structure Decision**: Patch `ReportsScreen.kt` directly; add helper composables or extensions under `ui/screens/reports` if needed for layout reuse. Update string resources across locales.

## Phase Plan

- **Phase 0 – Research**  
  - Confirm best practice for localized currency formatting with custom symbols using `NumberFormat` and user preferences.  
  - Evaluate Compose techniques for responsive text (e.g., `FlowRow`, `Modifier.padding`, `TextAlign`, `minLines`, `wrapContentWidth`) to handle long strings.

- **Phase 1 – Design Adjustments**  
  - Remove any bottom bar usage and ensure scaffold relies solely on `ScreenScaffoldConfig`.  
  - Refactor summary cards to use flexible layouts (e.g., `Column` with `Arrangement.spacedBy`, `maxLines`, or `Modifier.fillMaxWidth()`), and consider `FlowRow` if icons or additional stats emerge.  
  - Standardize bar chart styling with shared color/shape parameters and ensure month labels derive from `YearMonth` via locale abbreviations (e.g., `DateTimeFormatter.ofPattern("LLL")`).  
  - Format Total Earnings using the selected currency (pull from `LocalUserPreferences` or formatter extension) and update previews/tests.

- **Phase 2 – Validation Prep**  
  - Compose UI tests verifying absence of bottom nav semantics, correct currency symbol rendering, and layout resiliency with German locale (instrumentation or unit tests using `CompositionLocalProvider`).  
  - Manual QA checklist in quickstart for English/German locales.

## Implementation Details by Concern

- **Bottom Navigation Removal**
  - Ensure `ReportsScreen` no longer sets a `bottomBar` in any Scaffold. Rely on `ScreenScaffoldConfig` only.

- **Bar Chart Consistency**
  - Introduce shared dimensions and color constants for chart bars. Remove any conditional styling based on current month.
  - Guarantee month labels appear under each bar, using uppercase locale-aware abbreviations and consistent text style.

- **Currency Formatting**
  - Inject `LocalUserPreferences` to obtain `currencyCode`/`currencySymbol`.  
  - Create helper `formatCurrency(amount: BigDecimal, locale: Locale, currencyCode: String)` to enforce consistent formatting.

- **Localization-Resilient Summary Cards**
  - Replace fixed-height rows with wrap-content columns.  
  - Apply `Modifier.fillMaxWidth()` + `Arrangement.spacedBy` + `Text` with `maxLines = 2` and `overflow = TextOverflow.Ellipsis` or allow wrap with `softWrap = true`.  
  - Consider `ResponsiveThreeColumnLayout` using `FlowRow` (Material3 `FlowRow`) for small screens, but keep minimal to avoid over-engineering.

- **Testing**
  - New Compose test to assert `BottomNavigation` node absent and `Text` contains currency symbol.  
  - Locale-specific preview/test ensuring summary card text wraps instead of clipping.
