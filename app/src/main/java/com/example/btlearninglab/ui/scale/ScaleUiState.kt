package com.example.btlearninglab.ui.scale

sealed interface ScaleUiState {
    object Disconnected : ScaleUiState
    object Connecting : ScaleUiState
    data class Connected(
        val weight: Float,
        val rawData: String
    ) : ScaleUiState
    data class Error(val message: String) : ScaleUiState
}
