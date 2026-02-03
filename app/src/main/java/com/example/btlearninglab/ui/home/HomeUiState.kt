package com.example.btlearninglab.ui.home

import androidx.compose.ui.graphics.Color

data class DeviceInfo(
    val id: String,
    val name: String,
    val type: String,
    val backgroundColor: Color,
    val borderColor: Color,
    val iconColor: Color,
    val route: String
)

data class HomeUiState(
    val scaleConnected: Boolean = false,
    val printerConnected: Boolean = false,
    val epaperConnected: Boolean = false,
    val recentLogs: List<String> = emptyList()
)
