package com.example.btlearninglab.data.http

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * HTTP client for E-Paper communication.
 * Handles image upload to OpenEPaperLink AP via multipart/form-data POST request.
 */
class EPaperHttpClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    /**
     * Uploads an image to the E-Paper AP.
     *
     * @param apIp IP address of the AP (without http://)
     * @param macAddress MAC address of the E-Paper tag
     * @param imageData JPEG image data as ByteArray
     * @param dither Dithering setting (0 = disabled, 1 = enabled)
     * @return Result containing response text on success, or exception on failure
     */
    suspend fun uploadImage(
        apIp: String,
        macAddress: String,
        imageData: ByteArray,
        dither: Int = 0
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            addLog("> Connecting to AP...")
            addLog("> DEBUG: apIp='$apIp', mac='$macAddress' (len=${macAddress.length}), dither=$dither, imageSize=${imageData.size}")
            val url = "http://$apIp/imgupload"
            addLog("> URL: $url")

            // Build multipart form data
            // IMPORTANT: Order matters! ESP32 expects mac, dither, then file
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("mac", macAddress)
                .addFormDataPart("dither", dither.toString())
                .addFormDataPart(
                    "file",
                    "image.jpg",
                    imageData.toRequestBody("image/jpeg".toMediaType())
                )
                .build()

            addLog("> POST /imgupload")
            addLog("> Content-Type: multipart/form-data")
            addLog("> file: image.jpg (${imageData.size} bytes)")
            addLog("> mac: '$macAddress'")
            addLog("> dither: '$dither'")

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val responseText = response.body?.string() ?: ""
                addLog("> Response: ${response.code} OK")
                addLog("> $responseText")
                Result.success(responseText)
            } else {
                val errorBody = response.body?.string() ?: ""
                val errorMsg = "HTTP ${response.code}: ${response.message}"
                addLog("> Error: $errorMsg")
                if (errorBody.isNotEmpty()) {
                    addLog("> Error body: $errorBody")
                }
                Result.failure(Exception("$errorMsg\n$errorBody"))
            }
        } catch (e: Exception) {
            addLog("> Exception: ${e.message}")
            Result.failure(e)
        }
    }

    private fun addLog(message: String) {
        _logs.value = _logs.value + message
    }

    /**
     * Clears all logs.
     */
    fun clearLogs() {
        _logs.value = emptyList()
    }
}
