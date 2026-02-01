package com.example.btlearninglab.ui.printer

sealed interface PrinterUiState {
    object Disconnected : PrinterUiState
    object Connecting : PrinterUiState
    data class Connected(
        val printerName: String
    ) : PrinterUiState
    object Printing : PrinterUiState
    data class Error(val message: String) : PrinterUiState
}
