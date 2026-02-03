package com.example.btlearninglab.data.printer

import android.content.Context
import android.content.SharedPreferences

class PrinterDeviceRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "printer_device_prefs",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_SAVED_PRINTER_IDENTIFIER = "saved_printer_identifier"
    }

    fun saveSelectedPrinter(identifier: String) {
        prefs.edit().putString(KEY_SAVED_PRINTER_IDENTIFIER, identifier).apply()
    }

    fun getSavedPrinterIdentifier(): String? {
        return prefs.getString(KEY_SAVED_PRINTER_IDENTIFIER, null)
    }

    fun clearSavedPrinter() {
        prefs.edit().remove(KEY_SAVED_PRINTER_IDENTIFIER).apply()
    }
}
