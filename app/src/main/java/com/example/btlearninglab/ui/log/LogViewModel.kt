package com.example.btlearninglab.ui.log

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LogViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(
        LogUiState(
            logs = listOf(
                LogEntry("14:23:45", "Scale", "Scanning for \"Decent Scale\"", DeviceType.SCALE),
                LogEntry("14:23:46", "Scale", "Found: XX:XX:XX:XX:XX:XX", DeviceType.SCALE),
                LogEntry("14:23:47", "Scale", "Connected successfully", DeviceType.SCALE),
                LogEntry("14:24:12", "Printer", "Pairing with SM-S210i...", DeviceType.PRINTER),
                LogEntry("14:24:13", "Printer", "SPP Channel: 1", DeviceType.PRINTER),
                LogEntry("14:24:14", "Printer", "Connected via RFCOMM", DeviceType.PRINTER),
                LogEntry("14:25:01", "E-Paper", "POST /api/image", DeviceType.EPAPER),
                LogEntry("14:25:02", "E-Paper", "Response: 200 OK", DeviceType.EPAPER)
            ),
            scaleCount = 3,
            printerCount = 3,
            epaperCount = 2
        )
    )
    val uiState: StateFlow<LogUiState> = _uiState.asStateFlow()

    fun clearLogs() {
        _uiState.value = LogUiState(
            logs = emptyList(),
            scaleCount = 0,
            printerCount = 0,
            epaperCount = 0
        )
    }
}
