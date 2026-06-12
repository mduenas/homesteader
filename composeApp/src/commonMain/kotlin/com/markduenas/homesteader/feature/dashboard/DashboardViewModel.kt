package com.markduenas.homesteader.feature.dashboard

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.markduenas.homesteader.data.repository.AnimalRepository
import com.markduenas.homesteader.data.repository.EventRepository
import com.markduenas.homesteader.domain.model.Animal
import com.markduenas.homesteader.domain.model.AnimalEvent
import com.markduenas.homesteader.domain.model.AnimalStatus
import com.markduenas.homesteader.domain.model.Reminder
import com.markduenas.homesteader.domain.model.ReminderType
import com.markduenas.homesteader.domain.model.Species
import com.markduenas.homesteader.domain.monetization.FREE_TIER_ANIMAL_LIMIT
import com.markduenas.homesteader.domain.monetization.PremiumManager
import com.markduenas.homesteader.domain.service.ReminderService
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardState(
    val isLoading: Boolean = true,
    val error: String? = null,

    // Quick stats
    val totalAnimals: Int = 0,
    val activeAnimals: Int = 0,
    val animalsBySpecies: Map<Species, Int> = emptyMap(),
    val animalsByStatus: Map<AnimalStatus, Int> = emptyMap(),

    // Recent activity
    val recentEvents: List<AnimalEvent> = emptyList(),
    val recentAnimals: List<Animal> = emptyList(),

    // Upcoming tasks from reminders
    val upcomingTasks: List<UpcomingTask> = emptyList(),
    val overdueCount: Int = 0,

    // Premium upsell
    val isPremium: Boolean = false,
    val showUpgradePrompt: Boolean = false
)

data class UpcomingTask(
    val id: String,
    val title: String,
    val description: String?,
    val dueDate: String,
    val animalId: String?,
    val animalName: String?,
    val taskType: TaskType,
    val isOverdue: Boolean = false
)

enum class TaskType {
    VACCINATION_DUE,
    DEWORMING_DUE,
    VET_FOLLOWUP,
    MEDICATION_DUE,
    HOOF_TRIM_DUE,
    PREGNANCY_CHECK,
    BIRTH_DUE,
    WEANING_DUE,
    HEAT_EXPECTED,
    CUSTOM,
    RECURRING_TASK
}

sealed class DashboardIntent {
    data object Refresh : DashboardIntent()
    data class SelectAnimal(val animalId: String) : DashboardIntent()
    data object ViewAllAnimals : DashboardIntent()
    data object AddAnimal : DashboardIntent()
    data class CompleteTask(val taskId: String) : DashboardIntent()
    data object UpgradeToPremium : DashboardIntent()
}

sealed class DashboardEffect {
    data class NavigateToAnimalDetail(val animalId: String) : DashboardEffect()
    data object NavigateToAnimalList : DashboardEffect()
    data object NavigateToAddAnimal : DashboardEffect()
    data object NavigateToPremium : DashboardEffect()
}

