package com.markduenas.homesteader.feature.import

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.markduenas.homesteader.domain.model.Animal
import com.markduenas.homesteader.domain.service.CsvImportService
import com.markduenas.homesteader.domain.service.CsvParseResult
import com.markduenas.homesteader.domain.service.ImportResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ImportState(
    val isLoading: Boolean = false,
    val csvText: String = "",
    val parsedAnimals: List<Animal> = emptyList(),
    val parseErrors: List<String> = emptyList(),
    val importResult: ImportResult? = null,
    val showTemplate: Boolean = false
)

sealed class ImportIntent {
    data class UpdateCsvText(val text: String) : ImportIntent()
    data object ParseCsv : ImportIntent()
    data object ImportAnimals : ImportIntent()
    data object ClearResults : ImportIntent()
    data object ShowTemplate : ImportIntent()
    data object HideTemplate : ImportIntent()
    data object ClearAll : ImportIntent()
}

sealed class ImportEffect {
    data class ShowError(val message: String) : ImportEffect()
    data class ShowSuccess(val message: String) : ImportEffect()
}

class ImportViewModel(
    private val csvImportService: CsvImportService
) : ScreenModel {

    private val _state = MutableStateFlow(ImportState())
    val state: StateFlow<ImportState> = _state.asStateFlow()

    private val _effects = Channel<ImportEffect>()
    val effects = _effects.receiveAsFlow()

    fun handleIntent(intent: ImportIntent) {
        when (intent) {
            is ImportIntent.UpdateCsvText -> _state.update { it.copy(csvText = intent.text) }
            ImportIntent.ParseCsv -> parseCsv()
            ImportIntent.ImportAnimals -> importAnimals()
            ImportIntent.ClearResults -> _state.update { it.copy(parsedAnimals = emptyList(), parseErrors = emptyList(), importResult = null) }
            ImportIntent.ShowTemplate -> _state.update { it.copy(showTemplate = true) }
            ImportIntent.HideTemplate -> _state.update { it.copy(showTemplate = false) }
            ImportIntent.ClearAll -> _state.update { ImportState() }
        }
    }

    private fun parseCsv() {
        val csvText = _state.value.csvText
        if (csvText.isBlank()) {
            screenModelScope.launch {
                _effects.send(ImportEffect.ShowError("Please paste CSV data"))
            }
            return
        }

        val result = csvImportService.parseCsv(csvText)
        when (result) {
            is CsvParseResult.Success -> {
                _state.update {
                    it.copy(
                        parsedAnimals = result.animals,
                        parseErrors = result.errors
                    )
                }
            }
            is CsvParseResult.Error -> {
                screenModelScope.launch {
                    _effects.send(ImportEffect.ShowError(result.message))
                }
            }
        }
    }

    private fun importAnimals() {
        val animals = _state.value.parsedAnimals
        if (animals.isEmpty()) {
            screenModelScope.launch {
                _effects.send(ImportEffect.ShowError("No animals to import"))
            }
            return
        }

        screenModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                val result = csvImportService.importAnimals(animals)
                _state.update {
                    it.copy(
                        isLoading = false,
                        importResult = result,
                        parsedAnimals = emptyList(),
                        csvText = ""
                    )
                }
                _effects.send(ImportEffect.ShowSuccess("Imported ${result.importedCount} animals"))
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
                _effects.send(ImportEffect.ShowError("Import failed: ${e.message}"))
            }
        }
    }

    fun getCsvTemplate(): String = csvImportService.generateCsvTemplate()
}
