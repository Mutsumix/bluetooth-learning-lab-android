package com.example.btlearninglab.data.printer

data class ScannedPrinter(
    val name: String,
    val identifier: String,
    val modelName: String
) {
    fun getDisplayName(): String {
        return "$name ($modelName)"
    }
}
