package com.example.btlearninglab.ui

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.btlearninglab.R
import com.example.btlearninglab.ui.theme.AppColors
import com.example.btlearninglab.ui.components.BottomNavigationBar

@Composable
fun ScaleScreen(navController: NavController) {
    var isConnected by remember { mutableStateOf(false) }
    var weight by remember { mutableStateOf(125.4f) }
    val logs = remember { mutableStateListOf<String>() }

    val handleConnect = {
        if (!isConnected) {
            logs.clear()
            logs.addAll(
                listOf(
                    "> Scanning for \"Decent Scale\"",
                    "> Found: XX:XX:XX:XX:XX:XX",
                    "> Connecting...",
                    "> Subscribing to FFF4...",
                    "> Receiving notifications"
                )
            )
            isConnected = true
        } else {
            logs.add("> Disconnected")
            isConnected = false
        }
    }

    val handleTare = {
        if (isConnected) {
            logs.add("> Sending Tare command: 03 0F 00 00 00 01 0E")
            logs.add("> Weight reset to 0.0g")
            weight = 0.0f
        }
    }

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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.Transparent,
                    shadowElevation = 2.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        AppColors.PastelMint.copy(alpha = 0.4f),
                                        AppColors.Emerald100.copy(alpha = 0.3f)
                                    )
                                )
                            )
                            .border(
                                width = 1.dp,
                                color = AppColors.PastelMint,
                                shape = RoundedCornerShape(24.dp)
                            )
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
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
                    }
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = handleConnect,
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
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isConnected) "🔗" else "🔗",
                            fontSize = 18.sp
                        )
                        Text(
                            text = if (isConnected) "切断" else "接続",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    }
                }
                Button(
                    onClick = handleTare,
                    enabled = isConnected,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isConnected) AppColors.PastelMint.copy(alpha = 0.5f) else AppColors.Gray100,
                        contentColor = if (isConnected) AppColors.Emerald600 else AppColors.Gray400,
                        disabledContainerColor = AppColors.Gray100,
                        disabledContentColor = AppColors.Gray400
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
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
                            .padding(20.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "03 CE 04 E6 00 00 2B",
                                fontSize = 12.sp,
                                color = AppColors.Primary600,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                            Text(
                                text = "→ Weight: 1254 (125.4g)",
                                fontSize = 12.sp,
                                color = AppColors.Gray500,
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
