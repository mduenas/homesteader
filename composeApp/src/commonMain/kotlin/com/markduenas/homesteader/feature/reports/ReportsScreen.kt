package com.markduenas.homesteader.feature.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import com.markduenas.homesteader.core.designsystem.components.DatePickerField
import com.markduenas.homesteader.core.designsystem.components.LoadingIndicator
import com.markduenas.homesteader.domain.model.ReportColumn
import com.markduenas.homesteader.domain.model.ReportData
import com.markduenas.homesteader.domain.model.ReportRow
import com.markduenas.homesteader.domain.model.ReportType
import kotlinx.datetime.LocalDate

class ReportsScreen : Screen {

    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<ReportsViewModel>()
        val state by viewModel.state.collectAsState()

        LaunchedEffect(Unit) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    is ReportsEffect.ShowError -> {
                        // Show error snackbar
                    }
                    is ReportsEffect.ShareContent -> {
                        // Handle share/export
                    }
                }
            }
        }

        ReportsContent(
            state = state,
            onIntent = viewModel::handleIntent
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReportsContent(
    state: ReportsState,
    onIntent: (ReportsIntent) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    if (state.reportData != null) {
                        TextButton(onClick = { onIntent(ReportsIntent.ClearReport) }) {
                            Text("Clear")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                LoadingIndicator()
            } else if (state.reportData != null) {
                ReportResultView(
                    reportData = state.reportData,
                    columns = state.reportColumns,
                    onExportCsv = { onIntent(ReportsIntent.ExportCsv) },
                    onExportText = { onIntent(ReportsIntent.ExportText) }
                )
            } else {
                ReportSelectionView(
                    selectedReportType = state.selectedReportType,
                    dateRangeOption = state.dateRangeOption,
                    customStartDate = state.customStartDate,
                    customEndDate = state.customEndDate,
                    onSelectReportType = { onIntent(ReportsIntent.SelectReportType(it)) },
                    onSelectDateRange = { onIntent(ReportsIntent.SelectDateRange(it)) },
                    onSetCustomDateRange = { start, end ->
                        onIntent(ReportsIntent.SetCustomDateRange(start, end))
                    },
                    onGenerateReport = { onIntent(ReportsIntent.GenerateReport) }
                )
            }
        }
    }
}

@Composable
private fun ReportSelectionView(
    selectedReportType: ReportType?,
    dateRangeOption: DateRangeOption,
    customStartDate: LocalDate?,
    customEndDate: LocalDate?,
    onSelectReportType: (ReportType) -> Unit,
    onSelectDateRange: (DateRangeOption) -> Unit,
    onSetCustomDateRange: (LocalDate, LocalDate) -> Unit,
    onGenerateReport: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Select Report Type",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(ReportType.entries) { reportType ->
            ReportTypeCard(
                reportType = reportType,
                isSelected = reportType == selectedReportType,
                onClick = { onSelectReportType(reportType) }
            )
        }

        if (selectedReportType != null) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Date Range",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                DateRangeSelector(
                    selectedOption = dateRangeOption,
                    onSelectOption = onSelectDateRange
                )
            }

            if (dateRangeOption == DateRangeOption.CUSTOM) {
                item {
                    CustomDateRangeInput(
                        startDate = customStartDate,
                        endDate = customEndDate,
                        onSetRange = onSetCustomDateRange
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onGenerateReport,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Generate Report")
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ReportTypeCard(
    reportType: ReportType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = reportType.displayName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = reportType.description,
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                }
            )
        }
    }
}

@Composable
private fun DateRangeSelector(
    selectedOption: DateRangeOption,
    onSelectOption: (DateRangeOption) -> Unit
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DateRangeOption.entries.forEach { option ->
            FilterChip(
                selected = option == selectedOption,
                onClick = { onSelectOption(option) },
                label = { Text(option.displayName) }
            )
        }
    }
}

@Composable
private fun ReportResultView(
    reportData: ReportData,
    columns: List<ReportColumn>,
    onExportCsv: () -> Unit,
    onExportText: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = reportData.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Generated: ${reportData.generatedAt}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                reportData.dateRange?.let { range ->
                    Text(
                        text = "Date Range: ${range.startDate} to ${range.endDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Summary
        item {
            ReportSummaryCard(reportData = reportData)
        }

        // Export buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onExportCsv,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Export CSV")
                }
                OutlinedButton(
                    onClick = onExportText,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Export Text")
                }
            }
        }

        // Data table header
        item {
            Text(
                text = "Details (${reportData.rows.size} records)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Table
        item {
            ReportTable(
                columns = columns,
                rows = reportData.rows
            )
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ReportSummaryCard(reportData: ReportData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Summary",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(12.dp))

            val items = reportData.summary.items
            val chunkedItems = items.chunked(2)

            chunkedItems.forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    rowItems.forEach { item ->
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                text = item.value,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    // Fill empty space if odd number of items
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                if (chunkedItems.indexOf(rowItems) < chunkedItems.size - 1) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun ReportTable(
    columns: List<ReportColumn>,
    rows: List<ReportRow>
) {
    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .horizontalScroll(scrollState)
                .padding(8.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(4.dp)
                    )
                    .padding(8.dp)
            ) {
                columns.forEach { column ->
                    Text(
                        text = column.header,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width((column.width * 80).dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Data rows
            rows.take(50).forEachIndexed { index, row ->
                if (index > 0) {
                    HorizontalDivider(
                        modifier = Modifier.width((columns.sumOf { it.width.toDouble() } * 80).dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
                Row(
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp)
                ) {
                    row.columns.forEachIndexed { colIndex, value ->
                        val columnWidth = columns.getOrNull(colIndex)?.width ?: 1f
                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.width((columnWidth * 80).dp),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            if (rows.size > 50) {
                Text(
                    text = "... and ${rows.size - 50} more records",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

@Composable
private fun CustomDateRangeInput(
    startDate: LocalDate?,
    endDate: LocalDate?,
    onSetRange: (LocalDate, LocalDate) -> Unit
) {
    var startText by remember(startDate) { mutableStateOf(startDate?.toString() ?: "") }
    var endText by remember(endDate) { mutableStateOf(endDate?.toString() ?: "") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Custom Date Range",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            DatePickerField(
                value = startText,
                onDateSelected = { selected ->
                    startText = selected
                    val end = runCatching { LocalDate.parse(endText) }.getOrNull()
                    val start = runCatching { LocalDate.parse(selected) }.getOrNull()
                    if (start != null && end != null) onSetRange(start, end)
                },
                label = "Start Date",
                modifier = Modifier.fillMaxWidth()
            )
            DatePickerField(
                value = endText,
                onDateSelected = { selected ->
                    endText = selected
                    val start = runCatching { LocalDate.parse(startText) }.getOrNull()
                    val end = runCatching { LocalDate.parse(selected) }.getOrNull()
                    if (start != null && end != null) onSetRange(start, end)
                },
                label = "End Date",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
