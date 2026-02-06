package com.markduenas.homesteader.feature.animal.detail

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.markduenas.homesteader.core.mvi.UiEffect
import com.markduenas.homesteader.core.mvi.UiIntent
import com.markduenas.homesteader.core.mvi.UiState
import com.markduenas.homesteader.data.repository.AnimalRepository
import com.markduenas.homesteader.data.repository.EventRepository
import com.markduenas.homesteader.domain.model.Animal
import com.markduenas.homesteader.domain.model.AnimalEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AnimalDetailState(
    val animal: Animal? = null,
    val events: List<AnimalEvent> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
) : UiState

sealed interface AnimalDetailIntent : UiIntent {
    data object EditAnimal : AnimalDetailIntent
    data object DeleteAnimal : AnimalDetailIntent
    data object AddEvent : AnimalDetailIntent
    data class EditEvent(val eventId: String) : AnimalDetailIntent
    data object Refresh : AnimalDetailIntent
}

sealed interface AnimalDetailEffect : UiEffect {
    data object NavigateBack : AnimalDetailEffect
    data class NavigateToEdit(val animalId: String) : AnimalDetailEffect
    data class NavigateToAddEvent(val animalId: String, val animalName: String) : AnimalDetailEffect
    data class NavigateToEditEvent(val eventId: String, val animalId: String, val animalName: String) : AnimalDetailEffect
}

class AnimalDetailViewModel(
    private val animalId: String,
    private val animalRepository: AnimalRepository,
    private val eventRepository: EventRepository
) : ScreenModel {

    private val _state = MutableStateFlow(AnimalDetailState())
    val state: StateFlow<AnimalDetailState> = _state.asStateFlow()

    private val _effects = Channel<AnimalDetailEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        loadAnimalAndEvents()
    }

    fun handleIntent(intent: AnimalDetailIntent) {
        when (intent) {
            is AnimalDetailIntent.EditAnimal -> editAnimal()
            is AnimalDetailIntent.DeleteAnimal -> deleteAnimal()
            is AnimalDetailIntent.AddEvent -> addEvent()
            is AnimalDetailIntent.EditEvent -> editEvent(intent.eventId)
            is AnimalDetailIntent.Refresh -> loadAnimalAndEvents()
        }
    }

    private fun loadAnimalAndEvents() {
        screenModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            combine(
                animalRepository.getAnimalById(animalId),
                eventRepository.getEventsByAnimalId(animalId)
            ) { animal, events ->
                Pair(animal, events)
            }
                .catch { e ->
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { (animal, events) ->
                    _state.update {
                        it.copy(
                            animal = animal,
                            events = events,
                            isLoading = false
                        )
                    }
                }
        }
    }

    private fun editAnimal() {
        screenModelScope.launch {
            _effects.send(AnimalDetailEffect.NavigateToEdit(animalId))
        }
    }

    private fun addEvent() {
        screenModelScope.launch {
            val animal = _state.value.animal
            if (animal != null) {
                _effects.send(AnimalDetailEffect.NavigateToAddEvent(animalId, animal.displayName))
            }
        }
    }

    private fun editEvent(eventId: String) {
        screenModelScope.launch {
            val animal = _state.value.animal
            if (animal != null) {
                _effects.send(AnimalDetailEffect.NavigateToEditEvent(eventId, animalId, animal.displayName))
            }
        }
    }

    private fun deleteAnimal() {
        screenModelScope.launch {
            try {
                animalRepository.deleteAnimal(animalId)
                _effects.send(AnimalDetailEffect.NavigateBack)
            } catch (e: Exception) {
                _state.update { it.copy(error = "Failed to delete animal: ${e.message}") }
            }
        }
    }
}
