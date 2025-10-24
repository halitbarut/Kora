# Project Context

## Purpose
Kora is a privacy-first Android assistant that helps private tutors manage students, lessons, homework, payments, and AI-guided insights in a single polished experience. The goal is to ship a Play Store-ready app with professional UX, modern architecture, and localized content out of the box.

## Tech Stack
- Kotlin 2.0.21 (K2) targeting Java 17
- Jetpack Compose UI with Material 3 components and theming
- Hilt for dependency injection across UI, domain, and data layers
- Room (with KSP) for local persistence plus DataStore for preferences
- Kotlin Coroutines & Flow for async work and reactive state
- Jetpack Navigation Compose for in-app routing
- Google Gemini API for AI-powered lesson insights

## Project Conventions

### Code Style
- Follow Kotlin and Jetpack Compose style guides; prefer expressive Compose modifiers and theming helpers over ad-hoc styling.
- Keep all user-facing strings in `res/values*/strings.xml`; update localized variants (`values`, `values-tr`, `values-de`) whenever text changes.
- Drive dependencies through the version catalog (`gradle/libs.versions.toml`) and avoid hardcoded versions in Gradle scripts.
- Organize packages under `com.barutdev.kora` by layer (`data`, `domain`, `ui`, `navigation`, `notifications`, `di`, `util`) to mirror the Clean Architecture flow.

### Architecture Patterns
- MVVM with Clean Architecture layering: UI (Compose) → ViewModel → UseCase → Repository → DataSource.
- Single-activity navigation graph (`KoraNavGraph`) coordinates screen routes and injects ViewModels using Hilt.
- Room entities/DAOs back repositories; use cases expose suspend/Flow APIs to ViewModels that map into UI state.
- Notifications, DataStore preferences, and AI integrations have dedicated modules to keep cross-cutting concerns isolated.

### Testing Strategy
- Unit-test ViewModels, UseCases, repositories, and utility classes with `kotlinx-coroutines-test` and JUnit.
- Cover Room DAOs and navigation flows with instrumented tests (`./gradlew connectedAndroidTest`) as features stabilize.
- Compose UI tests rely on the Compose testing APIs bundled in the BOM; prefer scenario-focused tests over snapshot baselines.
- Run `./gradlew testDebugUnitTest` and lint tasks (`lint`, `ktlintCheck` when configured) before handoff.

### Git Workflow
- Work on short-lived feature branches off `main`; open pull requests for review before merging.
- Do not auto-commit; share a proposed, professional commit message once changes are ready and await approval.
- Keep commits scoped to a single concern and reference related specs or tasks when helpful.

## Domain Context
- Target users are private tutors managing recurring lessons, student progress, homework assignments, and payments.
- The app differentiates between upcoming, completed, and payment-pending sessions, surfacing reminders and insights to help tutors prepare.
- Gemini-powered recommendations analyze lesson notes and homework outcomes to suggest focus areas for future sessions.
- Data portability (CSV export/import) and on-device storage are key selling points for privacy-conscious educators.

## Important Constraints
- Localization-first: never hardcode text or currency symbols; rely on string resources and locale-aware formatting.
- UI must elevate HTML references using Material 3 patterns rather than copying layouts verbatim.
- Secrets such as `GEMINI_API_KEY` stay in `local.properties`; never commit credentials.
- Maintain offline-first behavior; changes to data flows should preserve Room-backed persistence and notification scheduling.
- Respect Clean Architecture boundaries and keep new dependencies registered in Hilt modules.

## External Dependencies
- Google Gemini Generative AI API (requires runtime API key for AI insights).
- Android Jetpack libraries: Compose BOM, Material3, Room, Navigation, DataStore, Lifecycle components.
- Kotlinx Coroutines & Flow for concurrency and reactive streams.
