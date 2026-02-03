package com.example.btlearninglab.data.ble

data class ScannedDevice(
    val name: String,
    val address: String,
    val rssi: Int
) {
    fun getDisplayName(): String {
        val shortAddress = address.takeLast(8)
        return "$name ($shortAddress) - RSSI: $rssi"
    }
}
