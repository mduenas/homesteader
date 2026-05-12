package com.markduenas.homesteader.domain.model

import kotlinx.datetime.LocalDate

/**
 * Types of reports available in the app
 */
enum class ReportType(val displayName: String, val description: String) {
    ANIMAL_INVENTORY("Animal Inventory", "Complete list of all animals with current status"),
    BREEDING_SUMMARY("Breeding Summary", "Breeding events, pregnancies, and births"),
    HEALTH_HISTORY("Health History", "Vaccinations, treatments, and vet visits"),
    PRODUCTION_REPORT("Production Report", "Milk, egg, and fiber production records"),
    WEIGHT_TRACKING("Weight Tracking", "Weight records and growth trends"),
    EVENT_TIMELINE("Event Timeline", "All events in chronological order"),
    SALES_REVENUE("Sales & Revenue", "Revenue from sold and harvested animals"),
    STEER_HARVEST_AVAILABILITY("Steer Harvest Availability", "Active male cattle approaching or ready for harvest")
}

/**
 * Base report data
 */
data class ReportData(
    val reportType: ReportType,
    val title: String,
    val generatedAt: String,
    val dateRange: DateRange?,
    val summary: ReportSummary,
    val rows: List<ReportRow>
)

data class DateRange(
    val startDate: LocalDate,
    val endDate: LocalDate
)

data class ReportSummary(
    val items: List<SummaryItem>
)

data class SummaryItem(
    val label: String,
    val value: String
)

/**
 * A row in the report table
 */
data class ReportRow(
    val columns: List<String>
)

/**
 * Report column definition
 */
data class ReportColumn(
    val header: String,
    val width: Float = 1f
)

// Specific report data classes

data class AnimalInventoryReport(
    val totalCount: Int,
    val activeCount: Int,
    val soldCount: Int,
    val deceasedCount: Int,
    val bySpecies: Map<Species, Int>,
    val animals: List<AnimalInventoryRow>
)

data class AnimalInventoryRow(
    val id: String,
    val name: String,
    val tagNumber: String?,
    val species: String,
    val sex: String,
    val birthDate: String?,
    val status: String
)

data class BreedingSummaryReport(
    val totalBreedings: Int,
    val confirmedPregnancies: Int,
    val births: Int,
    val totalOffspring: Int,
    val events: List<BreedingEventRow>
)

data class BreedingEventRow(
    val date: String,
    val animalName: String,
    val eventType: String,
    val sireName: String?,
    val offspringCount: Int?,
    val notes: String?
)

data class HealthHistoryReport(
    val totalEvents: Int,
    val vaccinations: Int,
    val treatments: Int,
    val vetVisits: Int,
    val events: List<HealthEventRow>
)

data class HealthEventRow(
    val date: String,
    val animalName: String,
    val eventType: String,
    val medication: String?,
    val veterinarian: String?,
    val notes: String?
)

data class ProductionReport(
    val totalMilkProduction: Double?,
    val totalEggCount: Int?,
    val totalFiberWeight: Double?,
    val records: List<ProductionRow>
)

data class ProductionRow(
    val date: String,
    val animalName: String,
    val productionType: String,
    val quantity: String,
    val notes: String?
)

data class WeightTrackingReport(
    val recordCount: Int,
    val averageWeight: Double?,
    val records: List<WeightRow>
)

data class WeightRow(
    val date: String,
    val animalName: String,
    val weight: String,
    val condition: String?,
    val notes: String?
)
