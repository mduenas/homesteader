package com.markduenas.homesteader.feature.animal.edit

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.benasher44.uuid.uuid4
import com.markduenas.homesteader.core.mvi.UiEffect
import com.markduenas.homesteader.core.mvi.UiIntent
import com.markduenas.homesteader.core.mvi.UiState
import com.markduenas.homesteader.core.util.DateTimeUtil
import com.markduenas.homesteader.data.repository.AnimalRepository
import com.markduenas.homesteader.data.repository.EventRepository
import com.markduenas.homesteader.domain.model.Animal
import com.markduenas.homesteader.domain.model.AnimalEvent
import com.markduenas.homesteader.domain.model.AnimalStatus
import com.markduenas.homesteader.domain.model.EventType
import com.markduenas.homesteader.domain.model.HarvestEventData
import com.markduenas.homesteader.domain.model.Sex
import com.markduenas.homesteader.domain.model.Species
import com.markduenas.homesteader.domain.model.StatusChangeEventData
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

data class AnimalEditState(
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val animalId: String = "",
    val name: String = "",
    val tagId: String = "",
    val species: Species = Species.CATTLE_BEEF,
    val breed: String = "",
    val sex: Sex = Sex.UNKNOWN,
    val birthDate: String = "",
    val acquisitionDate: String = DateTimeUtil.today().toString(),
    val status: AnimalStatus = AnimalStatus.ACTIVE,
    val previousStatus: AnimalStatus = AnimalStatus.ACTIVE, // for detecting changes
    val motherId: String = "",
    val fatherId: String = "",
    val notes: String = "",
    val photoUri: String = "",
    val pendingPhotoUri: String? = null,
    val showStatusTransitionDialog: Boolean = false,
    val pendingTransitionData: StatusTransitionData? = null
) : UiState

sealed interface AnimalEditIntent : UiIntent {
    data class UpdateName(val name: String) : AnimalEditIntent
    data class UpdateTagId(val tagId: String) : AnimalEditIntent
    data class UpdateSpecies(val species: Species) : AnimalEditIntent
    data class UpdateBreed(val breed: String) : AnimalEditIntent
    data class UpdateSex(val sex: Sex) : AnimalEditIntent
    data class UpdateBirthDate(val date: String) : AnimalEditIntent
    data class UpdateAcquisitionDate(val date: String) : AnimalEditIntent
    data class UpdateStatus(val status: AnimalStatus) : AnimalEditIntent
    data class UpdateMotherId(val id: String) : AnimalEditIntent
    data class UpdateFatherId(val id: String) : AnimalEditIntent
    data class UpdateNotes(val notes: String) : AnimalEditIntent
    data class UpdatePhotoUri(val uri: String) : AnimalEditIntent
    data class SetPendingPhoto(val uri: String) : AnimalEditIntent
    data object DismissCropDialog : AnimalEditIntent
    data class ConfirmStatusTransition(val data: StatusTransitionData) : AnimalEditIntent
    data object DismissStatusTransitionDialog : AnimalEditIntent
    data object Save : AnimalEditIntent
}

sealed interface AnimalEditEffect : UiEffect {
    data object NavigateBack : AnimalEditEffect
    data class ShowError(val message: String) : AnimalEditEffect
}

