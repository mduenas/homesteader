package com.markduenas.homesteader.feature.setup

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.markduenas.homesteader.data.repository.SpeciesConfigRepository
import com.markduenas.homesteader.domain.model.DefaultSpeciesConfigs
import com.markduenas.homesteader.domain.model.SpeciesConfig
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SpeciesSetupState(
    val availableSpecies: List<SpeciesConfig> = DefaultSpeciesConfigs.ALL_DEFAULTS,
    val selectedSpeciesKeys: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class SpeciesSetupIntent {
    data class ToggleSpecies(val speciesKey: String) : SpeciesSetupIntent()
    data object Continue : SpeciesSetupIntent()
    data object Skip : SpeciesSetupIntent()
}

sealed class SpeciesSetupEffect {
    data object NavigateToMain : SpeciesSetupEffect()
    data class ShowError(val message: String) : SpeciesSetupEffect()
}

class SpeciesSetupViewModel(
    private val speciesConfigRepository: SpeciesConfigRepository
) : ScreenModel {

    private val _state = MutableStateFlow(SpeciesSetupState())
    val state: StateFlow<SpeciesSetupState> = _state.asStateFlow()

    private val _effects = Channel<SpeciesSetupEffect>()
    val effects = _effects.receiveAsFlow()

    init {
        loadSpecies()
    }

    fun handleIntent(intent: SpeciesSetupIntent) {
        when (intent) {
            is SpeciesSetupIntent.ToggleSpecies -> toggleSpecies(intent.speciesKey)
            SpeciesSetupIntent.Continue -> saveAndContinue()
            SpeciesSetupIntent.Skip -> skipSetup()
        }
    }

    private fun loadSpecies() {
        screenModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                // Initialize default configs if needed
                speciesConfigRepository.initializeDefaultConfigs()

                // Load existing enabled species
                speciesConfigRepository.getEnabledConfigs().collect { enabledConfigs ->
                    _state.update {
                        it.copy(
                            selectedSpeciesKeys = enabledConfigs.map { c -> c.speciesKey }.toSet(),
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun toggleSpecies(speciesKey: String) {
        _state.update { state ->
            val newSelection = if (speciesKey in state.selectedSpeciesKeys) {
                state.selectedSpeciesKeys - speciesKey
            } else {
                state.selectedSpeciesKeys + speciesKey
            }
            state.copy(selectedSpeciesKeys = newSelection)
        }
    }

    private fun saveAndContinue() {
        val selectedKeys = _state.value.selectedSpeciesKeys

        if (selectedKeys.isEmpty()) {
            screenModelScope.launch {
                _effects.send(SpeciesSetupEffect.ShowError("Please select at least one species"))
            }
            return
        }

        screenModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                // Enable selected species
                speciesConfigRepository.enableSpecies(selectedKeys.toList())
                _effects.send(SpeciesSetupEffect.NavigateToMain)
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
                _effects.send(SpeciesSetupEffect.ShowError(e.message ?: "Failed to save settings"))
            }
        }
    }

    private fun skipSetup() {
        screenModelScope.launch {
            // Enable all species as default
            try {
                val allKeys = DefaultSpeciesConfigs.ALL_DEFAULTS.map { it.speciesKey }
                speciesConfigRepository.enableSpecies(allKeys)
            } catch (e: Exception) {
                // Ignore errors on skip
            }
            _effects.send(SpeciesSetupEffect.NavigateToMain)
        }
    }
}
