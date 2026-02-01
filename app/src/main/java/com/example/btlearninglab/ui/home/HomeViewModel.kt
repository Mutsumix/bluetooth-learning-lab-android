package com.example.btlearninglab.ui.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // In a real app, these would be updated based on actual device connections
    fun updateScaleConnection(connected: Boolean) {
        _uiState.value = _uiState.value.copy(scaleConnected = connected)
    }

    fun updatePrinterConnection(connected: Boolean) {
        _uiState.value = _uiState.value.copy(printerConnected = connected)
    }

    fun updateEPaperConnection(connected: Boolean) {
        _uiState.value = _uiState.value.copy(epaperConnected = connected)
    }
}
