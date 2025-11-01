<!--
Sync Impact Report
- Version: n/a -> 1.0.0
- Modified principles: n/a (initial adoption)
- Added sections: Implementation Standards; Delivery Workflow & Compliance
- Removed sections: None
- Templates requiring updates: ✅ .specify/templates/plan-template.md | ✅ .specify/templates/spec-template.md | ✅ .specify/templates/tasks-template.md
- Follow-up TODOs: None
-->

# Kora Android Application Constitution

## Core Principles

### I. Clean MVVM Contract (NON-NEGOTIABLE)
Mandates:
- Every feature flows strictly UI → ViewModel → UseCase → Repository → DataSource, with clear interface contracts between layers.
- ViewModels expose state via immutable data classes and `StateFlow`/`SharedFlow`; UI classes contain no business logic.
- UseCases encapsulate business rules, remain side-effect free except for repository access, and are fully unit-testable.
Rationale: Enforcing Clean Architecture boundaries keeps complex tutor workflows maintainable, testable, and ready for future scaling.

### II. Compose Material 3 Experience
Mandates:
- All UI must be implemented with Jetpack Compose and Material 3 components, theming, and typography.
- XML layouts, legacy Material components, or unthemed composables are prohibited.
- Reusable composables must consume design tokens from the central Material theme and support light/dark variants.
Rationale: Compose + Material 3 ensures a cohesive, modern Android experience that honors the Design Elevation principle.

### III. Kotlin 17 Concurrency Discipline
Mandates:
- Kotlin is the only implementation language, targeting JVM 17 for all modules and tooling.
- Asynchronous work must use Kotlin Coroutines and Flow; callbacks, RxJava, or threads are disallowed unless wrapped behind a Flow-based adapter.
- Suspend functions and Flows must surface domain errors explicitly (sealed results or exceptions) to keep ViewModels deterministic.
Rationale: A unified Kotlin + Coroutines stack maximizes safety, readability, and compatibility with Android Studio/Compose tooling.

### IV. Room Integrity & Immutable Data
Mandates:
- Room is the sole local persistence layer, configured with KSP-based code generation—no KAPT or alternative ORMs.
- Entities, DTOs, and domain models must be immutable `data class` instances; mutations require copy semantics or mapper transformations.
- Schema migrations must be versioned, tested, and documented; ad hoc SQL is prohibited outside Room DAOs or migrations.
Rationale: Immutable models plus Room’s compile-time guarantees preserve data accuracy for tutors’ mission-critical records.

### V. Platform Services Compliance
Mandates:
- Hilt provides dependency injection; manual service locators or alternative DI frameworks are disallowed.
- Navigation Compose orchestrates all screen transitions and deep links; legacy fragments or `NavController` XML graphs are not permitted.
- All user-facing strings reside in `strings.xml` with locale-aware formatting; reviews must reject hardcoded text.
Rationale: Standardized platform services keep the app modular, testable, accessible, and ready for global tutors.

## Implementation Standards
- Package code under `com.barutdev.kora` with separate `data`, `domain`, `ui`, `navigation`, and `di` packages that mirror the Clean Architecture flow.
- Compose modules must define typography, color, and shape tokens in the shared Material 3 theme and reference them rather than raw values.
- Multi-language support requires `strings.xml` mirrors per locale; string keys must be descriptive and stable.
- Persistence modules must expose DAO interfaces returning `Flow` or suspend results, with mapping functions isolating Room entities from domain models.
- Feature specifications must declare expected UseCases, ViewModels, and repositories, including how they integrate with Navigation Compose destinations.

## Delivery Workflow & Compliance
- Implementation plans must map work items to Clean Architecture layers and call out any cross-layer dependencies before coding.
- Code reviews block merges until each principle’s mandates are confirmed via evidence (e.g., Compose previews, coroutines usage, Room entities).
- Automated checks must run `./gradlew assembleDebug` and relevant tests; reviewers verify that new strings exist in `strings.xml`.
- Introduce new dependencies via the Gradle version catalog with justification recorded in the feature specification.
- Compliance retrospectives occur after each feature launch to document adherence, gaps, and required remedial tasks.

## Governance
- This constitution supersedes conflicting documentation; exceptions require an approved feature spec appendix detailing the temporary variance and rollback plan.
- Amendments demand consensus between product and engineering leads, an impact analysis, and updates to plans/specs/tasks templates before ratification.
- Versioning follows semantic rules: MAJOR for principle changes, MINOR for new sections or mandates, PATCH for clarifications.
- Each quarter, maintainers audit the repository for violations (architecture drift, localization gaps, DI misuse) and log remediation tickets.
- Ratified copies must live in `.specify/memory/constitution.md`; runtime guidance docs (README, dev guides) must reference any updates within one iteration.

**Version**: 1.0.0 | **Ratified**: 2025-10-31 | **Last Amended**: 2025-10-31
