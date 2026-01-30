package com.markduenas.homesteader.feature.animal.list

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.markduenas.homesteader.core.designsystem.accessibility.AnimalAccessibility
import com.markduenas.homesteader.core.designsystem.accessibility.NavigationAccessibility
import com.markduenas.homesteader.core.designsystem.components.AnimalListSkeleton
import com.markduenas.homesteader.core.designsystem.components.EmptyStates
import com.markduenas.homesteader.core.util.DateTimeUtil
import com.markduenas.homesteader.domain.model.Animal
import com.markduenas.homesteader.domain.model.AnimalStatus
import com.markduenas.homesteader.feature.animal.detail.AnimalDetailScreen
import com.markduenas.homesteader.feature.animal.edit.AnimalEditScreen

class AnimalListScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<AnimalListViewModel>()
        val state by viewModel.state.collectAsState()

        LaunchedEffect(Unit) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    is AnimalListEffect.NavigateToDetail -> {
                        navigator.push(AnimalDetailScreen(effect.animalId))
                    }
                    is AnimalListEffect.NavigateToAdd -> {
                        navigator.push(AnimalEditScreen())
                    }
                }
            }
        }

        AnimalListContent(
            state = state,
            onIntent = viewModel::handleIntent
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnimalListContent(
    state: AnimalListState,
    onIntent: (AnimalListIntent) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Animals") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onIntent(AnimalListIntent.AddAnimal) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { onIntent(AnimalListIntent.Search(it)) },
                placeholder = { Text("Search by name, tag, or breed...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true
            )

            // Filter chips
            if (state.availableSpecies.isNotEmpty() || state.selectedStatus != null) {
                FilterChipsRow(
                    state = state,
                    onIntent = onIntent
                )
            }

            // Animal list
            Box(modifier = Modifier.weight(1f)) {
                when {
                    state.isLoading -> {
                        AnimalListSkeleton(
                            itemCount = 5,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    state.allAnimals.isEmpty() -> {
                        EmptyStates.NoAnimals(
                            onAddAnimal = { onIntent(AnimalListIntent.AddAnimal) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    state.filteredAnimals.isEmpty() -> {
                        EmptyStates.NoSearchResults(
                            searchQuery = state.searchQuery.ifBlank { "selected filters" },
                            onClearSearch = { onIntent(AnimalListIntent.ClearFilters) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = state.filteredAnimals,
                                key = { it.id }
                            ) { animal ->
                                AnimalListItem(
                                    animal = animal,
                                    onClick = { onIntent(AnimalListIntent.SelectAnimal(animal.id)) }
                                )
                            }
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
}

@Composable
private fun FilterChipsRow(
    state: AnimalListState,
    onIntent: (AnimalListIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        // Species filter chips
        if (state.availableSpecies.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.selectedSpecies == null,
                    onClick = { onIntent(AnimalListIntent.FilterBySpecies(null)) },
                    label = { Text("All Species") }
                )
                state.availableSpecies.forEach { species ->
                    FilterChip(
                        selected = state.selectedSpecies == species,
                        onClick = { onIntent(AnimalListIntent.FilterBySpecies(species)) },
                        label = { Text(species.displayName) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Status filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = state.selectedStatus == null,
                onClick = { onIntent(AnimalListIntent.FilterByStatus(null)) },
                label = { Text("All Status") }
            )
            AnimalStatus.entries.forEach { status ->
                FilterChip(
                    selected = state.selectedStatus == status,
                    onClick = { onIntent(AnimalListIntent.FilterByStatus(status)) },
                    label = { Text(status.displayName) }
                )
            }
        }

        // Show clear filters button if any filters are active
        if (state.searchQuery.isNotBlank() || state.selectedSpecies != null || state.selectedStatus != null) {
            TextButton(
                onClick = { onIntent(AnimalListIntent.ClearFilters) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Clear All Filters")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun AnimalListItem(
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
            .fillMaxWidth()
            .semantics { contentDescription = accessibilityDescription }
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
                animal.birthDate?.let { birthDate ->
                    Text(
                        text = "Born: ${DateTimeUtil.formatShortDate(birthDate)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Surface(
                shape = MaterialTheme.shapes.small,
                color = when (animal.status) {
                    AnimalStatus.ACTIVE ->
                        MaterialTheme.colorScheme.primaryContainer
                    AnimalStatus.SOLD ->
                        MaterialTheme.colorScheme.tertiaryContainer
                    AnimalStatus.DECEASED ->
                        MaterialTheme.colorScheme.errorContainer
                    AnimalStatus.TRANSFERRED ->
                        MaterialTheme.colorScheme.secondaryContainer
                }
            ) {
                Text(
                    text = animal.status.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = when (animal.status) {
                        AnimalStatus.ACTIVE ->
                            MaterialTheme.colorScheme.onPrimaryContainer
                        AnimalStatus.SOLD ->
                            MaterialTheme.colorScheme.onTertiaryContainer
                        AnimalStatus.DECEASED ->
                            MaterialTheme.colorScheme.onErrorContainer
                        AnimalStatus.TRANSFERRED ->
                            MaterialTheme.colorScheme.onSecondaryContainer
                    }
                )
            }
        }
    }
}
