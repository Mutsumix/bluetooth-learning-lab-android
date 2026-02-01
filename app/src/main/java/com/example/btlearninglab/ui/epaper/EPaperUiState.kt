package com.example.btlearninglab.ui.epaper

sealed interface EPaperUiState {
    object Idle : EPaperUiState
    object Sending : EPaperUiState
    data class Sent(
        val httpRequest: List<String>
    ) : EPaperUiState
    data class Error(val message: String) : EPaperUiState
}
