package com.example.btlearninglab.ui.scale

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.btlearninglab.R
import com.example.btlearninglab.ui.theme.AppColors
import com.example.btlearninglab.ui.components.BottomNavigationBar

@Composable
fun ScaleScreen(
    navController: NavController,
    viewModel: ScaleViewModel = viewModel(
        factory = ScaleViewModelFactory(
            LocalContext.current.applicationContext as Application
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val logs by viewModel.logs.collectAsState()
    val scannedDevices by viewModel.scannedDevices.collectAsState()
    val selectedDevice by viewModel.selectedDevice.collectAsState()

    ScaleScreenContent(
        navController = navController,
        uiState = uiState,
        logs = logs,
        scannedDevices = scannedDevices,
        selectedDevice = selectedDevice,
        onStartScan = viewModel::startScan,
        onSelectDevice = viewModel::selectDevice,
        onConnectToSelected = viewModel::connectToSelected,
        onConnect = viewModel::connect,
        onDisconnect = viewModel::disconnect,
        onTare = viewModel::tare
    )
}

@Composable
private fun ScaleScreenContent(
    navController: NavController,
    uiState: ScaleUiState,
    logs: List<String>,
    scannedDevices: List<com.example.btlearninglab.data.ble.ScannedDevice>,
    selectedDevice: com.example.btlearninglab.data.ble.ScannedDevice?,
    onStartScan: () -> Unit,
    onSelectDevice: (com.example.btlearninglab.data.ble.ScannedDevice) -> Unit,
    onConnectToSelected: () -> Unit,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onTare: () -> Unit
) {
    val isConnected = uiState is ScaleUiState.Connected
    val weight = if (uiState is ScaleUiState.Connected) uiState.weight else 125.4f
    val rawData = if (uiState is ScaleUiState.Connected) uiState.rawData else "03 CE 04 E6 00 00 2B"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(AppColors.Primary50, Color.White)
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
            // Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White.copy(alpha = 0.8f),
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                ) {
                    Text(
                        text = "Scale BLE",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Gray800
                    )
                    Text(
                        text = "Decent Scale",
                        fontSize = 12.sp,
                        color = AppColors.Gray500,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            // Weight Display
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp)
                    .border(
                        width = 1.dp,
                        color = AppColors.PastelMint,
                        shape = RoundedCornerShape(24.dp)
                    ),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isConnected) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = String.format("%.1f", weight),
                                fontSize = 60.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.Emerald600
                            )
                            Text(
                                text = "g",
                                fontSize = 24.sp,
                                color = AppColors.Gray400
                            )
                        }
                    } else {
                        Text(
                            text = "未接続",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.Gray400
                        )
                    }
                }
            }

            // Device Selection Dropdown
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (scannedDevices.isNotEmpty()) {
                    var expanded by remember { mutableStateOf(false) }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "デバイス選択",
                            fontSize = 14.sp,
                            color = AppColors.Gray500
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            shadowElevation = 2.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box {
                                TextButton(
                                    onClick = { expanded = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = selectedDevice?.getDisplayName() ?: "デバイスを選択",
                                            fontSize = 14.sp,
                                            color = AppColors.Gray700
                                        )
                                        Text(
                                            text = "▼",
                                            fontSize = 12.sp,
                                            color = AppColors.Gray500
                                        )
                                    }
                                }
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    scannedDevices.forEach { device ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(
                                                        text = device.name,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp
                                                    )
                                                    Text(
                                                        text = "${device.address.takeLast(8)} / RSSI: ${device.rssi}",
                                                        fontSize = 12.sp,
                                                        color = AppColors.Gray500
                                                    )
                                                }
                                            },
                                            onClick = {
                                                onSelectDevice(device)
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (scannedDevices.isEmpty()) {
                    Button(
                        onClick = { if (isConnected) onDisconnect() else onStartScan() },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isConnected) AppColors.Red50 else AppColors.Primary400,
                            contentColor = if (isConnected) AppColors.Red500 else Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Text(
                            text = if (isConnected) "切断" else "スキャン",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    }
                } else {
                    Button(
                        onClick = { if (isConnected) onDisconnect() else onConnectToSelected() },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        enabled = selectedDevice != null || isConnected,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isConnected) AppColors.Red50 else AppColors.Primary400,
                            contentColor = if (isConnected) AppColors.Red500 else Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Text(
                            text = if (isConnected) "切断" else "接続",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    }
                }
                OutlinedButton(
                    onClick = onTare,
                    enabled = isConnected,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = if (isConnected) AppColors.Emerald600 else AppColors.Gray400,
                        disabledContainerColor = Color.White,
                        disabledContentColor = AppColors.Gray400
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = if (isConnected) AppColors.PastelMint else AppColors.Gray200
                    )
                ) {
                    Text(
                        text = "Tare",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            }

            // Raw Data
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
                    .border(
                        width = 1.dp,
                        color = AppColors.Primary100,
                        shape = RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "Raw Data",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.Gray600
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AppColors.Gray50.copy(alpha = 0.5f))
                            .padding(20.dp),
                        contentAlignment = if (!isConnected) Alignment.Center else Alignment.TopStart
                    ) {
                        if (isConnected) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = rawData,
                                    fontSize = 12.sp,
                                    color = AppColors.Primary600,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                )
                                Text(
                                    text = "→ Weight: ${(weight * 10).toInt()} (${String.format("%.1f", weight)}g)",
                                    fontSize = 12.sp,
                                    color = AppColors.Gray500,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                )
                            }
                        } else {
                            Text(
                                text = "(接続してください)",
                                fontSize = 12.sp,
                                color = AppColors.Gray400,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // Communication Log
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp)
                    .border(
                        width = 1.dp,
                        color = AppColors.Primary100,
                        shape = RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_log),
                            contentDescription = "Log",
                            modifier = Modifier.size(18.dp),
                            tint = AppColors.Primary500
                        )
                        Text(
                            text = "通信ログ",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.Gray600
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AppColors.Gray50.copy(alpha = 0.5f))
                            .padding(20.dp)
                            .heightIn(min = 100.dp, max = 200.dp),
                        contentAlignment = if (logs.isEmpty()) Alignment.Center else Alignment.TopStart
                    ) {
                        if (logs.isEmpty()) {
                            Text(
                                text = "(接続してください)",
                                fontSize = 12.sp,
                                color = AppColors.Gray400,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                logs.forEach { log ->
                                    Text(
                                        text = log,
                                        fontSize = 12.sp,
                                        color = AppColors.Primary600,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }
            }

            BottomNavigationBar(navController = navController)
        }
    }
}
