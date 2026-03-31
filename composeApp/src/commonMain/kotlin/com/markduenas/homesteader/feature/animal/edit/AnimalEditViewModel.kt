package com.markduenas.homesteader.feature.animal.edit

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.benasher44.uuid.uuid4
import com.markduenas.homesteader.core.mvi.UiEffect
import com.markduenas.homesteader.core.mvi.UiIntent
import com.markduenas.homesteader.core.mvi.UiState
import com.markduenas.homesteader.core.util.DateTimeUtil
import com.markduenas.homesteader.data.repository.AnimalRepository
import com.markduenas.homesteader.domain.model.Animal
import com.markduenas.homesteader.domain.model.AnimalStatus
import com.markduenas.homesteader.domain.model.Sex
import com.markduenas.homesteader.domain.model.Species
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
    val motherId: String = "",
    val fatherId: String = "",
    val notes: String = "",
    val photoUri: String = "",
    val pendingPhotoUri: String? = null
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
    data object Save : AnimalEditIntent
}

sealed interface AnimalEditEffect : UiEffect {
    data object NavigateBack : AnimalEditEffect
    data class ShowError(val message: String) : AnimalEditEffect
}

class AnimalEditViewModel(
    private val animalId: String?,
    private val animalRepository: AnimalRepository
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
            is AnimalEditIntent.UpdateStatus -> _state.update { it.copy(status = intent.status) }
            is AnimalEditIntent.UpdateMotherId -> _state.update { it.copy(motherId = intent.id) }
            is AnimalEditIntent.UpdateFatherId -> _state.update { it.copy(fatherId = intent.id) }
            is AnimalEditIntent.UpdateNotes -> _state.update { it.copy(notes = intent.notes) }
            is AnimalEditIntent.UpdatePhotoUri -> _state.update { it.copy(photoUri = intent.uri, pendingPhotoUri = null) }
            is AnimalEditIntent.SetPendingPhoto -> _state.update { it.copy(pendingPhotoUri = intent.uri) }
            is AnimalEditIntent.DismissCropDialog -> _state.update { it.copy(pendingPhotoUri = null) }
            is AnimalEditIntent.Save -> save()
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
