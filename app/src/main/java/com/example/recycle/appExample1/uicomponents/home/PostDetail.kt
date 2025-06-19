package com.example.recycle.appExample1.uicomponents.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.recycle.appExample1.model.Comment
import com.example.recycle.appExample1.model.CommunityPost
import com.example.recycle.appExample1.viewModel.CommunityViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postId: String,
    posts: List<CommunityPost>,
    currentUserId: String,
    onBackClick: () -> Unit,
    viewModel: CommunityViewModel,
    infoOnly: Boolean = false

) {
    val post = remember(posts, postId) {
        posts.find { it.id == postId }
    }
    val comments = remember { mutableStateListOf<Comment>() }
    var commentText by remember { mutableStateOf("") }

    LaunchedEffect(postId) {
        viewModel.loadComments(postId) { loaded ->
            comments.clear()
            comments.addAll(loaded)
        }
    }

    if (post == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(
                    text = post.title, maxLines = 1, style = MaterialTheme.typography.titleLarge
                )
            }, navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                }
            }, actions = {
                if (!infoOnly && post.author == currentUserId) {
                    TextButton(
                        onClick = {
                            viewModel.deletePost(post.id) {
                                onBackClick()
                            }
                        }
                    ) {
                        Text("삭제", color = MaterialTheme.colorScheme.error)
                    }
                }
            })
        }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Text(post.title, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "작성자 ${post.author} · ${post.date}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (post.imageUrls.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                ) {
                    items(post.imageUrls) { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = "게시글 이미지",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxHeight()
                                .aspectRatio(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text(post.content, fontSize = 16.sp)

            HorizontalDivider(Modifier.padding(vertical = 12.dp))

            Text("댓글", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))

            comments.filter { !it.isPrivate || post.author == currentUserId }.forEach { comment ->
                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                        Text(text = comment.content)
                        Text(
                            text = "작성자 ${comment.author} · ${comment.timestamp}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = commentText,
                onValueChange = { commentText = it },
                label = { Text("댓글을 입력하세요") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onClick@{
                    if (commentText.isBlank()) return@onClick

                    val sdf = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.KOREA)
                    val newComment = Comment(
                        content = commentText,
                        author = currentUserId,
                        timestamp = sdf.format(Date()),
                        isPrivate = false // 앱에서는 무조건 공개
                    )

                    viewModel.addComment(post.id, newComment) {
                        // 저장 후 댓글 목록 다시 불러오기
                        viewModel.loadComments(post.id) { loaded ->
                            comments.clear()
                            comments.addAll(loaded)
                        }
                        commentText = ""
                    }
                }, modifier = Modifier.align(Alignment.End)
            ) {
                Text("등록")
            }

        }
    }
}
