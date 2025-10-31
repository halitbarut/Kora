# Data Model Notes: Fix Reports Screen UI

- No domain or Room entities change. `ReportSummary`, `MonthlyEarningsPoint`, and `TopStudentEntry` remain immutable.
- Consumption updates:
  - `ReportSummary.totalEarnings` now formatted with user-selected currency code.
  - UI layer reads `LocalUserPreferences` for currency code/symbol.
- No additional validation rules or state transitions introduced.
