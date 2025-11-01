<!-- OPENSPEC:START -->
# OpenSpec Instructions

These instructions are for AI assistants working in this project.

Always open `@/openspec/AGENTS.md` when the request:
- Mentions planning or proposals (words like proposal, spec, change, plan)
- Introduces new capabilities, breaking changes, architecture shifts, or big performance/security work
- Sounds ambiguous and you need the authoritative spec before coding

Use `@/openspec/AGENTS.md` to learn:
- How to create and apply change proposals
- Spec format and conventions
- Project structure and guidelines

Keep this managed block so 'openspec update' can refresh the instructions.

<!-- OPENSPEC:END -->

# AGENTS.md - The Constitution of Project Kora

## 1. Project Goal & Vision
To create a modern, robust, and user-friendly Android application named **Kora** for private tutors to manage students, lessons, homework, and payments. The application, developed by **Barut Dev**, must be built on a professional, scalable architecture with a polished user experience, targeting an eventual Play Store release.

## 2. The Golden Rules (Core Principles)
-   **Principle 1: The Design Elevation Principle:** Elevate the UI from the provided HTML references using modern **Material 3** components. The HTML is a structural blueprint, not a pixel-perfect spec.
-   **Principle 2: Localization First (CRITICAL):** NO hardcoded strings. All user-facing text must be sourced from `strings.xml`.
-   **Principle 3: Professional Architecture:** Strictly adhere to MVVM combined with Clean Architecture principles (UI -> ViewModel -> UseCase -> Repository -> DataSource).
-   **Principle 4: Controlled Commits:** Do NOT commit changes automatically. Await explicit instructions and propose a professional commit message.

## 3. Core Technology Stack
-   **Language:** Kotlin (K2) targeting Java 17.
-   **UI:** Jetpack Compose with **Material 3**.
-   **DI:** Hilt.
-   **Database:** Room (using **KSP**, not KAPT).
-   **Async:** Kotlin Coroutines & Flow.
-   **Navigation:** Jetpack Navigation Compose.

## 4. Gradle & Dependency Management
-   **Version Catalog:** All external dependencies and their versions MUST be defined in `gradle/libs.versions.toml`. No hardcoded versions in `.kts` files.
-   **Required Plugins:** `com.android.application`, `org.jetbrains.kotlin.android`, `dagger.hilt.android.plugin`, `com.google.devtools.ksp`.

## 5. Package & File Structure
Organize all files according to the structure defined for `com.barutdev.kora` (data, di, domain, ui, navigation, util packages).

## 6. UI Implementation Guide: The Source of Truth
-   **Visual Reference Directory:** The sole source of truth is the HTML files in the versioned `screens_reference/` directory.
-   **File Mapping:** Prompts will specify the exact HTML file path for each screen.

## 7. Build, Test & Development Commands
-   Use standard Gradle commands: `./gradlew assembleDebug`, `testDebugUnitTest`, etc.

## 8. Security & Configuration
-   Do not commit secrets. `local.properties` must remain untracked.

## Active Technologies
- Kotlin (K2) targeting JVM 17 + Jetpack Compose Material 3, Kotlin Coroutines & Flow, Hilt, Room (existing) (001-add-reports-screen)
- Room database (existing StudentRepository, LessonRepository) (001-add-reports-screen)
- Kotlin (K2) targeting JDK 17 + Jetpack Compose Material 3, Navigation Compose, Hilt, Kotlin Coroutines/Flow (001-fix-reports-nav)
- Room database (existing, no schema change) (001-fix-reports-nav)
- Kotlin (K2) targeting JVM 17 + Jetpack Compose Material 3, Hilt, Kotlin Coroutines & Flow, Room (KSP), Navigation Compose (004-refactor-reports-entry-point)
- Room database with KSP processors (no KAPT) (004-refactor-reports-entry-point)

## Recent Changes
- 001-add-reports-screen: Added Kotlin (K2) targeting JVM 17 + Jetpack Compose Material 3, Kotlin Coroutines & Flow, Hilt, Room (existing)
