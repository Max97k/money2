# AGENTS.md — AI Agent Guidance for money2

> **Repository:** `Max97k/money2`  
> **Default Branch:** `main`  
> **Primary Technology Stack:** Kotlin 2.0.20+, Jetpack Compose Material 3, Koin DI, Room Database (KSP) + SQLCipher / EncryptedSharedPreferences, Retrofit 2 / OkHttp3, Android SDK (compileSdk 37, minSdk 26, targetSdk 37)  
> **Visibility:** Public  

---

## 1. Project Overview & Architecture

### 1.1 Purpose & Mission
money2 is a modern, privacy-first personal investment portfolio manager and net worth tracker for Android. Built with a local-first philosophy, it enables users to track multi-currency cash, stocks, cryptocurrencies, and precious metals with real-time market data quotes, historical performance analytics, and asset allocation breakdowns without transmitting personal financial records to third-party servers.

### 1.2 System Architecture & Component Diagram
The project strictly implements Clean Architecture combined with MVI/MVVM presentation patterns:
1. **Presentation Layer (`presentation/`)**:
   - UI Screens: `DashboardScreen.kt`, `HoldingsScreen.kt`, `HoldingDetailScreen.kt`, `SettingsScreen.kt`.
   - UI Components: Jetpack Compose Material 3 components, `DynamicBackground.kt` (interactive gradient rendering), `SpotlightTour.kt` (interactive onboarding guides).
   - ViewModels: `DashboardViewModel.kt`, `HoldingsViewModel.kt`, `HoldingDetailViewModel.kt`, `SettingsViewModel.kt` exposing immutable `StateFlow` UI state models.
2. **Domain Layer (`domain/`)**:
   - Pure Kotlin business logic with zero Android framework dependencies.
   - Use Cases: `AddHoldingUseCase.kt`, `GetDashboardStatsUseCase.kt`, `GetHoldingsUseCase.kt`.
   - Domain Models: `Holding.kt`, `HoldingTransaction.kt`.
   - Repository Interfaces: `HoldingRepository.kt`, `MarketRepository.kt`.
3. **Data Layer (`data/`)**:
   - Local Persistence: Room Database (`AppDatabase.kt`, `HoldingDao.kt`), Entities (`HoldingEntity.kt`, `HoldingTransactionEntity.kt`), Mappers (`HoldingMapper.kt`).
   - Secure Storage: `EncryptedPrefs.kt` backed by Android Keystore / `EncryptedSharedPreferences`.
   - Remote Data: Retrofit 2 interfaces (`MarketApi.kt`), DTOs (`FinnhubQuoteResponse.kt`, `ExchangeRateResponse.kt`, `GlobalQuoteDto.kt`), and repository implementations (`HoldingRepositoryImpl.kt`, `MarketRepositoryImpl.kt`).
4. **Dependency Injection (`di/`)**:
   - Koin DI modules defined in `AppModule.kt` for singletons, database instances, repositories, use cases, and ViewModels.

### 1.3 Key File & Directory Map
| Path | Purpose / Description |
|---|---|
| `app/build.gradle.kts` | App module build script, SDK targets (minSdk 26, targetSdk 37), KSP, and dependencies |
| `gradle/libs.versions.toml` | Gradle Version Catalog managing Kotlin, Compose M3, Room, Koin, and Retrofit |
| `app/src/main/java/com/example/money2/MoneyApp.kt` | Application class initializing Koin dependency injection container |
| `app/src/main/java/com/example/money2/MainActivity.kt` | Root Activity hosting Jetpack Compose navigation graph (`MoneyNavGraph.kt`) |
| `app/src/main/java/com/example/money2/presentation/` | Compose UI screens, components, themes, and ViewModels |
| `app/src/main/java/com/example/money2/domain/` | Clean Architecture domain layer (use cases, domain models, repository contracts) |
| `app/src/main/java/com/example/money2/data/` | Data layer (Room database, DAOs, entities, remote APIs, encrypted prefs) |
| `app/src/main/java/com/example/money2/di/` | Koin dependency injection definitions (`AppModule.kt`) |
| `app/src/test/java/com/example/money2/` | Unit test suite and performance benchmarks (`AddHoldingUseCaseTest.kt`, `HoldingsViewModelBenchmark.kt`) |
| `play/release-notes/whatsnew-zh-TW` | Google Play Store release notes file in Traditional Chinese (zh-TW) |
| `.github/workflows/` | GitHub Actions CI/CD workflows (`ci.yml`, `release.yml`) |

