package com.example.btlearninglab.ui.printer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PrinterViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<PrinterUiState>(PrinterUiState.Disconnected)
    val uiState: StateFlow<PrinterUiState> = _uiState.asStateFlow()

    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    private val _text = MutableStateFlow("Hello, Bluetooth!")
    val text: StateFlow<String> = _text.asStateFlow()

    private val _showCommand = MutableStateFlow(false)
    val showCommand: StateFlow<Boolean> = _showCommand.asStateFlow()

    fun updateText(newText: String) {
        if (newText.length <= 200) {
            _text.value = newText
        }
    }

    fun connect() {
        // 既に接続中または接続済みの場合は何もしない
        if (_uiState.value !is PrinterUiState.Disconnected) return

        viewModelScope.launch {
            try {
                _uiState.value = PrinterUiState.Connecting
                _logs.value = emptyList()

                // Simulate Bluetooth Classic connection
                _logs.value = _logs.value + "> Pairing with SM-S210i..."
                delay(500)
                _logs.value = _logs.value + "> SPP Channel: 1"
                delay(500)
                _logs.value = _logs.value + "> Connected via RFCOMM"

                _uiState.value = PrinterUiState.Connected(printerName = "SM-S210i")
            } catch (e: Exception) {
                _logs.value = _logs.value + "> Error: ${e.message}"
                _uiState.value = PrinterUiState.Error(message = e.message ?: "Unknown error")
                delay(2000)
                _uiState.value = PrinterUiState.Disconnected
            }
        }
    }

    fun disconnect() {
        _logs.value = _logs.value + "> Disconnected"
        _uiState.value = PrinterUiState.Disconnected
        _showCommand.value = false
    }

    fun print() {
        val currentState = _uiState.value
        if (currentState !is PrinterUiState.Connected || _text.value.trim().isEmpty()) return

        // 既に印刷中の場合は何もしない
        if (_uiState.value is PrinterUiState.Printing) return

        viewModelScope.launch {
            try {
                _uiState.value = PrinterUiState.Printing

                _logs.value = _logs.value + "> Sending ${_text.value.length + 10} bytes..."
                delay(1000)
                _logs.value = _logs.value + "> Done!"
                _showCommand.value = true

                _uiState.value = currentState
            } catch (e: Exception) {
                _logs.value = _logs.value + "> Print Error: ${e.message}"
                _uiState.value = PrinterUiState.Error(message = e.message ?: "Unknown error")
                delay(2000)
                _uiState.value = PrinterUiState.Disconnected
            }
        }
    }
}
