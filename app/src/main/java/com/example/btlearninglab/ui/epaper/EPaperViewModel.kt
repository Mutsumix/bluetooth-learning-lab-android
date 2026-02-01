package com.example.btlearninglab.ui.epaper

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EPaperViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<EPaperUiState>(EPaperUiState.Idle)
    val uiState: StateFlow<EPaperUiState> = _uiState.asStateFlow()

    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    private val _apUrl = MutableStateFlow("http://192.168.1.100")
    val apUrl: StateFlow<String> = _apUrl.asStateFlow()

    fun updateApUrl(newUrl: String) {
        _apUrl.value = newUrl
    }

    fun send() {
        // 基本的なバリデーション
        val url = _apUrl.value.trim()
        if (url.isEmpty()) return

        // 二重実行防止
        val currentState = _uiState.value
        if (currentState is EPaperUiState.Sending) return

        viewModelScope.launch {
            try {
                _uiState.value = EPaperUiState.Sending
                _logs.value = emptyList()

                // Simulate HTTP request with delays
                _logs.value = listOf("> Connecting to AP...")
                delay(500)

                _logs.value = _logs.value + "> POST /api/image"
                delay(500)

                _logs.value = _logs.value + "> Response: 200 OK"
                delay(500)

                _logs.value = _logs.value + "> (AP→BLE→ESL で転送中)"
                delay(500)

                val httpRequest = listOf(
                    "POST $url/imgupload",
                    "Content-Type: multipart/form-data",
                    "file: image.jpg (296x128)",
                    "mac: AA:BB:CC:DD:EE:FF",
                    "dither: 0"
                )

                _uiState.value = EPaperUiState.Sent(httpRequest = httpRequest)

                // 3秒後にIdleに戻す
                delay(3000)
                if (_uiState.value is EPaperUiState.Sent) {
                    _uiState.value = EPaperUiState.Idle
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _logs.value = _logs.value + "> Error: ${e.message ?: "Unknown error"}"
                _uiState.value = EPaperUiState.Error(message = e.message ?: "Unknown error")

                // エラー後Idleに戻す
                delay(2000)
                _uiState.value = EPaperUiState.Idle
            }
        }
    }
}
