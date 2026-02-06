package com.markduenas.homesteader.screenshots

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.markduenas.homesteader.core.designsystem.HomesteaderTheme
import com.markduenas.homesteader.domain.model.Animal
import com.markduenas.homesteader.domain.model.AnimalEvent
import com.markduenas.homesteader.domain.model.AnimalStatus
import com.markduenas.homesteader.domain.model.EventType
import com.markduenas.homesteader.domain.model.ReminderType
import com.markduenas.homesteader.domain.model.Sex
import com.markduenas.homesteader.domain.model.Species
import com.markduenas.homesteader.feature.animal.list.AnimalListState
import com.markduenas.homesteader.feature.calendar.CalendarReminder
import com.markduenas.homesteader.feature.calendar.CalendarState
import com.markduenas.homesteader.feature.dashboard.DashboardState
import com.markduenas.homesteader.feature.dashboard.TaskType
import com.markduenas.homesteader.feature.dashboard.UpcomingTask
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Screenshot tests for the Homesteader app.
 *
 * Generates screenshots for:
 * - Android Play Store (Pixel 6 - 1080x2400)
 * - iOS App Store:
 *   - iPhone 6.7" (1290x2796) - iPhone 15 Pro Max
 *   - iPhone 6.5" (1284x2778) - iPhone 14 Plus
 *   - iPhone 5.5" (1242x2208) - iPhone 8 Plus
 *
 * To record screenshots: ./gradlew :screenshots:recordRoborazziDebug
 * To verify screenshots: ./gradlew :screenshots:verifyRoborazziDebug
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ScreenshotTests {

    @get:Rule
    val composeRule = createComposeRule()

    // ==================== Demo Data ====================

    private val demoAnimals = listOf(
        Animal(
            id = "1",
            name = "Bella",
            tagId = "B-001",
            species = Species.CATTLE_BEEF,
            breed = "Angus",
            sex = Sex.FEMALE,
            birthDate = LocalDate(2022, 3, 15),
            status = AnimalStatus.ACTIVE
        ),
        Animal(
            id = "2",
            name = "Thunder",
            tagId = "H-001",
            species = Species.HORSE,
            breed = "Quarter Horse",
            sex = Sex.MALE,
            birthDate = LocalDate(2020, 6, 20),
            status = AnimalStatus.ACTIVE
        ),
        Animal(
            id = "3",
            name = "Daisy",
            tagId = "G-001",
            species = Species.GOAT_DAIRY,
            breed = "Nubian",
            sex = Sex.FEMALE,
            birthDate = LocalDate(2023, 1, 10),
            status = AnimalStatus.ACTIVE
        ),
        Animal(
            id = "4",
            name = "Woolly",
            tagId = "S-001",
            species = Species.SHEEP,
            breed = "Merino",
            sex = Sex.FEMALE,
            birthDate = LocalDate(2022, 4, 5),
            status = AnimalStatus.ACTIVE
        ),
        Animal(
            id = "5",
            name = "Porky",
            tagId = "P-001",
            species = Species.PIG,
            breed = "Berkshire",
            sex = Sex.MALE,
            birthDate = LocalDate(2023, 8, 12),
            status = AnimalStatus.SOLD
        )
    )

    private val demoEvents = listOf(
        AnimalEvent(
            id = "e1",
            animalId = "1",
            eventType = EventType.VACCINATION,
            eventDate = LocalDate(2024, 1, 15),
            notes = "Annual vaccination completed"
        ),
        AnimalEvent(
            id = "e2",
            animalId = "2",
            eventType = EventType.WEIGHT_RECORD,
            eventDate = LocalDate(2024, 1, 10),
            notes = "Weight: 1100 lbs"
        ),
        AnimalEvent(
            id = "e3",
            animalId = "3",
            eventType = EventType.BRED,
            eventDate = LocalDate(2024, 1, 5),
            notes = "Bred with Buck #42"
        )
    )

    private val demoTasks = listOf(
        UpcomingTask(
            id = "t1",
            title = "Vaccination Due",
            description = "Annual booster shot",
            dueDate = "Jan 25",
            animalId = "1",
            animalName = "Bella",
            taskType = TaskType.VACCINATION_DUE,
            isOverdue = false
        ),
        UpcomingTask(
            id = "t2",
            title = "Pregnancy Check",
            description = "30-day check",
            dueDate = "Jan 28",
            animalId = "3",
            animalName = "Daisy",
            taskType = TaskType.PREGNANCY_CHECK,
            isOverdue = false
        ),
        UpcomingTask(
            id = "t3",
            title = "Hoof Trim Due",
            description = "Quarterly trim",
            dueDate = "Overdue",
            animalId = "2",
            animalName = "Thunder",
            taskType = TaskType.HOOF_TRIM_DUE,
            isOverdue = true
        )
    )

    private val demoCalendarReminders = listOf(
        CalendarReminder(
            id = "r1",
            title = "Vaccination - Bella",
            description = "Annual booster shot",
            dueDate = LocalDate(2024, 1, 25),
            animalId = "1",
            animalName = "Bella",
            reminderType = ReminderType.VACCINATION_DUE,
            isCompleted = false,
            isOverdue = false
        ),
        CalendarReminder(
            id = "r2",
            title = "Pregnancy Check - Daisy",
            description = "30-day check",
            dueDate = LocalDate(2024, 1, 25),
            animalId = "3",
            animalName = "Daisy",
            reminderType = ReminderType.PREGNANCY_CHECK,
            isCompleted = false,
            isOverdue = false
        )
    )

    // ==================== Dashboard State ====================

    private val dashboardState = DashboardState(
        isLoading = false,
        totalAnimals = 12,
        activeAnimals = 10,
        animalsBySpecies = mapOf(
            Species.CATTLE_BEEF to 4,
            Species.HORSE to 2,
            Species.GOAT_DAIRY to 3,
            Species.SHEEP to 2,
            Species.PIG to 1
        ),
        animalsByStatus = mapOf(
            AnimalStatus.ACTIVE to 10,
            AnimalStatus.SOLD to 2,
            AnimalStatus.DECEASED to 0
        ),
        recentAnimals = demoAnimals.take(4),
        recentEvents = demoEvents,
        upcomingTasks = demoTasks,
        overdueCount = 1
    )

    // ==================== Animal List State ====================

    private val animalListState = AnimalListState(
        allAnimals = demoAnimals,
        filteredAnimals = demoAnimals,
        isLoading = false,
        availableSpecies = listOf(
            Species.CATTLE_BEEF,
            Species.HORSE,
            Species.GOAT_DAIRY,
            Species.SHEEP,
            Species.PIG
        )
    )

    // ==================== Calendar State ====================

    private val calendarState = CalendarState(
        isLoading = false,
        selectedDate = LocalDate(2024, 1, 25),
        currentMonth = LocalDate(2024, 1, 1),
        reminders = demoCalendarReminders,
        remindersForSelectedDate = demoCalendarReminders,
        remindersByDate = mapOf(
            LocalDate(2024, 1, 25) to demoCalendarReminders,
            LocalDate(2024, 1, 15) to listOf(demoCalendarReminders[0]),
            LocalDate(2024, 1, 28) to listOf(demoCalendarReminders[1])
        )
    )

    // ==================== Screenshot Wrapper ====================

    @Composable
    private fun ScreenshotWrapper(
        darkTheme: Boolean = false,
        content: @Composable () -> Unit
    ) {
        HomesteaderTheme(darkTheme = darkTheme) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                content()
            }
        }
    }

    // ==================== Android Play Store Screenshots (Pixel 6) ====================

    @Test
    @Config(sdk = [34], qualifiers = RobolectricDeviceQualifiers.Pixel6)
    fun dashboard_pixel6_light() {
        composeRule.setContent {
            ScreenshotWrapper(darkTheme = false) {
                DashboardScreenContent(state = dashboardState)
            }
        }
        composeRule.onRoot().captureRoboImage("screenshots/android/dashboard_light.png")
    }

    @Test
    @Config(sdk = [34], qualifiers = RobolectricDeviceQualifiers.Pixel6)
    fun dashboard_pixel6_dark() {
        composeRule.setContent {
            ScreenshotWrapper(darkTheme = true) {
                DashboardScreenContent(state = dashboardState)
            }
        }
        composeRule.onRoot().captureRoboImage("screenshots/android/dashboard_dark.png")
    }

    @Test
    @Config(sdk = [34], qualifiers = RobolectricDeviceQualifiers.Pixel6)
    fun animalList_pixel6_light() {
        composeRule.setContent {
            ScreenshotWrapper(darkTheme = false) {
                AnimalListScreenContent(state = animalListState)
            }
        }
        composeRule.onRoot().captureRoboImage("screenshots/android/animal_list_light.png")
    }

    @Test
    @Config(sdk = [34], qualifiers = RobolectricDeviceQualifiers.Pixel6)
    fun animalList_pixel6_dark() {
        composeRule.setContent {
            ScreenshotWrapper(darkTheme = true) {
                AnimalListScreenContent(state = animalListState)
            }
        }
        composeRule.onRoot().captureRoboImage("screenshots/android/animal_list_dark.png")
    }

    @Test
    @Config(sdk = [34], qualifiers = RobolectricDeviceQualifiers.Pixel6)
    fun calendar_pixel6_light() {
        composeRule.setContent {
            ScreenshotWrapper(darkTheme = false) {
                CalendarScreenContent(state = calendarState)
            }
        }
        composeRule.onRoot().captureRoboImage("screenshots/android/calendar_light.png")
    }

    @Test
    @Config(sdk = [34], qualifiers = RobolectricDeviceQualifiers.Pixel6)
    fun calendar_pixel6_dark() {
        composeRule.setContent {
            ScreenshotWrapper(darkTheme = true) {
                CalendarScreenContent(state = calendarState)
            }
        }
        composeRule.onRoot().captureRoboImage("screenshots/android/calendar_dark.png")
    }

    // ==================== iOS App Store Screenshots (6.7" - 1290x2796) ====================
    // iPhone 15 Pro Max, 14 Pro Max: 1290/3 = 430dp, 2796/3 = 932dp

    @Test
    @Config(sdk = [34], qualifiers = "w430dp-h932dp-xxhdpi")
    fun dashboard_iphone67_light() {
        composeRule.setContent {
            ScreenshotWrapper(darkTheme = false) {
                DashboardScreenContent(state = dashboardState)
            }
        }
        composeRule.onRoot().captureRoboImage("screenshots/ios/iphone67/dashboard_light.png")
    }

    @Test
    @Config(sdk = [34], qualifiers = "w430dp-h932dp-xxhdpi")
    fun dashboard_iphone67_dark() {
        composeRule.setContent {
            ScreenshotWrapper(darkTheme = true) {
                DashboardScreenContent(state = dashboardState)
            }
        }
        composeRule.onRoot().captureRoboImage("screenshots/ios/iphone67/dashboard_dark.png")
    }

    @Test
    @Config(sdk = [34], qualifiers = "w430dp-h932dp-xxhdpi")
    fun animalList_iphone67_light() {
        composeRule.setContent {
            ScreenshotWrapper(darkTheme = false) {
                AnimalListScreenContent(state = animalListState)
            }
        }
        composeRule.onRoot().captureRoboImage("screenshots/ios/iphone67/animal_list_light.png")
    }

    @Test
    @Config(sdk = [34], qualifiers = "w430dp-h932dp-xxhdpi")
    fun animalList_iphone67_dark() {
        composeRule.setContent {
            ScreenshotWrapper(darkTheme = true) {
                AnimalListScreenContent(state = animalListState)
            }
        }
        composeRule.onRoot().captureRoboImage("screenshots/ios/iphone67/animal_list_dark.png")
    }

    @Test
    @Config(sdk = [34], qualifiers = "w430dp-h932dp-xxhdpi")
    fun calendar_iphone67_light() {
        composeRule.setContent {
            ScreenshotWrapper(darkTheme = false) {
                CalendarScreenContent(state = calendarState)
            }
        }
        composeRule.onRoot().captureRoboImage("screenshots/ios/iphone67/calendar_light.png")
    }

    @Test
    @Config(sdk = [34], qualifiers = "w430dp-h932dp-xxhdpi")
    fun calendar_iphone67_dark() {
        composeRule.setContent {
            ScreenshotWrapper(darkTheme = true) {
                CalendarScreenContent(state = calendarState)
            }
        }
        composeRule.onRoot().captureRoboImage("screenshots/ios/iphone67/calendar_dark.png")
    }

    // ==================== iOS App Store Screenshots (6.5" - 1284x2778) ====================
    // iPhone 14 Plus, 13 Pro Max, 12 Pro Max, 11 Pro Max: 1284/3 = 428dp, 2778/3 = 926dp

    @Test
    @Config(sdk = [34], qualifiers = "w428dp-h926dp-xxhdpi")
    fun dashboard_iphone65_light() {
        composeRule.setContent {
            ScreenshotWrapper(darkTheme = false) {
                DashboardScreenContent(state = dashboardState)
            }
        }
        composeRule.onRoot().captureRoboImage("screenshots/ios/iphone65/dashboard_light.png")
    }

    @Test
    @Config(sdk = [34], qualifiers = "w428dp-h926dp-xxhdpi")
    fun dashboard_iphone65_dark() {
        composeRule.setContent {
            ScreenshotWrapper(darkTheme = true) {
                DashboardScreenContent(state = dashboardState)
            }
        }
        composeRule.onRoot().captureRoboImage("screenshots/ios/iphone65/dashboard_dark.png")
    }

    @Test
    @Config(sdk = [34], qualifiers = "w428dp-h926dp-xxhdpi")
    fun animalList_iphone65_light() {
        composeRule.setContent {
            ScreenshotWrapper(darkTheme = false) {
                AnimalListScreenContent(state = animalListState)
            }
        }
        composeRule.onRoot().captureRoboImage("screenshots/ios/iphone65/animal_list_light.png")
    }

    @Test
    @Config(sdk = [34], qualifiers = "w428dp-h926dp-xxhdpi")
    fun animalList_iphone65_dark() {
        composeRule.setContent {
            ScreenshotWrapper(darkTheme = true) {
                AnimalListScreenContent(state = animalListState)
            }
        }
        composeRule.onRoot().captureRoboImage("screenshots/ios/iphone65/animal_list_dark.png")
    }

    @Test
    @Config(sdk = [34], qualifiers = "w428dp-h926dp-xxhdpi")
    fun calendar_iphone65_light() {
        composeRule.setContent {
            ScreenshotWrapper(darkTheme = false) {
                CalendarScreenContent(state = calendarState)
            }
        }
        composeRule.onRoot().captureRoboImage("screenshots/ios/iphone65/calendar_light.png")
    }

    @Test
    @Config(sdk = [34], qualifiers = "w428dp-h926dp-xxhdpi")
    fun calendar_iphone65_dark() {
        composeRule.setContent {
            ScreenshotWrapper(darkTheme = true) {
                CalendarScreenContent(state = calendarState)
            }
        }
        composeRule.onRoot().captureRoboImage("screenshots/ios/iphone65/calendar_dark.png")
    }

    // ==================== iOS App Store Screenshots (5.5" - 1242x2208) ====================
    // iPhone 8 Plus, 7 Plus, 6s Plus: 1242/3 = 414dp, 2208/3 = 736dp

    @Test
    @Config(sdk = [34], qualifiers = "w414dp-h736dp-xxhdpi")
    fun dashboard_iphone55_light() {
        composeRule.setContent {
            ScreenshotWrapper(darkTheme = false) {
                DashboardScreenContent(state = dashboardState)
            }
        }
        composeRule.onRoot().captureRoboImage("screenshots/ios/iphone55/dashboard_light.png")
    }

    @Test
    @Config(sdk = [34], qualifiers = "w414dp-h736dp-xxhdpi")
    fun dashboard_iphone55_dark() {
        composeRule.setContent {
            ScreenshotWrapper(darkTheme = true) {
                DashboardScreenContent(state = dashboardState)
            }
        }
        composeRule.onRoot().captureRoboImage("screenshots/ios/iphone55/dashboard_dark.png")
    }

    @Test
    @Config(sdk = [34], qualifiers = "w414dp-h736dp-xxhdpi")
    fun animalList_iphone55_light() {
        composeRule.setContent {
            ScreenshotWrapper(darkTheme = false) {
                AnimalListScreenContent(state = animalListState)
            }
        }
        composeRule.onRoot().captureRoboImage("screenshots/ios/iphone55/animal_list_light.png")
    }

    @Test
    @Config(sdk = [34], qualifiers = "w414dp-h736dp-xxhdpi")
    fun animalList_iphone55_dark() {
        composeRule.setContent {
            ScreenshotWrapper(darkTheme = true) {
                AnimalListScreenContent(state = animalListState)
            }
        }
        composeRule.onRoot().captureRoboImage("screenshots/ios/iphone55/animal_list_dark.png")
    }

    @Test
    @Config(sdk = [34], qualifiers = "w414dp-h736dp-xxhdpi")
    fun calendar_iphone55_light() {
        composeRule.setContent {
            ScreenshotWrapper(darkTheme = false) {
                CalendarScreenContent(state = calendarState)
            }
        }
        composeRule.onRoot().captureRoboImage("screenshots/ios/iphone55/calendar_light.png")
    }

    @Test
    @Config(sdk = [34], qualifiers = "w414dp-h736dp-xxhdpi")
    fun calendar_iphone55_dark() {
        composeRule.setContent {
            ScreenshotWrapper(darkTheme = true) {
                CalendarScreenContent(state = calendarState)
            }
        }
        composeRule.onRoot().captureRoboImage("screenshots/ios/iphone55/calendar_dark.png")
    }
}
