# Quick Start: Reports Layout Polish Implementation

**Branch**: `001-fix-reports-layout` | **Date**: 2025-10-31

## Overview

This guide provides step-by-step instructions to implement the two layout fixes for the Reports screen:
1. Replace FlowRow with LazyRow for summary cards horizontal scrolling
2. Fix bar chart label occlusion by reserving label space in height calculations

**Estimated time**: 30-45 minutes
**Files modified**: 1 (`ReportsScreen.kt`)
**Lines changed**: ~40 lines

---

## Prerequisites

- Branch `001-fix-reports-layout` checked out
- Android Studio with Kotlin plugin
- Local build succeeds: `./gradlew assembleDebug`
- Familiarity with Jetpack Compose and Material 3

---

## Implementation Steps

### Step 1: Fix Summary Card Overflow (15 minutes)

**File**: `app/src/main/java/com/barutdev/kora/ui/screens/reports/ReportsScreen.kt`

#### 1.1 Remove ExperimentalLayoutApi annotation
**Location**: Line 241

**Before**:
```kotlin
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SummaryCards(
    summary: ReportSummary,
    locale: Locale,
    currencyFormatter: NumberFormat,
    modifier: Modifier = Modifier
) {
```

**After**:
```kotlin
@Composable
private fun SummaryCards(
    summary: ReportSummary,
    locale: Locale,
    currencyFormatter: NumberFormat,
    modifier: Modifier = Modifier
) {
```

#### 1.2 Replace FlowRow with LazyRow
**Location**: Lines 267-303

**Before**:
```kotlin
Column(
    modifier = modifier
) {
    Text(
        text = koraStringResource(id = R.string.reports_title),
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(16.dp))
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        maxItemsInEachRow = 3
    ) {
        SummaryCard(
            title = koraStringResource(id = R.string.reports_summary_total_earnings),
            value = earningsValue,
            modifier = Modifier
                .weight(1f, fill = true)
                .fillMaxWidth()
        )
        SummaryCard(
            title = koraStringResource(id = R.string.reports_summary_total_hours),
            value = hoursValue,
            modifier = Modifier
                .weight(1f, fill = true)
                .fillMaxWidth()
        )
        SummaryCard(
            title = koraStringResource(id = R.string.reports_summary_active_students),
            value = activeStudentsValue,
            modifier = Modifier
                .weight(1f, fill = true)
                .fillMaxWidth()
        )
    }
}
```

**After**:
```kotlin
Column(
    modifier = modifier
) {
    Text(
        text = koraStringResource(id = R.string.reports_title),
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(16.dp))
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        item(key = "total_earnings") {
            SummaryCard(
                title = koraStringResource(id = R.string.reports_summary_total_earnings),
                value = earningsValue,
                modifier = Modifier.width(160.dp)
            )
        }
        item(key = "total_hours") {
            SummaryCard(
                title = koraStringResource(id = R.string.reports_summary_total_hours),
                value = hoursValue,
                modifier = Modifier.width(160.dp)
            )
        }
        item(key = "active_students") {
            SummaryCard(
                title = koraStringResource(id = R.string.reports_summary_active_students),
                value = activeStudentsValue,
                modifier = Modifier.width(160.dp)
            )
        }
    }
}
```

**Key Changes**:
- Replace `FlowRow` with `LazyRow`
- Use `item()` DSL instead of direct children
- Add stable `key` parameters for each card
- Replace `.weight(1f, fill = true).fillMaxWidth()` with fixed `.width(160.dp)`
- Add `contentPadding` for edge spacing
- Remove `maxItemsInEachRow` (not applicable to LazyRow)

#### 1.3 Remove ExperimentalLayoutApi import
**Location**: Line 8

**Before**:
```kotlin
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
```

**After**:
```kotlin
// Remove ExperimentalLayoutApi import
// Keep or remove FlowRow import (no longer used)
```

**Verification**: Build the project. Summary cards should now scroll horizontally.

---

### Step 2: Fix Bar Chart Label Occlusion (20 minutes)

**File**: `app/src/main/java/com/barutdev/kora/ui/screens/reports/ReportsScreen.kt`

