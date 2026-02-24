package com.example.btlearninglab.ui.printer

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.btlearninglab.data.printer.PrinterDeviceRepository
import com.example.btlearninglab.data.printer.StarXpandPrinterClient
import com.example.btlearninglab.data.scale.WeightRepository

class PrinterViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PrinterViewModel::class.java)) {
            val printerClient = StarXpandPrinterClient(application.applicationContext)
            val deviceRepository = PrinterDeviceRepository(application.applicationContext)
            val weightRepository = WeightRepository(application.applicationContext)

            @Suppress("UNCHECKED_CAST")
            return PrinterViewModel(printerClient, deviceRepository, weightRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
