package com.example.btlearninglab.data.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager as SystemBluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BluetoothManager(private val context: Context) {
    private val bluetoothAdapter: BluetoothAdapter? =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as? SystemBluetoothManager)?.adapter

    private val bluetoothLeScanner: BluetoothLeScanner? =
        bluetoothAdapter?.bluetoothLeScanner

    private val _scanState = MutableStateFlow<BleConnectionState>(BleConnectionState.Idle)
    val scanState: StateFlow<BleConnectionState> = _scanState.asStateFlow()

    private val _scannedDevices = MutableStateFlow<List<ScannedDevice>>(emptyList())
    val scannedDevices: StateFlow<List<ScannedDevice>> = _scannedDevices.asStateFlow()

    private val scannedDeviceMap = mutableMapOf<String, ScannedDevice>()
    private val scanScope = CoroutineScope(Dispatchers.Default)
    private var scanTimeoutJob: Job? = null

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val rssi = result.rssi
            val name = device.name ?: "Unknown"

            val scannedDevice = ScannedDevice(
                name = name,
                address = device.address,
                rssi = rssi
            )

            scannedDeviceMap[device.address] = scannedDevice
            _scannedDevices.value = scannedDeviceMap.values
                .sortedByDescending { it.rssi }
                .toList()

            if (_scanState.value is BleConnectionState.Scanning) {
                _scanState.value = BleConnectionState.DeviceFound(device.address, rssi)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            _scanState.value = BleConnectionState.Error("Scan failed with error code: $errorCode")
            scanTimeoutJob?.cancel()
        }
    }

    @SuppressLint("MissingPermission")
    fun startScan() {
        if (bluetoothAdapter == null) {
            _scanState.value = BleConnectionState.Error("Bluetooth not supported on this device")
            return
        }

        if (!bluetoothAdapter.isEnabled) {
            _scanState.value = BleConnectionState.Error("Bluetooth is disabled. Please enable it in settings.")
            return
        }

        if (bluetoothLeScanner == null) {
            _scanState.value = BleConnectionState.Error("BLE scanner not available")
            return
        }

        scannedDeviceMap.clear()
        _scannedDevices.value = emptyList()
        scanTimeoutJob?.cancel()

        val scanFilter = ScanFilter.Builder()
            .setDeviceName("Decent Scale")
            .build()

        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        bluetoothLeScanner.startScan(
            listOf(scanFilter),
            scanSettings,
            scanCallback
        )

        _scanState.value = BleConnectionState.Scanning

        scanTimeoutJob = scanScope.launch {
            delay(10000)
            stopScan()
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        bluetoothLeScanner?.stopScan(scanCallback)
        scanTimeoutJob?.cancel()
    }

    fun getDeviceByAddress(address: String) = bluetoothAdapter?.getRemoteDevice(address)
}
