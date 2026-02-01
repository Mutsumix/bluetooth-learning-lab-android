package com.example.btlearninglab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.btlearninglab.data.ble.PermissionHelper
import com.example.btlearninglab.navigation.NavGraph
import com.example.btlearninglab.ui.theme.BtLearningLabTheme

class MainActivity : ComponentActivity() {
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (!allGranted) {
            // User denied permissions - they can still navigate the app but BLE won't work
            // Error will be shown when they try to connect
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check and request BLE permissions
        val permissionHelper = PermissionHelper(this)
        if (!permissionHelper.hasRequiredPermissions()) {
            permissionLauncher.launch(permissionHelper.getRequiredPermissions())
        }

        setContent {
            BtLearningLabTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavGraph()
                }
            }
        }
    }
}
