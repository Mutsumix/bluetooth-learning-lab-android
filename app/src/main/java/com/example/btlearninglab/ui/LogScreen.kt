package com.example.btlearninglab.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.btlearninglab.ui.theme.AppColors
import com.example.btlearninglab.ui.components.BottomNavigationBar

data class LogEntry(
    val time: String,
    val device: String,
    val message: String,
    val type: String
)

@Composable
fun LogScreen(navController: NavController) {
    val logs = remember {
        listOf(
            LogEntry("14:23:45", "Scale", "Scanning for \"Decent Scale\"", "ble"),
            LogEntry("14:23:46", "Scale", "Found: XX:XX:XX:XX:XX:XX", "ble"),
            LogEntry("14:23:47", "Scale", "Connected successfully", "ble"),
            LogEntry("14:24:12", "Printer", "Pairing with SM-S210i...", "classic"),
            LogEntry("14:24:13", "Printer", "SPP Channel: 1", "classic"),
            LogEntry("14:24:14", "Printer", "Connected via RFCOMM", "classic"),
            LogEntry("14:25:01", "E-Paper", "POST /api/image", "http"),
            LogEntry("14:25:02", "E-Paper", "Response: 200 OK", "http")
        )
    }

    fun getDeviceColor(type: String): Color {
        return when (type) {
            "ble" -> AppColors.ScaleIcon
            "classic" -> AppColors.PrinterIcon
            "http" -> AppColors.EPaperIcon
            else -> AppColors.Gray400
        }
    }

    fun getDeviceBgColor(type: String): Color {
        return when (type) {
            "ble" -> AppColors.PastelMint.copy(alpha = 0.3f)
            "classic" -> AppColors.PastelPeach.copy(alpha = 0.3f)
            "http" -> AppColors.PastelLavender.copy(alpha = 0.3f)
            else -> AppColors.Gray100
        }
    }

    fun getDeviceBorderColor(type: String): Color {
        return when (type) {
            "ble" -> AppColors.PastelMint
            "classic" -> AppColors.PastelPeach
            "http" -> AppColors.PastelLavender
            else -> AppColors.Gray300
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
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "通信ログ",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.Gray800
                            )
                            Text(
                                text = "全デバイスの通信履歴",
                                fontSize = 12.sp,
                                color = AppColors.Gray500,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        IconButton(
                            onClick = { /* クリアログ */ }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_delete),
                                contentDescription = "Clear log",
                                modifier = Modifier.size(24.dp),
                                tint = AppColors.Gray400
                            )
                        }
                    }
                }

                // Log Entries
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    logs.forEach { log ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.5f),
                            shadowElevation = 2.dp
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(getDeviceBgColor(log.type))
                                    .border(
                                        width = 1.dp,
                                        color = getDeviceBorderColor(log.type),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(16.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = log.time,
                                        fontSize = 12.sp,
                                        color = AppColors.Gray400,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        modifier = Modifier
                                            .width(64.dp)
                                            .padding(top = 2.dp)
                                    )
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(getDeviceColor(log.type))
                                            )
                                            Text(
                                                text = log.device,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = getDeviceColor(log.type)
                                            )
                                        }
                                        Text(
                                            text = log.message,
                                            fontSize = 14.sp,
                                            color = AppColors.Gray600,
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Stats
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "統計情報",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.Gray600
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // BLE
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = AppColors.PastelMint.copy(alpha = 0.2f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "3",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.ScaleIcon
                                    )
                                    Text(
                                        text = "BLE",
                                        fontSize = 12.sp,
                                        color = AppColors.Gray500
                                    )
                                }
                            }
                            // Classic
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = AppColors.PastelPeach.copy(alpha = 0.2f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "3",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.PrinterIcon
                                    )
                                    Text(
                                        text = "Classic",
                                        fontSize = 12.sp,
                                        color = AppColors.Gray500
                                    )
                                }
                            }
                            // HTTP
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = AppColors.PastelLavender.copy(alpha = 0.2f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "2",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.EPaperIcon
                                    )
                                    Text(
                                        text = "HTTP",
                                        fontSize = 12.sp,
                                        color = AppColors.Gray500
                                    )
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
