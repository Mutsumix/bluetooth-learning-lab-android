package com.example.btlearninglab.ui.scale

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.btlearninglab.data.ble.BluetoothManager
import com.example.btlearninglab.data.ble.DecentScaleBleClient
import com.example.btlearninglab.data.ble.PermissionHelper

class ScaleViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScaleViewModel::class.java)) {
            val bleClient = DecentScaleBleClient(application.applicationContext)
            val bluetoothManager = BluetoothManager(application.applicationContext)
            val permissionHelper = PermissionHelper(application.applicationContext)

            @Suppress("UNCHECKED_CAST")
            return ScaleViewModel(bleClient, bluetoothManager, permissionHelper) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
