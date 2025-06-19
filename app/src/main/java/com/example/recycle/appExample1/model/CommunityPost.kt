package com.example.recycle.appExample1.model

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

enum class CommunityPostCategory(val label: String) {
    ALL("전체 글"), QUESTION("질문"), INFO("정보"), PROOF("인증")
}

data class CommunityPost(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val author: String = "",
    val date: String = "",
    val category: CommunityPostCategory = CommunityPostCategory.QUESTION,
    val imageUrls: List<String> = emptyList()
)


@Composable
fun CommunityPostItem(post: CommunityPost, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() }) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (post.imageUrls.isNotEmpty()) {
                AsyncImage(
                    model = post.imageUrls.first(),
                    contentDescription = "Post Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color.LightGray)
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            // Text content
            Column(modifier = Modifier.weight(1f)) {
                Text(text = post.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = post.content, maxLines = 2, fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "작성자 ${post.author} · ${post.date}", fontSize = 12.sp, color = Color.Gray
                )
            }
        }
    }
}
