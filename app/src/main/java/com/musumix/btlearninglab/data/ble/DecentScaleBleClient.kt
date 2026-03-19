package com.musumix.btlearninglab.data.ble

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

    private fun timestamp(): String {
        val sdf = java.text.SimpleDateFormat("HH:mm:ss.SSS", java.util.Locale.US)
        return sdf.format(java.util.Date())
    }

    private fun addLog(tag: String, message: String) {
        _logs.value = _logs.value + "[${timestamp()}][$tag] $message"
    }

    private fun shortUuid(uuid: UUID): String {
        val hex = uuid.toString().substring(4, 8).uppercase()
        return hex
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    addLog("GATT", "Connected (status=$status)")
                    _connectionState.value = BleConnectionState.Connected
                    addLog("GATT", "Discovering services...")
                    gatt.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    addLog("GATT", "Disconnected (status=$status)")
                    _connectionState.value = BleConnectionState.Idle
                    _weightData.value = null
                }
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                addLog("GATT", "Services discovered: ${gatt.services.size} service(s)")
                for (service in gatt.services) {
                    val sUuid = shortUuid(service.uuid)
                    val chars = service.characteristics
                    addLog("GATT", "  Service: $sUuid (${chars.size} characteristic(s))")
                    for (char in chars) {
                        val cUuid = shortUuid(char.uuid)
                        val props = mutableListOf<String>()
                        if (char.properties and BluetoothGattCharacteristic.PROPERTY_READ != 0) props.add("Read")
                        if (char.properties and BluetoothGattCharacteristic.PROPERTY_WRITE != 0) props.add("Write")
                        if (char.properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE != 0) props.add("WriteNoResp")
                        if (char.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0) props.add("Notify")
                        if (char.properties and BluetoothGattCharacteristic.PROPERTY_INDICATE != 0) props.add("Indicate")
                        addLog("GATT", "    └ $cUuid [${props.joinToString(", ")}]")
                    }
                }
                setupNotifications(gatt)
            } else {
                addLog("GATT", "Service discovery failed (status=$status)")
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
                addLog("NOTIFY", "Enabled on ${shortUuid(descriptor.characteristic.uuid)} (CCCD=0x0001)")
                addLog("NOTIFY", "Receiving weight data at ~10Hz")
            } else {
                addLog("NOTIFY", "Failed to enable (status=$status)")
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
                    val stability = if (parsedData.isStable) "stable" else "unstable"
                    addLog("NOTIFY", "RX: ${parsedData.rawHex} → ${parsedData.weight}g ($stability)")
                } catch (e: Exception) {
                    addLog("NOTIFY", "Parse error: ${e.message}")
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
                addLog("WRITE", "Success on ${shortUuid(characteristic.uuid)}")
            } else {
                addLog("WRITE", "Failed (status=$status)")
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun connect(device: BluetoothDevice) {
        addLog("GATT", "Connecting to ${device.address} (transport=LE)...")
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
            addLog("GATT", "Service ${shortUuid(SERVICE_UUID)} not found")
            _connectionState.value = BleConnectionState.Error("Service not found")
            return
        }

        val characteristic = service.getCharacteristic(NOTIFY_UUID)
        if (characteristic == null) {
            addLog("GATT", "Characteristic ${shortUuid(NOTIFY_UUID)} not found")
            _connectionState.value = BleConnectionState.Error("Characteristic not found")
            return
        }

        addLog("NOTIFY", "Subscribing to ${shortUuid(NOTIFY_UUID)}...")
        gatt.setCharacteristicNotification(characteristic, true)

        val descriptor = characteristic.getDescriptor(CCCD_UUID)
        if (descriptor == null) {
            addLog("NOTIFY", "CCCD descriptor (2902) not found")
            _connectionState.value = BleConnectionState.Error("Descriptor not found")
            return
        }

        addLog("NOTIFY", "Writing CCCD descriptor (0x0001 = ENABLE_NOTIFICATION)")
        descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        gatt.writeDescriptor(descriptor)
    }

    @SuppressLint("MissingPermission")
    fun sendTare() {
        val gatt = bluetoothGatt
        if (gatt == null) {
            addLog("WRITE", "Not connected")
            return
        }

        val service = gatt.getService(SERVICE_UUID)
        val characteristic = service?.getCharacteristic(WRITE_UUID)

        if (characteristic == null) {
            addLog("WRITE", "Characteristic ${shortUuid(WRITE_UUID)} not found")
            return
        }

        val tareCommand = byteArrayOf(0x03, 0x0F, 0x00, 0x00, 0x00, 0x01, 0x0E)
        val hexString = tareCommand.joinToString(" ") { "%02X".format(it) }

        addLog("WRITE", "TX: $hexString → ${shortUuid(WRITE_UUID)} (Tare)")

        characteristic.value = tareCommand
        characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        gatt.writeCharacteristic(characteristic)
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        addLog("GATT", "Disconnecting...")
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        _connectionState.value = BleConnectionState.Idle
        _weightData.value = null
    }
}
