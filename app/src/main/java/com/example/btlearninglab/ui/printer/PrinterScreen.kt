package com.example.btlearninglab.ui.printer

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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.btlearninglab.R
import com.example.btlearninglab.ui.theme.AppColors
import com.example.btlearninglab.ui.components.BottomNavigationBar

@Composable
fun PrinterScreen(
    navController: NavController,
    viewModel: PrinterViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val logs by viewModel.logs.collectAsState()
    val text by viewModel.text.collectAsState()
    val showCommand by viewModel.showCommand.collectAsState()

    PrinterScreenContent(
        navController = navController,
        uiState = uiState,
        logs = logs,
        text = text,
        showCommand = showCommand,
        onTextChange = viewModel::updateText,
        onConnect = viewModel::connect,
        onDisconnect = viewModel::disconnect,
        onPrint = viewModel::print
    )
}

@Composable
private fun PrinterScreenContent(
    navController: NavController,
    uiState: PrinterUiState,
    logs: List<String>,
    text: String,
    showCommand: Boolean,
    onTextChange: (String) -> Unit,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onPrint: () -> Unit
) {
    val isConnected = uiState is PrinterUiState.Connected
    val isPrinting = uiState is PrinterUiState.Printing

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
                            onValueChange = onTextChange,
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
                    onClick = { if (isConnected) onDisconnect() else onConnect() },
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
                        text = if (isConnected) "切断" else "接続",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
                OutlinedButton(
                    onClick = onPrint,
                    enabled = isConnected && text.trim().isNotEmpty() && !isPrinting,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = if (isConnected && text.trim().isNotEmpty())
                            AppColors.Orange600
                        else AppColors.Gray400,
                        disabledContainerColor = Color.White,
                        disabledContentColor = AppColors.Gray400
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = if (isConnected && text.trim().isNotEmpty() && !isPrinting)
                            AppColors.PastelPeach
                        else AppColors.Gray200
                    )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isPrinting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = AppColors.Orange600,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_printer),
                                contentDescription = "Print",
                                modifier = Modifier.size(20.dp)
                            )
                        }
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
