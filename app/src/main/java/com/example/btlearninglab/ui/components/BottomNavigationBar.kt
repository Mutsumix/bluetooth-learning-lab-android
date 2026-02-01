package com.example.btlearninglab.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.btlearninglab.R
import com.example.btlearninglab.navigation.Screen
import com.example.btlearninglab.ui.theme.AppColors

data class TabItem(
    val route: String,
    val iconRes: Int,
    val label: String
)

@Composable
fun BottomNavigationBar(navController: NavController) {
    val tabs = listOf(
        TabItem(Screen.Home.route, R.drawable.ic_home, "ホーム"),
        TabItem(Screen.Scale.route, R.drawable.ic_scale, "Scale"),
        TabItem(Screen.Printer.route, R.drawable.ic_printer, "Printer"),
        TabItem(Screen.EPaper.route, R.drawable.ic_epaper, "E-Paper"),
        TabItem(Screen.Log.route, R.drawable.ic_log, "ログ")
    )

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = Color.White
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(AppColors.Primary100)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                tabs.forEach { tab ->
                    val isActive = currentRoute == tab.route
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable {
                                if (currentRoute != tab.route) {
                                    navController.navigate(tab.route) {
                                        popUpTo(Screen.Home.route) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = tab.iconRes),
                                contentDescription = tab.label,
                                modifier = Modifier.size(24.dp),
                                tint = if (isActive) AppColors.Primary500 else AppColors.Gray400
                            )
                            Text(
                                text = tab.label,
                                fontSize = 10.sp,
                                fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal,
                                color = if (isActive) AppColors.Primary500 else AppColors.Gray400
                            )
                        }
                    }
                }
            }
        }
    }
}
