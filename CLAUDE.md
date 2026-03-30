# Homesteader (Steady Hand - Livestock Manager)

Livestock management app for homesteaders, hobby farmers, and small-scale ranchers. Track animals, breeding cycles, health records, production metrics, and reminders across 15+ species.

## Tech Stack

- **Kotlin**: 2.3.0
- **Compose Multiplatform**: 1.10.0
- **SQLDelight**: 2.0.2
- **Koin**: 4.0.0
- **Voyager**: 1.1.0-beta03
- **Kotlinx Serialization**: 1.7.3
- **Kotlinx Coroutines**: 1.9.0
- **Kotlinx DateTime**: 0.6.1
- **UUID**: 0.8.4
- **Firebase (Android)**: BOM 33.7.0 (Analytics, Crashlytics)
- **AdMob**: play-services-ads 23.6.0
- **Google Play Billing**: 7.1.1
- **WorkManager**: 2.10.0 (background tasks)
- **Android**: minSdk 24, targetSdk 36, compileSdk 36
- **Testing**: Roborazzi 1.42.0, Robolectric 4.14.1, Koin Test 4.0.0

## Commands

```bash
# Build
./gradlew :composeApp:assembleDebug
./gradlew :composeApp:assembleRelease    # Requires keystore in local.properties

# Test
./gradlew :composeApp:test

# Screenshot tests
./gradlew :composeApp:recordScreenshots
./gradlew :composeApp:verifyScreenshots
```

## Architecture

MVVM + MVI with Voyager ScreenModels. SQLDelight local persistence (no cloud sync). Koin DI.

```
composeApp/src/commonMain/kotlin/com/markduenas/homesteader/
├── app/di/              # Koin modules: analytics, database, repository, service, viewModel
├── core/
│   ├── designsystem/    # Material3 theme, typography, reusable components
│   ├── analytics/       # AppInsights (Firebase on Android, debug fallback)
│   ├── mvi/             # MVI base classes
│   └── util/            # DateTimeUtil
├── domain/
│   ├── model/           # Animal, AnimalEvent, Reminder, SpeciesConfig
│   ├── service/         # ReminderService, BackupService, CsvImportService, ReportGenerator
│   ├── notification/    # NotificationService (platform-specific)
│   └── monetization/    # AdManager, PremiumManager
├── data/
│   ├── database/        # SQLDelight schemas, drivers, mappers
│   └── repository/      # AnimalRepository, EventRepository, ReminderRepository, SpeciesConfigRepository
├── feature/
│   ├── main/            # MainScreen with tab navigator
│   ├── dashboard/       # Quick stats, recent activity, upcoming tasks
│   ├── animal/          # List, detail, edit screens
│   ├── calendar/        # Month view of events/reminders
│   ├── event/           # Event entry
│   ├── reports/         # Inventory, breeding, health, production reports
│   ├── backup/          # JSON backup/restore, CSV import
│   └── settings/        # Preferences
└── navigation/          # Voyager screen definitions
```

## SQLDelight Database (4 tables)

- **AnimalEntity**: id, tagId, name, species, breed, sex, birthDate, status (active/sold/deceased/transferred), motherId, fatherId, photoUri, customFields (JSON)
- **AnimalEventEntity**: id, animalId, eventType, eventDate, notes, eventData (JSON). Types: Health, Breeding, Production, Weight, Movement, Status changes
- **SpeciesConfigEntity**: species_key, displayName, icon, tracking options, biological params (gestationDays, heatCycleDays, weaningAgeDays), custom field schemas
- **ReminderEntity**: id, animalId, title, reminderType, dueDate, isCompleted, isRecurring, recurrenceIntervalDays, sourceEventId

Schemas at: `composeApp/src/commonMain/sqldelight/`

## Smart Reminders (`ReminderService`)

Auto-generated from events using species-specific biological parameters:
- **Pregnancy check**: 35 days after breeding
- **Birth due**: Based on species gestation period
- **Weaning**: Based on offspring age
- **Next heat**: Based on species cycle length
- **Vaccination follow-ups**: Customizable intervals

Recurring reminders auto-create next occurrence on completion.

## Species Configuration

15 pre-configured species with optimal defaults: Cattle (beef/dairy), Goats (meat/dairy), Sheep, Pigs, Horses, Donkeys, Chickens (layers/broilers), Turkeys, Ducks, Quail, Rabbits, Alpacas, Llamas, Bees.

Per-species: tracking options (breeding, pregnancy, milk, eggs, weight, feed, health), gestation/cycle/weaning parameters, custom field definitions.

## Key Features

- Animal management with lineage tracking (mother/father)
- Event tracking: health (vaccinations, treatments, vet visits, deworming), breeding (heat, mating, pregnancy, births, weaning), production (milk, eggs, shearing), weight, movement, status changes
- Calendar view with month navigation
- Reports: inventory, breeding, health, production, weight trends (CSV export)
- JSON backup/restore of all data, CSV import for bulk animals
- Dashboard with quick stats, recent activity, overdue task badges
- AdMob ads with optional premium to remove

## Key Files

- `domain/service/ReminderService.kt` - Auto-reminder generation from events
- `domain/service/BackupService.kt` - JSON backup/restore
- `domain/service/ReportGenerator.kt` - Analytics reports
- `domain/model/SpeciesConfig.kt` - DefaultSpeciesConfigs with 15+ species
- `domain/model/Animal.kt` - Animal, Sex, AnimalStatus, Species enums
- `domain/model/Event.kt` - AnimalEvent, EventType, EventCategory

## Development Notes

- JSON fields for flexible/extensible data (eventData, customFields, customData)
- Cascading deletes on animal deletion
- Firebase Analytics on Android, debug fallback on iOS
- MVI pattern with State/Intent/Effect in ViewModels
- Versions managed in `gradle/libs.versions.toml`
