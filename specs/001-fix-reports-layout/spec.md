# Feature Specification: Reports Layout Polish

**Feature Branch**: `[001-fix-reports-layout]`  
**Created**: 2025-10-31  
**Status**: Draft  
**Input**: User description: "Create a new bug fix specification to resolve the final layout issues on the "Reports Screen". The user story is: "As a tutor, I want the summary cards and the bar chart on the Reports screen to be visually correct and readable in all supported languages." Two specific bugs must be fixed: 1. Fix Summary Card Overflow: The summary cards ("Total Earnings", etc.) break the layout on long-text languages like German. To fix this, the three summary cards MUST be placed inside a LazyRow. This will allow the user to scroll horizontally if the cards overflow the screen width, ensuring the layout never breaks. 2. Fix Bar Chart Label Occlusion: In the "Earnings last 6 months" chart, the bar for the most recent month is drawn on top of its corresponding month label, making the label invisible. The Canvas drawing logic MUST be corrected to ensure there is always sufficient vertical spacing between the bottom of each bar and the top of its month label. All six month labels must be visible at all times.

This change must also include running the automated tests. The task list should include a task to execute ./gradlew connectedDebugAndroidTest and verify that all tests pass."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Summary cards stay readable (Priority: P1)

As a tutor reviewing my performance, I can open the Reports screen and read every summary card even when my device is set to a long-text language, because the cards align cleanly and I can scroll horizontally if needed.

**Why this priority**: The summary metrics are the first content tutors see on the Reports screen; broken layout diminishes trust in the product and blocks comprehension.

**Independent Test**: Switch the device language to German, reopen the Reports screen, and confirm all three cards remain accessible without overlapping or truncation.

**Acceptance Scenarios**:

1. **Given** the tutor’s device language uses long words, **When** the Reports screen loads, **Then** all three summary cards render without wrapping or clipping and remain aligned.
2. **Given** the tutor views the screen on a narrow device, **When** only part of the card row is visible, **Then** horizontal scrolling reveals the remaining cards without snapping or visual artifacts.

---

### User Story 2 - Month labels remain visible (Priority: P2)

As a tutor comparing monthly performance, I can read every month label beneath the earnings chart because the bars never cover the text.

**Why this priority**: Hidden month labels make the chart unusable for the most recent data, undermining the insights the feature promises.

**Independent Test**: Load the Reports screen with live data and confirm each of the six labels is visible regardless of bar height or language length.

**Acceptance Scenarios**:

1. **Given** a month with the highest earnings, **When** the bar renders, **Then** a gap remains between the bar and its label so that the label is fully legible.
2. **Given** the device language uses longer month names, **When** the chart renders, **Then** all six labels remain unobstructed and aligned with their bars.

---

### Edge Cases

- Reports screen opened with dynamic type or accessibility font scaling at maximum.
- Landscape orientation on tablets where additional horizontal space is available.
- Devices using right-to-left locales where horizontal scrolling direction reverses.
- Charts where the latest month has zero earnings yet still requires a label gap.
- Offline mode where cached data populates the chart and cards.

## Requirements *(mandatory)*

**Constitution Guardrails**:
- Layers in scope: UI draws and layout behavior; existing ViewModel/use case data contracts remain unchanged; repositories/datasources unaffected.
- Compose Material 3 updates: Summary cards continue using the current elevated `Card` components arranged in a horizontally scrollable row with the existing color scheme. No new user-facing strings are introduced; current localized keys are reused.
- Coroutines/Flow: No changes to async pipelines; existing flows feeding summary metrics and chart data stay intact.
- Room (KSP): No schema, DAO, or model updates required.
- Hilt & Navigation: No new bindings or navigation routes are necessary; Reports screen destination remains the entry point for these fixes.

### Functional Requirements

- **FR-001 (UI)**: The Reports screen MUST render the three summary cards inside a horizontally scrollable Material 3 card row so that any locale or font size keeps each card readable without overlap.
- **FR-002 (UI)**: The summary card row MUST preserve consistent card spacing, ordering, and padding across phone and tablet layouts, allowing full keyboard or touch scrolling when content extends beyond the viewport.
- **FR-003 (UI)**: The earnings chart MUST reserve sufficient vertical clearance between every bar and its month label so that 100% of labels remain fully visible regardless of earnings values or language length.
- **FR-004 (Quality Assurance)**: Release verification MUST include executing `./gradlew connectedDebugAndroidTest` and confirming all automated tests succeed before handing off the fix.

### Key Entities *(include if feature involves data)*

- **Summary Metric**: Aggregated figure with localized title and value representing totals (earnings, lessons, students) sourced from existing reporting data.
- **Monthly Earnings Bucket**: Ordered set of six recent months containing localized month labels and numeric totals used to plot chart bars.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: In QA screenshots across three target locales (including German), all summary cards remain readable with zero layout breakage.
- **SC-002**: On every tested device size and orientation, six month labels remain fully visible with no overlap in 100% of verification runs.
- **SC-003**: Accessibility review confirms horizontal scrolling works with touch and keyboard navigation without obstructing VoiceOver/TalkBack announcements.
- **SC-004**: Automated test run (`connectedDebugAndroidTest`) completes successfully with no failing cases before sign-off.

## Assumptions

- Existing summary card content and chart data sources already provide accurate values; this work focuses purely on presentation.
- QA can access localized builds in at least English, German, and one additional non-Latin locale to validate readability.
- Performance impact of introducing horizontal scrolling and additional spacing is negligible for the dataset size (three cards, six bars).
