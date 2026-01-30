package com.markduenas.homesteader.feature.dashboard

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.markduenas.homesteader.core.designsystem.accessibility.AnimalAccessibility
import com.markduenas.homesteader.core.designsystem.accessibility.NavigationAccessibility
import com.markduenas.homesteader.core.designsystem.accessibility.StatAccessibility
import com.markduenas.homesteader.core.designsystem.accessibility.TaskAccessibility
import com.markduenas.homesteader.core.designsystem.components.LoadingScreen
import com.markduenas.homesteader.core.util.DateTimeUtil
import com.markduenas.homesteader.domain.model.Animal
import com.markduenas.homesteader.domain.model.AnimalEvent
import com.markduenas.homesteader.domain.model.AnimalStatus
import com.markduenas.homesteader.domain.model.EventCategory
import com.markduenas.homesteader.domain.model.Species
import com.markduenas.homesteader.feature.animal.detail.AnimalDetailScreen
import com.markduenas.homesteader.feature.animal.edit.AnimalEditScreen
import com.markduenas.homesteader.feature.animal.list.AnimalListScreen

class DashboardScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<DashboardViewModel>()
        val state by viewModel.state.collectAsState()

        LaunchedEffect(Unit) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    is DashboardEffect.NavigateToAnimalDetail -> {
                        navigator.push(AnimalDetailScreen(effect.animalId))
                    }
                    DashboardEffect.NavigateToAnimalList -> {
                        navigator.push(AnimalListScreen())
                    }
                    DashboardEffect.NavigateToAddAnimal -> {
                        navigator.push(AnimalEditScreen())
                    }
                }
            }
        }

        DashboardContent(
            state = state,
            onIntent = viewModel::handleIntent
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardContent(
    state: DashboardState,
    onIntent: (DashboardIntent) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Homesteader") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onIntent(DashboardIntent.AddAnimal) },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.semantics {
                    contentDescription = NavigationAccessibility.addButtonDescription("animal")
                }
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
                LoadingScreen(
                    message = "Loading dashboard...",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Quick Stats Widget
                    item {
                        QuickStatsWidget(
                            totalAnimals = state.totalAnimals,
                            activeAnimals = state.activeAnimals,
                            animalsByStatus = state.animalsByStatus,
                            onViewAll = { onIntent(DashboardIntent.ViewAllAnimals) }
                        )
                    }

                    // Species Breakdown Widget
                    if (state.animalsBySpecies.isNotEmpty()) {
                        item {
                            SpeciesBreakdownWidget(
                                animalsBySpecies = state.animalsBySpecies
                            )
                        }
                    }

                    // Upcoming Tasks Widget
                    item {
                        UpcomingTasksWidget(
                            tasks = state.upcomingTasks,
                            overdueCount = state.overdueCount,
                            onCompleteTask = { taskId ->
                                onIntent(DashboardIntent.CompleteTask(taskId))
                            }
                        )
                    }

                    // Recent Animals Widget
                    if (state.recentAnimals.isNotEmpty()) {
                        item {
                            RecentAnimalsWidget(
                                animals = state.recentAnimals,
                                onAnimalClick = { onIntent(DashboardIntent.SelectAnimal(it.id)) },
                                onViewAll = { onIntent(DashboardIntent.ViewAllAnimals) }
                            )
                        }
                    }

                    // Recent Activity Widget
                    if (state.recentEvents.isNotEmpty()) {
                        item {
                            RecentActivityWidget(
                                events = state.recentEvents
                            )
                        }
                    }

                    // Bottom spacing for FAB
                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }

            state.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun QuickStatsWidget(
    totalAnimals: Int,
    activeAnimals: Int,
    animalsByStatus: Map<AnimalStatus, Int>,
    onViewAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    DashboardCard(
        title = "Quick Stats",
        action = { TextButton(onClick = onViewAll) { Text("View All") } },
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                value = totalAnimals.toString(),
                label = "Total",
                color = MaterialTheme.colorScheme.primary
            )
            StatItem(
                value = activeAnimals.toString(),
                label = "Active",
                color = MaterialTheme.colorScheme.tertiary
            )
            StatItem(
                value = (animalsByStatus[AnimalStatus.SOLD] ?: 0).toString(),
                label = "Sold",
                color = MaterialTheme.colorScheme.secondary
            )
            StatItem(
                value = (animalsByStatus[AnimalStatus.DECEASED] ?: 0).toString(),
                label = "Deceased",
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    val accessibilityDescription = StatAccessibility.statCardDescription(
        title = label,
        value = value,
        subtitle = null
    )

    Column(
        modifier = modifier.semantics { contentDescription = accessibilityDescription },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
private fun SpeciesBreakdownWidget(
    animalsBySpecies: Map<Species, Int>,
    modifier: Modifier = Modifier
) {
    DashboardCard(
        title = "By Species",
        modifier = modifier
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(animalsBySpecies.entries.toList()) { (species, count) ->
                SpeciesChip(species = species, count = count)
            }
        }
    }
}

@Composable
private fun SpeciesChip(
    species: Species,
    count: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
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
private fun UpcomingTasksWidget(
    tasks: List<UpcomingTask>,
    overdueCount: Int = 0,
    onCompleteTask: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val title = if (overdueCount > 0) {
        "Upcoming Tasks ($overdueCount overdue)"
    } else {
        "Upcoming Tasks"
    }

    DashboardCard(
        title = title,
        modifier = modifier
    ) {
        if (tasks.isEmpty()) {
            Text(
                text = "No upcoming tasks. Events like breeding and vaccinations will generate automatic reminders.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                tasks.take(5).forEach { task ->
                    TaskItem(
                        task = task,
                        onComplete = { onCompleteTask(task.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskItem(
    task: UpcomingTask,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val indicatorColor = if (task.isOverdue) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary
    }

    val dateColor = if (task.isOverdue) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.outline
    }

    val accessibilityDescription = TaskAccessibility.taskCardDescription(
        title = task.title,
        dueDate = task.dueDate,
        isOverdue = task.isOverdue
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = accessibilityDescription }
            .clickable(onClick = onComplete),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(8.dp),
            shape = CircleShape,
            color = indicatorColor
        ) {}
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
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = if (task.isOverdue) "OVERDUE" else task.dueDate,
                style = MaterialTheme.typography.bodySmall,
                color = dateColor
            )
            if (!task.isOverdue) {
                Text(
                    text = task.dueDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun RecentAnimalsWidget(
    animals: List<Animal>,
    onAnimalClick: (Animal) -> Unit,
    onViewAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    DashboardCard(
        title = "Recent Animals",
        action = { TextButton(onClick = onViewAll) { Text("View All") } },
        modifier = modifier
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(animals) { animal ->
                AnimalCard(
                    animal = animal,
                    onClick = { onAnimalClick(animal) }
                )
            }
        }
    }
}

@Composable
private fun AnimalCard(
    animal: Animal,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accessibilityDescription = AnimalAccessibility.animalCardDescription(
        name = animal.displayName,
        species = animal.species.displayName,
        status = animal.status.displayName,
        tagId = animal.tagId
    )

    Card(
        modifier = modifier
            .width(140.dp)
            .semantics { contentDescription = accessibilityDescription }
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
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
private fun RecentActivityWidget(
    events: List<AnimalEvent>,
    modifier: Modifier = Modifier
) {
    DashboardCard(
        title = "Recent Activity",
        modifier = modifier
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            events.take(5).forEach { event ->
                ActivityItem(event = event)
            }
        }
    }
}

@Composable
private fun ActivityItem(
    event: AnimalEvent,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
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

        Text(
            text = DateTimeUtil.formatShortDate(event.eventDate),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
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
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.semantics { heading() }
                )
                action?.invoke()
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}
