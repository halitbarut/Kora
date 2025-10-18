# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Project Overview
**Kora** is an Android application for private tutors to manage students, lessons, homework, and payments. Developed by **Barut Dev**, the app targets Play Store release with professional architecture and Material 3 design.

## Architecture
The project follows **MVVM + Clean Architecture**:
```
UI Layer (Compose) → ViewModel → UseCase → Repository → DataSource (Room)
```

### Package Structure
- `com.barutdev.kora/`
  - `data/` - Data layer (repositories, data sources, DAOs, entities)
  - `di/` - Dependency injection modules (Hilt)
  - `domain/` - Business logic (use cases, models, repository interfaces)
  - `ui/` - Presentation layer (screens, components, theme, ViewModels)
  - `navigation/` - Navigation graph and routing
  - `notifications/` - Notification handling
  - `util/` - Utility classes and extensions

## Build Commands

### Core Development
```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Clean build
./gradlew clean

# Run all unit tests
./gradlew testDebugUnitTest

# Run specific test class
./gradlew testDebugUnitTest --tests "com.barutdev.kora.YourTestClass"

# Run Android instrumented tests
./gradlew connectedAndroidTest
```

### Code Quality
```bash
# Lint check
./gradlew lint

# Lint with auto-fix (if configured)
./gradlew lintFix

# Check Kotlin code style (if ktlint is configured)
./gradlew ktlintCheck

# Format Kotlin code (if ktlint is configured)
./gradlew ktlintFormat
```

## Technology Stack

### Core Technologies
- **Language**: Kotlin (K2) targeting Java 17
- **UI Framework**: Jetpack Compose with Material 3
- **Dependency Injection**: Hilt
- **Database**: Room with KSP (not KAPT)
- **Async**: Kotlin Coroutines & Flow
- **Navigation**: Jetpack Navigation Compose

### Dependency Management
All dependencies are managed through **version catalog** in `gradle/libs.versions.toml`. Never hardcode versions directly in `.kts` files.

## Critical Development Rules

### Localization
**NO hardcoded strings** - All user-facing text must be in `strings.xml`. This is critical for Play Store release.

### UI Implementation
- HTML reference files in `screens_reference/` directory serve as structural blueprints
- Elevate designs using Material 3 components, not pixel-perfect replication
- Use Material 3 theming system for consistency

### API Keys & Secrets
- Secrets stored in `local.properties` (never commit)
- Access via BuildConfig: `BuildConfig.GEMINI_API_KEY`

### Commit Policy
**Never auto-commit**. Always await explicit user instructions and propose professional commit messages.

## Data Flow Pattern
1. **UI Event** → ViewModel receives user action
2. **ViewModel** → Calls UseCase with required parameters
3. **UseCase** → Orchestrates business logic, calls Repository
4. **Repository** → Coordinates between data sources (Room, Remote APIs)
5. **DataSource** → Performs actual data operations
6. **Flow/State** → Data flows back up through reactive streams

## Testing Strategy
- Unit tests for ViewModels, UseCases, and Repositories
- Instrumented tests for Room DAOs and UI components
- Use `kotlinx-coroutines-test` for testing coroutines
- Mock dependencies using Hilt's testing modules

## Room Database Guidelines
- Use KSP for code generation (configured in `app/build.gradle.kts`)
- Define entities with proper indices for query performance
- Use Flow for observing data changes
- Implement migrations for schema changes

## Navigation
- Single Activity architecture with Compose Navigation
- Define routes as sealed classes or objects
- Use `hilt-navigation-compose` for ViewModel injection in composables