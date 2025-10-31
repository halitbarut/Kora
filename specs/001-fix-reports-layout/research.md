# Research: Reports Layout Polish

**Branch**: `001-fix-reports-layout` | **Date**: 2025-10-31

## Problem Analysis

### Issue 1: Summary Card Overflow
**Current Implementation** (ReportsScreen.kt:241-305):
- Uses `FlowRow` with `ExperimentalLayoutApi`
- Cards use `.weight(1f, fill = true)` to distribute space evenly
- On long-text locales (German), card titles overflow because FlowRow wraps items but individual cards cannot expand beyond screen width
- Located at ReportsScreen.kt:276-303

**Root Cause**: FlowRow wraps entire cards to new rows, but doesn't provide horizontal scrolling. When 3 cards try to fit with German text like "Gesamteinnahmen", "Gesamtstunden", "Aktive Schüler", the weight distribution forces cards to be too narrow, causing text truncation or layout breakage.

### Issue 2: Bar Chart Label Occlusion
**Current Implementation** (ReportsScreen.kt:428-481):
- BarChart component uses `Row` with `Alignment.Bottom` to align bars and labels
- Each bar is placed in a Column with the label below it (lines 450-478)
- Chart height is fixed at `ReportsChartHeight = 160.dp`
- Bar height is calculated as `(chartHeight.value * heightFraction).dp`
- Label spacing is `ReportsChartLabelSpacing = 8.dp` below the bar
- **Problem**: When heightFraction is 1.0 (maximum earnings), the bar uses the full 160.dp, leaving only the 8.dp spacer before the label, which positions the label's top edge at the bottom of the chart container. This causes the label to be drawn outside the visible chart area or overlap with content below.

**Current Layout Structure**:
```
Column (verticalArrangement = Bottom)
├─ Box (Bar) - height = chartHeight * heightFraction
├─ Spacer - 8.dp
└─ Text (Label)
```

When the bar is at maximum height (160.dp), the label's baseline is pushed below the chart's allocated space.

## Solutions

### Solution 1: Replace FlowRow with LazyRow

**Decision**: Replace `FlowRow` with `LazyRow` for horizontal scrolling

**Implementation**:
- Remove `@OptIn(ExperimentalLayoutApi::class)` from SummaryCards
- Replace FlowRow with LazyRow using `items()` DSL
- Remove `.weight()` modifiers from cards (LazyRow items have intrinsic sizing)
- Add horizontal padding/spacing via `horizontalArrangement` and `contentPadding`
- Keep card width flexible using `fillMaxWidth()` within a reasonable minimum (e.g., 140.dp minimum width)

**Rationale**:
- LazyRow provides native horizontal scrolling on touch and keyboard
- Removes dependency on experimental API
- Cards can expand to their natural content size
- Consistent with existing RangeFilterSection which already uses LazyRow (ReportsScreen.kt:210-238)
- Accessibility: LazyRow supports TalkBack/VoiceOver scroll announcements

**Alternatives Considered**:
- HorizontalPager: Overkill for simple scrolling, adds snap behavior which isn't needed
- Custom scrollable Row: LazyRow provides better performance with built-in recycling

### Solution 2: Reserve Label Space in Chart Height

**Decision**: Calculate bar height by reserving space for label height + minimum padding at the bottom of the chart

**Implementation**:
```kotlin
// Constants
private val ReportsChartHeight = 160.dp
private val ReportsChartMinLabelPadding = 4.dp  // NEW: minimum padding above label

// In BarChart composable
@Composable
private fun BarChart(...) {
    // Measure label height at runtime
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.bodySmall
    val sampleLabelHeight = remember(labelStyle) {
        val textLayoutResult = textMeasurer.measure(
            text = "MMM",  // representative 3-char month
            style = labelStyle
        )
        textLayoutResult.size.height.dp
    }

    // Calculate available height for bars
    val availableBarHeight = chartHeight - labelHeight - ReportsChartMinLabelPadding - ReportsChartLabelSpacing

    // Use availableBarHeight instead of chartHeight for bar calculations
    val targetHeight = (availableBarHeight.value * heightFraction).dp
    ...
}
```

**Layout Structure** (after fix):
```
Column (verticalArrangement = Bottom, height = 160.dp)
├─ Box (Bar) - height = (160 - labelHeight - 4 - 8) * heightFraction
├─ Spacer - 8.dp
└─ Text (Label) - measured height
└─ Implicit bottom padding (4.dp minimum)
```

**Rationale**:
- Ensures labels are always visible within the chart's allocated space
- Text measurement accounts for locale-specific font metrics and dynamic type
- Minimum 4.dp padding prevents labels from touching the chart bottom edge
- Preserves existing bar styling, corner radius, and color scheme
- Works for all bar heights including zero-earnings (minimum bar height)

**Alternatives Considered**:
- Increase chart height: Would work but wastes vertical space and doesn't scale across devices
- Use clipToBounds = false: Labels would overlap with TopStudentsSection below
- Position labels above bars: Against standard bar chart conventions, reduces readability

## Best Practices Applied

### Jetpack Compose Layout
- **LazyRow**: Standard Material 3 pattern for horizontally scrollable lists, matches existing RangeFilterSection implementation
- **Text measurement**: Using `rememberTextMeasurer` provides accurate runtime text dimensions accounting for typography, locale, and accessibility font scaling
- **Minimum touch targets**: Cards remain tappable (though not interactive in this feature, maintains future extensibility)

### Localization
- LazyRow respects RTL layouts automatically via Compose's layout direction system
- Text measurement works with any locale's character widths
- No hardcoded widths that might break with longer translations

### Accessibility
- LazyRow provides scroll announcements to screen readers
- Chart labels remain visible for low-vision users
- Semantic content descriptions already exist on bars (line 467-469), remain unchanged

### Performance
- LazyRow uses Compose's lazy layout system for efficient composition and recomposition
- Text measurement is memoized with `remember()` to avoid recalculation
- No impact on ViewModel or data layer (UI-only changes)

## Constitution Compliance

✅ **Clean MVVM**: Changes are UI-only, no ViewModel/UseCase/Repository modifications
✅ **Compose Material 3**: Using standard LazyRow and built-in typography measurement
✅ **Kotlin Coroutines**: No async changes required
✅ **Room**: No data model changes
✅ **Hilt & Navigation**: No DI or navigation changes
✅ **Strings**: No new user-facing strings, existing localized keys reused

## Testing Strategy

### Manual Testing (from spec SC-001, SC-002, SC-003)
- Verify summary cards scroll horizontally in German locale
- Confirm all 6 month labels visible across earnings ranges
- Test with accessibility font scaling at maximum
- Verify landscape orientation on tablets
- Test RTL locales (Arabic layout direction)

### Automated Testing (spec FR-004)
- Run `./gradlew connectedDebugAndroidTest`
- Existing tests should pass without modification
- Consider adding visual regression tests for card scrolling and chart label positioning

## Implementation Effort

**Estimated Changes**:
- File: `app/src/main/java/com/barutdev/kora/ui/screens/reports/ReportsScreen.kt`
  - SummaryCards function: ~15 lines (replace FlowRow with LazyRow)
  - BarChart function: ~20 lines (add text measurement and height calculation)

**Total**: ~35 lines changed in 1 file, 2 composables modified
**Risk**: Low - isolated UI changes with no data flow impact
