package com.example.btlearninglab.data.ble

sealed interface BleConnectionState {
    object Idle : BleConnectionState
    object Scanning : BleConnectionState
    data class DeviceFound(val address: String, val rssi: Int) : BleConnectionState
    object Connecting : BleConnectionState
    object Connected : BleConnectionState
    object Disconnecting : BleConnectionState
    data class Error(val message: String) : BleConnectionState
}
