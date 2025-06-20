package com.example.recycle.communityExample.uicomponents.home.layout

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.recycle.R

@Composable
fun CommonScaffold(
    selectedTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit,
    onUserClick: () -> Unit,
    onDeleteClick: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val iconSize = 40.dp

    Scaffold(
        topBar = {
            Surface {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 32.dp,
                            bottom = 8.dp,
                            start = 8.dp,
                            end = 8.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Image(
                            painter = painterResource(id = R.drawable.app_icon),
                            contentDescription = "아이콘",
                            modifier = Modifier.size(iconSize)
                    )

                    IconButton(
                        onClick = onUserClick,
                        modifier = Modifier.size(iconSize)
                    ) {
                        Icon(
                            painter = painterResource(
                                id = if (selectedTab == HomeTab.USER)
                                    R.drawable.baseline_account_box_24
                                else
                                    R.drawable.baseline_account_circle_24
                            ),
                            contentDescription = "프로필",
                            modifier = Modifier.size(iconSize)
                        )
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar {
                HomeTab.entries
                    .filter { it != HomeTab.USER }
                    .forEach { tab ->
                        NavigationBarItem(
                            selected = tab == selectedTab,
                            onClick = { onTabSelected(tab) },
                            icon = {
                                Icon(
                                    painter = painterResource(
                                        id = if (tab == selectedTab)
                                            R.drawable.baseline_stars_24
                                        else
                                            R.drawable.baseline_star_24
                                    ),
                                    contentDescription = tab.name
                                )
                            },
                            label = {
                                Text(
                                    text = when (tab) {
                                        HomeTab.APPMAIN -> "메인"
                                        HomeTab.RECYCLING -> "재활용품 분류"
                                        HomeTab.WASTEMAP -> "쓰레기 배출위치"
                                        HomeTab.COMMUNITY -> "커뮤니티"
                                        else -> ""
                                    },
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Clip
                                )
                            }
                        )
                    }
            }
        },
        content = content
    )
}

@Preview
@Composable
private fun CommonScaffoldPreview() {
    CommonScaffold(
        selectedTab = HomeTab.APPMAIN,
        onTabSelected = {},
        onUserClick = {},
        onDeleteClick = {},
    ) { }
}