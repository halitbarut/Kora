# Feature Specification: Fix Reports Screen UI

**Feature Branch**: `[005-fix-reports-screen]`  
**Created**: 2025-10-31  
**Status**: Draft  
**Input**: User story: "As a tutor, I want the Reports screen to be visually correct, consistent, and fully functional across all supported languages."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Polished cross-locale reports (Priority: P1)

Tutors access the Reports screen from the Student List top bar and expect a consistent experience regardless of language or locale. The screen must render without the bottom navigation bar, display earnings accurately (including currency symbols), show consistent bar charts, and keep summary cards readable in long-text languages like German.

**Why this priority**: Reports provide key business insights; visual or localization glitches erode trust and usability.

**Independent Test**: Launch Reports from Student List in English and German; verify bottom navigation is absent, charts use consistent styling with labels, earnings show currency symbols, and summary cards remain visually intact.

**Acceptance Scenarios**:

1. **Given** the tutor opens Reports from Student List, **When** the screen renders, **Then** no bottom navigation bar is visible.
2. **Given** the "Last 6 months' earnings" chart loads, **When** inspecting the bars, **Then** all bars share identical styling and each shows the month label underneath.
3. **Given** the Total Earnings summary is displayed, **When** the user's currency is USD/Try/EUR, **Then** the value includes the correct symbol and locale-specific formatting.
4. **Given** the device language is German, **When** summary cards render, **Then** the layout adapts (wrapping or resizing text) without truncation or overflow.

### Edge Cases

- Reports data contains zero earnings for several months → chart should still show uniform bars with minimal height and accurate labels.
- Currency symbol/format differs from locale (e.g., German EUR vs Turkish TRY) → Summary cards must respect the app’s selected currency rather than device defaults if they differ.
- Extremely long translated strings (e.g., German/Spanish) in summary titles → Layout adapts via wrapping or dynamic typography without clipping.
- Empty dataset and error state remain unaffected by removal of bottom navigation.

## Requirements *(mandatory)*

**Constitution Guardrails**:
- UI changes stay within Compose + Material 3; no XML fragments.
- Navigation updates must keep Clean Architecture flow—Reports still routed via `KoraDestination.Reports`.
- Localization: all new strings in `strings.xml` with translations; reuse existing keys when possible.
- Coroutines/Flow usage unchanged; no new asynchronous logic needed.
- Room/data models untouched—presentation-only refactor.

### Functional Requirements

- **FR-001 (UI)**: Remove any bottom navigation exposure in Reports—`ReportsScreen` must not provide a bottomBar in its Scaffold or related composables.
- **FR-002 (UI)**: Standardize bar chart styling so every bar shares identical color, width, corner radius, and opacity; remove special styling for the "current" month.
- **FR-003 (UI)**: Display uppercase month labels (e.g., JAN, FEB) beneath each bar using locale-aware abbreviations while preserving equal spacing.
- **FR-004 (Formatting)**: Format Total Earnings using the tutor’s selected currency symbol from user preferences (not just locale default) and ensure NumberFormat respects that selection.
- **FR-005 (UI Layout)**: Refactor summary cards layout to support wrapped text or adaptive typography so longer translations render without clipping or misalignment.
- **FR-006 (Localization)**: Validate the updated UI in German and Turkish previews/tests to confirm layout resilience.
- **FR-007 (Testing)**: Add Compose UI tests or screenshot assertions ensuring bottom nav is absent and summary cards accommodate long text (at minimum semantics or layout assertions).

### Key Entities *(include if feature involves data)*

- **ReportSummary**: Provides earnings/hours/students values; no schema change, but formatting logic must consume currency symbol from user preferences.
- **UserPreferences**: Source of currency code/symbol. Ensure formatting pulls from `LocalUserPreferences`.
- **MonthlyEarningsPoint**: Drives chart bars; styling changes act on presentation only.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Reports screen renders without bottom navigation bar in 100% of manual checks.
- **SC-002**: UI regression test verifies chart bars share identical style and have localized month labels.
- **SC-003**: Snapshot/manual verification shows Total Earnings value includes the correct currency symbol across USD/TRY/EUR configurations.
- **SC-004**: German locale smoke test confirms summary cards remain readable with no truncated text or layout overflow.
