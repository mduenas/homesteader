package com.markduenas.homesteader.feature.reports

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.markduenas.homesteader.core.util.DateTimeUtil
import com.markduenas.homesteader.domain.model.DateRange
import com.markduenas.homesteader.domain.model.ReportColumn
import com.markduenas.homesteader.domain.model.ReportData
import com.markduenas.homesteader.domain.model.ReportType
import com.markduenas.homesteader.domain.monetization.PremiumManager
import com.markduenas.homesteader.domain.service.ReportGenerator
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

data class ReportsState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedReportType: ReportType? = null,
    val dateRangeOption: DateRangeOption = DateRangeOption.ALL_TIME,
    val customStartDate: LocalDate? = null,
    val customEndDate: LocalDate? = null,
    val reportData: ReportData? = null,
    val reportColumns: List<ReportColumn> = emptyList(),
    val showDateRangePicker: Boolean = false,
    val exportedContent: String? = null,
    val harvestAgeYears: Int = 2
)

enum class DateRangeOption(val displayName: String) {
    LAST_7_DAYS("Last 7 Days"),
    LAST_30_DAYS("Last 30 Days"),
    LAST_90_DAYS("Last 90 Days"),
    THIS_YEAR("This Year"),
    ALL_TIME("All Time"),
    CUSTOM("Custom Range")
}

sealed class ReportsIntent {
    data class SelectReportType(val reportType: ReportType) : ReportsIntent()
    data class SelectDateRange(val option: DateRangeOption) : ReportsIntent()
    data class SetCustomDateRange(val startDate: LocalDate, val endDate: LocalDate) : ReportsIntent()
    data object GenerateReport : ReportsIntent()
    data object ClearReport : ReportsIntent()
    data object ExportCsv : ReportsIntent()
    data object ExportText : ReportsIntent()
    data object ShowDateRangePicker : ReportsIntent()
    data object HideDateRangePicker : ReportsIntent()
    data object ClearExportedContent : ReportsIntent()
    data class SetHarvestAgeYears(val years: Int) : ReportsIntent()
}

sealed class ReportsEffect {
    data class ShowError(val message: String) : ReportsEffect()
    data class ShareContent(val content: String, val filename: String, val mimeType: String) : ReportsEffect()
    data object ShowPremiumUpsell : ReportsEffect()
}

class ReportsViewModel(
    private val reportGenerator: ReportGenerator,
    private val premiumManager: PremiumManager
) : ScreenModel {

    private val _state = MutableStateFlow(ReportsState())
    val state: StateFlow<ReportsState> = _state.asStateFlow()

    private val _effects = Channel<ReportsEffect>()
    val effects = _effects.receiveAsFlow()

    fun handleIntent(intent: ReportsIntent) {
        when (intent) {
            is ReportsIntent.SelectReportType -> selectReportType(intent.reportType)
            is ReportsIntent.SelectDateRange -> selectDateRange(intent.option)
            is ReportsIntent.SetCustomDateRange -> setCustomDateRange(intent.startDate, intent.endDate)
            ReportsIntent.GenerateReport -> generateReport()
            ReportsIntent.ClearReport -> clearReport()
            ReportsIntent.ExportCsv -> exportCsv()
            ReportsIntent.ExportText -> exportText()
            ReportsIntent.ShowDateRangePicker -> _state.update { it.copy(showDateRangePicker = true) }
            ReportsIntent.HideDateRangePicker -> _state.update { it.copy(showDateRangePicker = false) }
            ReportsIntent.ClearExportedContent -> _state.update { it.copy(exportedContent = null) }
            is ReportsIntent.SetHarvestAgeYears -> _state.update { it.copy(harvestAgeYears = intent.years) }
        }
    }

    private fun selectReportType(reportType: ReportType) {
        val columns = reportGenerator.getColumnsForReportType(reportType)
        _state.update {
            it.copy(
                selectedReportType = reportType,
                reportColumns = columns,
                reportData = null
            )
        }
    }

    private fun selectDateRange(option: DateRangeOption) {
        _state.update { it.copy(dateRangeOption = option) }
    }

    private fun setCustomDateRange(startDate: LocalDate, endDate: LocalDate) {
        _state.update {
            it.copy(
                customStartDate = startDate,
                customEndDate = endDate,
                dateRangeOption = DateRangeOption.CUSTOM,
                showDateRangePicker = false
            )
        }
    }

    private fun getDateRange(): DateRange? {
        val today = DateTimeUtil.today()
        return when (_state.value.dateRangeOption) {
            DateRangeOption.LAST_7_DAYS -> DateRange(
                startDate = today.minus(DatePeriod(days = 7)),
                endDate = today
            )
            DateRangeOption.LAST_30_DAYS -> DateRange(
                startDate = today.minus(DatePeriod(days = 30)),
                endDate = today
            )
            DateRangeOption.LAST_90_DAYS -> DateRange(
                startDate = today.minus(DatePeriod(days = 90)),
                endDate = today
            )
            DateRangeOption.THIS_YEAR -> DateRange(
                startDate = LocalDate(today.year, 1, 1),
                endDate = today
            )
            DateRangeOption.ALL_TIME -> null
            DateRangeOption.CUSTOM -> {
                val start = _state.value.customStartDate
                val end = _state.value.customEndDate
                if (start != null && end != null) {
                    DateRange(startDate = start, endDate = end)
                } else {
                    null
                }
            }
        }
    }

    private fun generateReport() {
        val reportType = _state.value.selectedReportType ?: return

        screenModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                val dateRange = getDateRange()
                val reportData = reportGenerator.generateReport(reportType, dateRange, _state.value.harvestAgeYears)

                _state.update {
                    it.copy(
                        isLoading = false,
                        reportData = reportData
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
                _effects.send(ReportsEffect.ShowError("Failed to generate report: ${e.message}"))
            }
        }
    }

    private fun clearReport() {
        _state.update {
            it.copy(
                selectedReportType = null,
                reportData = null,
                reportColumns = emptyList()
            )
        }
    }

    private fun exportCsv() {
        val reportData = _state.value.reportData ?: return
        val columns = _state.value.reportColumns

        screenModelScope.launch {
            if (!premiumManager.isPremium.value) {
                _effects.send(ReportsEffect.ShowPremiumUpsell)
                return@launch
            }
            try {
                val csv = reportGenerator.exportToCsv(reportData, columns)
                val filename = "${reportData.reportType.name.lowercase()}_${DateTimeUtil.nowIsoString().replace(":", "-")}.csv"
                _state.update { it.copy(exportedContent = csv) }
                _effects.send(ReportsEffect.ShareContent(csv, filename, "text/csv"))
            } catch (e: Exception) {
                _effects.send(ReportsEffect.ShowError("Failed to export CSV: ${e.message}"))
            }
        }
    }

    private fun exportText() {
        val reportData = _state.value.reportData ?: return
        val columns = _state.value.reportColumns

        screenModelScope.launch {
            if (!premiumManager.isPremium.value) {
                _effects.send(ReportsEffect.ShowPremiumUpsell)
                return@launch
            }
            try {
                val text = reportGenerator.exportToText(reportData, columns)
                val filename = "${reportData.reportType.name.lowercase()}_${DateTimeUtil.nowIsoString().replace(":", "-")}.txt"
                _state.update { it.copy(exportedContent = text) }
                _effects.send(ReportsEffect.ShareContent(text, filename, "text/plain"))
            } catch (e: Exception) {
                _effects.send(ReportsEffect.ShowError("Failed to export text: ${e.message}"))
            }
        }
    }
}
