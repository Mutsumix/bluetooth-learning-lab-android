package com.example.btlearninglab.ui.epaper

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.btlearninglab.R
import com.example.btlearninglab.ui.theme.AppColors
import com.example.btlearninglab.ui.components.BottomNavigationBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EPaperScreen(
    navController: NavController,
    viewModel: EPaperViewModel = viewModel(
        factory = EPaperViewModelFactory(
            LocalContext.current.applicationContext as Application
        )
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val logs by viewModel.logs.collectAsState()
    val apUrl by viewModel.apUrl.collectAsState()
    val macAddress by viewModel.macAddress.collectAsState()
    val ditherEnabled by viewModel.ditherEnabled.collectAsState()
    val savedTags by viewModel.savedTags.collectAsState()
    val currentWeight by viewModel.currentWeight.collectAsState()

    EPaperScreenContent(
        navController = navController,
        uiState = uiState,
        logs = logs,
        apUrl = apUrl,
        macAddress = macAddress,
        ditherEnabled = ditherEnabled,
        savedTags = savedTags,
        currentWeight = currentWeight,
        onApUrlChange = viewModel::updateApUrl,
        onMacAddressChange = viewModel::updateMacAddress,
        onDitherChange = viewModel::updateDitherEnabled,
        onTagSelect = viewModel::selectTag,
        onSend = viewModel::send,
        onRefreshWeight = viewModel::refreshWeight
    )
}

@Composable
private fun EPaperScreenContent(
    navController: NavController,
    uiState: EPaperUiState,
    logs: List<String>,
    apUrl: String,
    macAddress: String,
    ditherEnabled: Boolean,
    savedTags: List<com.example.btlearninglab.data.epaper.EPaperTag>,
    currentWeight: Double,
    onApUrlChange: (String) -> Unit,
    onMacAddressChange: (String) -> Unit,
    onDitherChange: (Boolean) -> Unit,
    onTagSelect: (com.example.btlearninglab.data.epaper.EPaperTag) -> Unit,
    onSend: () -> Unit,
    onRefreshWeight: () -> Unit
) {
    val isSending = uiState is EPaperUiState.Sending
    val httpRequest = if (uiState is EPaperUiState.Sent) uiState.httpRequest else null
    val errorMessage = if (uiState is EPaperUiState.Error) uiState.message else null
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Show snackbar on success/error
    LaunchedEffect(uiState) {
        when (uiState) {
            is EPaperUiState.Sent -> {
                snackbarHostState.showSnackbar(
                    message = "✓ 送信成功！E-Paperに書き込まれました",
                    duration = SnackbarDuration.Short
                )
            }
            is EPaperUiState.Error -> {
                snackbarHostState.showSnackbar(
                    message = "✗ エラー: ${uiState.message}",
                    duration = SnackbarDuration.Long
                )
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(AppColors.Primary50, Color.White)
                    )
                )
                .clickable(
                    onClick = { focusManager.clearFocus() },
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
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
                        text = "E-Paper HTTP→AP→BLE",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Gray800
                    )
                    Text(
                        text = "Gicisky 2.9\"",
                        fontSize = 12.sp,
                        color = AppColors.Gray500,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            // AP Settings
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // AP URL
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "AP URL",
                        fontSize = 14.sp,
                        color = AppColors.Gray500
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        shadowElevation = 2.dp
                    ) {
                        TextField(
                            value = apUrl,
                            onValueChange = onApUrlChange,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    text = "192.168.1.100",
                                    color = AppColors.Gray400
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                disabledContainerColor = Color.White,
                                focusedIndicatorColor = AppColors.Purple400,
                                unfocusedIndicatorColor = AppColors.PastelLavender,
                                focusedTextColor = AppColors.Gray800,
                                unfocusedTextColor = AppColors.Gray800
                            ),
                            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
                        )
                    }
                }

                // Saved Tags Dropdown
                if (savedTags.isNotEmpty()) {
                    var expanded by remember { mutableStateOf(false) }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "保存済みタグ",
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
                                            text = "タグを選択",
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
                                    savedTags.forEach { tag ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(
                                                        text = tag.name,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp
                                                    )
                                                    Text(
                                                        text = "${tag.ipAddress} / ${tag.macAddress.takeLast(8)}",
                                                        fontSize = 12.sp,
                                                        color = AppColors.Gray500
                                                    )
                                                }
                                            },
                                            onClick = {
                                                onTagSelect(tag)
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // MAC Address
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "電子タグのMACアドレス",
                        fontSize = 14.sp,
                        color = AppColors.Gray500
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        shadowElevation = 2.dp
                    ) {
                        TextField(
                            value = macAddress,
                            onValueChange = onMacAddressChange,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    text = "AA:BB:CC:DD:EE:FF",
                                    color = AppColors.Gray400
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                disabledContainerColor = Color.White,
                                focusedIndicatorColor = AppColors.Purple400,
                                unfocusedIndicatorColor = AppColors.PastelLavender,
                                focusedTextColor = AppColors.Gray800,
                                unfocusedTextColor = AppColors.Gray800
                            ),
                            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
                        )
                    }
                }

                // Dithering
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ディザリング",
                            fontSize = 14.sp,
                            color = AppColors.Gray700
                        )
                        Switch(
                            checked = ditherEnabled,
                            onCheckedChange = onDitherChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AppColors.Primary400,
                                checkedTrackColor = AppColors.Primary100
                            )
                        )
                    }
                }

                // Error message
                if (errorMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFFEBEE)
                    ) {
                        Text(
                            text = errorMessage,
                            fontSize = 12.sp,
                            color = Color(0xFFC62828),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            // Preview
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "プレビュー (296x128)",
                        fontSize = 14.sp,
                        color = AppColors.Gray500
                    )
                    TextButton(onClick = onRefreshWeight) {
                        Text(
                            text = "更新",
                            fontSize = 12.sp,
                            color = AppColors.Primary400
                        )
                    }
                }
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = AppColors.PastelLavender,
                            shape = RoundedCornerShape(16.dp)
                        ),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 1.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AppColors.Gray50
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(296f / 128f)
                                    .border(
                                        width = 1.dp,
                                        color = AppColors.Gray300,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(8.dp),
                                contentAlignment = Alignment.TopStart
                            ) {
                                val dateFormatter = remember { SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN) }
                                val currentDate = remember { dateFormatter.format(Date()) }

                                Column(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = currentDate,
                                        fontSize = 10.sp,
                                        color = AppColors.Gray700
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "現在の重さ:",
                                        fontSize = 12.sp,
                                        color = AppColors.Gray700
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = String.format(Locale.JAPAN, "%.1f g", currentWeight),
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AppColors.Gray800
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Send Button
            val canSend = apUrl.trim().isNotEmpty() && macAddress.trim().isNotEmpty() && !isSending
            Button(
                onClick = {
                    if (!isSending) {
                        onSend()
                    }
                },
                enabled = canSend,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canSend)
                        AppColors.Primary400
                    else AppColors.Gray100,
                    contentColor = if (canSend)
                        Color.White
                    else AppColors.Gray400,
                    disabledContainerColor = AppColors.Gray100,
                    disabledContentColor = AppColors.Gray400
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                if (isSending) {
                    Text(
                        text = "送信中...",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_send),
                            contentDescription = "Send",
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "送信",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            // HTTP Request
            if (httpRequest != null) {
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
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = "HTTP Request",
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
                                httpRequest.forEach { req ->
                                    Text(
                                        text = req,
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
                            .heightIn(min = 100.dp, max = 300.dp),
                        contentAlignment = if (logs.isEmpty()) Alignment.Center else Alignment.TopStart
                    ) {
                        if (logs.isEmpty()) {
                            Text(
                                text = "(送信してください)",
                                fontSize = 12.sp,
                                color = AppColors.Gray400,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState())
                                    .padding(20.dp),
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
}
