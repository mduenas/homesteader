package com.markduenas.homesteader.domain.service

import com.markduenas.homesteader.core.util.DateTimeUtil
import com.markduenas.homesteader.core.util.formatDecimal
import com.markduenas.homesteader.data.repository.AnimalRepository
import com.markduenas.homesteader.data.repository.EventRepository
import com.markduenas.homesteader.domain.model.AnimalInventoryReport
import com.markduenas.homesteader.domain.model.AnimalInventoryRow
import com.markduenas.homesteader.domain.model.AnimalStatus
import com.markduenas.homesteader.domain.model.BreedingEventData
import com.markduenas.homesteader.domain.model.BreedingEventRow
import com.markduenas.homesteader.domain.model.BreedingSummaryReport
import com.markduenas.homesteader.domain.model.DateRange
import com.markduenas.homesteader.domain.model.EventCategory
import com.markduenas.homesteader.domain.model.EventType
import com.markduenas.homesteader.domain.model.HealthEventData
import com.markduenas.homesteader.domain.model.HealthEventRow
import com.markduenas.homesteader.domain.model.HealthHistoryReport
import com.markduenas.homesteader.domain.model.ProductionEventData
import com.markduenas.homesteader.domain.model.ProductionReport
import com.markduenas.homesteader.domain.model.ProductionRow
import com.markduenas.homesteader.domain.model.ReportColumn
import com.markduenas.homesteader.domain.model.ReportData
import com.markduenas.homesteader.domain.model.ReportRow
import com.markduenas.homesteader.domain.model.ReportSummary
import com.markduenas.homesteader.domain.model.ReportType
import com.markduenas.homesteader.domain.model.SummaryItem
import com.markduenas.homesteader.domain.model.HarvestEventData
import com.markduenas.homesteader.domain.model.StatusChangeEventData
import com.markduenas.homesteader.domain.model.WeightEventData
import com.markduenas.homesteader.domain.model.WeightRow
import com.markduenas.homesteader.domain.model.WeightTrackingReport
import com.markduenas.homesteader.domain.model.Sex
import com.markduenas.homesteader.domain.model.Species
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate

