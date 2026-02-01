package com.example.btlearninglab.data.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class DecentScaleBleClient(private val context: Context) {
    private var bluetoothGatt: BluetoothGatt? = null

    private val _connectionState = MutableStateFlow<BleConnectionState>(BleConnectionState.Idle)
    val connectionState: StateFlow<BleConnectionState> = _connectionState.asStateFlow()

    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    private val _weightData = MutableStateFlow<ScaleData?>(null)
    val weightData: StateFlow<ScaleData?> = _weightData.asStateFlow()

    companion object {
        val SERVICE_UUID: UUID = UUID.fromString("0000FFF0-0000-1000-8000-00805F9B34FB")
        val NOTIFY_UUID: UUID = UUID.fromString("0000FFF4-0000-1000-8000-00805F9B34FB")
        val WRITE_UUID: UUID = UUID.fromString("000036F5-0000-1000-8000-00805F9B34FB")
        private val CCCD_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB")
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    addLog("> GATT Connected")
                    _connectionState.value = BleConnectionState.Connected
                    addLog("> Discovering services...")
                    gatt.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    addLog("> Disconnected")
                    _connectionState.value = BleConnectionState.Idle
                    _weightData.value = null
                }
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                addLog("> Services discovered: ${gatt.services.size}")
                setupNotifications(gatt)
            } else {
                addLog("> Service discovery failed: $status")
                _connectionState.value = BleConnectionState.Error("Service discovery failed")
            }
        }

        @SuppressLint("MissingPermission")
        override fun onDescriptorWrite(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                addLog("> Notification enabled")
                addLog("> Receiving data at 10Hz")
            } else {
                addLog("> Failed to enable notification: $status")
                _connectionState.value = BleConnectionState.Error("Failed to enable notifications")
            }
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            if (characteristic.uuid == NOTIFY_UUID) {
                val data = characteristic.value
                try {
                    val parsedData = DecentScaleDataParser.parse(data)
                    _weightData.value = parsedData
                    addLog("> RX: ${parsedData.rawHex} → ${parsedData.weight}g")
                } catch (e: Exception) {
                    addLog("> Parse error: ${e.message}")
                }
            }
        }

        @SuppressLint("MissingPermission")
        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                addLog("> Tare command sent")
                addLog("> Weight reset to 0.0g")
            } else {
                addLog("> Write failed: $status")
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun connect(device: BluetoothDevice) {
        addLog("> Connecting to ${device.address}...")
        _connectionState.value = BleConnectionState.Connecting

        bluetoothGatt = device.connectGatt(
            context,
            false,
            gattCallback,
            BluetoothDevice.TRANSPORT_LE
        )
    }

    @SuppressLint("MissingPermission")
    private fun setupNotifications(gatt: BluetoothGatt) {
        val service = gatt.getService(SERVICE_UUID)
        if (service == null) {
            addLog("> Service FFF0 not found")
            _connectionState.value = BleConnectionState.Error("Service not found")
            return
        }

        val characteristic = service.getCharacteristic(NOTIFY_UUID)
        if (characteristic == null) {
            addLog("> Characteristic FFF4 not found")
            _connectionState.value = BleConnectionState.Error("Characteristic not found")
            return
        }

        addLog("> Subscribing to FFF4...")
        gatt.setCharacteristicNotification(characteristic, true)

        val descriptor = characteristic.getDescriptor(CCCD_UUID)
        if (descriptor == null) {
            addLog("> CCCD descriptor not found")
            _connectionState.value = BleConnectionState.Error("Descriptor not found")
            return
        }

        descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        gatt.writeDescriptor(descriptor)
    }

    @SuppressLint("MissingPermission")
    fun sendTare() {
        val gatt = bluetoothGatt
        if (gatt == null) {
            addLog("> Not connected")
            return
        }

        val service = gatt.getService(SERVICE_UUID)
        val characteristic = service?.getCharacteristic(WRITE_UUID)

        if (characteristic == null) {
            addLog("> Write characteristic not found")
            return
        }

        val tareCommand = byteArrayOf(0x03, 0x0F, 0x00, 0x00, 0x00, 0x01, 0x0E)
        val hexString = tareCommand.joinToString(" ") { "%02X".format(it) }

        addLog("> Sending Tare: $hexString")

        characteristic.value = tareCommand
        characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        gatt.writeCharacteristic(characteristic)
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        _connectionState.value = BleConnectionState.Idle
        _weightData.value = null
    }

    private fun addLog(message: String) {
        _logs.value = _logs.value + message
    }
}
