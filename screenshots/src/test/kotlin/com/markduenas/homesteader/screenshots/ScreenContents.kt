package com.markduenas.homesteader.screenshots

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.markduenas.homesteader.domain.model.Animal
import com.markduenas.homesteader.domain.model.AnimalEvent
import com.markduenas.homesteader.domain.model.AnimalStatus
import com.markduenas.homesteader.domain.model.EventCategory
import com.markduenas.homesteader.domain.model.ReminderType
import com.markduenas.homesteader.domain.model.Species
import com.markduenas.homesteader.feature.animal.list.AnimalListState
import com.markduenas.homesteader.feature.calendar.CalendarReminder
import com.markduenas.homesteader.feature.calendar.CalendarState
import com.markduenas.homesteader.feature.dashboard.DashboardState
import com.markduenas.homesteader.feature.dashboard.UpcomingTask
import kotlinx.datetime.LocalDate

// ==================== Dashboard Screen Content ====================

@Composable
fun DashboardScreenContent(state: DashboardState) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Quick Stats Widget
            item {
                DashboardCard(title = "Quick Stats") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(value = state.totalAnimals.toString(), label = "Total", color = MaterialTheme.colorScheme.primary)
                        StatItem(value = state.activeAnimals.toString(), label = "Active", color = MaterialTheme.colorScheme.tertiary)
                        StatItem(value = (state.animalsByStatus[AnimalStatus.SOLD] ?: 0).toString(), label = "Sold", color = MaterialTheme.colorScheme.secondary)
                        StatItem(value = (state.animalsByStatus[AnimalStatus.DECEASED] ?: 0).toString(), label = "Deceased", color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            // Species Breakdown Widget
            if (state.animalsBySpecies.isNotEmpty()) {
                item {
                    DashboardCard(title = "By Species") {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(state.animalsBySpecies.entries.toList()) { (species, count) ->
                                SpeciesChip(species = species, count = count)
                            }
                        }
                    }
                }
            }

            // Upcoming Tasks Widget
            item {
                val title = if (state.overdueCount > 0) {
                    "Upcoming Tasks (${state.overdueCount} overdue)"
                } else {
                    "Upcoming Tasks"
                }
                DashboardCard(title = title) {
                    if (state.upcomingTasks.isEmpty()) {
                        Text(
                            text = "No upcoming tasks",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.upcomingTasks.take(5).forEach { task ->
                                TaskItem(task = task)
                            }
                        }
                    }
                }
            }

            // Recent Animals Widget
            if (state.recentAnimals.isNotEmpty()) {
                item {
                    DashboardCard(title = "Recent Animals", action = { TextButton(onClick = {}) { Text("View All") } }) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(state.recentAnimals) { animal ->
                                AnimalCard(animal = animal)
                            }
                        }
                    }
                }
            }

            // Recent Activity Widget
            if (state.recentEvents.isNotEmpty()) {
                item {
                    DashboardCard(title = "Recent Activity") {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            state.recentEvents.take(5).forEach { event ->
                                ActivityItem(event = event)
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }
}

@Composable
private fun DashboardCard(
    title: String,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                action?.invoke()
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun StatItem(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SpeciesChip(species: Species, count: Int) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(24.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = species.displayName.first().uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = species.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "$count animals",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun TaskItem(task: UpcomingTask) {
    val indicatorColor = if (task.isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    val dateColor = if (task.isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(modifier = Modifier.size(8.dp), shape = CircleShape, color = indicatorColor) {}
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyMedium,
                color = if (task.isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            task.animalName?.let { name ->
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Text(
            text = if (task.isOverdue) "OVERDUE" else task.dueDate,
            style = MaterialTheme.typography.bodySmall,
            color = dateColor
        )
    }
}

@Composable
private fun AnimalCard(animal: Animal) {
    Card(
        modifier = Modifier.width(140.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = animal.displayName.first().uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = animal.displayName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = animal.species.displayName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ActivityItem(event: AnimalEvent) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(8.dp),
            shape = CircleShape,
            color = when (event.eventType.category) {
                EventCategory.HEALTH -> MaterialTheme.colorScheme.error
                EventCategory.BREEDING -> MaterialTheme.colorScheme.tertiary
                EventCategory.PRODUCTION -> MaterialTheme.colorScheme.secondary
                EventCategory.WEIGHT -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.outline
            }
        ) {}
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.eventType.displayName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            event.notes?.let { notes ->
                Text(
                    text = notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ==================== Animal List Screen Content ====================

@Composable
fun AnimalListScreenContent(state: AnimalListState) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { },
                placeholder = { Text("Search by name, tag, or breed...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true
            )

            // Filter chips
            if (state.availableSpecies.isNotEmpty()) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = state.selectedSpecies == null,
                            onClick = { },
                            label = { Text("All Species") }
                        )
                        state.availableSpecies.forEach { species ->
                            FilterChip(
                                selected = state.selectedSpecies == species,
                                onClick = { },
                                label = { Text(species.displayName) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = state.selectedStatus == null,
                            onClick = { },
                            label = { Text("All Status") }
                        )
                        AnimalStatus.entries.forEach { status ->
                            FilterChip(
                                selected = state.selectedStatus == status,
                                onClick = { },
                                label = { Text(status.displayName) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Animal list
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.filteredAnimals) { animal ->
                    AnimalListItem(animal = animal)
                }
            }
        }
    }
}

@Composable
private fun AnimalListItem(animal: Animal) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = animal.displayName.first().uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = animal.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = animal.species.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    animal.breed?.let { breed ->
                        Text(
                            text = breed,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Surface(
                shape = MaterialTheme.shapes.small,
                color = when (animal.status) {
                    AnimalStatus.ACTIVE -> MaterialTheme.colorScheme.primaryContainer
                    AnimalStatus.SOLD -> MaterialTheme.colorScheme.tertiaryContainer
                    AnimalStatus.DECEASED -> MaterialTheme.colorScheme.errorContainer
                    AnimalStatus.TRANSFERRED -> MaterialTheme.colorScheme.secondaryContainer
                }
            ) {
                Text(
                    text = animal.status.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = when (animal.status) {
                        AnimalStatus.ACTIVE -> MaterialTheme.colorScheme.onPrimaryContainer
                        AnimalStatus.SOLD -> MaterialTheme.colorScheme.onTertiaryContainer
                        AnimalStatus.DECEASED -> MaterialTheme.colorScheme.onErrorContainer
                        AnimalStatus.TRANSFERRED -> MaterialTheme.colorScheme.onSecondaryContainer
                    }
                )
            }
        }
    }
}

// ==================== Calendar Screen Content ====================

@Composable
fun CalendarScreenContent(state: CalendarState) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Month navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "<",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = formatMonthYear(state.currentMonth),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = ">",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Calendar grid
            CalendarGrid(
                currentMonth = state.currentMonth,
                selectedDate = state.selectedDate,
                remindersByDate = state.remindersByDate
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
                        ReminderCard(reminder = reminder)
                    }
                    item { Spacer(modifier = Modifier.height(72.dp)) }
                }
            }
        }
    }
}

@Composable
private fun CalendarGrid(
    currentMonth: LocalDate,
    selectedDate: LocalDate,
    remindersByDate: Map<LocalDate, List<CalendarReminder>>
) {
    val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
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

        val calendarDays = getCalendarDays(currentMonth)

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(300.dp),
            userScrollEnabled = false
        ) {
            items(calendarDays) { day ->
                CalendarDay(
                    date = day,
                    isCurrentMonth = day?.monthNumber == currentMonth.monthNumber,
                    isToday = false,
                    isSelected = day == selectedDate,
                    hasReminders = day?.let { remindersByDate[it]?.isNotEmpty() == true } ?: false,
                    hasOverdue = day?.let { date ->
                        remindersByDate[date]?.any { it.isOverdue } == true
                    } ?: false
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
    hasOverdue: Boolean
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isToday -> MaterialTheme.colorScheme.primaryContainer
                    else -> MaterialTheme.colorScheme.surface
                }
            ),
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
private fun ReminderCard(reminder: CalendarReminder) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
            Surface(
                modifier = Modifier.size(8.dp),
                shape = CircleShape,
                color = getReminderTypeColor(reminder.reminderType)
            ) {}
            Spacer(modifier = Modifier.width(12.dp))
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
            }
            TextButton(onClick = {}) { Text("Done", color = MaterialTheme.colorScheme.primary) }
            TextButton(onClick = {}) { Text("Delete", color = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
private fun getReminderTypeColor(type: ReminderType): Color {
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
    val adjustedFirstDay = (firstDayOfWeek + 1) % 7

    val days = mutableListOf<LocalDate?>()
    repeat(adjustedFirstDay) { days.add(null) }
    for (day in 1..daysInMonth) {
        days.add(LocalDate(month.year, month.monthNumber, day))
    }
    while (days.size < 42) { days.add(null) }

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
