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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.recycle.appExample1.model.Routes
import com.example.recycle.appExample1.viewModel.UserViewModel
import com.example.recycle.communityExample.uicomponents.home.layout.CommonScaffold
import com.example.recycle.communityExample.uicomponents.home.layout.HomeTab
import com.github.tehras.charts.piechart.PieChart
import com.github.tehras.charts.piechart.PieChartData
import com.github.tehras.charts.piechart.PieChartData.Slice
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun User(
    userId: String,
    navController: NavHostController,
    viewModel: UserViewModel
) {
    val stats by viewModel.stats.collectAsState()

    LaunchedEffect(userId) {
        if (userId.isNotBlank()) {
            viewModel.loadStats(userId)
        }
    }

    val pieData = stats
        .filterKeys { it != "totalSuccess" && it != "totalFail" }
        .map { (label, value) ->
            Slice(
                value = value,
                color = generateColorForLabel(label)
            )
        }

    val resultPieData = listOf("성공" to "totalSuccess", "실패" to "totalFail").map { (label, key) ->
        Slice(
            value = stats[key] ?: 0f,
            color = if (label == "성공") Color(0xFF4CAF50) else Color.LightGray
        )
    }

    val success = stats["totalSuccess"] ?: 0f
    val fail = stats["totalFail"] ?: 0f
    val total = success + fail
    val successRate = if (total > 0) (success / total * 100).toInt() else 0

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
        if (userId.isBlank()) {
            // 비회원 전용 메시지 UI
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "비회원은 이 기능을 사용할 수 없습니다.",
                    color = Color.Red
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("나의 분리수거 통계", fontSize = 24.sp, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))

                PieChart(
                    pieChartData = PieChartData(resultPieData),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("성공률 : $successRate%  (${success.toInt()}/${total.toInt()})", fontSize = 16.sp)
                Text("*성공률은 인증글 작성 후 다른 회원의 댓글에 의해 결정됩니다.", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(32.dp))
                Text("분리수거 된 재활용품 비율", fontSize = 24.sp, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))

                PieChart(
                    pieChartData = PieChartData(pieData),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text("분리수거 종류별 배출 비율", fontSize = 20.sp)

                stats
                    .filterKeys { it != "totalSuccess" && it != "totalFail" }
                    .forEach { (label, _) ->
                        val color = generateColorForLabel(label)
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
                            Text("$label: ${stats[label]?.toInt() ?: 0}개")
                        }
                    }
                Text("*재활용품 분류탭에 이미지 인식을 통해 분리수거 된 재활용품의 종류가 기록됩니다.", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

// 최초 생성 시 초기화
fun createEmptyStats(userId: String) {
    val empty = mapOf(
        "totalSuccess" to 0,
        "totalFail" to 0,
        "recycledItemCount" to mapOf<String, Int>()
    )
    FirebaseFirestore.getInstance()
        .collection("users")
        .document(userId)
        .set(empty)
}

fun generateColorForLabel(label: String): Color {
    val colors = listOf(
        Color(0xFF4CAF50),
        Color(0xFFFF9800),
        Color(0xFF2196F3),
        Color(0xFF9C27B0),
        Color(0xFFE91E63),
        Color(0xFF00BCD4),
        Color(0xFF795548),
        Color(0xFF8BC34A),
        Color(0xFF673AB7),
        Color(0xFF3F51B5)
    )
    val index = (label.hashCode() and 0x7FFFFFFF) % colors.size
    return colors[index]
}
