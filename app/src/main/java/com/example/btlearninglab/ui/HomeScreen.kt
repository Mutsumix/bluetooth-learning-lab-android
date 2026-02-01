package com.example.btlearninglab.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.btlearninglab.R
import com.example.btlearninglab.navigation.Screen
import com.example.btlearninglab.ui.theme.AppColors
import com.example.btlearninglab.ui.components.BottomNavigationBar

data class DeviceInfo(
    val id: String,
    val name: String,
    val type: String,
    val status: String,
    val backgroundColor: Color,
    val borderColor: Color,
    val iconColor: Color,
    val route: String
)

@Composable
fun HomeScreen(navController: NavController) {
    val devices = remember {
        listOf(
            DeviceInfo(
                id = "scale",
                name = "Decent Scale",
                type = "BLE · 重量取得",
                status = "未接続",
                backgroundColor = AppColors.PastelMint.copy(alpha = 0.3f),
                borderColor = AppColors.PastelMint,
                iconColor = AppColors.ScaleIcon,
                route = Screen.Scale.route
            ),
            DeviceInfo(
                id = "printer",
                name = "SM-S210i Printer",
                type = "Bluetooth Classic · 印刷",
                status = "未接続",
                backgroundColor = AppColors.PastelPeach.copy(alpha = 0.3f),
                borderColor = AppColors.PastelPeach,
                iconColor = AppColors.PrinterIcon,
                route = Screen.Printer.route
            ),
            DeviceInfo(
                id = "epaper",
                name = "Gicisky E-Paper",
                type = "HTTP → ESP32 AP → BLE",
                status = "未接続",
                backgroundColor = AppColors.PastelLavender.copy(alpha = 0.3f),
                borderColor = AppColors.PastelLavender,
                iconColor = AppColors.EPaperIcon,
                route = Screen.EPaper.route
            )
        )
    }

    val logs = remember { mutableStateListOf<String>() }

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
                modifier = Modifier.weight(1f)
            ) {
            // Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White.copy(alpha = 0.8f),
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(AppColors.Primary400, AppColors.Primary500)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_bluetooth),
                            contentDescription = "Bluetooth",
                            modifier = Modifier.size(24.dp),
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "BT Learning Lab",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Gray800
                    )
                }
            }

            // Device Cards
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                devices.forEach { device ->
                    DeviceCard(
                        device = device,
                        onClick = { navController.navigate(device.route) }
                    )
                }
            }

            // Communication Log
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                tonalElevation = 0.dp
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = AppColors.Primary50,
                                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                            )
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_log),
                            contentDescription = "Log",
                            modifier = Modifier.size(20.dp),
                            tint = AppColors.Primary500
                        )
                        Text(
                            text = "通信ログ",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.Gray700
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AppColors.Gray50.copy(alpha = 0.5f))
                            .padding(20.dp)
                            .heightIn(min = 120.dp),
                        contentAlignment = if (logs.isEmpty()) Alignment.Center else Alignment.TopStart
                    ) {
                        if (logs.isEmpty()) {
                            Text(
                                text = "(起動時は空)",
                                fontSize = 14.sp,
                                color = AppColors.Gray400,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
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

@Composable
fun DeviceCard(
    device: DeviceInfo,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .border(
                width = 1.dp,
                color = device.borderColor,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Box(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(
                            width = 1.dp,
                            color = AppColors.Gray100,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(
                            id = when (device.id) {
                                "scale" -> R.drawable.ic_scale
                                "printer" -> R.drawable.ic_printer
                                "epaper" -> R.drawable.ic_epaper
                                else -> R.drawable.ic_bluetooth
                            }
                        ),
                        contentDescription = device.name,
                        modifier = Modifier.size(28.dp),
                        tint = device.iconColor
                    )
                }

                // Info
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = device.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.Gray800
                    )
                    Text(
                        text = device.type,
                        fontSize = 14.sp,
                        color = AppColors.Gray500
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(AppColors.Gray300)
                        )
                        Text(
                            text = device.status,
                            fontSize = 12.sp,
                            color = AppColors.Gray400
                        )
                    }
                }

                // Arrow
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_right),
                    contentDescription = "Navigate",
                    modifier = Modifier.size(24.dp),
                    tint = AppColors.Gray300
                )
            }
        }
    }
}
