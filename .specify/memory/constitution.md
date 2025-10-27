<!--
Sync Impact Report
Version: N/A -> 1.0.0
Modified Principles:
- NEW -> I. Layered MVVM Clean Flow
- NEW -> II. Compose Material 3 Experience
- NEW -> III. Kotlin Coroutines Discipline
- NEW -> IV. Room Persistence & Hilt Wiring
- NEW -> V. Localized Navigation Integrity
Added Sections:
- Platform Standards
- Development Workflow
Removed Sections: None
Templates requiring updates:
- .specify/templates/plan-template.md ✅ updated
- .specify/templates/spec-template.md ✅ no change required
- .specify/templates/tasks-template.md ✅ updated
Follow-up TODOs: None
-->
# Kora Constitution

## Core Principles

### I. Layered MVVM Clean Flow
- Features MUST follow the UI → ViewModel → UseCase → Repository → DataSource pipeline with no cross-layer coupling.
- ViewModels expose immutable UI state via Kotlin `StateFlow`/`Flow` and never reference Android Views or repositories directly.
- UseCases encapsulate business rules, remain side-effect free, and receive dependencies through the Hilt graph.
- Repositories coordinate domain models and map between use cases and data sources; data sources isolate Room, network, and platform APIs.
- Any exception to the layering requires a constitution amendment approved before implementation.

Rationale: Enforces separation of concerns that keeps the codebase testable, maintainable, and ready for scale.

### II. Compose Material 3 Experience
- All UI MUST be authored in Jetpack Compose with Material 3 components, theming, and design tokens.
- XML layouts, legacy view hierarchies, and Material 2 widgets are prohibited in production modules.
- Theming derives from a single `MaterialTheme` definition covering color, typography, and shape; custom components must honour Material 3 elevation and motion.
- UI previews and design reviews evaluate Compose implementations only.

Rationale: Guarantees a cohesive, modern Android experience aligned with the project's design elevation principle.

### III. Kotlin Coroutines Discipline
- 100% of production code is written in Kotlin targeting JVM 17 with the K2 compiler pipeline.
- Asynchronous work MUST use Kotlin Coroutines and Flow; callbacks, RxJava, or threading primitives are not permitted without prior amendment.
- Coroutine scopes leverage structured concurrency (e.g., `viewModelScope`, `lifecycleScope`); `GlobalScope` and unmanaged jobs are disallowed.
- Domain models exposed to coroutines remain immutable to support predictable state updates.

Rationale: Ensures language-level consistency, modern concurrency, and deterministic behaviour.

### IV. Room Persistence & Hilt Wiring
- Local persistence is exclusively implemented with Room and KSP; direct SQLite helpers or alternative ORMs are forbidden.
- Room entities, DAOs, and migrations must cover every persisted model, with migration tests delivered alongside schema changes.
- Hilt is the only dependency injection framework; manual service locators or alternative DI containers are not allowed.
- The Hilt graph provides Room databases, DAOs, repositories, use cases, and any coroutine dispatchers required for the architecture.

Rationale: Provides a standardised infrastructure layer that is observable, testable, and easy to evolve.

### V. Localized Navigation Integrity
- All user-facing strings reside in `res/values/strings.xml` (and locale variants); code and Compose previews reference string resources only.
- Navigation is managed via Jetpack Navigation Compose with typed routes and strongly-typed arguments.
- Domain and UI state models are immutable Kotlin data classes; mutations occur by creating new instances.
- Feature plans MUST document localization impacts and navigation destinations before implementation.

Rationale: Protects global readiness, navigation coherence, and state integrity.

## Platform Standards
- Tooling: Android Studio (latest stable), Gradle with version catalogs, Kotlin K2 compiler, and JDK 17 are mandatory.
- Modules follow the `com.barutdev.kora` package structure, keeping domain, data, and UI layers in separate packages per Clean Architecture.
- `./gradlew assembleDebug`, `testDebugUnitTest`, and relevant instrumented tests MUST pass in CI before merge.
- Feature modules and build logic MUST respect the Compose Material 3 theme and contribute previews for primary screens.

## Development Workflow
- Specifications and plans document how work respects each principle, with explicit references to architecture layering, Compose UI, Room, Hilt, and localization impacts.
- Code reviews block changes that break the constitutional rules; reviewers cite the violated clause when requesting corrections.
- Every feature branch includes string resource updates, navigation graph entries, immutable model definitions, and Room schema updates as needed.
- Quarterly audits confirm DI graph integrity, Room migrations, coroutine usage, and localization coverage; findings trigger corrective tasks.

## Governance
- Amendments require a written proposal, impact analysis, and approval from the project lead and design lead before merge.
- Constitution versions follow semantic versioning: MAJOR for breaking governance changes, MINOR for new principles or sections, PATCH for clarifications.
- Meeting notes for amendment approvals and quarterly audits are stored in `/docs/governance/`.
- Compliance reviews are mandatory during PRs and release candidates to ensure no principle regressions; violations halt release until resolved.

**Version**: 1.0.0 | **Ratified**: 2025-10-27 | **Last Amended**: 2025-10-27