#### 2.1 Add new constant for minimum label padding
**Location**: After line 401 (after existing constants)

**Add**:
```kotlin
private val ReportsChartMinLabelPadding = 4.dp
```

**Result**:
```kotlin
private const val MONTH_LABEL_PATTERN = "LLL"
private val ReportsChartHeight = 160.dp
private val ReportsChartBarWidth = 28.dp
private val ReportsChartBarSpacing = 16.dp
private val ReportsChartLabelSpacing = 8.dp
private val ReportsChartMinBarHeight = 8.dp
private val ReportsChartBarCornerRadius = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
private val ReportsChartMinLabelPadding = 4.dp  // NEW
```

#### 2.2 Add text measurement imports
**Location**: Top of file (around line 36-52)

**Add these imports**:
```kotlin
import androidx.compose.ui.text.rememberTextMeasurer
```

#### 2.3 Update BarChart composable with label space reservation
**Location**: Lines 428-481

**Before** (key section, lines 434-460):
```kotlin
val maxValue = bars.maxOfOrNull { it.value }.takeUnless { it == null || it == 0.0 } ?: 1.0
Row(
    modifier = modifier
        .fillMaxWidth()
        .height(chartHeight),
    verticalAlignment = Alignment.Bottom,
    horizontalArrangement = Arrangement.spacedBy(ReportsChartBarSpacing)
) {
    bars.forEach { bar ->
        val heightFraction = (bar.value / maxValue).coerceIn(0.0, 1.0).toFloat()
        val barDescription = koraStringResource(
            id = R.string.reports_chart_accessibility_bar,
            bar.label,
            bar.formattedValue
        )
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            val targetHeight = (chartHeight.value * heightFraction).dp
            val resolvedHeight = if (targetHeight < ReportsChartMinBarHeight) {
                ReportsChartMinBarHeight
            } else {
                targetHeight
            }
```

**After**:
```kotlin
// Measure label text height for space reservation
val textMeasurer = rememberTextMeasurer()
val labelStyle = MaterialTheme.typography.bodySmall
val labelHeight = remember(labelStyle) {
    val textLayoutResult = textMeasurer.measure(
        text = "MMM",
        style = labelStyle
    )
    textLayoutResult.size.height.toDp()
}

// Calculate available height for bars (reserve space for label + padding)
val availableBarHeight = chartHeight - labelHeight - ReportsChartMinLabelPadding - ReportsChartLabelSpacing

val maxValue = bars.maxOfOrNull { it.value }.takeUnless { it == null || it == 0.0 } ?: 1.0
Row(
    modifier = modifier
        .fillMaxWidth()
        .height(chartHeight),
    verticalAlignment = Alignment.Bottom,
    horizontalArrangement = Arrangement.spacedBy(ReportsChartBarSpacing)
) {
    bars.forEach { bar ->
        val heightFraction = (bar.value / maxValue).coerceIn(0.0, 1.0).toFloat()
        val barDescription = koraStringResource(
            id = R.string.reports_chart_accessibility_bar,
            bar.label,
            bar.formattedValue
        )
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            val targetHeight = (availableBarHeight.value * heightFraction).dp
            val resolvedHeight = if (targetHeight < ReportsChartMinBarHeight) {
                ReportsChartMinBarHeight
            } else {
                targetHeight
            }
```

**Key Changes**:
- Add `textMeasurer` using `rememberTextMeasurer()`
- Measure sample text "MMM" to get label height
- Calculate `availableBarHeight` by subtracting label space from total chart height
- Replace `chartHeight.value` with `availableBarHeight.value` in bar height calculation

**Note**: The rest of the BarChart function (lines 461-481) remains unchanged.

#### 2.4 Add extension function for Int to Dp conversion (if needed)
**Location**: Near the BarChart function or at file bottom

**Check if** `toDp()` is available on `Int`. If not, add:
```kotlin
import androidx.compose.ui.platform.LocalDensity

// Then inside BarChart, use:
val density = LocalDensity.current
val labelHeight = remember(labelStyle, density) {
    val textLayoutResult = textMeasurer.measure(
        text = "MMM",
        style = labelStyle
    )
    with(density) { textLayoutResult.size.height.toDp() }
}
```

