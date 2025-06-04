package com.example.recycle.appExample1.uicomponents.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.example.recycle.appExample1.model.Routes
import com.example.recycle.appExample1.uicomponents.home.layout.CommonScaffold
import com.example.recycle.appExample1.uicomponents.home.layout.HomeTab

@Composable
fun WasteMap(
    userId: String,
    navController: NavHostController
) {
    CommonScaffold(
        selectedTab = HomeTab.WASTEMAP,

        onTabSelected = { tab ->
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
        },

        onUserClick = {
            navController.navigate("${Routes.User.route}/$userId") {
                launchSingleTop = true
            }
        },

        onDeleteClick = { /* TODO */ }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Text("WasteMap")
        }
    }
}

// preview
@Preview(showBackground = true)
@Composable
fun WasteMapPreview() {
    WasteMap(
        userId = "testUser",
        navController = NavHostController(LocalContext.current)
    )
}