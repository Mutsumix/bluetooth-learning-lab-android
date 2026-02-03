package com.example.btlearninglab.ui.printer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.btlearninglab.data.printer.PrinterConnectionState
import com.example.btlearninglab.data.printer.PrinterDeviceRepository
import com.example.btlearninglab.data.printer.ScannedPrinter
import com.example.btlearninglab.data.printer.StarXpandPrinterClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PrinterViewModel(
    private val printerClient: StarXpandPrinterClient,
    private val deviceRepository: PrinterDeviceRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<PrinterUiState>(PrinterUiState.Disconnected)
    val uiState: StateFlow<PrinterUiState> = _uiState.asStateFlow()

    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    private val _text = MutableStateFlow("Hello, Bluetooth!")
    val text: StateFlow<String> = _text.asStateFlow()

    private val _showCommand = MutableStateFlow(false)
    val showCommand: StateFlow<Boolean> = _showCommand.asStateFlow()

    val scannedPrinters: StateFlow<List<ScannedPrinter>> = printerClient.scannedPrinters

    private val _selectedPrinter = MutableStateFlow<ScannedPrinter?>(null)
    val selectedPrinter: StateFlow<ScannedPrinter?> = _selectedPrinter.asStateFlow()

    init {
        observePrinterConnectionState()
        observePrinterLogs()
        observeScannedPrinters()
    }

    fun updateText(newText: String) {
        if (newText.length <= 200) {
            _text.value = newText
        }
    }

    fun startScan() {
        if (_uiState.value !is PrinterUiState.Disconnected) return

        viewModelScope.launch {
            try {
                _uiState.value = PrinterUiState.Connecting
                _logs.value = emptyList()
                printerClient.discoverPrinters()
            } catch (e: Exception) {
                _logs.value = _logs.value + "> Error: ${e.message}"
                _uiState.value = PrinterUiState.Error(message = e.message ?: "Unknown error")
            }
        }
    }

    fun selectPrinter(printer: ScannedPrinter) {
        _selectedPrinter.value = printer
        deviceRepository.saveSelectedPrinter(printer.identifier)
        _logs.value = _logs.value + "> Selected: ${printer.name}"
    }

    fun connectToSelected() {
        val printer = _selectedPrinter.value
        if (printer == null) {
            _uiState.value = PrinterUiState.Error("プリンターを選択してください")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = PrinterUiState.Connecting
                _logs.value = _logs.value + "> Connecting to ${printer.name}..."
                printerClient.connectToPrinter(printer)
            } catch (e: Exception) {
                _logs.value = _logs.value + "> Error: ${e.message}"
                _uiState.value = PrinterUiState.Error(message = e.message ?: "Unknown error")
            }
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

    private fun observeScannedPrinters() {
        viewModelScope.launch {
            scannedPrinters.collect { printers ->
                if (printers.isNotEmpty() && _selectedPrinter.value == null) {
                    val savedIdentifier = deviceRepository.getSavedPrinterIdentifier()
                    val printerToSelect = if (savedIdentifier != null) {
                        printers.find { it.identifier == savedIdentifier } ?: printers.first()
                    } else {
                        printers.first()
                    }
                    _selectedPrinter.value = printerToSelect
                    _logs.value = _logs.value + "> Auto-selected: ${printerToSelect.name}"
                }
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
