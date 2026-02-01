package com.example.btlearninglab.ui.scale

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ScaleViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<ScaleUiState>(ScaleUiState.Disconnected)
    val uiState: StateFlow<ScaleUiState> = _uiState.asStateFlow()

    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    fun connect() {
        viewModelScope.launch {
            _uiState.value = ScaleUiState.Connecting
            _logs.value = emptyList()

            // Simulate BLE connection
            _logs.value = _logs.value + "> Scanning for \"Decent Scale\""
            delay(500)
            _logs.value = _logs.value + "> Found: XX:XX:XX:XX:XX:XX"
            delay(500)
            _logs.value = _logs.value + "> Connecting..."
            delay(500)
            _logs.value = _logs.value + "> Subscribing to FFF4..."
            delay(500)
            _logs.value = _logs.value + "> Receiving notifications"

            _uiState.value = ScaleUiState.Connected(
                weight = 125.4f,
                rawData = "03 CE 04 E6 00 00 2B"
            )
        }
    }

    fun disconnect() {
        _logs.value = _logs.value + "> Disconnected"
        _uiState.value = ScaleUiState.Disconnected
    }

    fun tare() {
        val currentState = _uiState.value
        if (currentState is ScaleUiState.Connected) {
            viewModelScope.launch {
                _logs.value = _logs.value + "> Sending Tare command: 03 0F 00 00 00 01 0E"
                delay(300)
                _logs.value = _logs.value + "> Weight reset to 0.0g"
                _uiState.value = currentState.copy(weight = 0.0f)
            }
        }
    }
}
