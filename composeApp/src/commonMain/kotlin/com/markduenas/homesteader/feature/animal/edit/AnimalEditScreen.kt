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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.markduenas.homesteader.core.designsystem.components.DatePickerField
import com.markduenas.homesteader.core.designsystem.components.ImageCropDialog
import com.markduenas.homesteader.core.designsystem.components.LoadingIndicator
import com.markduenas.homesteader.core.util.rememberImagePickerLauncher
import com.markduenas.homesteader.domain.model.AnimalStatus
import com.markduenas.homesteader.domain.model.Sex
import com.markduenas.homesteader.domain.model.Species
import kotlinx.datetime.LocalDate
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
                    is AnimalEditEffect.ShowError -> {}
                }
            }
        }

        AnimalEditContent(
            state = state,
            onIntent = viewModel::handleIntent,
            onBackClick = { navigator.pop() },
            isEditing = animalId != null
        )

        // Status transition dialog (SOLD / TRANSFERRED / DECEASED)
        if (state.showStatusTransitionDialog) {
            StatusTransitionDialog(
                animalName = state.name.ifBlank { state.tagId },
                newStatus = state.status,
                species = state.species,
                birthDate = state.birthDate.ifBlank { null }?.let {
                    runCatching { LocalDate.parse(it) }.getOrNull()
                },
                onConfirm = { data ->
                    viewModel.handleIntent(AnimalEditIntent.ConfirmStatusTransition(data))
                },
                onDismiss = { viewModel.handleIntent(AnimalEditIntent.DismissStatusTransitionDialog) }
            )
        }

        // Photo crop dialog
        state.pendingPhotoUri?.let { uri ->
            ImageCropDialog(
                sourceUri = uri,
                onCropComplete = { croppedUri ->
                    viewModel.handleIntent(AnimalEditIntent.UpdatePhotoUri(croppedUri))
                },
                onDismiss = { viewModel.handleIntent(AnimalEditIntent.DismissCropDialog) }
            )
        }
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
                    TextButton(onClick = onBackClick) { Text("Cancel") }
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
                AnimalEditForm(state = state, onIntent = onIntent)
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
    val pickImage = rememberImagePickerLauncher { uri ->
        if (uri != null) onIntent(AnimalEditIntent.SetPendingPhoto(uri))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Photo picker
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (state.photoUri.isNotBlank()) {
                AsyncImage(
                    model = state.photoUri,
                    contentDescription = "Animal photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                )
            } else {
                Surface(
                    modifier = Modifier.size(120.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = state.name.firstOrNull()?.uppercase()
                                ?: state.tagId.firstOrNull()?.uppercase()
                                ?: "?",
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            TextButton(onClick = { pickImage() }) {
                Text("📷  Change Photo")
            }
        }

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
                onValueChange = {},
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

        // Birth date
        DatePickerField(
            value = state.birthDate,
            onDateSelected = { onIntent(AnimalEditIntent.UpdateBirthDate(it)) },
            label = "Birth Date",
            modifier = Modifier.fillMaxWidth()
        )

        // Acquisition date
        DatePickerField(
            value = state.acquisitionDate,
            onDateSelected = { onIntent(AnimalEditIntent.UpdateAcquisitionDate(it)) },
            label = "Acquisition Date",
            modifier = Modifier.fillMaxWidth()
        )

        // Status dropdown (editing only)
        if (state.isEditing) {
            var statusExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = statusExpanded,
                onExpandedChange = { statusExpanded = it }
            ) {
                OutlinedTextField(
                    value = state.status.displayName,
                    onValueChange = {},
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

        state.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

