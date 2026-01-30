package com.markduenas.homesteader.feature.calendar

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.markduenas.homesteader.core.util.DateTimeUtil
import com.markduenas.homesteader.data.repository.AnimalRepository
import com.markduenas.homesteader.data.repository.ReminderRepository
import com.markduenas.homesteader.domain.model.Animal
import com.markduenas.homesteader.domain.model.Reminder
import com.markduenas.homesteader.domain.model.ReminderType
import com.markduenas.homesteader.domain.service.ReminderService
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

data class CalendarState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val selectedDate: LocalDate = DateTimeUtil.today(),
    val currentMonth: LocalDate = DateTimeUtil.today(),
    val reminders: List<CalendarReminder> = emptyList(),
    val remindersForSelectedDate: List<CalendarReminder> = emptyList(),
    val remindersByDate: Map<LocalDate, List<CalendarReminder>> = emptyMap(),
    val showAddReminderDialog: Boolean = false
)

data class CalendarReminder(
    val id: String,
    val title: String,
    val description: String?,
    val dueDate: LocalDate,
    val animalId: String?,
    val animalName: String?,
    val reminderType: ReminderType,
    val isCompleted: Boolean,
    val isOverdue: Boolean
)

sealed class CalendarIntent {
    data class SelectDate(val date: LocalDate) : CalendarIntent()
    data object PreviousMonth : CalendarIntent()
    data object NextMonth : CalendarIntent()
    data class CompleteReminder(val reminderId: String) : CalendarIntent()
    data class DeleteReminder(val reminderId: String) : CalendarIntent()
    data object ShowAddReminder : CalendarIntent()
    data object HideAddReminder : CalendarIntent()
    data class AddReminder(
        val title: String,
        val description: String?,
        val dueDate: LocalDate,
        val animalId: String?,
        val isRecurring: Boolean,
        val recurrenceIntervalDays: Int?
    ) : CalendarIntent()
    data object Refresh : CalendarIntent()
}

sealed class CalendarEffect {
    data class NavigateToAnimalDetail(val animalId: String) : CalendarEffect()
    data class ShowError(val message: String) : CalendarEffect()
}

class CalendarViewModel(
    private val reminderRepository: ReminderRepository,
    private val reminderService: ReminderService,
    private val animalRepository: AnimalRepository
) : ScreenModel {

    private val _state = MutableStateFlow(CalendarState())
    val state: StateFlow<CalendarState> = _state.asStateFlow()

    private val _effects = Channel<CalendarEffect>()
    val effects = _effects.receiveAsFlow()

    private var animalsById: Map<String, Animal> = emptyMap()

    init {
        loadData()
    }

    fun handleIntent(intent: CalendarIntent) {
        when (intent) {
            is CalendarIntent.SelectDate -> selectDate(intent.date)
            CalendarIntent.PreviousMonth -> navigateMonth(-1)
            CalendarIntent.NextMonth -> navigateMonth(1)
            is CalendarIntent.CompleteReminder -> completeReminder(intent.reminderId)
            is CalendarIntent.DeleteReminder -> deleteReminder(intent.reminderId)
            CalendarIntent.ShowAddReminder -> showAddReminderDialog()
            CalendarIntent.HideAddReminder -> hideAddReminderDialog()
            is CalendarIntent.AddReminder -> addReminder(intent)
            CalendarIntent.Refresh -> loadData()
        }
    }

    private fun loadData() {
        screenModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val currentMonth = _state.value.currentMonth
            val startDate = LocalDate(currentMonth.year, currentMonth.monthNumber, 1)
            val endDate = startDate.plus(DatePeriod(months = 1)).minus(DatePeriod(days = 1))

            combine(
                reminderRepository.getPendingRemindersInRange(
                    startDate.minus(DatePeriod(days = 7)), // Include previous week
                    endDate.plus(DatePeriod(days = 7)) // Include next week
                ),
                animalRepository.getAllAnimals()
            ) { reminders, animals ->
                Pair(reminders, animals)
            }
                .catch { e ->
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { (reminders, animals) ->
                    animalsById = animals.associateBy { it.id }
                    val today = DateTimeUtil.today()

                    val calendarReminders = reminders.map { reminder ->
                        CalendarReminder(
                            id = reminder.id,
                            title = reminder.title,
                            description = reminder.description,
                            dueDate = reminder.dueDate,
                            animalId = reminder.animalId,
                            animalName = reminder.animalId?.let { animalsById[it]?.displayName },
                            reminderType = reminder.reminderType,
                            isCompleted = reminder.isCompleted,
                            isOverdue = reminder.dueDate < today && !reminder.isCompleted
                        )
                    }

                    val remindersByDate = calendarReminders.groupBy { it.dueDate }
                    val selectedDate = _state.value.selectedDate
                    val remindersForSelected = remindersByDate[selectedDate] ?: emptyList()

                    _state.update {
                        it.copy(
                            isLoading = false,
                            reminders = calendarReminders,
                            remindersByDate = remindersByDate,
                            remindersForSelectedDate = remindersForSelected
                        )
                    }
                }
        }
    }

    private fun selectDate(date: LocalDate) {
        val remindersForDate = _state.value.remindersByDate[date] ?: emptyList()
        _state.update {
            it.copy(
                selectedDate = date,
                remindersForSelectedDate = remindersForDate
            )
        }
    }

    private fun navigateMonth(delta: Int) {
        val newMonth = _state.value.currentMonth.plus(DatePeriod(months = delta))
        _state.update { it.copy(currentMonth = newMonth) }
        loadData()
    }

    private fun completeReminder(reminderId: String) {
        screenModelScope.launch {
            try {
                val reminder = reminderRepository.getReminderById(reminderId).first()
                if (reminder != null) {
                    reminderService.completeReminder(reminder)
                    loadData()
                }
            } catch (e: Exception) {
                _effects.send(CalendarEffect.ShowError("Failed to complete reminder: ${e.message}"))
            }
        }
    }

    private fun deleteReminder(reminderId: String) {
        screenModelScope.launch {
            try {
                reminderService.deleteReminder(reminderId)
                loadData()
            } catch (e: Exception) {
                _effects.send(CalendarEffect.ShowError("Failed to delete reminder: ${e.message}"))
            }
        }
    }

    private fun showAddReminderDialog() {
        _state.update { it.copy(showAddReminderDialog = true) }
    }

    private fun hideAddReminderDialog() {
        _state.update { it.copy(showAddReminderDialog = false) }
    }

    private fun addReminder(intent: CalendarIntent.AddReminder) {
        screenModelScope.launch {
            try {
                reminderService.createCustomReminder(
                    animalId = intent.animalId,
                    title = intent.title,
                    description = intent.description,
                    dueDate = intent.dueDate,
                    isRecurring = intent.isRecurring,
                    recurrenceIntervalDays = intent.recurrenceIntervalDays
                )
                _state.update { it.copy(showAddReminderDialog = false) }
                loadData()
            } catch (e: Exception) {
                _effects.send(CalendarEffect.ShowError("Failed to add reminder: ${e.message}"))
            }
        }
    }
}
