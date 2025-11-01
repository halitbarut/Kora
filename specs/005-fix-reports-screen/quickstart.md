# Quickstart: Fix Reports Screen UI

1. **Checkout feature branch**
   ```bash
   git checkout 004-refactor-reports-entry-point
   ```
2. **Build (optional if already run)**
   ```bash
   ./gradlew assembleDebug
   ```
3. **Run targeted UI tests (to be added)**
   ```bash
   ./gradlew connectedDebugAndroidTest \
     -Pandroid.testInstrumentationRunnerArguments.class=com.barutdev.kora.ui.reports.ReportsScreenUiTest
   ```
4. **Manual validation**
   - Launch Student List, tap reports icon.
   - Confirm bottom navigation not visible on Reports.
   - Check Total Earnings shows correct currency symbol (test USD/TRY/EUR).
   - Switch device/app language to German; ensure summary cards wrap gracefully.
   - Verify bar chart bars share identical styling and have month labels.
