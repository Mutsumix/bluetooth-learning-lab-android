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
fun PrinterScreen(navController: NavController) {
    var isConnected by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf("Hello, Bluetooth!") }
    val logs = remember { mutableStateListOf<String>() }
    var showCommand by remember { mutableStateOf(false) }

    val handleConnect = {
        if (!isConnected) {
            logs.clear()
            logs.addAll(
                listOf(
                    "> Pairing with SM-S210i...",
                    "> SPP Channel: 1",
                    "> Connected via RFCOMM"
                )
            )
            isConnected = true
        } else {
            logs.add("> Disconnected")
            isConnected = false
            showCommand = false
        }
    }

    val handlePrint = {
        if (isConnected && text.trim().isNotEmpty()) {
            logs.add("> Sending ${text.length + 10} bytes...")
            logs.add("> Done!")
            showCommand = true
        }
    }

    val sentCommand = listOf(
        "1B 40          (Initialize)",
        "1B 74 13       (Set encoding: Japanese)",
        "48 65 6C 6C 6F (\"Hello\")",
        "0A             (Line feed)",
        "1D 56 01       (Cut paper)"
    )

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
                        text = "Printer BT Classic",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Gray800
                    )
                    Text(
                        text = "SM-S210i",
                        fontSize = 12.sp,
                        color = AppColors.Gray500,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            // Text Input
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Box(
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = AppColors.PastelPeach,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        TextField(
                            value = text,
                            onValueChange = { if (it.length <= 200) text = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(96.dp),
                            placeholder = {
                                Text(
                                    text = "印刷するテキストを入力...",
                                    color = AppColors.Gray400
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                focusedTextColor = AppColors.Gray800,
                                unfocusedTextColor = AppColors.Gray800
                            ),
                            textStyle = LocalTextStyle.current.copy(fontSize = 16.sp)
                        )
                        Text(
                            text = "${text.length} / 200",
                            fontSize = 12.sp,
                            color = AppColors.Gray400,
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(top = 8.dp)
                        )
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
                    onClick = handlePrint,
                    enabled = isConnected && text.trim().isNotEmpty(),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isConnected && text.trim().isNotEmpty())
                            AppColors.PastelPeach.copy(alpha = 0.5f)
                        else AppColors.Gray100,
                        contentColor = if (isConnected && text.trim().isNotEmpty())
                            AppColors.Orange600
                        else AppColors.Gray400,
                        disabledContainerColor = AppColors.Gray100,
                        disabledContentColor = AppColors.Gray400
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🖨️",
                            fontSize = 18.sp
                        )
                        Text(
                            text = "印刷",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            // Sent Command
            if (showCommand) {
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
                                text = "Sent Command",
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
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                sentCommand.forEach { cmd ->
                                    Text(
                                        text = cmd,
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
