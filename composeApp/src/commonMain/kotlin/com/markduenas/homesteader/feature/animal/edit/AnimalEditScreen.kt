package com.markduenas.homesteader.feature.animal.edit

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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import com.markduenas.homesteader.domain.model.AnimalStatus
import com.markduenas.homesteader.domain.model.Sex
import com.markduenas.homesteader.domain.model.Species
import org.koin.core.parameter.parametersOf

data class AnimalEditScreen(val animalId: String? = null) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<AnimalEditViewModel> { parametersOf(animalId) }
        val state by viewModel.state.collectAsState()

        LaunchedEffect(Unit) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    is AnimalEditEffect.NavigateBack -> navigator.pop()
                    is AnimalEditEffect.ShowError -> {
                        // Show snackbar or toast
                    }
                }
            }
        }

        AnimalEditContent(
            state = state,
            onIntent = viewModel::handleIntent,
            onBackClick = { navigator.pop() },
            isEditing = animalId != null
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnimalEditContent(
    state: AnimalEditState,
    onIntent: (AnimalEditIntent) -> Unit,
    onBackClick: () -> Unit,
    isEditing: Boolean
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Animal" else "Add Animal") },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text("Cancel")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onIntent(AnimalEditIntent.Save) },
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
                AnimalEditForm(
                    state = state,
                    onIntent = onIntent
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnimalEditForm(
    state: AnimalEditState,
    onIntent: (AnimalEditIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Name field
        OutlinedTextField(
            value = state.name,
            onValueChange = { onIntent(AnimalEditIntent.UpdateName(it)) },
            label = { Text("Name") },
            placeholder = { Text("Enter animal name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Tag ID field
        OutlinedTextField(
            value = state.tagId,
            onValueChange = { onIntent(AnimalEditIntent.UpdateTagId(it)) },
            label = { Text("Tag ID") },
            placeholder = { Text("Enter tag ID") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Species dropdown
        var speciesExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = speciesExpanded,
            onExpandedChange = { speciesExpanded = it }
        ) {
            OutlinedTextField(
                value = state.species.displayName,
                onValueChange = { },
                readOnly = true,
                label = { Text("Species *") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = speciesExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(
                expanded = speciesExpanded,
                onDismissRequest = { speciesExpanded = false }
            ) {
                Species.entries.filter { it != Species.CUSTOM }.forEach { species ->
                    DropdownMenuItem(
                        text = { Text(species.displayName) },
                        onClick = {
                            onIntent(AnimalEditIntent.UpdateSpecies(species))
                            speciesExpanded = false
                        }
                    )
                }
            }
        }

        // Breed field
        OutlinedTextField(
            value = state.breed,
            onValueChange = { onIntent(AnimalEditIntent.UpdateBreed(it)) },
            label = { Text("Breed") },
            placeholder = { Text("Enter breed") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Sex selection
        Column {
            Text(
                text = "Sex *",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                Sex.entries.forEachIndexed { index, sex ->
                    SegmentedButton(
                        selected = state.sex == sex,
                        onClick = { onIntent(AnimalEditIntent.UpdateSex(sex)) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = Sex.entries.size
                        )
                    ) {
                        Text(sex.displayName)
                    }
                }
            }
        }

        // Birth date field
        OutlinedTextField(
            value = state.birthDate,
            onValueChange = { onIntent(AnimalEditIntent.UpdateBirthDate(it)) },
            label = { Text("Birth Date") },
            placeholder = { Text("YYYY-MM-DD") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        // Acquisition date field
        OutlinedTextField(
            value = state.acquisitionDate,
            onValueChange = { onIntent(AnimalEditIntent.UpdateAcquisitionDate(it)) },
            label = { Text("Acquisition Date") },
            placeholder = { Text("YYYY-MM-DD") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        // Status dropdown (only show for editing)
        if (state.isEditing) {
            var statusExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = statusExpanded,
                onExpandedChange = { statusExpanded = it }
            ) {
                OutlinedTextField(
                    value = state.status.displayName,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Status") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = statusExpanded,
                    onDismissRequest = { statusExpanded = false }
                ) {
                    AnimalStatus.entries.forEach { status ->
                        DropdownMenuItem(
                            text = { Text(status.displayName) },
                            onClick = {
                                onIntent(AnimalEditIntent.UpdateStatus(status))
                                statusExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Parent IDs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = state.motherId,
                onValueChange = { onIntent(AnimalEditIntent.UpdateMotherId(it)) },
                label = { Text("Mother ID") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = state.fatherId,
                onValueChange = { onIntent(AnimalEditIntent.UpdateFatherId(it)) },
                label = { Text("Father ID") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        // Notes field
        OutlinedTextField(
            value = state.notes,
            onValueChange = { onIntent(AnimalEditIntent.UpdateNotes(it)) },
            label = { Text("Notes") },
            placeholder = { Text("Enter any notes about this animal") },
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
