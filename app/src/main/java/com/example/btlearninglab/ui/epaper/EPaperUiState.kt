package com.example.btlearninglab.ui.epaper

enum class SendAction { Weight, Logo, Reset }

sealed interface EPaperUiState {
    object Idle : EPaperUiState
    data class Sending(val action: SendAction) : EPaperUiState
    data class Sent(
        val httpRequest: List<String>
    ) : EPaperUiState
    data class Error(val message: String) : EPaperUiState
}