class AnimalEditViewModel(
    private val animalId: String?,
    private val animalRepository: AnimalRepository,
    private val eventRepository: EventRepository
) : ScreenModel {

    private val _state = MutableStateFlow(AnimalEditState(isEditing = animalId != null))
    val state: StateFlow<AnimalEditState> = _state.asStateFlow()

    private val _effects = Channel<AnimalEditEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        if (animalId != null) {
            loadAnimal(animalId)
        }
    }

    fun handleIntent(intent: AnimalEditIntent) {
        when (intent) {
            is AnimalEditIntent.UpdateName -> _state.update { it.copy(name = intent.name) }
            is AnimalEditIntent.UpdateTagId -> _state.update { it.copy(tagId = intent.tagId) }
            is AnimalEditIntent.UpdateSpecies -> _state.update { it.copy(species = intent.species) }
            is AnimalEditIntent.UpdateBreed -> _state.update { it.copy(breed = intent.breed) }
            is AnimalEditIntent.UpdateSex -> _state.update { it.copy(sex = intent.sex) }
            is AnimalEditIntent.UpdateBirthDate -> _state.update { it.copy(birthDate = intent.date) }
            is AnimalEditIntent.UpdateAcquisitionDate -> _state.update { it.copy(acquisitionDate = intent.date) }
            is AnimalEditIntent.UpdateStatus -> onStatusChanged(intent.status)
            is AnimalEditIntent.UpdateMotherId -> _state.update { it.copy(motherId = intent.id) }
            is AnimalEditIntent.UpdateFatherId -> _state.update { it.copy(fatherId = intent.id) }
            is AnimalEditIntent.UpdateNotes -> _state.update { it.copy(notes = intent.notes) }
            is AnimalEditIntent.UpdatePhotoUri -> _state.update { it.copy(photoUri = intent.uri, pendingPhotoUri = null) }
            is AnimalEditIntent.SetPendingPhoto -> _state.update { it.copy(pendingPhotoUri = intent.uri) }
            is AnimalEditIntent.DismissCropDialog -> _state.update { it.copy(pendingPhotoUri = null) }
            is AnimalEditIntent.ConfirmStatusTransition -> _state.update {
                it.copy(
                    pendingTransitionData = intent.data,
                    showStatusTransitionDialog = false
                )
            }
            is AnimalEditIntent.DismissStatusTransitionDialog -> _state.update {
                it.copy(showStatusTransitionDialog = false)
            }
            is AnimalEditIntent.Save -> save()
        }
    }

    private fun onStatusChanged(newStatus: AnimalStatus) {
        val current = _state.value
        _state.update { it.copy(status = newStatus) }
        // Show dialog when status changes to any non-ACTIVE value
        if (newStatus != AnimalStatus.ACTIVE && newStatus != current.previousStatus) {
            _state.update { it.copy(showStatusTransitionDialog = true) }
        }
    }

    private fun loadAnimal(id: String) {
        screenModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val animal = animalRepository.getAnimalById(id).firstOrNull()
                if (animal != null) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            animalId = animal.id,
                            name = animal.name ?: "",
                            tagId = animal.tagId ?: "",
                            species = animal.species,
                            breed = animal.breed ?: "",
                            sex = animal.sex,
                            birthDate = animal.birthDate?.toString() ?: "",
                            acquisitionDate = animal.acquisitionDate?.toString() ?: "",
                            status = animal.status,
                            previousStatus = animal.status,
                            motherId = animal.motherId ?: "",
                            fatherId = animal.fatherId ?: "",
                            notes = animal.notes ?: "",
                            photoUri = animal.photoUri ?: ""
                        )
                    }
                } else {
                    _state.update { it.copy(isLoading = false, error = "Animal not found") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun save() {
        val currentState = _state.value

        // Validation
        if (currentState.sex == Sex.UNKNOWN && currentState.name.isBlank() && currentState.tagId.isBlank()) {
            _state.update { it.copy(error = "Please provide a name or tag ID") }
            return
        }

        screenModelScope.launch {
            _state.update { it.copy(isSaving = true, error = null) }
            try {
                val animal = Animal(
                    id = if (currentState.isEditing) currentState.animalId else uuid4().toString(),
                    name = currentState.name.ifBlank { null },
                    tagId = currentState.tagId.ifBlank { null },
                    species = currentState.species,
                    breed = currentState.breed.ifBlank { null },
                    sex = currentState.sex,
                    birthDate = parseDate(currentState.birthDate),
                    acquisitionDate = parseDate(currentState.acquisitionDate),
                    status = currentState.status,
                    motherId = currentState.motherId.ifBlank { null },
                    fatherId = currentState.fatherId.ifBlank { null },
                    notes = currentState.notes.ifBlank { null },
                    photoUri = currentState.photoUri.ifBlank { null }
                )

                if (currentState.isEditing) {
                    animalRepository.updateAnimal(animal)
                } else {
                    animalRepository.insertAnimal(animal)
                }

                // Create a status-change or harvest event if status changed
                val statusChanged = currentState.status != currentState.previousStatus
                val transitionData = currentState.pendingTransitionData
                if (statusChanged && currentState.status != AnimalStatus.ACTIVE) {
                    val today = DateTimeUtil.today()
                    if (currentState.status == AnimalStatus.DECEASED && transitionData != null
                        && transitionData.liveWeight != null
                    ) {
                        // Harvest event (user provided weight data)
                        eventRepository.insertEvent(
                            AnimalEvent(
                                id = uuid4().toString(),
                                animalId = animal.id,
                                eventType = EventType.HARVEST,
                                eventDate = today,
                                notes = transitionData.reason,
                                eventData = HarvestEventData(
                                    liveWeight = transitionData.liveWeight,
                                    dressedWeight = transitionData.dressedWeight,
                                    purpose = transitionData.harvestPurpose,
                                    revenue = transitionData.harvestRevenue,
                                    buyer = transitionData.buyer
                                )
                            )
                        )
                    } else {
                        // Generic status change (sold, deceased, transferred)
                        eventRepository.insertEvent(
                            AnimalEvent(
                                id = uuid4().toString(),
                                animalId = animal.id,
                                eventType = EventType.STATUS_CHANGE,
                                eventDate = today,
                                notes = transitionData?.reason,
                                eventData = StatusChangeEventData(
                                    previousStatus = currentState.previousStatus.name,
                                    newStatus = currentState.status.name,
                                    reason = transitionData?.reason,
                                    salePrice = transitionData?.salePrice,
                                    buyer = transitionData?.buyer,
                                    buyerContact = transitionData?.buyerContact
                                )
                            )
                        )
                    }
                }

                _effects.send(AnimalEditEffect.NavigateBack)
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false, error = "Failed to save: ${e.message}") }
            }
        }
    }

    private fun parseDate(dateString: String): LocalDate? {
        if (dateString.isBlank()) return null
        return try {
            LocalDate.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }
}
