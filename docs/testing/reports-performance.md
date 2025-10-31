# Reports Screen Performance Checklist

## Objective
- Confirm the Reports dashboard loads within **3 seconds** on first open.
- Confirm refreshing after a range change completes within **2 seconds**.

## Preconditions
- Use a debug build compiled with minify disabled.
- Emulator or device aligned to UTC to match canned test data.
- Seed database with ~24 months of lessons (>=500 entries) and at least 100 students.
- Disable animations in Developer Options to reduce variance.
- Ensure device is unplugged from debugger when recording timings.

## Tooling
- Android Studio `Logcat` with `ViewModel` tag filtering.
- `adb shell am broadcast -a com.barutdev.kora.DEBUG_SEED` (if seeding script exists) or manual fixture import.
- Optional: `adb shell dumpsys gfxinfo` for frame timing snapshots.

## Test Matrix
| Scenario | Data State | Network | Expected |
|----------|------------|---------|----------|
| Initial load | Warm process, Reports tab opened from cold start | Offline | ≤ 3 s to render summary, chart, and top students |
| Range change | Switch from `This Month` to `Last 30 Days` | Offline | ≤ 2 s for UI to settle with updated metrics |
| Range change repeat | Rapid taps across all presets | Offline | Only final selection emits updates; no jitter |

## Steps
1. Launch app fresh; start timer on tapping **Reports** tab.
2. Stop timer when summary cards and chart bars render (use `Compose` inspection or screenshot).
3. Record duration in log table.
4. Tap `Last 30 Days`; start timer on tap.
5. Stop when top students list updates (observe new ordering) and progress indicator hides.
6. Repeat range taps sequentially (`All time` → `Last 7 Days` → `This Month`) to confirm cancellation behaviour.
7. Capture `adb shell dumpsys gfxinfo com.barutdev.kora` after interactions for frame stats.

## Pass/Fail Criteria
- All measurements within thresholds.
- No dropped frames above 10% in `gfxinfo` summary.
- ViewModel logs indicate only one active load job per selection (`ReportsViewModel` log tag optional).

## Reporting Template
| Run | Initial Load (s) | Last 30 Days (s) | Notes |
|-----|------------------|------------------|-------|
| 1 |  |  |  |
| 2 |  |  |  |

Log findings in this table and attach `gfxinfo` output to the QA ticket.
