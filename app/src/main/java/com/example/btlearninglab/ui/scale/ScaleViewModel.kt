package com.example.btlearninglab.ui.scale

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.btlearninglab.data.ble.BleConnectionState
import com.example.btlearninglab.data.ble.BluetoothManager
import com.example.btlearninglab.data.ble.DecentScaleBleClient
import com.example.btlearninglab.data.ble.PermissionHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ScaleViewModel(
    private val bleClient: DecentScaleBleClient,
    private val bluetoothManager: BluetoothManager,
    private val permissionHelper: PermissionHelper
) : ViewModel() {
    private val _uiState = MutableStateFlow<ScaleUiState>(ScaleUiState.Disconnected)
    val uiState: StateFlow<ScaleUiState> = _uiState.asStateFlow()

    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    private var deviceAddress: String? = null

    init {
        observeBleState()
        observeWeightData()
        observeBleClientLogs()
    }

    fun connect() {
        // Check permissions
        if (!permissionHelper.hasRequiredPermissions()) {
            _uiState.value = ScaleUiState.Error("Bluetoothパーミッションが必要です")
            _logs.value = listOf("> Permission denied")
            return
        }

        // Start scanning
        _logs.value = emptyList()
        _logs.value = _logs.value + "> Scanning for \"Decent Scale\"..."
        _uiState.value = ScaleUiState.Connecting
        bluetoothManager.startScan()

        // Set timeout for scanning
        viewModelScope.launch {
            delay(10000)
            val currentState = bluetoothManager.scanState.value
            if (currentState is BleConnectionState.Scanning) {
                bluetoothManager.stopScan()
                _uiState.value = ScaleUiState.Error("デバイスが見つかりませんでした")
                _logs.value = _logs.value + "> Scan timeout"
                viewModelScope.launch {
                    delay(2000)
                    if (_uiState.value is ScaleUiState.Error) {
                        _uiState.value = ScaleUiState.Disconnected
                    }
                }
            }
        }
    }

    private fun observeBleState() {
        viewModelScope.launch {
            bluetoothManager.scanState.collect { state ->
                when (state) {
                    is BleConnectionState.Scanning -> {
                        // Already handled in connect()
                    }
                    is BleConnectionState.DeviceFound -> {
                        _logs.value = _logs.value + "> Found: ${state.address}"
                        _logs.value = _logs.value + "> Signal: ${state.rssi} dBm"
                        deviceAddress = state.address

                        // Connect to device
                        val device = bluetoothManager.getDeviceByAddress(state.address)
                        if (device != null) {
                            bleClient.connect(device)
                        } else {
                            _logs.value = _logs.value + "> Error: Cannot get device"
                            _uiState.value = ScaleUiState.Error("デバイスの取得に失敗しました")
                        }
                    }
                    is BleConnectionState.Error -> {
                        _logs.value = _logs.value + "> Error: ${state.message}"
                        _uiState.value = ScaleUiState.Error(state.message)
                        viewModelScope.launch {
                            delay(2000)
                            if (_uiState.value is ScaleUiState.Error) {
                                _uiState.value = ScaleUiState.Disconnected
                            }
                        }
                    }
                    else -> {
                        // Ignore other states
                    }
                }
            }
        }

        viewModelScope.launch {
            bleClient.connectionState.collect { state ->
                when (state) {
                    is BleConnectionState.Connecting -> {
                        _uiState.value = ScaleUiState.Connecting
                    }
                    is BleConnectionState.Connected -> {
                        // Weight data will update the UI state
                    }
                    is BleConnectionState.Idle -> {
                        if (_uiState.value !is ScaleUiState.Error) {
                            _uiState.value = ScaleUiState.Disconnected
                        }
                    }
                    is BleConnectionState.Error -> {
                        _logs.value = _logs.value + "> Error: ${state.message}"
                        _uiState.value = ScaleUiState.Error(state.message)
                        viewModelScope.launch {
                            delay(2000)
                            if (_uiState.value is ScaleUiState.Error) {
                                _uiState.value = ScaleUiState.Disconnected
                            }
                        }
                    }
                    else -> {
                        // Ignore other states
                    }
                }
            }
        }
    }

    private fun observeWeightData() {
        viewModelScope.launch {
            bleClient.weightData.collect { data ->
                data?.let {
                    _uiState.value = ScaleUiState.Connected(
                        weight = it.weight,
                        rawData = it.rawHex
                    )
                }
            }
        }
    }

    private fun observeBleClientLogs() {
        viewModelScope.launch {
            bleClient.logs.collect { logs ->
                _logs.value = logs
            }
        }
    }

    fun disconnect() {
        bleClient.disconnect()
        _uiState.value = ScaleUiState.Disconnected
    }

    fun tare() {
        bleClient.sendTare()
    }

    override fun onCleared() {
        super.onCleared()
        bleClient.disconnect()
        bluetoothManager.stopScan()
    }
}