class ReportGenerator(
    private val animalRepository: AnimalRepository,
    private val eventRepository: EventRepository
) {

    suspend fun generateReport(
        reportType: ReportType,
        dateRange: DateRange? = null,
        harvestAgeYears: Int = 2
    ): ReportData {
        return when (reportType) {
            ReportType.ANIMAL_INVENTORY -> generateAnimalInventoryReport()
            ReportType.BREEDING_SUMMARY -> generateBreedingSummaryReport(dateRange)
            ReportType.HEALTH_HISTORY -> generateHealthHistoryReport(dateRange)
            ReportType.PRODUCTION_REPORT -> generateProductionReport(dateRange)
            ReportType.WEIGHT_TRACKING -> generateWeightTrackingReport(dateRange)
            ReportType.EVENT_TIMELINE -> generateEventTimelineReport(dateRange)
            ReportType.SALES_REVENUE -> generateSalesRevenueReport(dateRange)
            ReportType.STEER_HARVEST_AVAILABILITY -> generateSteerHarvestAvailabilityReport(harvestAgeYears)
        }
    }

    fun getColumnsForReportType(reportType: ReportType): List<ReportColumn> {
        return when (reportType) {
            ReportType.ANIMAL_INVENTORY -> listOf(
                ReportColumn("Name", 1.5f),
                ReportColumn("Tag #", 1f),
                ReportColumn("Species", 1f),
                ReportColumn("Sex", 0.5f),
                ReportColumn("Birth Date", 1f),
                ReportColumn("Status", 0.8f)
            )
            ReportType.BREEDING_SUMMARY -> listOf(
                ReportColumn("Date", 1f),
                ReportColumn("Animal", 1.5f),
                ReportColumn("Event", 1f),
                ReportColumn("Sire", 1f),
                ReportColumn("Offspring", 0.8f),
                ReportColumn("Notes", 2f)
            )
            ReportType.HEALTH_HISTORY -> listOf(
                ReportColumn("Date", 1f),
                ReportColumn("Animal", 1.5f),
                ReportColumn("Type", 1f),
                ReportColumn("Medication", 1.2f),
                ReportColumn("Vet", 1f),
                ReportColumn("Notes", 2f)
            )
            ReportType.PRODUCTION_REPORT -> listOf(
                ReportColumn("Date", 1f),
                ReportColumn("Animal", 1.5f),
                ReportColumn("Type", 1f),
                ReportColumn("Quantity", 1f),
                ReportColumn("Notes", 2f)
            )
            ReportType.WEIGHT_TRACKING -> listOf(
                ReportColumn("Date", 1f),
                ReportColumn("Animal", 1.5f),
                ReportColumn("Weight", 1f),
                ReportColumn("Condition", 1f),
                ReportColumn("Notes", 2f)
            )
            ReportType.EVENT_TIMELINE -> listOf(
                ReportColumn("Date", 1f),
                ReportColumn("Animal", 1.5f),
                ReportColumn("Event Type", 1.2f),
                ReportColumn("Details", 2f)
            )
            ReportType.SALES_REVENUE -> listOf(
                ReportColumn("Date", 1f),
                ReportColumn("Animal", 1.2f),
                ReportColumn("Species", 1f),
                ReportColumn("Age", 0.8f),
                ReportColumn("Type", 0.8f),
                ReportColumn("Live Wt", 0.7f),
                ReportColumn("Dressed Wt", 0.8f),
                ReportColumn("Gross Rev", 0.9f),
                ReportColumn("Processing", 0.9f),
                ReportColumn("Net Rev", 0.9f),
                ReportColumn("$/lb", 0.7f),
                ReportColumn("Buyer", 1f)
            )
            ReportType.STEER_HARVEST_AVAILABILITY -> listOf(
                ReportColumn("Name", 1.5f),
                ReportColumn("Tag #", 1f),
                ReportColumn("Breed", 1f),
                ReportColumn("Birth Date", 1f),
                ReportColumn("Age", 0.8f),
                ReportColumn("Harvest Date", 1f),
                ReportColumn("Status", 1.2f)
            )
        }
    }

    private suspend fun generateAnimalInventoryReport(): ReportData {
        val animals = animalRepository.getAllAnimals().first()

        val bySpecies = animals.groupBy { it.species }.mapValues { it.value.size }
        val activeCount = animals.count { it.status == AnimalStatus.ACTIVE }
        val soldCount = animals.count { it.status == AnimalStatus.SOLD }
        val deceasedCount = animals.count { it.status == AnimalStatus.DECEASED }

        val rows = animals.map { animal ->
            ReportRow(
                listOf(
                    animal.displayName,
                    animal.tagId ?: "-",
                    animal.species.displayName,
                    animal.sex.displayName,
                    animal.birthDate?.toString() ?: "-",
                    animal.status.displayName
                )
            )
        }

        val summaryItems = mutableListOf(
            SummaryItem("Total Animals", animals.size.toString()),
            SummaryItem("Active", activeCount.toString()),
            SummaryItem("Sold", soldCount.toString()),
            SummaryItem("Deceased", deceasedCount.toString())
        )

        bySpecies.forEach { (species, count) ->
            summaryItems.add(SummaryItem(species.displayName, count.toString()))
        }

        return ReportData(
            reportType = ReportType.ANIMAL_INVENTORY,
            title = "Animal Inventory Report",
            generatedAt = DateTimeUtil.nowIsoString(),
            dateRange = null,
            summary = ReportSummary(summaryItems),
            rows = rows
        )
    }

    private suspend fun generateBreedingSummaryReport(dateRange: DateRange?): ReportData {
        val animals = animalRepository.getAllAnimals().first()
        val animalsById = animals.associateBy { it.id }

        val allEvents = if (dateRange != null) {
            eventRepository.getEventsByDateRange(dateRange.startDate, dateRange.endDate).first()
        } else {
            eventRepository.getRecentEvents(1000).first()
        }

        val breedingEvents = allEvents.filter { it.eventType.category == EventCategory.BREEDING }

        val bredEvents = breedingEvents.count { it.eventType == EventType.BRED }
        val pregnancyChecks = breedingEvents.count { it.eventType == EventType.PREGNANCY_CHECK }
        val births = breedingEvents.count { it.eventType == EventType.BIRTH }

        var totalOffspring = 0
        breedingEvents.forEach { event ->
            if (event.eventType == EventType.BIRTH) {
                val data = event.eventData as? BreedingEventData
                totalOffspring += data?.offspringCount ?: 0
            }
        }

        val rows = breedingEvents.map { event ->
            val animalName = animalsById[event.animalId]?.displayName ?: "Unknown"
            val data = event.eventData as? BreedingEventData

            ReportRow(
                listOf(
                    event.eventDate.toString(),
                    animalName,
                    event.eventType.displayName,
                    data?.sireName ?: "-",
                    data?.offspringCount?.toString() ?: "-",
                    event.notes ?: "-"
                )
            )
        }

        return ReportData(
            reportType = ReportType.BREEDING_SUMMARY,
            title = "Breeding Summary Report",
            generatedAt = DateTimeUtil.nowIsoString(),
            dateRange = dateRange,
            summary = ReportSummary(
                listOf(
                    SummaryItem("Total Breeding Events", breedingEvents.size.toString()),
                    SummaryItem("Breedings", bredEvents.toString()),
                    SummaryItem("Pregnancy Checks", pregnancyChecks.toString()),
                    SummaryItem("Births", births.toString()),
                    SummaryItem("Total Offspring", totalOffspring.toString())
                )
            ),
            rows = rows
        )
    }

    private suspend fun generateHealthHistoryReport(dateRange: DateRange?): ReportData {
        val animals = animalRepository.getAllAnimals().first()
        val animalsById = animals.associateBy { it.id }

        val allEvents = if (dateRange != null) {
            eventRepository.getEventsByDateRange(dateRange.startDate, dateRange.endDate).first()
        } else {
            eventRepository.getRecentEvents(1000).first()
        }

        val healthEvents = allEvents.filter { it.eventType.category == EventCategory.HEALTH }

        val vaccinations = healthEvents.count { it.eventType == EventType.VACCINATION }
        val treatments = healthEvents.count { it.eventType == EventType.TREATMENT }
        val vetVisits = healthEvents.count { it.eventType == EventType.VET_VISIT }

        val rows = healthEvents.map { event ->
            val animalName = animalsById[event.animalId]?.displayName ?: "Unknown"
            val data = event.eventData as? HealthEventData

            ReportRow(
                listOf(
                    event.eventDate.toString(),
                    animalName,
                    event.eventType.displayName,
                    data?.medicationName ?: "-",
                    data?.veterinarian ?: "-",
                    event.notes ?: "-"
                )
            )
        }

        return ReportData(
            reportType = ReportType.HEALTH_HISTORY,
            title = "Health History Report",
            generatedAt = DateTimeUtil.nowIsoString(),
            dateRange = dateRange,
            summary = ReportSummary(
                listOf(
                    SummaryItem("Total Health Events", healthEvents.size.toString()),
                    SummaryItem("Vaccinations", vaccinations.toString()),
                    SummaryItem("Treatments", treatments.toString()),
                    SummaryItem("Vet Visits", vetVisits.toString())
                )
            ),
            rows = rows
        )
    }

    private suspend fun generateProductionReport(dateRange: DateRange?): ReportData {
        val animals = animalRepository.getAllAnimals().first()
        val animalsById = animals.associateBy { it.id }

        val allEvents = if (dateRange != null) {
            eventRepository.getEventsByDateRange(dateRange.startDate, dateRange.endDate).first()
        } else {
            eventRepository.getRecentEvents(1000).first()
        }

        val productionEvents = allEvents.filter { it.eventType.category == EventCategory.PRODUCTION }

        var totalMilk = 0.0
        var totalEggs = 0
        var totalFiber = 0.0

        val rows = productionEvents.map { event ->
            val animalName = animalsById[event.animalId]?.displayName ?: "Unknown"
            val data = event.eventData as? ProductionEventData

            val (productionType, quantity) = when (event.eventType) {
                EventType.MILK_RECORD -> {
                    data?.milkQuantity?.let { totalMilk += it }
                    Pair("Milk", "${data?.milkQuantity ?: "-"} ${data?.milkUnit ?: ""}")
                }
                EventType.EGG_COLLECTION -> {
                    data?.eggCount?.let { totalEggs += it }
                    Pair("Eggs", "${data?.eggCount ?: "-"} eggs")
                }
                EventType.SHEARING -> {
                    data?.fiberWeight?.let { totalFiber += it }
                    Pair("Fiber", "${data?.fiberWeight ?: "-"} ${data?.fiberUnit ?: ""}")
                }
                else -> Pair(event.eventType.displayName, "-")
            }

            ReportRow(
                listOf(
                    event.eventDate.toString(),
                    animalName,
                    productionType,
                    quantity,
                    event.notes ?: "-"
                )
            )
        }

        val summaryItems = mutableListOf(
            SummaryItem("Total Records", productionEvents.size.toString())
        )
        if (totalMilk > 0) summaryItems.add(SummaryItem("Total Milk", formatDouble(totalMilk, 2)))
        if (totalEggs > 0) summaryItems.add(SummaryItem("Total Eggs", totalEggs.toString()))
        if (totalFiber > 0) summaryItems.add(SummaryItem("Total Fiber", formatDouble(totalFiber, 2)))

        return ReportData(
            reportType = ReportType.PRODUCTION_REPORT,
            title = "Production Report",
            generatedAt = DateTimeUtil.nowIsoString(),
            dateRange = dateRange,
            summary = ReportSummary(summaryItems),
            rows = rows
        )
    }

    private suspend fun generateWeightTrackingReport(dateRange: DateRange?): ReportData {
        val animals = animalRepository.getAllAnimals().first()
        val animalsById = animals.associateBy { it.id }

        val allEvents = if (dateRange != null) {
            eventRepository.getEventsByDateRange(dateRange.startDate, dateRange.endDate).first()
        } else {
            eventRepository.getRecentEvents(1000).first()
        }

        val weightEvents = allEvents.filter { it.eventType.category == EventCategory.WEIGHT }

        val weights = mutableListOf<Double>()
        val rows = weightEvents.map { event ->
            val animalName = animalsById[event.animalId]?.displayName ?: "Unknown"
            val data = event.eventData as? WeightEventData

            data?.weight?.let { weights.add(it) }

            ReportRow(
                listOf(
                    event.eventDate.toString(),
                    animalName,
                    "${data?.weight ?: "-"} ${data?.weightUnit ?: "lbs"}",
                    data?.condition ?: "-",
                    event.notes ?: data?.notes ?: "-"
                )
            )
        }

        val avgWeight = if (weights.isNotEmpty()) weights.average() else null

        return ReportData(
            reportType = ReportType.WEIGHT_TRACKING,
            title = "Weight Tracking Report",
            generatedAt = DateTimeUtil.nowIsoString(),
            dateRange = dateRange,
            summary = ReportSummary(
                listOf(
                    SummaryItem("Total Records", weightEvents.size.toString()),
                    SummaryItem("Average Weight", avgWeight?.let { "${formatDouble(it, 1)} lbs" } ?: "-")
                )
            ),
            rows = rows
        )
    }

    private suspend fun generateEventTimelineReport(dateRange: DateRange?): ReportData {
        val animals = animalRepository.getAllAnimals().first()
        val animalsById = animals.associateBy { it.id }

        val allEvents = if (dateRange != null) {
            eventRepository.getEventsByDateRange(dateRange.startDate, dateRange.endDate).first()
        } else {
            eventRepository.getRecentEvents(1000).first()
        }

        val rows = allEvents.sortedByDescending { it.eventDate }.map { event ->
            val animalName = animalsById[event.animalId]?.displayName ?: "Unknown"

            val details = buildString {
                event.notes?.let { append(it) }
            }

            ReportRow(
                listOf(
                    event.eventDate.toString(),
                    animalName,
                    "${event.eventType.category.displayName}: ${event.eventType.displayName}",
                    details.ifEmpty { "-" }
                )
            )
        }

        val eventsByCategory = allEvents.groupBy { it.eventType.category }

        val summaryItems = mutableListOf(
            SummaryItem("Total Events", allEvents.size.toString())
        )
        eventsByCategory.forEach { (category, events) ->
            summaryItems.add(SummaryItem(category.displayName, events.size.toString()))
        }

        return ReportData(
            reportType = ReportType.EVENT_TIMELINE,
            title = "Event Timeline Report",
            generatedAt = DateTimeUtil.nowIsoString(),
            dateRange = dateRange,
            summary = ReportSummary(summaryItems),
            rows = rows
        )
    }

    // Export functions

    fun exportToCsv(report: ReportData, columns: List<ReportColumn>): String {
        val sb = StringBuilder()

        // Header row
        sb.appendLine(columns.joinToString(",") { "\"${it.header}\"" })

        // Data rows
        report.rows.forEach { row ->
            sb.appendLine(row.columns.joinToString(",") { "\"${it.replace("\"", "\"\"")}\"" })
        }

        return sb.toString()
    }

    fun exportToText(report: ReportData, columns: List<ReportColumn>): String {
        val sb = StringBuilder()

        // Title
        sb.appendLine("=" .repeat(60))
        sb.appendLine(report.title)
        sb.appendLine("Generated: ${report.generatedAt}")
        report.dateRange?.let {
            sb.appendLine("Date Range: ${it.startDate} to ${it.endDate}")
        }
        sb.appendLine("=" .repeat(60))
        sb.appendLine()

        // Summary
        sb.appendLine("SUMMARY")
        sb.appendLine("-".repeat(30))
        report.summary.items.forEach { item ->
            sb.appendLine("${item.label}: ${item.value}")
        }
        sb.appendLine()

        // Data table
        sb.appendLine("DETAILS")
        sb.appendLine("-".repeat(30))

        // Header
        sb.appendLine(columns.joinToString(" | ") { it.header.padEnd(15) })
        sb.appendLine("-".repeat(columns.size * 17))

        // Rows
        report.rows.forEach { row ->
            sb.appendLine(row.columns.joinToString(" | ") { it.take(15).padEnd(15) })
        }

        return sb.toString()
    }

    private fun formatDouble(value: Double, decimals: Int): String {
        val parts = value.toString().split(".")
        val intPart = parts[0]
        val decPart = if (parts.size > 1) parts[1] else ""
        return if (decimals > 0) {
            "$intPart.${decPart.padEnd(decimals, '0').take(decimals)}"
        } else {
            intPart
        }
    }

    private suspend fun generateSalesRevenueReport(dateRange: DateRange?): ReportData {
        val animals = animalRepository.getAllAnimals().first()
        val animalMap = animals.associateBy { it.id }

        val statusEvents = eventRepository.getEventsByType(EventType.STATUS_CHANGE).first()
        val harvestEvents = eventRepository.getEventsByType(EventType.HARVEST).first()
        val allEvents = (statusEvents + harvestEvents).sortedByDescending { it.eventDate }

        val filtered = if (dateRange != null) {
            allEvents.filter { it.eventDate >= dateRange.startDate && it.eventDate <= dateRange.endDate }
        } else allEvents

        data class RevenueRow(
            val date: String,
            val animalName: String,
            val species: String,
            val ageAtEvent: String,
            val type: String,
            val liveWeight: String,
            val dressedWeight: String,
            val grossRevenue: Double?,
            val processingCost: Double?,
            val netRevenue: Double?,
            val pricePerLb: Double?,
            val buyer: String?
        )

        fun calcAge(animal: com.markduenas.homesteader.domain.model.Animal, eventDate: kotlinx.datetime.LocalDate): String {
            val birth = animal.birthDate ?: return "-"
            val days = (eventDate.toEpochDays() - birth.toEpochDays()).toInt()
            if (days < 0) return "-"
            return when {
                days < 30 -> "$days days"
                days < 365 -> "${days / 30} mo"
                else -> "${days / 365} yr ${(days % 365) / 30} mo"
            }
        }

        val rows = filtered.mapNotNull { event ->
            val animal = animalMap[event.animalId] ?: return@mapNotNull null
            val name = animal.name ?: animal.tagId ?: event.animalId
            val species = animal.species.displayName
            val date = DateTimeUtil.formatDate(event.eventDate)
            val age = calcAge(animal, event.eventDate)

            when (val data = event.eventData) {
                is StatusChangeEventData -> RevenueRow(
                    date = date,
                    animalName = name,
                    species = species,
                    ageAtEvent = age,
                    type = data.newStatus.lowercase().replaceFirstChar { it.uppercase() },
                    liveWeight = data.saleWeight?.let { "$it lbs" } ?: "-",
                    dressedWeight = "-",
                    grossRevenue = data.salePrice,
                    processingCost = null,
                    netRevenue = data.salePrice,
                    pricePerLb = null,
                    buyer = data.buyer
                )
                is HarvestEventData -> {
                    val processingCost = (data.killFee ?: 0.0) +
                        ((data.butcherPricePerPound ?: 0.0) * (data.dressedWeight ?: 0.0))
                    val grossRev = data.revenue
                    val netRev = if (grossRev != null) grossRev - processingCost else null
                    val perLb = if (grossRev != null && data.dressedWeight != null && data.dressedWeight > 0.0)
                        grossRev / data.dressedWeight else null
                    RevenueRow(
                        date = date,
                        animalName = name,
                        species = species,
                        ageAtEvent = age,
                        type = "Harvest (${data.purpose ?: "Personal Use"})",
                        liveWeight = data.liveWeight?.let { "$it lbs" } ?: "-",
                        dressedWeight = data.dressedWeight?.let { "$it lbs" } ?: "-",
                        grossRevenue = grossRev,
                        processingCost = if (processingCost > 0.0) processingCost else null,
                        netRevenue = netRev,
                        pricePerLb = perLb,
                        buyer = data.buyer
                    )
                }
                else -> null
            }
        }

        val totalGross = rows.mapNotNull { it.grossRevenue }.fold(0.0) { acc, v -> acc + v }
        val totalNet = rows.mapNotNull { it.netRevenue }.fold(0.0) { acc, v -> acc + v }
        val totalProcessing = rows.mapNotNull { it.processingCost }.fold(0.0) { acc, v -> acc + v }
        val soldCount = rows.count { it.type.contains("Sold", ignoreCase = true) }
        val harvestCount = rows.count { it.type.contains("Harvest", ignoreCase = true) }

        return ReportData(
            reportType = ReportType.SALES_REVENUE,
            title = "Sales & Revenue Report",
            generatedAt = DateTimeUtil.nowIsoString(),
            dateRange = dateRange,
            summary = ReportSummary(
                items = listOf(
                    SummaryItem("Gross Revenue", "$${totalGross.formatDecimal()}"),
                    SummaryItem("Processing Costs", "$${totalProcessing.formatDecimal()}"),
                    SummaryItem("Net Revenue", "$${totalNet.formatDecimal()}"),
                    SummaryItem("Animals Sold", soldCount.toString()),
                    SummaryItem("Animals Harvested", harvestCount.toString()),
                    SummaryItem("Total Records", rows.size.toString())
                )
            ),
            rows = rows.map { row ->
                ReportRow(
                    columns = listOf(
                        row.date,
                        row.animalName,
                        row.species,
                        row.ageAtEvent,
                        row.type,
                        row.liveWeight,
                        row.dressedWeight,
                        row.grossRevenue?.let { "$${it.formatDecimal()}" } ?: "-",
                        row.processingCost?.let { "$${it.formatDecimal()}" } ?: "-",
                        row.netRevenue?.let { "$${it.formatDecimal()}" } ?: "-",
                        row.pricePerLb?.let { "$${it.formatDecimal()}" } ?: "-",
                        row.buyer ?: "-"
                    )
                )
            }
        )
    }

    private suspend fun generateSteerHarvestAvailabilityReport(harvestAgeYears: Int): ReportData {
        val animals = animalRepository.getAllAnimals().first()
        val today = DateTimeUtil.today()
        val harvestAgeDays = harvestAgeYears * 365

        val maleCattle = animals.filter {
            (it.species == Species.CATTLE_BEEF || it.species == Species.CATTLE_DAIRY) &&
                it.sex == Sex.MALE &&
                it.status == AnimalStatus.ACTIVE
        }

        val withBirthDate = maleCattle.filter { it.birthDate != null }
        val noBirthDate = maleCattle.filter { it.birthDate == null }

        val sorted = withBirthDate.sortedBy { it.birthDate!!.toEpochDays() + harvestAgeDays }

        val todayEpoch = today.toEpochDays()
        val readyCount = sorted.count { todayEpoch >= it.birthDate!!.toEpochDays() + harvestAgeDays }
        val within90 = sorted.count {
            val daysUntil = (it.birthDate!!.toEpochDays() + harvestAgeDays) - todayEpoch
            daysUntil in 1..90
        }
        val within180 = sorted.count {
            val daysUntil = (it.birthDate!!.toEpochDays() + harvestAgeDays) - todayEpoch
            daysUntil in 91..180
        }

        val summaryItems = mutableListOf(
            SummaryItem("Total Active Male Cattle", maleCattle.size.toString()),
            SummaryItem("Ready Now", readyCount.toString()),
            SummaryItem("Within 90 Days", within90.toString()),
            SummaryItem("Within 180 Days", within180.toString())
        )
        if (noBirthDate.isNotEmpty()) {
            summaryItems.add(SummaryItem("No Birth Date", noBirthDate.size.toString()))
        }

        val dataRows = sorted.map { animal ->
            val harvestEpoch = animal.birthDate!!.toEpochDays() + harvestAgeDays
            val daysUntil = harvestEpoch - todayEpoch
            val ageInDays = todayEpoch - animal.birthDate.toEpochDays()
            val ageYears = ageInDays / 365.0
            val harvestDate = LocalDate.fromEpochDays(harvestEpoch)
            val statusLabel = when {
                daysUntil <= 0 -> "Ready Now"
                daysUntil <= 90 -> "Within 90 days"
                daysUntil <= 180 -> "Within 180 days"
                else -> "> 6 months"
            }
            ReportRow(listOf(
                animal.displayName,
                animal.tagId ?: "-",
                animal.breed ?: "-",
                animal.birthDate.toString(),
                "${ageYears.formatDecimal(1)} yrs",
                harvestDate.toString(),
                statusLabel
            ))
        } + noBirthDate.map { animal ->
            ReportRow(listOf(
                animal.displayName,
                animal.tagId ?: "-",
                animal.breed ?: "-",
                "Unknown",
                "-",
                "-",
                "No birth date"
            ))
        }

        return ReportData(
            reportType = ReportType.STEER_HARVEST_AVAILABILITY,
            title = "Steer Harvest Availability ($harvestAgeYears-Year Target)",
            generatedAt = DateTimeUtil.nowIsoString(),
            dateRange = null,
            summary = ReportSummary(summaryItems),
            rows = dataRows
        )
    }
}
