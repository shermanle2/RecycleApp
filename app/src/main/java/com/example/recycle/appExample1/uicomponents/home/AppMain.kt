package com.example.recycle.appExample1.uicomponents.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.recycle.appExample1.model.CommunityPostCategory
import com.example.recycle.appExample1.model.Routes
import com.example.recycle.appExample1.viewModel.CommunityViewModel
import com.example.recycle.communityExample.uicomponents.home.layout.CommonScaffold
import com.example.recycle.communityExample.uicomponents.home.layout.HomeTab

@Composable
fun AppMain(
    userId: String?,
    navController: NavHostController,
    viewModel: CommunityViewModel
) {
    val posts by viewModel.posts.collectAsState()
    val infoPosts = posts.filter { it.category == CommunityPostCategory.INFO }

    CommonScaffold(
        selectedTab = HomeTab.APPMAIN,

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

        onDeleteClick = { }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Text(
                text = "분리수거 정보",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, end = 12.dp)
            )

            LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
                items(infoPosts) { post ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable {
                                navController.navigate("${Routes.PostDetail.route}/${post.id}/${userId}?infoOnly=true")
                            }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = post.title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = post.content.take(50), fontSize = 14.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "작성자 ${post.author} | ${post.date}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}


@Preview
@Composable
private fun AppMainPreview() {
    AppMain(
        userId = "testUser",
        navController = NavHostController(context = LocalContext.current),
        viewModel = viewModel()
    )
}