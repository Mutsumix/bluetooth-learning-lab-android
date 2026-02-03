package com.example.btlearninglab.ui.epaper

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.btlearninglab.data.epaper.EPaperTag
import com.example.btlearninglab.data.epaper.EPaperTagRepository
import com.example.btlearninglab.data.http.EPaperHttpClient
import com.example.btlearninglab.data.http.ImageGenerator
import com.example.btlearninglab.data.scale.WeightRepository
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for E-Paper screen.
 * Manages HTTP communication with OpenEPaperLink AP.
 */
class EPaperViewModel(
    private val httpClient: EPaperHttpClient,
    private val context: Context
) : ViewModel() {

    private val tagRepository = EPaperTagRepository(context)
    private val weightRepository = WeightRepository(context)

    private val _uiState = MutableStateFlow<EPaperUiState>(EPaperUiState.Idle)
    val uiState: StateFlow<EPaperUiState> = _uiState.asStateFlow()

    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    private val _savedTags = MutableStateFlow<List<EPaperTag>>(emptyList())
    val savedTags: StateFlow<List<EPaperTag>> = _savedTags.asStateFlow()

    private val _apUrl = MutableStateFlow("192.168.1.100")
    val apUrl: StateFlow<String> = _apUrl.asStateFlow()

    private val _macAddress = MutableStateFlow("")
    val macAddress: StateFlow<String> = _macAddress.asStateFlow()

    private val _currentWeight = MutableStateFlow(0.0)
    val currentWeight: StateFlow<Double> = _currentWeight.asStateFlow()

    init {
        observeHttpClientLogs()
        loadSavedTags()
        loadCurrentWeight()
    }

    private fun loadCurrentWeight() {
        viewModelScope.launch {
            _currentWeight.value = weightRepository.getLatestWeight()
        }
    }

    fun refreshWeight() {
        loadCurrentWeight()
    }

    fun updateApUrl(newUrl: String) {
        _apUrl.value = newUrl
    }

    fun updateMacAddress(newMac: String) {
        _macAddress.value = newMac
    }

    fun selectTag(tag: EPaperTag) {
        _apUrl.value = tag.ipAddress
        _macAddress.value = tag.macAddress
    }

    private fun loadSavedTags() {
        viewModelScope.launch {
            val tags = tagRepository.getTags()
            _savedTags.value = tags

            // Load last selected tag
            val lastIndex = tagRepository.getLastSelectedIndex()
            if (lastIndex >= 0 && lastIndex < tags.size) {
                selectTag(tags[lastIndex])
            }
        }
    }

    private suspend fun saveCurrentTag() {
        val url = _apUrl.value.trim()
        val mac = _macAddress.value.trim()

        if (url.isNotEmpty() && mac.isNotEmpty()) {
            // Auto-generate name from MAC
            val name = "Tag ${mac.takeLast(8)}"
            val tag = EPaperTag(name, url, mac)
            tagRepository.addOrUpdateTag(tag)

            // Reload tags
            val tags = tagRepository.getTags()
            _savedTags.value = tags

            // Save as last selected
            val index = tags.indexOfFirst { it.macAddress == mac }
            if (index >= 0) {
                tagRepository.saveLastSelectedIndex(index)
            }
        }
    }

    fun send() {
        val url = _apUrl.value.trim()
        var mac = _macAddress.value.trim()

        // Validation
        if (url.isEmpty() || mac.isEmpty()) {
            _uiState.value = EPaperUiState.Error("AP URLとMACアドレスを入力してください")
            return
        }

        // Remove colons from MAC address - ESP32 expects format without colons
        // 40:33:FF:FF:92:99:21:00 -> 4033FFFF92992100
        mac = mac.replace(":", "")

        // Prevent double execution
        if (_uiState.value is EPaperUiState.Sending) return

        viewModelScope.launch {
            try {
                _uiState.value = EPaperUiState.Sending
                _logs.value = listOf("> [ViewModel] url='$url', mac='$mac'")

                // Get weight data from repository
                val weight = weightRepository.getLatestWeight()
                val currentDate = Date()

                _logs.value = _logs.value + "> Weight: ${weight}g, Date: ${currentDate}"

                // Generate image with weight and date
                val imageData = ImageGenerator.generateWeightImage(weight, currentDate)

                // HTTP send
                // Extract IP address (remove http:// if present)
                val apIp = url.replace("http://", "").replace("https://", "").split("/").first()

                _logs.value = _logs.value + "> [ViewModel] apIp='$apIp'"

                val result = httpClient.uploadImage(apIp, mac, imageData)

                // Build full URL for display
                val fullUrl = if (url.startsWith("http://") || url.startsWith("https://")) url else "http://$url"

                result.fold(
                    onSuccess = { responseText ->
                        val httpRequest = listOf(
                            "POST $fullUrl/imgupload",
                            "Content-Type: multipart/form-data",
                            "file: image.jpg (296x128)",
                            "mac: $mac",
                            "---",
                            "Response: $responseText"
                        )
                        _uiState.value = EPaperUiState.Sent(httpRequest = httpRequest)

                        // Save current tag configuration
                        saveCurrentTag()

                        // Return to Idle after 3 seconds
                        delay(3000)
                        if (_uiState.value is EPaperUiState.Sent) {
                            _uiState.value = EPaperUiState.Idle
                        }
                    },
                    onFailure = { error ->
                        _uiState.value = EPaperUiState.Error(
                            error.message ?: "送信に失敗しました"
                        )

                        delay(2000)
                        _uiState.value = EPaperUiState.Idle
                    }
                )
            } catch (e: Exception) {
                _logs.value = _logs.value + "> Exception: ${e.message}"
                _uiState.value = EPaperUiState.Error(e.message ?: "Unknown error")

                delay(2000)
                _uiState.value = EPaperUiState.Idle
            }
        }
    }

    private fun observeHttpClientLogs() {
        viewModelScope.launch {
            httpClient.logs.collect { httpLogs ->
                // Keep existing ViewModel logs and append HTTP client logs
                val viewModelLogs = _logs.value.filter { it.startsWith("> [ViewModel]") }
                _logs.value = viewModelLogs + httpLogs
            }
        }
    }

}