---

## 2. Development, Build & Verification Commands

### 2.1 Prerequisites & Environment Setup
- **JDK**: Java Development Kit 17 (or JDK 21).
- **Android SDK**: Android SDK Platform 37, Build Tools 34.0.0+.
- **Gradle**: Gradle 8.5+ with Kotlin DSL.

### 2.2 Build & Compilation Commands
```bash```
# Build Debug APK
./gradlew assembleDebug

# Build Release App Bundle (AAB for Google Play Store)
./gradlew bundleRelease

# Build Release APK
./gradlew assembleRelease
``````

### 2.3 Verification & Testing Suite
```bash```
# Run all unit tests and performance benchmarks
./gradlew testDebugUnitTest

# Run Android Lint analysis
./gradlew lint

# Run instrumentation tests (connected device or emulator)
./gradlew connectedDebugAndroidTest
``````

### 2.4 Clean & Reset
```bash```
# Clean Gradle cache and build outputs
./gradlew clean
``````

---

## 3. Coding Standards & Conventions

### 3.1 Code Style & Layer Isolation
- **Clean Architecture Boundaries**: The `domain/` layer MUST remain pure Kotlin with no dependencies on `android.*` or `androidx.*` classes.
- **Data Mapping**: Always map database entities and network DTOs to pure domain models via `HoldingMapper.kt` before exposing them to domain use cases or presentation ViewModels.
- **Compose Material 3**: UI components must use Material 3 (`androidx.compose.material3`). Keep Composables stateless and pass state down via immutable data models.

### 3.2 State Management & Concurrency
- Use Kotlin Coroutines and `StateFlow` for reactive state streams from repositories to ViewModels.
- Database access and network requests must run on `Dispatchers.IO`.

---

## 4. Safety, Security & Resource Constraints

### 4.1 Privacy-First & Local Storage Security
- **Local-First Data Storage**: All sensitive financial records, asset holdings, and transaction history are stored strictly on-device in Room.
- **Encrypted Preferences**: Sensitive tokens and preferences are protected with hardware-backed Keystore encryption via `EncryptedSharedPreferences`.
- **Zero Third-Party Telemetry**: Never integrate analytics SDKs or send user transaction data to remote servers.

### 4.2 Secrets & Proxy Configuration
- API keys (e.g. Finnhub, AlphaVantage) and `PROXY_SECRET` must be injected via `local.properties` or environment variables and NEVER committed into version control.

---

## 5. Git & Release Workflow

### 5.1 Branch & Commit Conventions
- **Target Default Branch**: `main`
- **Commit Format**: Conventional commits (`feat:`, `fix:`, `refactor:`, `docs:`, `test:`, `chore:`).

---

### 5.2 🔒 Mandatory Automated Google Play Release Notes Rule (What is New)
*(Preserved and enforced from `.agents/AGENTS.md`)*

When the user asks to "prepare a release", "create a new release", or "tag a new version":
1. **Do NOT immediately tag and push.**
2. First, analyze the recent Git commits (`git log` from the last tag to `HEAD`).
3. Summarize the technical commits into a highly polished, user-friendly "What is New" release note in **Traditional Chinese (zh-TW)**. It should be suitable for general users to read on the Google Play Store.
4. Save this release note to the file: `play/release-notes/whatsnew-zh-TW` (overwrite it if it exists).  
   *Note: The filename must be exactly `whatsnew-zh-TW` without any file extension.*
5. Create the directory `play/release-notes/` if it does not exist.
6. Commit the `play/release-notes/whatsnew-zh-TW` file to the repository.
7. Only after the commit is successful, create the Git tag (e.g., `v1.2.0`) and push both the commit and the tag to GitHub to trigger the release workflow.
