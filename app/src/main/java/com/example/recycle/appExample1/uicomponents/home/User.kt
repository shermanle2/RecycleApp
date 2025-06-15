package com.example.recycle.appExample1.uicomponents.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.recycle.appExample1.model.Routes
import com.example.recycle.communityExample.uicomponents.home.layout.CommonScaffold
import com.example.recycle.communityExample.uicomponents.home.layout.HomeTab
import com.github.tehras.charts.piechart.PieChart
import com.github.tehras.charts.piechart.PieChartData
import com.github.tehras.charts.piechart.PieChartData.Slice

@Composable
fun User(
    userId: String, navController: NavHostController
) {
    val dummyStats = mapOf(
        "플라스틱" to 12f, "캔" to 5f, "종이" to 8f, "유리병" to 3f
    )

    val pieData = dummyStats.map { (label, value) ->
        Slice(
            value = value, color = when (label) {
                "플라스틱" -> Color(0xFF4CAF50)
                "캔" -> Color(0xFFFF9800)
                "종이" -> Color(0xFF2196F3)
                "유리병" -> Color(0xFF9C27B0)
                else -> Color.Gray
            }
        )
    }

    val resultStats = mapOf(
        "성공" to 24f, "실패" to 6f
    )

    val resultPieData = resultStats.map { (label, value) ->
        Slice(
            value = value, color = when (label) {
                "성공" -> Color(0xFF4CAF50)
                "실패" -> Color.LightGray
                else -> Color.Gray
            }
        )
    }

    CommonScaffold(selectedTab = HomeTab.USER, onTabSelected = { tab ->
        val route = when (tab) {
            HomeTab.APPMAIN -> "${Routes.Main.route}/$userId"
            HomeTab.RECYCLING -> "${Routes.Recycling.route}/$userId"
            HomeTab.WASTEMAP -> "${Routes.WasteMap.route}/$userId"
            HomeTab.COMMUNITY -> "${Routes.Community.route}/$userId"
            HomeTab.USER -> "${Routes.User.route}/$userId"
        }
        navController.navigate(route) {
            launchSingleTop = true
            popUpTo(Routes.Main.route) { inclusive = false }
        }
    }, onUserClick = {
        navController.navigate("${Routes.User.route}/$userId") {
            launchSingleTop = true
        }
    }, onDeleteClick = { }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("최근 7일 분리수거 통계", fontSize = 24.sp, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            PieChart(
                pieChartData = PieChartData(resultPieData),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("성공률 : 80%", fontSize = 16.sp)
            Spacer(modifier = Modifier.height(32.dp))
            Text("최근 7일 분리수거 통계", fontSize = 24.sp, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            PieChart(
                pieChartData = PieChartData(pieData),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text("분리수거 종류별 배출 비율", fontSize = 20.sp)

            dummyStats.keys.forEach { label ->
                val color = when (label) {
                    "플라스틱" -> Color(0xFF4CAF50)
                    "캔" -> Color(0xFFFF9800)
                    "종이" -> Color(0xFF2196F3)
                    "유리병" -> Color(0xFF9C27B0)
                    else -> Color.Gray
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(color)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(label)
                }
            }
        }
    }
}