class DashboardViewModel(
    private val animalRepository: AnimalRepository,
    private val eventRepository: EventRepository,
    private val reminderService: ReminderService,
    private val premiumManager: PremiumManager
) : ScreenModel {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    private val _effects = Channel<DashboardEffect>()
    val effects = _effects.receiveAsFlow()

    // Cache animals by ID for quick lookup
    private var animalsById: Map<String, Animal> = emptyMap()

    init {
        loadDashboardData()
        observePremiumStatus()
    }

    fun handleIntent(intent: DashboardIntent) {
        when (intent) {
            DashboardIntent.Refresh -> loadDashboardData()
            is DashboardIntent.SelectAnimal -> navigateToAnimal(intent.animalId)
            DashboardIntent.ViewAllAnimals -> navigateToAnimalList()
            DashboardIntent.AddAnimal -> navigateToAddAnimal()
            is DashboardIntent.CompleteTask -> completeTask(intent.taskId)
            DashboardIntent.UpgradeToPremium -> navigateToPremium()
        }
    }

    private fun observePremiumStatus() {
        screenModelScope.launch {
            premiumManager.isPremium.collect { isPremium ->
                _state.update { it.copy(isPremium = isPremium) }
            }
        }
    }

    private fun loadDashboardData() {
        screenModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            combine(
                animalRepository.getAllAnimals(),
                eventRepository.getRecentEvents(10),
                reminderService.getUpcomingReminders(days = 14, limit = 20),
                reminderService.getOverdueReminders()
            ) { animals, recentEvents, upcomingReminders, overdueReminders ->
                DashboardData(animals, recentEvents, upcomingReminders, overdueReminders)
            }
                .catch { e ->
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { data ->
                    // Cache animals for lookup
                    animalsById = data.animals.associateBy { it.id }

                    val animalsBySpecies = data.animals.groupBy { it.species }
                        .mapValues { it.value.size }

                    val animalsByStatus = data.animals.groupBy { it.status }
                        .mapValues { it.value.size }

                    val activeAnimals = data.animals.count { it.status == AnimalStatus.ACTIVE }

                    // Get the 5 most recently added/updated animals
                    val recentAnimals = data.animals.take(5)

                    // Convert reminders to UpcomingTask
                    val overdueTasks = data.overdueReminders.map { reminder ->
                        reminderToTask(reminder, isOverdue = true)
                    }
                    val upcomingTasks = data.upcomingReminders.map { reminder ->
                        reminderToTask(reminder, isOverdue = false)
                    }

                    // Combine overdue (first) and upcoming tasks
                    val allTasks = overdueTasks + upcomingTasks

                    val isPremium = premiumManager.isPremium.value
                    val showUpgradePrompt = !isPremium &&
                        data.animals.size >= FREE_TIER_ANIMAL_LIMIT - 5

                    _state.update {
                        it.copy(
                            isLoading = false,
                            totalAnimals = data.animals.size,
                            activeAnimals = activeAnimals,
                            animalsBySpecies = animalsBySpecies,
                            animalsByStatus = animalsByStatus,
                            recentEvents = data.recentEvents,
                            recentAnimals = recentAnimals,
                            upcomingTasks = allTasks,
                            overdueCount = data.overdueReminders.size,
                            isPremium = isPremium,
                            showUpgradePrompt = showUpgradePrompt
                        )
                    }
                }
        }
    }

    private fun reminderToTask(reminder: Reminder, isOverdue: Boolean): UpcomingTask {
        val animalName = reminder.animalId?.let { animalsById[it]?.displayName }

        return UpcomingTask(
            id = reminder.id,
            title = reminder.title,
            description = reminder.description,
            dueDate = reminder.dueDate.toString(),
            animalId = reminder.animalId,
            animalName = animalName,
            taskType = reminderTypeToTaskType(reminder.reminderType),
            isOverdue = isOverdue
        )
    }

    private fun reminderTypeToTaskType(reminderType: ReminderType): TaskType {
        return when (reminderType) {
            ReminderType.HEAT_EXPECTED -> TaskType.HEAT_EXPECTED
            ReminderType.PREGNANCY_CHECK -> TaskType.PREGNANCY_CHECK
            ReminderType.BIRTH_DUE -> TaskType.BIRTH_DUE
            ReminderType.WEANING_DUE -> TaskType.WEANING_DUE
            ReminderType.VACCINATION_DUE -> TaskType.VACCINATION_DUE
            ReminderType.DEWORMING_DUE -> TaskType.DEWORMING_DUE
            ReminderType.VET_FOLLOWUP -> TaskType.VET_FOLLOWUP
            ReminderType.MEDICATION_DUE -> TaskType.MEDICATION_DUE
            ReminderType.HOOF_TRIM_DUE -> TaskType.HOOF_TRIM_DUE
            ReminderType.CUSTOM -> TaskType.CUSTOM
            ReminderType.RECURRING_TASK -> TaskType.RECURRING_TASK
        }
    }

    private fun completeTask(taskId: String) {
        screenModelScope.launch {
            try {
                // Find the reminder in the current state
                val task = _state.value.upcomingTasks.find { it.id == taskId }
                if (task != null) {
                    // Get the actual reminder from repository and complete it
                    reminderService.getUpcomingReminders(days = 30, limit = 100)
                        .collect { reminders ->
                            val reminder = reminders.find { it.id == taskId }
                            if (reminder != null) {
                                reminderService.completeReminder(reminder)
                            }
                        }
                }
                // Refresh to update the list
                loadDashboardData()
            } catch (e: Exception) {
                _state.update { it.copy(error = "Failed to complete task: ${e.message}") }
            }
        }
    }

    private fun navigateToAnimal(animalId: String) {
        screenModelScope.launch {
            _effects.send(DashboardEffect.NavigateToAnimalDetail(animalId))
        }
    }

    private fun navigateToAnimalList() {
        screenModelScope.launch {
            _effects.send(DashboardEffect.NavigateToAnimalList)
        }
    }

    private fun navigateToAddAnimal() {
        screenModelScope.launch {
            _effects.send(DashboardEffect.NavigateToAddAnimal)
        }
    }

    private fun navigateToPremium() {
        screenModelScope.launch {
            _effects.send(DashboardEffect.NavigateToPremium)
        }
    }
}

private data class DashboardData(
    val animals: List<Animal>,
    val recentEvents: List<AnimalEvent>,
    val upcomingReminders: List<Reminder>,
    val overdueReminders: List<Reminder>
)
