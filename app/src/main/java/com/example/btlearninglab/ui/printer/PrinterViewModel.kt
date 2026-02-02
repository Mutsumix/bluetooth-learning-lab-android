package com.example.btlearninglab.ui.printer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.btlearninglab.data.printer.PrinterConnectionState
import com.example.btlearninglab.data.printer.StarXpandPrinterClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PrinterViewModel(
    private val printerClient: StarXpandPrinterClient
) : ViewModel() {
    private val _uiState = MutableStateFlow<PrinterUiState>(PrinterUiState.Disconnected)
    val uiState: StateFlow<PrinterUiState> = _uiState.asStateFlow()

    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    private val _text = MutableStateFlow("Hello, Bluetooth!")
    val text: StateFlow<String> = _text.asStateFlow()

    private val _showCommand = MutableStateFlow(false)
    val showCommand: StateFlow<Boolean> = _showCommand.asStateFlow()

    init {
        observePrinterConnectionState()
        observePrinterLogs()
    }

    fun updateText(newText: String) {
        if (newText.length <= 200) {
            _text.value = newText
        }
    }

    fun connect() {
        if (_uiState.value !is PrinterUiState.Disconnected) return

        viewModelScope.launch {
            try {
                _uiState.value = PrinterUiState.Connecting
                _logs.value = emptyList()
                printerClient.discoverAndConnect()
            } catch (e: Exception) {
                _logs.value = _logs.value + "> Error: ${e.message}"
                _uiState.value = PrinterUiState.Error(message = e.message ?: "Unknown error")
            }
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            printerClient.disconnect()
            _showCommand.value = false
        }
    }

    fun print() {
        val currentState = _uiState.value
        if (currentState !is PrinterUiState.Connected || _text.value.trim().isEmpty()) return

        viewModelScope.launch {
            try {
                _uiState.value = PrinterUiState.Printing
                val textToPrint = _text.value
                _logs.value = _logs.value + "> [ViewModel] Printing text: \"$textToPrint\""
                printerClient.print(textToPrint)
                _showCommand.value = true
            } catch (e: Exception) {
                _logs.value = _logs.value + "> Print Error: ${e.message}"
                _uiState.value = PrinterUiState.Error(message = e.message ?: "Unknown error")
            }
        }
    }

    private fun observePrinterConnectionState() {
        viewModelScope.launch {
            printerClient.connectionState.collect { state ->
                when (state) {
                    is PrinterConnectionState.Idle -> {
                        _uiState.value = PrinterUiState.Disconnected
                    }
                    is PrinterConnectionState.Discovering,
                    is PrinterConnectionState.DeviceFound,
                    is PrinterConnectionState.Connecting -> {
                        _uiState.value = PrinterUiState.Connecting
                    }
                    is PrinterConnectionState.Connected -> {
                        _uiState.value = PrinterUiState.Connected(printerName = state.printerName)
                    }
                    is PrinterConnectionState.Printing -> {
                        _uiState.value = PrinterUiState.Printing
                    }
                    is PrinterConnectionState.Error -> {
                        _uiState.value = PrinterUiState.Error(state.message)
                        viewModelScope.launch {
                            delay(2000)
                            if (_uiState.value is PrinterUiState.Error) {
                                _uiState.value = PrinterUiState.Disconnected
                            }
                        }
                    }
                }
            }
        }
    }

    private fun observePrinterLogs() {
        viewModelScope.launch {
            printerClient.logs.collect { logs ->
                _logs.value = logs
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            printerClient.disconnect()
        }
    }
}
