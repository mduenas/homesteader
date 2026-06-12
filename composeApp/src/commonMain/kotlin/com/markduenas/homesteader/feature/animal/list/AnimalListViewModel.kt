package com.markduenas.homesteader.feature.animal.list

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.markduenas.homesteader.core.mvi.UiEffect
import com.markduenas.homesteader.core.mvi.UiIntent
import com.markduenas.homesteader.core.mvi.UiState
import com.markduenas.homesteader.data.repository.AnimalRepository
import com.markduenas.homesteader.domain.model.Animal
import com.markduenas.homesteader.domain.model.AnimalStatus
import com.markduenas.homesteader.domain.model.Species
import com.markduenas.homesteader.domain.monetization.FREE_TIER_ANIMAL_LIMIT
import com.markduenas.homesteader.domain.monetization.PremiumManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AnimalListState(
    val allAnimals: List<Animal> = emptyList(),
    val filteredAnimals: List<Animal> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val searchQuery: String = "",
    val selectedSpecies: Species? = null,
    val selectedStatus: AnimalStatus? = null,
    val availableSpecies: List<Species> = emptyList(),
    val isPremium: Boolean = false,
    val freeTierLimit: Int = FREE_TIER_ANIMAL_LIMIT
) : UiState {
    val animalCount: Int get() = allAnimals.size
    val atFreeLimit: Boolean get() = !isPremium && animalCount >= freeTierLimit
    val nearFreeLimit: Boolean get() = !isPremium && animalCount >= freeTierLimit - 5
}

sealed interface AnimalListIntent : UiIntent {
    data object LoadAnimals : AnimalListIntent
    data class SelectAnimal(val animalId: String) : AnimalListIntent
    data object AddAnimal : AnimalListIntent
    data class Search(val query: String) : AnimalListIntent
    data class FilterBySpecies(val species: Species?) : AnimalListIntent
    data class FilterByStatus(val status: AnimalStatus?) : AnimalListIntent
    data object ClearFilters : AnimalListIntent
}

sealed interface AnimalListEffect : UiEffect {
    data class NavigateToDetail(val animalId: String) : AnimalListEffect
    data object NavigateToAdd : AnimalListEffect
    data object ShowPremiumUpsell : AnimalListEffect
}

class AnimalListViewModel(
    private val animalRepository: AnimalRepository,
    private val premiumManager: PremiumManager
) : ScreenModel {

    private val _state = MutableStateFlow(AnimalListState())
    val state: StateFlow<AnimalListState> = _state.asStateFlow()

    private val _effects = Channel<AnimalListEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var loadJob: Job? = null

    init {
        loadAnimals()
        observePremiumStatus()
    }

    fun handleIntent(intent: AnimalListIntent) {
        when (intent) {
            is AnimalListIntent.LoadAnimals -> loadAnimals()
            is AnimalListIntent.SelectAnimal -> navigateToDetail(intent.animalId)
            is AnimalListIntent.AddAnimal -> onAddAnimal()
            is AnimalListIntent.Search -> search(intent.query)
            is AnimalListIntent.FilterBySpecies -> filterBySpecies(intent.species)
            is AnimalListIntent.FilterByStatus -> filterByStatus(intent.status)
            is AnimalListIntent.ClearFilters -> clearFilters()
        }
    }

    private fun loadAnimals() {
        loadJob?.cancel()
        loadJob = screenModelScope.launch {
            animalRepository.getAllAnimals()
                .onStart {
                    _state.update { it.copy(isLoading = true, error = null) }
                }
                .catch { e ->
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { animals ->
                    val availableSpecies = animals.map { it.species }.distinct().sortedBy { it.displayName }
                    _state.update {
                        it.copy(
                            allAnimals = animals,
                            availableSpecies = availableSpecies,
                            isLoading = false
                        )
                    }
                    applyFilters()
                }
        }
    }

    private fun observePremiumStatus() {
        screenModelScope.launch {
            premiumManager.isPremium.collect { isPremium ->
                _state.update { it.copy(isPremium = isPremium) }
            }
        }
    }

    private fun onAddAnimal() {
        screenModelScope.launch {
            if (_state.value.atFreeLimit) {
                _effects.send(AnimalListEffect.ShowPremiumUpsell)
            } else {
                _effects.send(AnimalListEffect.NavigateToAdd)
            }
        }
    }

    private fun navigateToDetail(animalId: String) {
        screenModelScope.launch {
            _effects.send(AnimalListEffect.NavigateToDetail(animalId))
        }
    }

    private fun search(query: String) {
        _state.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    private fun filterBySpecies(species: Species?) {
        _state.update { it.copy(selectedSpecies = species) }
        applyFilters()
    }

    private fun filterByStatus(status: AnimalStatus?) {
        _state.update { it.copy(selectedStatus = status) }
        applyFilters()
    }

    private fun clearFilters() {
        _state.update {
            it.copy(
                searchQuery = "",
                selectedSpecies = null,
                selectedStatus = null
            )
        }
        applyFilters()
    }

    private fun applyFilters() {
        val currentState = _state.value
        var filtered = currentState.allAnimals

        // Apply search query
        if (currentState.searchQuery.isNotBlank()) {
            val query = currentState.searchQuery.lowercase()
            filtered = filtered.filter { animal ->
                animal.displayName.lowercase().contains(query) ||
                        animal.tagId?.lowercase()?.contains(query) == true ||
                        animal.breed?.lowercase()?.contains(query) == true
            }
        }

        // Apply species filter
        currentState.selectedSpecies?.let { species ->
            filtered = filtered.filter { it.species == species }
        }

        // Apply status filter
        currentState.selectedStatus?.let { status ->
            filtered = filtered.filter { it.status == status }
        }

        _state.update { it.copy(filteredAnimals = filtered) }
    }
}
