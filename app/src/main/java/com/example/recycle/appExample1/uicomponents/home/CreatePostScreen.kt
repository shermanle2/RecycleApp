package com.example.recycle.appExample1.uicomponents.home

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.recycle.appExample1.model.CommunityPost
import com.example.recycle.appExample1.model.CommunityPostCategory
import com.example.recycle.appExample1.viewModel.CommunityViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    userId: String,
    navController: NavHostController,
    viewModel: CommunityViewModel
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(CommunityPostCategory.QUESTION) }
    val imageUris = remember { mutableStateListOf<Uri>() }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(), // Android 13+
        onResult = { uri ->
            if (uri != null && imageUris.size < 5) imageUris.add(uri)
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("새 게시글 작성") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("제목") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("내용") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 카테고리 선택 드롭다운
            var expanded by remember { mutableStateOf(false) }
            Box {
                OutlinedTextField(
                    value = selectedCategory.label,
                    onValueChange = {},
                    label = { Text("카테고리") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "카테고리 선택")
                        }
                    }
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    CommunityPostCategory.entries
                        .filter { it != CommunityPostCategory.ALL }
                        .forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.label) },
                                onClick = {
                                    selectedCategory = category
                                    expanded = false
                                }
                            )
                        }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 썸네일 미리보기
            LazyRow {
                items(imageUris) { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        modifier = Modifier
                            .size(64.dp)
                            .padding(end = 8.dp)
                    )
                }
            }

// 사진 선택 버튼
            OutlinedButton(onClick = {
                if (imageUris.size < 5) {
                    launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            }) {
                Text("사진 추가 (${imageUris.size}/5)")
            }


            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onClick@{
                    if (title.isBlank() || content.isBlank()) {
                        errorMessage = "제목과 내용을 입력해주세요."
                        return@onClick
                    }

                    if (selectedCategory == CommunityPostCategory.PROOF && imageUris.isEmpty()) {
                        errorMessage = "인증 글은 최소 한 장 이상의 사진이 필요합니다."
                        return@onClick
                    }

                    // 문제 없으면 에러 메시지 초기화
                    errorMessage = null

                    val sdf = SimpleDateFormat("yyyy.MM.dd", Locale.KOREA)
                    val post = CommunityPost(
                        id = "",
                        title = title,
                        content = content,
                        author = userId,
                        authorEmail = Firebase.auth.currentUser?.email ?: "익명",
                        date = sdf.format(Date()),
                        category = selectedCategory,
                        imageUrls = emptyList()
                    )

                    viewModel.uploadImagesAndAddPost(
                        post = post,
                        imageUris = imageUris,
                        context = context,
                        onComplete = { navController.popBackStack() }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("등록하기")
            }
            // 인증글에 이미지 없을 때 에러 표시
            errorMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = it, color = Color.Red)
            }
        }
    }
}