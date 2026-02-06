package com.markduenas.homesteader.feature.event

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.markduenas.homesteader.core.designsystem.components.LoadingIndicator
import com.markduenas.homesteader.domain.model.EventCategory
import com.markduenas.homesteader.domain.model.EventType
import org.koin.core.parameter.parametersOf

data class EventAddScreen(
    val animalId: String,
    val animalName: String,
    val eventId: String? = null
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<EventAddViewModel> { parametersOf(animalId, animalName, eventId) }
        val state by viewModel.state.collectAsState()
        var showDeleteDialog by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    is EventAddEffect.NavigateBack -> navigator.pop()
                    is EventAddEffect.ShowError -> {
                        // Show snackbar or toast
                    }
                }
            }
        }

        if (showDeleteDialog) {
            DeleteEventDialog(
                onConfirm = {
                    showDeleteDialog = false
                    viewModel.handleIntent(EventAddIntent.DeleteEvent)
                },
                onDismiss = { showDeleteDialog = false }
            )
        }

        EventAddContent(
            state = state,
            onIntent = viewModel::handleIntent,
            onBackClick = { navigator.pop() },
            onDeleteClick = { showDeleteDialog = true }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventAddContent(
    state: EventAddState,
    onIntent: (EventAddIntent) -> Unit,
    onBackClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditing) "Edit Event" else "Add Event") },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text("Cancel")
                    }
                },
                actions = {
                    if (state.isEditing) {
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
            FloatingActionButton(
                onClick = { onIntent(EventAddIntent.Save) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    text = "Save",
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp)
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
                EventAddForm(
                    state = state,
                    onIntent = onIntent
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventAddForm(
    state: EventAddState,
    onIntent: (EventAddIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Animal name display
        Text(
            text = "Recording event for: ${state.animalName}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        // Event Category Selection
        Text(
            text = "Event Category",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val mainCategories = listOf(
                EventCategory.HEALTH,
                EventCategory.BREEDING,
                EventCategory.PRODUCTION,
                EventCategory.WEIGHT
            )
            mainCategories.forEach { category ->
                FilterChip(
                    selected = state.selectedCategory == category,
                    onClick = { onIntent(EventAddIntent.SetCategory(category)) },
                    label = { Text(category.displayName) }
                )
            }
        }

        // Event Type Selection based on category
        var eventTypeExpanded by remember { mutableStateOf(false) }
        val availableTypes = EventType.entries.filter { it.category == state.selectedCategory }

        ExposedDropdownMenuBox(
            expanded = eventTypeExpanded,
            onExpandedChange = { eventTypeExpanded = it }
        ) {
            OutlinedTextField(
                value = state.selectedEventType.displayName,
                onValueChange = { },
                readOnly = true,
                label = { Text("Event Type") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = eventTypeExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(
                expanded = eventTypeExpanded,
                onDismissRequest = { eventTypeExpanded = false }
            ) {
                availableTypes.forEach { eventType ->
                    DropdownMenuItem(
                        text = { Text(eventType.displayName) },
                        onClick = {
                            onIntent(EventAddIntent.SetEventType(eventType))
                            eventTypeExpanded = false
                        }
                    )
                }
            }
        }

        // Event Date
        OutlinedTextField(
            value = state.eventDate,
            onValueChange = { onIntent(EventAddIntent.UpdateEventDate(it)) },
            label = { Text("Event Date") },
            placeholder = { Text("YYYY-MM-DD") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        // Category-specific fields
        when (state.selectedCategory) {
            EventCategory.HEALTH -> HealthEventFields(state, onIntent)
            EventCategory.BREEDING -> BreedingEventFields(state, onIntent)
            EventCategory.PRODUCTION -> ProductionEventFields(state, onIntent)
            EventCategory.WEIGHT -> WeightEventFields(state, onIntent)
            else -> { /* No additional fields */ }
        }

        // Notes field (always visible)
        OutlinedTextField(
            value = state.notes,
            onValueChange = { onIntent(EventAddIntent.UpdateNotes(it)) },
            label = { Text("Notes") },
            placeholder = { Text("Additional notes about this event") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            maxLines = 5
        )

        // Error message
        state.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
    }
}

@Composable
private fun HealthEventFields(
    state: EventAddState,
    onIntent: (EventAddIntent) -> Unit
) {
    OutlinedTextField(
        value = state.medicationName,
        onValueChange = { onIntent(EventAddIntent.UpdateMedicationName(it)) },
        label = { Text("Medication/Vaccine Name") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )

    OutlinedTextField(
        value = state.dosage,
        onValueChange = { onIntent(EventAddIntent.UpdateDosage(it)) },
        label = { Text("Dosage") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )

    OutlinedTextField(
        value = state.veterinarian,
        onValueChange = { onIntent(EventAddIntent.UpdateVeterinarian(it)) },
        label = { Text("Veterinarian") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )

    if (state.selectedEventType in listOf(EventType.ILLNESS, EventType.INJURY, EventType.VET_VISIT)) {
        OutlinedTextField(
            value = state.diagnosis,
            onValueChange = { onIntent(EventAddIntent.UpdateDiagnosis(it)) },
            label = { Text("Diagnosis") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BreedingEventFields(
    state: EventAddState,
    onIntent: (EventAddIntent) -> Unit
) {
    when (state.selectedEventType) {
        EventType.BRED -> {
            OutlinedTextField(
                value = state.sireName,
                onValueChange = { onIntent(EventAddIntent.UpdateSireName(it)) },
                label = { Text("Sire Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = state.sireId,
                onValueChange = { onIntent(EventAddIntent.UpdateSireId(it)) },
                label = { Text("Sire ID/Tag") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Breeding method selection
            Column {
                Text(
                    text = "Breeding Method",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                val methods = listOf("Natural", "AI")
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    methods.forEachIndexed { index, method ->
                        SegmentedButton(
                            selected = state.breedingMethod == method,
                            onClick = { onIntent(EventAddIntent.UpdateBreedingMethod(method)) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = methods.size)
                        ) {
                            Text(method)
                        }
                    }
                }
            }
        }

        EventType.PREGNANCY_CHECK -> {
            Column {
                Text(
                    text = "Pregnancy Result",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                val results = listOf("Pregnant" to true, "Not Pregnant" to false, "Unknown" to null)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    results.forEachIndexed { index, (label, value) ->
                        SegmentedButton(
                            selected = state.isPregnant == value,
                            onClick = { onIntent(EventAddIntent.UpdateIsPregnant(value)) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = results.size)
                        ) {
                            Text(label)
                        }
                    }
                }
            }

            if (state.isPregnant == true) {
                OutlinedTextField(
                    value = state.expectedDueDate,
                    onValueChange = { onIntent(EventAddIntent.UpdateExpectedDueDate(it)) },
                    label = { Text("Expected Due Date") },
                    placeholder = { Text("YYYY-MM-DD") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }

        EventType.BIRTH -> {
            OutlinedTextField(
                value = state.offspringCount,
                onValueChange = { onIntent(EventAddIntent.UpdateOffspringCount(it)) },
                label = { Text("Number of Offspring") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        else -> { /* No additional fields for heat observed, weaning */ }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductionEventFields(
    state: EventAddState,
    onIntent: (EventAddIntent) -> Unit
) {
    when (state.selectedEventType) {
        EventType.MILK_RECORD -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = state.milkQuantity,
                    onValueChange = { onIntent(EventAddIntent.UpdateMilkQuantity(it)) },
                    label = { Text("Quantity") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                // Unit selection
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Unit",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val units = listOf("gallons", "liters")
                    SingleChoiceSegmentedButtonRow {
                        units.forEachIndexed { index, unit ->
                            SegmentedButton(
                                selected = state.milkUnit == unit,
                                onClick = { onIntent(EventAddIntent.UpdateMilkUnit(unit)) },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = units.size)
                            ) {
                                Text(unit.take(3))
                            }
                        }
                    }
                }
            }
        }

        EventType.EGG_COLLECTION -> {
            OutlinedTextField(
                value = state.eggCount,
                onValueChange = { onIntent(EventAddIntent.UpdateEggCount(it)) },
                label = { Text("Number of Eggs") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        EventType.SHEARING -> {
            OutlinedTextField(
                value = state.fiberWeight,
                onValueChange = { onIntent(EventAddIntent.UpdateFiberWeight(it)) },
                label = { Text("Fiber Weight (lbs)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        }

        else -> { /* No additional fields */ }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeightEventFields(
    state: EventAddState,
    onIntent: (EventAddIntent) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = state.weight,
            onValueChange = { onIntent(EventAddIntent.UpdateWeight(it)) },
            label = { Text("Weight") },
            modifier = Modifier.weight(1f),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Unit",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            val units = listOf("lbs", "kg")
            SingleChoiceSegmentedButtonRow {
                units.forEachIndexed { index, unit ->
                    SegmentedButton(
                        selected = state.weightUnit == unit,
                        onClick = { onIntent(EventAddIntent.UpdateWeightUnit(unit)) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = units.size)
                    ) {
                        Text(unit)
                    }
                }
            }
        }
    }

    OutlinedTextField(
        value = state.bodyCondition,
        onValueChange = { onIntent(EventAddIntent.UpdateBodyCondition(it)) },
        label = { Text("Body Condition Score (1-9)") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}

@Composable
private fun DeleteEventDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Event") },
        text = { Text("Are you sure you want to delete this event? This action cannot be undone.") },
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
