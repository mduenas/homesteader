package com.markduenas.homesteader.feature.animal.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import com.markduenas.homesteader.core.designsystem.components.LoadingIndicator
import com.markduenas.homesteader.core.util.DateTimeUtil
import com.markduenas.homesteader.core.util.formatDecimal
import com.markduenas.homesteader.domain.model.Animal
import com.markduenas.homesteader.domain.model.AnimalEvent
import com.markduenas.homesteader.domain.model.AnimalStatus
import com.markduenas.homesteader.domain.model.EventCategory
import com.markduenas.homesteader.domain.model.EventType
import com.markduenas.homesteader.domain.model.HarvestEventData
import com.markduenas.homesteader.domain.model.StatusChangeEventData
import com.markduenas.homesteader.feature.animal.edit.AnimalEditScreen
import com.markduenas.homesteader.feature.event.EventAddScreen
import kotlinx.datetime.LocalDate
import org.koin.core.parameter.parametersOf

data class AnimalDetailScreen(val animalId: String) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<AnimalDetailViewModel> { parametersOf(animalId) }
        val state by viewModel.state.collectAsState()
        var showDeleteDialog by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    is AnimalDetailEffect.NavigateBack -> navigator.pop()
                    is AnimalDetailEffect.NavigateToEdit -> {
                        navigator.push(AnimalEditScreen(effect.animalId))
                    }
                    is AnimalDetailEffect.NavigateToAddEvent -> {
                        navigator.push(EventAddScreen(effect.animalId, effect.animalName))
                    }
                    is AnimalDetailEffect.NavigateToEditEvent -> {
                        navigator.push(EventAddScreen(effect.animalId, effect.animalName, effect.eventId))
                    }
                }
            }
        }

        if (showDeleteDialog) {
            DeleteConfirmationDialog(
                animalName = state.animal?.displayName ?: "this animal",
                onConfirm = {
                    showDeleteDialog = false
                    viewModel.handleIntent(AnimalDetailIntent.DeleteAnimal)
                },
                onDismiss = { showDeleteDialog = false }
            )
        }

        AnimalDetailContent(
            state = state,
            onIntent = viewModel::handleIntent,
            onBackClick = { navigator.pop() },
            onDeleteClick = { showDeleteDialog = true }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnimalDetailContent(
    state: AnimalDetailState,
    onIntent: (AnimalDetailIntent) -> Unit,
    onBackClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.animal?.displayName ?: "Animal Details") },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text("Back")
                    }
                },
                actions = {
                    state.animal?.let {
                        TextButton(onClick = { onIntent(AnimalDetailIntent.EditAnimal) }) {
                            Text("Edit")
                        }
                        TextButton(onClick = onDeleteClick) {
                            Text("Delete", color = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            state.animal?.let {
                FloatingActionButton(
                    onClick = { onIntent(AnimalDetailIntent.AddEvent) },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = "+ Event",
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> LoadingIndicator()
                state.animal != null -> AnimalDetailBody(
                    animal = state.animal,
                    events = state.events,
                    onEventClick = { event -> onIntent(AnimalDetailIntent.EditEvent(event.id)) }
                )
                state.error != null -> {
                    Text(
                        text = state.error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimalDetailBody(
    animal: Animal,
    events: List<AnimalEvent>,
    onEventClick: (AnimalEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header with avatar / photo
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!animal.photoUri.isNullOrBlank()) {
                AsyncImage(
                    model = animal.photoUri,
                    contentDescription = "${animal.displayName} photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                )
            } else {
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = animal.displayName.first().uppercase(),
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = animal.displayName,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${animal.species.displayName} - ${animal.sex.displayName}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = animal.status.displayName,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sale / Harvest / Transfer summary card
        if (animal.status != AnimalStatus.ACTIVE) {
            val transitionEvent = events.firstOrNull {
                it.eventType == EventType.STATUS_CHANGE || it.eventType == EventType.HARVEST
            }
            SaleHarvestSummaryCard(animal = animal, event = transitionEvent)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Basic Information Card
        DetailCard(title = "Basic Information") {
            animal.tagId?.let { DetailRow("Tag ID", it) }
            animal.breed?.let { DetailRow("Breed", it) }
            animal.birthDate?.let { DetailRow("Birth Date", DateTimeUtil.formatDate(it)) }
            animal.acquisitionDate?.let { DetailRow("Acquisition Date", DateTimeUtil.formatDate(it)) }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lineage Card (if has parents)
        if (animal.motherId != null || animal.fatherId != null) {
            DetailCard(title = "Lineage") {
                animal.motherId?.let { DetailRow("Mother ID", it) }
                animal.fatherId?.let { DetailRow("Father ID", it) }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Notes Card
        if (!animal.notes.isNullOrBlank()) {
            DetailCard(title = "Notes") {
                Text(
                    text = animal.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Custom Fields Card
        if (animal.customFields.isNotEmpty()) {
            DetailCard(title = "Custom Fields") {
                animal.customFields.forEach { (key, value) ->
                    DetailRow(key, value)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Events Timeline
        EventsTimeline(events = events, onEventClick = onEventClick)

        Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
    }
}

@Composable
private fun EventsTimeline(
    events: List<AnimalEvent>,
    onEventClick: (AnimalEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    DetailCard(title = "Event History (${events.size})", modifier = modifier) {
        if (events.isEmpty()) {
            Text(
                text = "No events recorded yet. Tap + Event to add one.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            events.forEach { event ->
                EventTimelineItem(
                    event = event,
                    onClick = { onEventClick(event) }
                )
                if (event != events.last()) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun EventTimelineItem(
    event: AnimalEvent,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Category indicator
            Surface(
                modifier = Modifier
                    .size(8.dp)
                    .padding(top = 6.dp),
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = event.eventType.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = DateTimeUtil.formatShortDate(event.eventDate),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                event.notes?.let { notes ->
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun DeleteConfirmationDialog(
    animalName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Animal") },
        text = { Text("Are you sure you want to delete $animalName? This action cannot be undone.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
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
private fun SaleHarvestSummaryCard(
    animal: Animal,
    event: AnimalEvent?,
    modifier: Modifier = Modifier
) {
    val containerColor = when (animal.status) {
        AnimalStatus.SOLD -> MaterialTheme.colorScheme.secondaryContainer
        AnimalStatus.DECEASED -> MaterialTheme.colorScheme.errorContainer
        AnimalStatus.TRANSFERRED -> MaterialTheme.colorScheme.tertiaryContainer
        AnimalStatus.ACTIVE -> MaterialTheme.colorScheme.surface
    }
    val onContainerColor = when (animal.status) {
        AnimalStatus.SOLD -> MaterialTheme.colorScheme.onSecondaryContainer
        AnimalStatus.DECEASED -> MaterialTheme.colorScheme.onErrorContainer
        AnimalStatus.TRANSFERRED -> MaterialTheme.colorScheme.onTertiaryContainer
        AnimalStatus.ACTIVE -> MaterialTheme.colorScheme.onSurface
    }
    val title = when (animal.status) {
        AnimalStatus.SOLD -> "Sale Record"
        AnimalStatus.DECEASED -> "Death / Harvest Record"
        AnimalStatus.TRANSFERRED -> "Transfer Record"
        AnimalStatus.ACTIVE -> ""
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = onContainerColor
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (event == null) {
                Text(
                    text = "No details recorded.",
                    style = MaterialTheme.typography.bodySmall,
                    color = onContainerColor.copy(alpha = 0.7f)
                )
            } else {
                // Age at event date
                val ageText = animal.birthDate?.let { birth ->
                    val totalDays = (event.eventDate.toEpochDays() - birth.toEpochDays()).toInt()
                    when {
                        totalDays < 0 -> null
                        totalDays < 30 -> "$totalDays days"
                        totalDays < 365 -> "${totalDays / 30} months, ${totalDays % 30} days"
                        else -> {
                            val years = totalDays / 365
                            val months = (totalDays % 365) / 30
                            if (months > 0) "$years yr $months mo" else "$years years"
                        }
                    }
                }

                SummaryRow("Date", DateTimeUtil.formatDate(event.eventDate), onContainerColor)
                ageText?.let { SummaryRow("Age", it, onContainerColor) }

                when (val data = event.eventData) {
                    is StatusChangeEventData -> {
                        data.salePrice?.let {
                            SummaryRow("Sale Price", "$${it.formatDecimal()}", onContainerColor, bold = true)
                        }
                        data.saleWeight?.let {
                            SummaryRow("Live Weight at Sale", "$it ${data.weightUnit}", onContainerColor)
                        }
                        data.buyer?.let { SummaryRow("Buyer", it, onContainerColor) }
                        data.buyerContact?.let {
                            SummaryRow("Contact", it, onContainerColor, secondary = true)
                        }
                        data.reason?.let {
                            SummaryRow("Notes", it, onContainerColor, secondary = true)
                        }
                    }
                    is HarvestEventData -> {
                        data.liveWeight?.let {
                            SummaryRow("Live Weight", "$it ${data.weightUnit}", onContainerColor)
                        }
                        data.dressedWeight?.let {
                            SummaryRow("Dressed Weight", "$it ${data.weightUnit}", onContainerColor)
                        }
                        data.numberOfAnimals?.let { n ->
                            if (n > 1) SummaryRow("Number of Animals", "$n", onContainerColor)
                        }
                        data.purpose?.let { SummaryRow("Purpose", it.replaceFirstChar { c -> c.uppercaseChar() }, onContainerColor) }
                        data.revenue?.let { rev ->
                            SummaryRow("Gross Revenue", "$${rev.formatDecimal()}", onContainerColor, bold = true)
                        }
                        // Processing costs
                        val killFee = data.killFee
                        val butcherRate = data.butcherPricePerPound
                        val dressedWt = data.dressedWeight
                        val processingCost = (killFee ?: 0.0) + ((butcherRate ?: 0.0) * (dressedWt ?: 0.0))
                        if (processingCost > 0.0) {
                            killFee?.let { SummaryRow("Kill Fee", "$${it.formatDecimal()}", onContainerColor, secondary = true) }
                            if (butcherRate != null && dressedWt != null) {
                                SummaryRow(
                                    "Butcher Cost",
                                    "$${(butcherRate * dressedWt).formatDecimal()} ($${butcherRate.formatDecimal()}/lb)",
                                    onContainerColor, secondary = true
                                )
                            }
                            val netRev = (data.revenue ?: 0.0) - processingCost
                            SummaryRow("Net Revenue", "$${netRev.formatDecimal()}", onContainerColor, bold = true)
                        }
                        // $/lb dressed
                        val rev = data.revenue
                        if (rev != null && dressedWt != null && dressedWt > 0.0) {
                            SummaryRow(
                                "Revenue/lb Dressed",
                                "$${(rev / dressedWt).formatDecimal()}",
                                onContainerColor, secondary = true
                            )
                        }
                        data.buyer?.let { SummaryRow("Buyer", it, onContainerColor) }
                    }
                    else -> {
                        event.notes?.let {
                            SummaryRow("Notes", it, onContainerColor, secondary = true)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    bold: Boolean = false,
    secondary: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = if (secondary) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
            color = color.copy(alpha = if (secondary) 0.7f else 1f)
        )
        Text(
            text = value,
            style = if (secondary) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
            color = color.copy(alpha = if (secondary) 0.7f else 1f),
            fontWeight = if (bold) androidx.compose.ui.text.font.FontWeight.Bold else null
        )
    }
}
