package com.example.btlearninglab.ui.log

data class LogEntry(
    val time: String,
    val device: String,
    val message: String,
    val type: DeviceType
)

enum class DeviceType {
    SCALE,
    PRINTER,
    EPAPER
}

data class LogUiState(
    val logs: List<LogEntry> = emptyList(),
    val scaleCount: Int = 0,
    val printerCount: Int = 0,
    val epaperCount: Int = 0
)
