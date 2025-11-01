# Data Model: Refactor Reports Entry Point

No new domain entities or Room schemas are introduced. Existing models remain immutable and unchanged.

| Layer | Impact | Notes |
|-------|--------|-------|
| Domain models | None | Student and report summaries untouched. |
| Room entities / DAO | None | No schema or migration required. |
| UI state objects | Minimal | Student List top bar gains a new action but no state properties change. |
