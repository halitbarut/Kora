# Bug Fix Specification: Reports Layout Legibility

**Feature Branch**: `[fix-reports-layout-spacing]`  
**Created**: 2025-10-31  
**Status**: Draft  
**User Story**: "As a tutor, I want the summary cards and the bar chart on the Reports screen to be visually correct and readable in all supported languages."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Reports remain legible across locales (Priority: P0)

Tutors must be able to read every summary card and month label on the Reports screen regardless of device width or translation length.

**Why this priority**: Broken layouts undermine the app's localization-first principle and make key revenue data unreadable.

**Independent Test**: Switch the device to German, open the Reports screen, and confirm the summary cards scroll horizontally without clipping plus every month label remains visible beneath its bar.

**Acceptance Scenarios**:

1. **Given** the device locale is German (or any long-text language), **When** the Reports screen loads, **Then** the three summary cards render inside a horizontally scrollable row so no card truncates or breaks the layout.
2. **Given** the Reports screen shows the earnings chart for the last six months, **When** the most recent month has the highest bar, **Then** the bar stops above a reserved label band so its month label remains fully visible.
3. **Given** the Reports screen shows the earnings chart for six months, **When** the tutor inspects each label, **Then** all six month labels render with at least the current typography's line height clearance from the nearest bar edge.

### Edge Cases

- How should the summary row behave on tablets in landscape? ➝ Cards still sit in the LazyRow but can fit side-by-side without scroll when width allows; scrolling remains enabled to preserve consistency.
- What happens when earnings are zero for all months? ➝ Existing empty-period state continues; these fixes must not regress that logic.

## Requirements *(mandatory)*

### Constitution Guardrails
- UI must remain Compose Material 3 with existing theming tokens for cards and chart colors.
- Localization-first: summary card titles continue to use `strings.xml`; no literal strings introduced.
- Architecture remains unchanged: updates limited to the Reports UI composables and supporting formatting helpers; no repository or use-case changes expected.
- Accessibility: maintain current `contentDescription` semantics on summary cards and bars; ensure LazyRow scrolling is discoverable via standard gestures.

## ADDED Requirements

### Requirement: Reports summary cards SHALL remain scrollable and unbroken
The Reports screen MUST render the three summary cards inside a horizontally scrollable LazyRow so translated titles of any supported length never wrap in a way that distorts the layout or pushes cards out of view.

#### Scenario: Long translated titles stay readable
- **GIVEN** the device language uses long translations (e.g., German)
- **WHEN** the tutor opens the Reports screen
- **THEN** each summary card appears at its intended size inside a LazyRow, and the tutor can horizontally scroll to view all three cards without clipped text or broken card stacking

#### Scenario: Default locale still shows aligned cards
- **GIVEN** the device uses English and a standard phone width
- **WHEN** the Reports screen loads
- **THEN** all three summary cards are visible within the LazyRow with consistent spacing, while horizontal scrolling remains available with no regressions to card styling or semantics

### Requirement: Reports earnings chart SHALL reserve label space beneath bars
The Canvas-backed bar chart MUST subtract a dedicated label band from the drawable height before plotting bars so every bar stops above its label, leaving at least the bodySmall line height plus 4 dp of padding between the bar and the label baseline.

#### Scenario: Tallest bar preserves its label
- **GIVEN** the latest month produces the highest earnings
- **WHEN** the bar chart renders
- **THEN** the tallest bar ends above the label band, leaving the month label fully readable with the required clearance

#### Scenario: All month labels remain visible
- **GIVEN** the chart displays six months of earnings
- **WHEN** the tutor reviews the axis
- **THEN** each of the six month labels renders unobstructed beneath its bar with equal spacing, regardless of bar height variations