**Verification**: Build and run. Bar chart labels should now be visible even for maximum earnings bars.

---

## Step 3: Verify Changes (10 minutes)

### 3.1 Build the app
```bash
./gradlew assembleDebug
```

**Expected**: Build succeeds with no errors.

### 3.2 Test Summary Cards
1. Launch app and navigate to Reports screen
2. Switch device language to German (Settings > Language)
3. Scroll summary cards horizontally
4. Verify all three cards remain readable without text truncation

### 3.3 Test Bar Chart Labels
1. Ensure Reports screen has data with varying earnings
2. Identify the month with maximum earnings
3. Verify the label for that month is fully visible
4. Verify all 6 month labels are visible and properly spaced

### 3.4 Run automated tests (per spec FR-004)
```bash
./gradlew connectedDebugAndroidTest
```

**Expected**: All tests pass. If any layout-related tests fail, update assertions to expect LazyRow instead of FlowRow.

---

## Step 4: Manual Testing Checklist

**Locale Testing**:
- [ ] English (en-US): Cards fit side-by-side, labels visible
- [ ] German (de-DE): Cards scroll horizontally, no overflow
- [ ] Arabic (ar): RTL scrolling works, labels aligned correctly

**Device Testing**:
- [ ] Phone portrait: Cards scroll, chart labels visible
- [ ] Phone landscape: Cards may fit without scroll, labels visible
- [ ] Tablet: Wider viewport, verify layout adapts

**Accessibility Testing**:
- [ ] Font scaling 100%: Baseline behavior
- [ ] Font scaling 150%: Cards scroll, chart adjusts
- [ ] Font scaling 200%: Maximum scaling, labels remain visible
- [ ] TalkBack: Scroll gestures announced, bar descriptions read correctly

**Edge Cases**:
- [ ] All zero earnings: Empty state displayed
- [ ] Single month data: One bar with label
- [ ] Maximum earnings in latest month: Label not occluded (primary bug fix)

---

## Troubleshooting

### Issue: Build error "Unresolved reference: LazyRow"
**Solution**: Ensure import is present:
```kotlin
import androidx.compose.foundation.lazy.LazyRow
```

### Issue: Build error "Unresolved reference: items"
**Solution**: LazyRow uses `item()` (singular) not `items()` (plural) for individual items. Use:
```kotlin
item(key = "...") { /* content */ }
```

### Issue: Cards too narrow on wide screens
**Solution**: Increase card width from `160.dp` to `180.dp` or use `width(160.dp).widthIn(min = 160.dp, max = 220.dp)` for flexible sizing.

### Issue: Text measurement crashes on preview
**Solution**: `rememberTextMeasurer()` requires composition context. Ensure it's inside `@Composable` function, not a top-level variable.

### Issue: Labels still clipped on some devices
**Solution**: Increase `ReportsChartMinLabelPadding` from `4.dp` to `6.dp` or `8.dp`.

---

## Rollback Plan

If critical issues arise:

1. **Revert summary cards**:
   - Replace LazyRow with original FlowRow
   - Restore `.weight()` modifiers
   - Re-add `@OptIn(ExperimentalLayoutApi::class)`

2. **Revert bar chart**:
   - Remove text measurement logic
   - Restore original bar height calculation: `(chartHeight.value * heightFraction).dp`
   - Remove `ReportsChartMinLabelPadding` constant

3. **Git revert**:
   ```bash
   git diff HEAD ReportsScreen.kt > rollback.patch
   git checkout HEAD -- app/src/main/java/com/barutdev/kora/ui/screens/reports/ReportsScreen.kt
   ```

---

## Next Steps

After implementation:
1. Create pull request with screenshots (English + German)
2. Request QA review with locales: en, de, ar
3. Update release notes: "Fixed Reports screen layout issues in long-text languages"
4. Monitor crash reports for any text measurement edge cases

---

## Resources

- Compose LazyRow documentation: https://developer.android.com/jetpack/compose/lists#lazy
- Text measurement API: https://developer.android.com/jetpack/compose/text#measure-text
- Material 3 Card: https://m3.material.io/components/cards
- Accessibility testing: https://developer.android.com/guide/topics/ui/accessibility/testing
