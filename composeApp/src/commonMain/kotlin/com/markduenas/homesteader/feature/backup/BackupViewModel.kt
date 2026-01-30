package com.markduenas.homesteader.feature.backup

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.markduenas.homesteader.domain.service.BackupService
import com.markduenas.homesteader.domain.service.RestoreResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BackupState(
    val isLoading: Boolean = false,
    val backupContent: String? = null,
    val restoreResult: RestoreResult? = null,
    val showRestoreDialog: Boolean = false,
    val restoreText: String = ""
)

sealed class BackupIntent {
    data object CreateBackup : BackupIntent()
    data object ClearBackup : BackupIntent()
    data object ShowRestoreDialog : BackupIntent()
    data object HideRestoreDialog : BackupIntent()
    data class UpdateRestoreText(val text: String) : BackupIntent()
    data object RestoreBackup : BackupIntent()
    data object ClearRestoreResult : BackupIntent()
}

sealed class BackupEffect {
    data class ShowError(val message: String) : BackupEffect()
    data class ShareBackup(val content: String, val filename: String) : BackupEffect()
    data class ShowSuccess(val message: String) : BackupEffect()
}

class BackupViewModel(
    private val backupService: BackupService
) : ScreenModel {

    private val _state = MutableStateFlow(BackupState())
    val state: StateFlow<BackupState> = _state.asStateFlow()

    private val _effects = Channel<BackupEffect>()
    val effects = _effects.receiveAsFlow()

    fun handleIntent(intent: BackupIntent) {
        when (intent) {
            BackupIntent.CreateBackup -> createBackup()
            BackupIntent.ClearBackup -> _state.update { it.copy(backupContent = null) }
            BackupIntent.ShowRestoreDialog -> _state.update { it.copy(showRestoreDialog = true) }
            BackupIntent.HideRestoreDialog -> _state.update { it.copy(showRestoreDialog = false, restoreText = "") }
            is BackupIntent.UpdateRestoreText -> _state.update { it.copy(restoreText = intent.text) }
            BackupIntent.RestoreBackup -> restoreBackup()
            BackupIntent.ClearRestoreResult -> _state.update { it.copy(restoreResult = null) }
        }
    }

    private fun createBackup() {
        screenModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                val backupContent = backupService.createBackup()
                val filename = backupService.generateBackupFilename()

                _state.update {
                    it.copy(
                        isLoading = false,
                        backupContent = backupContent
                    )
                }

                _effects.send(BackupEffect.ShareBackup(backupContent, filename))
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
                _effects.send(BackupEffect.ShowError("Failed to create backup: ${e.message}"))
            }
        }
    }

    private fun restoreBackup() {
        val backupJson = _state.value.restoreText
        if (backupJson.isBlank()) {
            screenModelScope.launch {
                _effects.send(BackupEffect.ShowError("Please paste your backup data"))
            }
            return
        }

        screenModelScope.launch {
            _state.update { it.copy(isLoading = true, showRestoreDialog = false) }

            try {
                val result = backupService.restoreBackup(backupJson)
                _state.update {
                    it.copy(
                        isLoading = false,
                        restoreResult = result,
                        restoreText = ""
                    )
                }

                when (result) {
                    is RestoreResult.Success -> {
                        _effects.send(
                            BackupEffect.ShowSuccess(
                                "Restored ${result.animalsRestored} animals, ${result.eventsRestored} events"
                            )
                        )
                    }
                    is RestoreResult.Error -> {
                        _effects.send(BackupEffect.ShowError(result.message))
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
                _effects.send(BackupEffect.ShowError("Failed to restore: ${e.message}"))
            }
        }
    }
}
