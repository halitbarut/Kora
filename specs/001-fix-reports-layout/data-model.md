# Data Model: Reports Layout Polish

**Branch**: `001-fix-reports-layout` | **Date**: 2025-10-31

## Overview

This bug fix involves **UI-only changes** with no modifications to data models, domain entities, or persistence layer. All existing data contracts remain unchanged.

## Affected UI Components

### 1. SummaryCards Composable

**File**: `app/src/main/java/com/barutdev/kora/ui/screens/reports/ReportsScreen.kt:241-305`

**Current Signature**:
```kotlin
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SummaryCards(
    summary: ReportSummary,
    locale: Locale,
    currencyFormatter: NumberFormat,
    modifier: Modifier = Modifier
)
```

**Changes**:
- Remove `@OptIn(ExperimentalLayoutApi::class)` annotation
- Replace `FlowRow` layout with `LazyRow`
- Adjust card sizing modifiers

**Input Data**: `ReportSummary` (unchanged)
```kotlin
data class ReportSummary(
    val totalEarnings: BigDecimal,
    val totalHours: Duration,
    val activeStudents: Int
)
```

**Output**: Three Material 3 Card composables in horizontally scrollable row
- Card 1: Total Earnings (formatted currency)
- Card 2: Total Hours (formatted duration)
- Card 3: Active Students (formatted integer)

**Behavior Changes**:
- **Before**: Cards wrap to multiple rows on narrow screens or long text
- **After**: Cards remain in single row with horizontal scrolling

---

### 2. BarChart Composable

**File**: `app/src/main/java/com/barutdev/kora/ui/screens/reports/ReportsScreen.kt:428-481`

**Current Signature**:
```kotlin
@Composable
private fun BarChart(
    bars: List<ChartBar>,
    locale: Locale,
    modifier: Modifier = Modifier,
    chartHeight: Dp = ReportsChartHeight
)
```

**Changes**:
- Add text measurement logic for label height calculation
- Adjust bar height calculation to reserve space for labels
- Introduce new constant: `ReportsChartMinLabelPadding`

**Input Data**: `List<ChartBar>` (unchanged)
```kotlin
private data class ChartBar(
    val label: String,       // Month abbreviation (e.g., "Jan", "Feb")
    val value: Double,       // Numeric earnings value
    val formattedValue: String  // Currency-formatted string
)
```

**Layout Calculation** (new):
```kotlin
// Measure label text height at runtime
val textMeasurer = rememberTextMeasurer()
val labelStyle = MaterialTheme.typography.bodySmall
val labelHeight = remember(labelStyle) {
    val result = textMeasurer.measure("MMM", style = labelStyle)
    result.size.height.dp
}

// Reserve space for label + padding + spacer
val availableBarHeight = chartHeight
    - labelHeight
    - ReportsChartMinLabelPadding
    - ReportsChartLabelSpacing

// Calculate bar height within available space
val targetHeight = (availableBarHeight.value * heightFraction).dp
val resolvedHeight = max(targetHeight, ReportsChartMinBarHeight)
```

**Constants** (existing + new):
```kotlin
private val ReportsChartHeight = 160.dp             // Total chart container height
private val ReportsChartBarWidth = 28.dp            // Individual bar width
private val ReportsChartBarSpacing = 16.dp          // Horizontal gap between bars
private val ReportsChartLabelSpacing = 8.dp         // Spacer between bar and label
private val ReportsChartMinBarHeight = 8.dp         // Minimum bar height (existing)
private val ReportsChartMinLabelPadding = 4.dp      // NEW: Minimum padding above label
```

**Behavior Changes**:
- **Before**: Bars use full chart height, labels may be clipped/hidden
- **After**: Bars constrained to `chartHeight - labelHeight - 12.dp`, labels always visible

---

## Data Flow (Unchanged)

```
ReportsViewModel
    ↓ (StateFlow)
ReportsUiState
    ↓
ReportsScreenContent
    ↓
ReportsContent
    ↓
SummaryCards / EarningsChart / BarChart
    ↓
Material 3 UI Components
```

**No changes** to:
- `ReportsUiState` data class
- `ReportSummary` domain model
- `MonthlyEarningsPoint` domain model
- `ReportsViewModel` state management
- Use case layer (`GetReportSummaryUseCase`, etc.)
- Repository layer
- DataSource layer

---

## UI State Validation

### Summary Cards
**Validation Rules** (existing, unchanged):
- `totalEarnings` must be formatted per user's currency preference
- `totalHours` must be converted to decimal hours with 0-1 decimal places
- `activeStudents` must be formatted as integer with locale-specific grouping

**New Layout Constraints**:
- Cards must have minimum width to ensure readability (proposed: 140.dp)
- Cards maintain consistent internal padding: `16.dp` horizontal, `18.dp` vertical
- Card spacing: `12.dp` horizontal gaps

### Bar Chart
**Validation Rules** (existing, unchanged):
- Maximum 6 bars displayed (last 6 months)
- Bars normalized to max value in dataset
- Empty state shown when all values are zero
- Minimum bar height: 8.dp for non-zero values

**New Layout Constraints**:
- Label text height measured per locale and font scaling
- Available bar height = `160.dp - labelHeight - 12.dp`
- Minimum label padding: 4.dp from chart bottom
- Labels always rendered within chart container bounds

---

## Testing Considerations

### Data Scenarios
Test both UI fixes with these data conditions:

**Summary Cards**:
- Zero earnings, hours, students (edge case)
- Large numbers: €10,000+, 100+ hours, 50+ students
- Decimal values: €1,234.56, 42.5 hours
- Long locale strings: German translations

**Bar Chart**:
- All zero earnings (empty state)
- Single non-zero month
- All equal values (flat chart)
- Maximum earnings in latest month (label occlusion scenario)
- Maximum earnings in earliest month (should not occlude)

### Locale Testing
Required locales per spec:
- English (en-US)
- German (de-DE) - longest strings
- One non-Latin locale (e.g., Arabic for RTL)

### Accessibility Testing
- Font scaling: 100%, 150%, 200%
- Screen readers: TalkBack (Android)
- Keyboard navigation: Tab through scrollable cards

---

## Relationship Diagram

```
ReportSummary (domain model)
    ├─ totalEarnings: BigDecimal ──► Card 1 (LazyRow item)
    ├─ totalHours: Duration ───────► Card 2 (LazyRow item)
    └─ activeStudents: Int ────────► Card 3 (LazyRow item)

List<MonthlyEarningsPoint> (domain model)
    ├─ month: YearMonth ───────────► ChartBar.label (formatted)
    └─ earnings: BigDecimal ───────► ChartBar.value (normalized)
            └─► Bar height = (availableBarHeight * normalized) + spacing + label
```

---

## Migration Notes

**No database migration required** - UI-only changes

**No API version changes** - domain models unchanged

**No breaking changes** - existing tests should pass with minor assertions updates for layout structure (FlowRow → LazyRow)

**Backward compatibility**: Fully compatible, visual polish only
