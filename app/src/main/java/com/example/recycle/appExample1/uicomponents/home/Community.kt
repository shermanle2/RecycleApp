package com.example.recycle.appExample1.uicomponents.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.recycle.appExample1.model.Routes
import com.example.recycle.appExample1.model.CommunityPostCategory
import com.example.recycle.appExample1.model.CommunityPostItem
import com.example.recycle.appExample1.viewModel.CommunityViewModel
import com.example.recycle.communityExample.uicomponents.home.layout.CommonScaffold
import com.example.recycle.communityExample.uicomponents.home.layout.HomeTab

@Composable
fun Community(
    userId: String,
    navController: NavHostController,
    viewModel: CommunityViewModel
) {
    val posts by viewModel.posts.collectAsState()
    var selectedCategory by remember { mutableStateOf(CommunityPostCategory.ALL) }

    val filteredPosts = if (selectedCategory == CommunityPostCategory.ALL) {
        posts
    } else {
        posts.filter { it.category == selectedCategory }
    }

    CommonScaffold(
        selectedTab = HomeTab.COMMUNITY,
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
        onDeleteClick = { /* */ }
    ) { innerPadding ->

        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) {

            Column(modifier = Modifier.fillMaxSize()) {

                // 카테고리 탭
                TabRow(
                    selectedTabIndex = selectedCategory.ordinal,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CommunityPostCategory.entries.forEach { category ->
                        Tab(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            text = { Text(category.label) }
                        )
                    }
                }

                // 게시글 목록
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 80.dp) // prevent FAB overlap
                ) {
                    items(filteredPosts) { post ->
                        CommunityPostItem(post = post) {
                            navController.navigate("${Routes.PostDetail.route}/${post.id}/$userId")
                        }
                    }
                }
            }

            // 새 글 작성 버튼
            FloatingActionButton(
                onClick = {
                    navController.navigate("${Routes.CreatePost.route}/$userId")
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "새 글 작성")
            }
        }
    }
}
