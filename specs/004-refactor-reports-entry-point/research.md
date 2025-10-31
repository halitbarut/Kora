# Research: Refactor Reports Entry Point

## Decision: Preserve student-scoped state through reports navigation
- **Rationale**: Tutors expect to resume exactly where they left off (e.g., a specific student's dashboard). Using `launchSingleTop` with direct navigation to `KoraDestination.Reports` keeps the existing back stack intact so the back button restores the prior screen.
- **Alternatives considered**: Popping to `StudentList` before navigating (rejected—would lose student context); adding a dedicated navigation graph for reports (rejected—overkill for a single route and complicates DI bindings).

## Decision: Use Material 3 bar chart icon with localized description
- **Rationale**: `Icons.Outlined.BarChart` aligns with existing Material icon usage and communicates analytics. Adding `student_list_reports_action_description` to all `strings.xml` files preserves localization-first principle.
- **Alternatives considered**: Custom vector asset (rejected—adds maintenance overhead); using settings icon badge (rejected—ambiguous affordance).

## Decision: Remove reports from bottom nav instead of hiding it conditionally
- **Rationale**: Simplifies navigation state by shrinking `bottomBarDestinations` to three entries, avoiding conditional recomposition logic and reducing the risk of stale student IDs in `BottomBarState`.
- **Alternatives considered**: Dynamically hiding the tab via runtime flags (rejected—adds branching inside bottom bar composable); leaving tab but disabling (rejected—clutters UI and violates requirement).
