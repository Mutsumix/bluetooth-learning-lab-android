package com.example.btlearninglab.ui.epaper

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.btlearninglab.data.http.EPaperHttpClient

/**
 * Factory for creating EPaperViewModel with dependencies.
 */
class EPaperViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EPaperViewModel::class.java)) {
            val httpClient = EPaperHttpClient()

            @Suppress("UNCHECKED_CAST")
            return EPaperViewModel(httpClient, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
