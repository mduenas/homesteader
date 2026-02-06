package com.markduenas.homesteader.feature.event

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.markduenas.homesteader.core.util.DateTimeUtil
import com.markduenas.homesteader.data.repository.AnimalRepository
import com.markduenas.homesteader.data.repository.EventRepository
import com.markduenas.homesteader.domain.model.AnimalEvent
import com.markduenas.homesteader.domain.model.BreedingEventData
import com.markduenas.homesteader.domain.model.EventCategory
import com.markduenas.homesteader.domain.model.EventData
import com.markduenas.homesteader.domain.model.EventType
import com.markduenas.homesteader.domain.model.HealthEventData
import com.markduenas.homesteader.domain.model.ProductionEventData
import com.markduenas.homesteader.domain.model.WeightEventData
import com.markduenas.homesteader.domain.service.ReminderService
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

data class EventAddState(
    val isEditing: Boolean = false,
    val eventId: String? = null,
    val animalId: String = "",
    val animalName: String = "",
    val selectedCategory: EventCategory = EventCategory.HEALTH,
    val selectedEventType: EventType = EventType.VACCINATION,
    val eventDate: String = "",
    val notes: String = "",

    // Health event fields
    val medicationName: String = "",
    val dosage: String = "",
    val veterinarian: String = "",
    val diagnosis: String = "",
    val vaccinationIntervalDays: String = "", // For follow-up reminders

    // Breeding event fields
    val sireId: String = "",
    val sireName: String = "",
    val breedingMethod: String = "Natural",
    val isPregnant: Boolean? = null,
    val expectedDueDate: String = "",
    val offspringCount: String = "",

    // Production event fields
    val milkQuantity: String = "",
    val milkUnit: String = "gallons",
    val eggCount: String = "",
    val fiberWeight: String = "",

    // Weight event fields
    val weight: String = "",
    val weightUnit: String = "lbs",
    val bodyCondition: String = "",

    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class EventAddIntent {
    data class SetCategory(val category: EventCategory) : EventAddIntent()
    data class SetEventType(val eventType: EventType) : EventAddIntent()
    data class UpdateEventDate(val date: String) : EventAddIntent()
    data class UpdateNotes(val notes: String) : EventAddIntent()

    // Health fields
    data class UpdateMedicationName(val name: String) : EventAddIntent()
    data class UpdateDosage(val dosage: String) : EventAddIntent()
    data class UpdateVeterinarian(val vet: String) : EventAddIntent()
    data class UpdateDiagnosis(val diagnosis: String) : EventAddIntent()
    data class UpdateVaccinationInterval(val days: String) : EventAddIntent()

    // Breeding fields
    data class UpdateSireId(val id: String) : EventAddIntent()
    data class UpdateSireName(val name: String) : EventAddIntent()
    data class UpdateBreedingMethod(val method: String) : EventAddIntent()
    data class UpdateIsPregnant(val pregnant: Boolean?) : EventAddIntent()
    data class UpdateExpectedDueDate(val date: String) : EventAddIntent()
    data class UpdateOffspringCount(val count: String) : EventAddIntent()

    // Production fields
    data class UpdateMilkQuantity(val quantity: String) : EventAddIntent()
    data class UpdateMilkUnit(val unit: String) : EventAddIntent()
    data class UpdateEggCount(val count: String) : EventAddIntent()
    data class UpdateFiberWeight(val weight: String) : EventAddIntent()

    // Weight fields
    data class UpdateWeight(val weight: String) : EventAddIntent()
    data class UpdateWeightUnit(val unit: String) : EventAddIntent()
    data class UpdateBodyCondition(val condition: String) : EventAddIntent()

    data object Save : EventAddIntent()
    data object DeleteEvent : EventAddIntent()
}

sealed class EventAddEffect {
    data object NavigateBack : EventAddEffect()
    data class ShowError(val message: String) : EventAddEffect()
}

class EventAddViewModel(
    private val animalId: String,
    private val animalName: String,
    private val eventId: String?,
    private val eventRepository: EventRepository,
    private val animalRepository: AnimalRepository,
    private val reminderService: ReminderService
) : ScreenModel {

    private val _state = MutableStateFlow(
        EventAddState(
            isEditing = eventId != null,
            eventId = eventId,
            animalId = animalId,
            animalName = animalName,
            eventDate = DateTimeUtil.today().toString()
        )
    )
    val state: StateFlow<EventAddState> = _state.asStateFlow()

    private val _effects = Channel<EventAddEffect>()
    val effects = _effects.receiveAsFlow()

    init {
        if (eventId != null) {
            loadEvent(eventId)
        }
    }

    fun handleIntent(intent: EventAddIntent) {
        when (intent) {
            is EventAddIntent.SetCategory -> setCategory(intent.category)
            is EventAddIntent.SetEventType -> setEventType(intent.eventType)
            is EventAddIntent.UpdateEventDate -> updateField { copy(eventDate = intent.date) }
            is EventAddIntent.UpdateNotes -> updateField { copy(notes = intent.notes) }

            // Health
            is EventAddIntent.UpdateMedicationName -> updateField { copy(medicationName = intent.name) }
            is EventAddIntent.UpdateDosage -> updateField { copy(dosage = intent.dosage) }
            is EventAddIntent.UpdateVeterinarian -> updateField { copy(veterinarian = intent.vet) }
            is EventAddIntent.UpdateDiagnosis -> updateField { copy(diagnosis = intent.diagnosis) }
            is EventAddIntent.UpdateVaccinationInterval -> updateField { copy(vaccinationIntervalDays = intent.days) }

            // Breeding
            is EventAddIntent.UpdateSireId -> updateField { copy(sireId = intent.id) }
            is EventAddIntent.UpdateSireName -> updateField { copy(sireName = intent.name) }
            is EventAddIntent.UpdateBreedingMethod -> updateField { copy(breedingMethod = intent.method) }
            is EventAddIntent.UpdateIsPregnant -> updateField { copy(isPregnant = intent.pregnant) }
            is EventAddIntent.UpdateExpectedDueDate -> updateField { copy(expectedDueDate = intent.date) }
            is EventAddIntent.UpdateOffspringCount -> updateField { copy(offspringCount = intent.count) }

            // Production
            is EventAddIntent.UpdateMilkQuantity -> updateField { copy(milkQuantity = intent.quantity) }
            is EventAddIntent.UpdateMilkUnit -> updateField { copy(milkUnit = intent.unit) }
            is EventAddIntent.UpdateEggCount -> updateField { copy(eggCount = intent.count) }
            is EventAddIntent.UpdateFiberWeight -> updateField { copy(fiberWeight = intent.weight) }

            // Weight
            is EventAddIntent.UpdateWeight -> updateField { copy(weight = intent.weight) }
            is EventAddIntent.UpdateWeightUnit -> updateField { copy(weightUnit = intent.unit) }
            is EventAddIntent.UpdateBodyCondition -> updateField { copy(bodyCondition = intent.condition) }

            EventAddIntent.Save -> saveEvent()
            EventAddIntent.DeleteEvent -> deleteEvent()
        }
    }

    private fun setCategory(category: EventCategory) {
        val defaultType = EventType.entries.first { it.category == category }
        _state.update { it.copy(selectedCategory = category, selectedEventType = defaultType) }
    }

    private fun setEventType(eventType: EventType) {
        _state.update { it.copy(selectedEventType = eventType) }
    }

    private fun updateField(update: EventAddState.() -> EventAddState) {
        _state.update { it.update() }
    }

    private fun loadEvent(id: String) {
        screenModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val event = eventRepository.getEventById(id).first()
                if (event != null) {
                    _state.update { state ->
                        state.copy(
                            isLoading = false,
                            selectedCategory = event.eventType.category,
                            selectedEventType = event.eventType,
                            eventDate = event.eventDate.toString(),
                            notes = event.notes ?: ""
                        ).populateFromEventData(event.eventData)
                    }
                } else {
                    _state.update { it.copy(isLoading = false, error = "Event not found") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun EventAddState.populateFromEventData(data: EventData?): EventAddState {
        return when (data) {
            is HealthEventData -> this.copy(
                medicationName = data.medicationName ?: "",
                dosage = data.dosage ?: "",
                veterinarian = data.veterinarian ?: "",
                diagnosis = data.diagnosis ?: ""
            )
            is BreedingEventData -> this.copy(
                sireId = data.sireId ?: "",
                sireName = data.sireName ?: "",
                breedingMethod = data.breedingMethod ?: "Natural",
                isPregnant = data.isConfirmedPregnant,
                expectedDueDate = data.expectedDueDate ?: "",
                offspringCount = data.offspringCount?.toString() ?: ""
            )
            is ProductionEventData -> this.copy(
                milkQuantity = data.milkQuantity?.toString() ?: "",
                milkUnit = data.milkUnit ?: "gallons",
                eggCount = data.eggCount?.toString() ?: "",
                fiberWeight = data.fiberWeight?.toString() ?: ""
            )
            is WeightEventData -> this.copy(
                weight = data.weight.toString(),
                weightUnit = data.weightUnit,
                bodyCondition = data.condition ?: ""
            )
            else -> this
        }
    }

    private fun deleteEvent() {
        val currentEventId = _state.value.eventId ?: return

        screenModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                eventRepository.deleteEvent(currentEventId)
                _effects.send(EventAddEffect.NavigateBack)
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "Failed to delete: ${e.message}") }
                _effects.send(EventAddEffect.ShowError("Failed to delete event"))
            }
        }
    }

    private fun saveEvent() {
        val currentState = _state.value

        // Validate date
        val eventDate = try {
            LocalDate.parse(currentState.eventDate)
        } catch (e: Exception) {
            screenModelScope.launch {
                _effects.send(EventAddEffect.ShowError("Invalid date format. Use YYYY-MM-DD"))
            }
            return
        }

        screenModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                val eventData = buildEventData(currentState)
                val event = AnimalEvent(
                    id = currentState.eventId ?: "",
                    animalId = currentState.animalId,
                    eventType = currentState.selectedEventType,
                    eventDate = eventDate,
                    notes = currentState.notes.ifBlank { null },
                    eventData = eventData
                )

                if (currentState.isEditing) {
                    eventRepository.updateEvent(event)
                } else {
                    val newEventId = eventRepository.insertEvent(event)
                    // Generate automatic reminders only for new events
                    generateReminders(event.copy(id = newEventId), currentState)
                }

                _effects.send(EventAddEffect.NavigateBack)
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
                _effects.send(EventAddEffect.ShowError(e.message ?: "Failed to save event"))
            }
        }
    }

    private suspend fun generateReminders(event: AnimalEvent, state: EventAddState) {
        try {
            // Get the animal to find species key
            val animal = animalRepository.getAnimalById(animalId).first() ?: return
            val speciesKey = animal.species.name.lowercase()

            when (event.eventType) {
                EventType.BRED -> {
                    reminderService.generateBreedingReminders(event, animalName, speciesKey)
                }
                EventType.BIRTH -> {
                    reminderService.generateBirthReminders(event, animalName, speciesKey)
                }
                EventType.HEAT_OBSERVED -> {
                    reminderService.generateHeatReminders(event, animalName, speciesKey)
                }
                EventType.VACCINATION -> {
                    val intervalDays = state.vaccinationIntervalDays.toIntOrNull()
                    if (intervalDays != null && intervalDays > 0) {
                        reminderService.generateVaccinationReminders(event, animalName, intervalDays)
                    }
                }
                else -> {
                    // No automatic reminders for other event types
                }
            }
        } catch (e: Exception) {
            // Log error but don't fail the event save
            println("Failed to generate reminders: ${e.message}")
        }
    }

    private fun buildEventData(state: EventAddState): EventData? {
        return when (state.selectedCategory) {
            EventCategory.HEALTH -> HealthEventData(
                medicationName = state.medicationName.ifBlank { null },
                dosage = state.dosage.ifBlank { null },
                veterinarian = state.veterinarian.ifBlank { null },
                diagnosis = state.diagnosis.ifBlank { null }
            )
            EventCategory.BREEDING -> BreedingEventData(
                sireId = state.sireId.ifBlank { null },
                sireName = state.sireName.ifBlank { null },
                breedingMethod = state.breedingMethod.ifBlank { null },
                isConfirmedPregnant = state.isPregnant,
                expectedDueDate = state.expectedDueDate.ifBlank { null },
                offspringCount = state.offspringCount.toIntOrNull()
            )
            EventCategory.PRODUCTION -> ProductionEventData(
                milkQuantity = state.milkQuantity.toDoubleOrNull(),
                milkUnit = state.milkUnit.ifBlank { null },
                eggCount = state.eggCount.toIntOrNull(),
                fiberWeight = state.fiberWeight.toDoubleOrNull()
            )
            EventCategory.WEIGHT -> {
                val weight = state.weight.toDoubleOrNull() ?: return null
                WeightEventData(
                    weight = weight,
                    weightUnit = state.weightUnit,
                    condition = state.bodyCondition.ifBlank { null }
                )
            }
            else -> null
        }
    }
}
