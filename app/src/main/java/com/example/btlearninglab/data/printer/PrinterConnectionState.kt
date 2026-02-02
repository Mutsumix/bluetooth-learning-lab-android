package com.example.btlearninglab.data.printer

sealed interface PrinterConnectionState {
    object Idle : PrinterConnectionState
    object Discovering : PrinterConnectionState
    data class DeviceFound(val name: String, val identifier: String) : PrinterConnectionState
    object Connecting : PrinterConnectionState
    data class Connected(val printerName: String) : PrinterConnectionState
    object Printing : PrinterConnectionState
    data class Error(val message: String) : PrinterConnectionState
}
