package com.markduenas.homesteader.feature.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import com.markduenas.homesteader.core.designsystem.components.LoadingIndicator
import com.markduenas.homesteader.core.util.DateTimeUtil
import com.markduenas.homesteader.domain.model.ReminderType
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

class CalendarScreen : Screen {

    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<CalendarViewModel>()
        val state by viewModel.state.collectAsState()

        LaunchedEffect(Unit) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    is CalendarEffect.NavigateToAnimalDetail -> {
                        // Handle navigation
                    }
                    is CalendarEffect.ShowError -> {
                        // Show error snackbar
                    }
                }
            }
        }

        CalendarContent(
            state = state,
            onIntent = viewModel::handleIntent
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalendarContent(
    state: CalendarState,
    onIntent: (CalendarIntent) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendar") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onIntent(CalendarIntent.ShowAddReminder) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                LoadingIndicator()
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Month navigation
                    MonthHeader(
                        currentMonth = state.currentMonth,
                        onPreviousMonth = { onIntent(CalendarIntent.PreviousMonth) },
                        onNextMonth = { onIntent(CalendarIntent.NextMonth) }
                    )

                    // Calendar grid
                    CalendarGrid(
                        currentMonth = state.currentMonth,
                        selectedDate = state.selectedDate,
                        remindersByDate = state.remindersByDate,
                        onDateSelected = { onIntent(CalendarIntent.SelectDate(it)) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Reminders for selected date
                    Text(
                        text = "Tasks for ${formatDate(state.selectedDate)}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (state.remindersForSelectedDate.isEmpty()) {
                        Text(
                            text = "No tasks for this date",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(state.remindersForSelectedDate) { reminder ->
                                ReminderCard(
                                    reminder = reminder,
                                    onComplete = { onIntent(CalendarIntent.CompleteReminder(reminder.id)) },
                                    onDelete = { onIntent(CalendarIntent.DeleteReminder(reminder.id)) }
                                )
                            }

                            item {
                                Spacer(modifier = Modifier.height(72.dp))
                            }
                        }
                    }
                }
            }

            if (state.showAddReminderDialog) {
                AddReminderDialog(
                    selectedDate = state.selectedDate,
                    onDismiss = { onIntent(CalendarIntent.HideAddReminder) },
                    onAdd = { title, description, date ->
                        onIntent(
                            CalendarIntent.AddReminder(
                                title = title,
                                description = description,
                                dueDate = date,
                                animalId = null,
                                isRecurring = false,
                                recurrenceIntervalDays = null
                            )
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun MonthHeader(
    currentMonth: LocalDate,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Text(
                text = "<",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Text(
            text = formatMonthYear(currentMonth),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        IconButton(onClick = onNextMonth) {
            Text(
                text = ">",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    currentMonth: LocalDate,
    selectedDate: LocalDate,
    remindersByDate: Map<LocalDate, List<CalendarReminder>>,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = DateTimeUtil.today()
    val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

    Column(modifier = modifier.padding(horizontal = 8.dp)) {
        // Day of week headers
        Row(modifier = Modifier.fillMaxWidth()) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Calendar days
        val calendarDays = getCalendarDays(currentMonth)

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(240.dp),
            userScrollEnabled = false
        ) {
            items(calendarDays) { day ->
                CalendarDay(
                    date = day,
                    isCurrentMonth = day?.monthNumber == currentMonth.monthNumber,
                    isToday = day == today,
                    isSelected = day == selectedDate,
                    hasReminders = day?.let { remindersByDate[it]?.isNotEmpty() == true } ?: false,
                    hasOverdue = day?.let { date ->
                        remindersByDate[date]?.any { it.isOverdue } == true
                    } ?: false,
                    onClick = { day?.let { onDateSelected(it) } }
                )
            }
        }
    }
}

@Composable
private fun CalendarDay(
    date: LocalDate?,
    isCurrentMonth: Boolean,
    isToday: Boolean,
    isSelected: Boolean,
    hasReminders: Boolean,
    hasOverdue: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isToday -> MaterialTheme.colorScheme.primaryContainer
                    else -> MaterialTheme.colorScheme.surface
                }
            )
            .clickable(enabled = date != null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (date != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = when {
                        isSelected -> MaterialTheme.colorScheme.onPrimary
                        !isCurrentMonth -> MaterialTheme.colorScheme.outline
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )

                if (hasReminders) {
                    Surface(
                        modifier = Modifier.size(4.dp),
                        shape = CircleShape,
                        color = when {
                            hasOverdue -> MaterialTheme.colorScheme.error
                            isSelected -> MaterialTheme.colorScheme.onPrimary
                            else -> MaterialTheme.colorScheme.primary
                        }
                    ) {}
                }
            }
        }
    }
}

@Composable
private fun ReminderCard(
    reminder: CalendarReminder,
    onComplete: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (reminder.isOverdue) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicator
            Surface(
                modifier = Modifier.size(8.dp),
                shape = CircleShape,
                color = getReminderTypeColor(reminder.reminderType)
            ) {}

            Spacer(modifier = Modifier.width(12.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                reminder.animalName?.let { name ->
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                reminder.description?.let { desc ->
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Actions
            TextButton(onClick = onComplete) {
                Text("Done", color = MaterialTheme.colorScheme.primary)
            }
            TextButton(onClick = onDelete) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun AddReminderDialog(
    selectedDate: LocalDate,
    onDismiss: () -> Unit,
    onAdd: (title: String, description: String?, dueDate: LocalDate) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Reminder") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Due: ${formatDate(selectedDate)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        onAdd(title, description.ifBlank { null }, selectedDate)
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun getReminderTypeColor(type: ReminderType): androidx.compose.ui.graphics.Color {
    return when (type) {
        ReminderType.HEAT_EXPECTED,
        ReminderType.PREGNANCY_CHECK,
        ReminderType.BIRTH_DUE,
        ReminderType.WEANING_DUE -> MaterialTheme.colorScheme.tertiary

        ReminderType.VACCINATION_DUE,
        ReminderType.DEWORMING_DUE,
        ReminderType.VET_FOLLOWUP,
        ReminderType.MEDICATION_DUE,
        ReminderType.HOOF_TRIM_DUE -> MaterialTheme.colorScheme.error

        ReminderType.CUSTOM,
        ReminderType.RECURRING_TASK -> MaterialTheme.colorScheme.primary
    }
}

private fun getCalendarDays(month: LocalDate): List<LocalDate?> {
    val firstOfMonth = LocalDate(month.year, month.monthNumber, 1)
    val daysInMonth = when (month.monthNumber) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (isLeapYear(month.year)) 29 else 28
        else -> 30
    }

    val firstDayOfWeek = firstOfMonth.dayOfWeek.ordinal
    val adjustedFirstDay = (firstDayOfWeek + 1) % 7 // Adjust for Sunday start

    val days = mutableListOf<LocalDate?>()

    // Add empty cells for days before the first of the month
    repeat(adjustedFirstDay) {
        days.add(null)
    }

    // Add the days of the month
    for (day in 1..daysInMonth) {
        days.add(LocalDate(month.year, month.monthNumber, day))
    }

    // Add empty cells to complete the grid
    while (days.size < 42) {
        days.add(null)
    }

    return days
}

private fun isLeapYear(year: Int): Boolean {
    return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
}

private fun formatMonthYear(date: LocalDate): String {
    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    return "${monthNames[date.monthNumber - 1]} ${date.year}"
}

private fun formatDate(date: LocalDate): String {
    val monthNames = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )
    return "${monthNames[date.monthNumber - 1]} ${date.dayOfMonth}, ${date.year}"
}
