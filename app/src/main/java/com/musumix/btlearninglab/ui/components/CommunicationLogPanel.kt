package com.musumix.btlearninglab.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musumix.btlearninglab.R
import com.musumix.btlearninglab.ui.theme.AppColors

/**
 * 通信ログを表示し、テキスト選択・全選択・クリップボードへのコピーができるパネル。
 */
@Composable
fun CommunicationLogPanel(
    logText: String,
    emptyPlaceholder: String,
    modifier: Modifier = Modifier,
    title: String = "通信ログ",
    showLogIcon: Boolean = true,
    minHeight: Dp = 100.dp,
    maxHeight: Dp = 200.dp,
) {
    val scrollState = rememberScrollState()
    var textFieldValue by remember { mutableStateOf(TextFieldValue("")) }
    LaunchedEffect(logText) {
        textFieldValue = TextFieldValue(logText)
    }

    val clipboard = LocalClipboardManager.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
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
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (showLogIcon) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_log),
                            contentDescription = "Log",
                            modifier = Modifier.size(18.dp),
                            tint = AppColors.Primary500
                        )
                    }
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.Gray600
                    )
                }
                if (logText.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                        TextButton(
                            onClick = {
                                val t = textFieldValue.text
                                textFieldValue = textFieldValue.copy(selection = TextRange(0, t.length))
                            }
                        ) {
                            Text(
                                text = "全選択",
                                fontSize = 12.sp,
                                color = AppColors.Primary500
                            )
                        }
                        TextButton(
                            onClick = {
                                val t = textFieldValue.text
                                val sel = textFieldValue.selection
                                val slice = if (sel.collapsed) {
                                    t
                                } else {
                                    val a = minOf(sel.start, sel.end)
                                    val b = maxOf(sel.start, sel.end)
                                    t.substring(a, b)
                                }
                                clipboard.setText(AnnotatedString(slice))
                            }
                        ) {
                            Text(
                                text = "コピー",
                                fontSize = 12.sp,
                                color = AppColors.Primary500
                            )
                        }
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = minHeight, max = maxHeight)
                    .background(AppColors.Gray50.copy(alpha = 0.5f)),
                contentAlignment = if (logText.isEmpty()) Alignment.Center else Alignment.TopStart
            ) {
                if (logText.isEmpty()) {
                    Text(
                        text = emptyPlaceholder,
                        fontSize = 12.sp,
                        color = AppColors.Gray400,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(20.dp)
                    )
                } else {
                    BasicTextField(
                        value = textFieldValue,
                        onValueChange = { textFieldValue = it },
                        readOnly = true,
                        textStyle = TextStyle(
                            fontSize = 12.sp,
                            color = AppColors.Primary600,
                            fontFamily = FontFamily.Monospace
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(scrollState)
                            .padding(20.dp)
                    )
                }
            }
        }
    }
}
