package com.yike.jarvis.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yike.jarvis.database.BackupManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val backupManager: BackupManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun exportDatabase(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = SettingsUiState.Loading
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                val result = backupManager.exportDatabase(outputStream)
                if (result.isSuccess) {
                    _uiState.value = SettingsUiState.Success("Database exported successfully")
                } else {
                    _uiState.value = SettingsUiState.Error(result.exceptionOrNull()?.message ?: "Export failed")
                }
            } ?: run {
                _uiState.value = SettingsUiState.Error("Could not open output stream")
            }
        }
    }

    fun importDatabase(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = SettingsUiState.Loading
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val result = backupManager.importDatabase(inputStream)
                if (result.isSuccess) {
                    _uiState.value = SettingsUiState.Success("Database imported successfully. Please restart the app.")
                } else {
                    _uiState.value = SettingsUiState.Error(result.exceptionOrNull()?.message ?: "Import failed")
                }
            } ?: run {
                _uiState.value = SettingsUiState.Error("Could not open input stream")
            }
        }
    }

    fun resetState() {
        _uiState.value = SettingsUiState.Idle
    }
}

sealed class SettingsUiState {
    object Idle : SettingsUiState()
    object Loading : SettingsUiState()
    data class Success(val message: String) : SettingsUiState()
    data class Error(val message: String) : SettingsUiState()
}
