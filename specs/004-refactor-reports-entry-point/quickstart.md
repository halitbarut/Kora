# Quickstart: Refactor Reports Entry Point

1. **Checkout feature branch**
   ```bash
   git checkout 004-refactor-reports-entry-point
   ```
2. **Sync dependencies and build**
   ```bash
   ./gradlew assembleDebug
   ```
3. **Run unit + UI smoke tests**
   ```bash
   ./gradlew testDebugUnitTest connectedDebugAndroidTest \
     -Pandroid.testInstrumentationRunnerArguments.class=com.barutdev.kora.navigation.BottomBarNavigationTest
   ```
   *(Adjust the instrumentation test class once added for this feature.)*
4. **Manual validation**
   - Launch the app to the Student List screen.
   - Tap the reports icon in the top app bar; verify the Reports screen appears.
   - Navigate back; confirm you return to the prior destination (student dashboard or Student List).
   - Inspect the bottom navigation across dashboards to ensure only three tabs are rendered.
