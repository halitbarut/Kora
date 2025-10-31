# Reports Screen – Release Notes

## Summary
- Adds dedicated **Reports** tab with earnings snapshot and performance trends.
- Introduces range presets (Last 7 Days, This Month, Last 30 Days, All Time) with in-session persistence.
- Surfaces Top 3 students by hours taught plus a six-month earnings chart with accessibility semantics.

## Screenshots
- `screenshots/reports-default.png` – Default "This Month" dashboard.
- `screenshots/reports-top-students.png` – Filtered view with Top 3 list populated.

> _Note:_ Capture screenshots on Pixel 6, light mode, English locale.

## Testing
- Unit: `testDebugUnitTest` (pass)
- UI: `connectedDebugAndroidTest` (pending before release)
- Manual: Refer to `docs/testing/reports-performance.md` for performance checklist.

## Known Issues
- None observed during implementation. Continue monitoring for dataset > 2 years.

## Rollout Checklist
- [ ] QA sign-off on performance runs.
- [ ] Update Play Store listing with new screenshots post-release.
