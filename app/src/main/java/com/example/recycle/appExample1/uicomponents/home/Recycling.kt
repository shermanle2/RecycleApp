package com.example.recycle.appExample1.uicomponents.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.example.recycle.appExample1.model.Routes
import com.example.recycle.appExample1.uicomponents.home.layout.CommonScaffold
import com.example.recycle.appExample1.uicomponents.home.layout.HomeTab
import android.graphics.Bitmap
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import java.io.File

import android.graphics.Matrix
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import java.io.FileOutputStream
import androidx.camera.core.AspectRatio
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Alignment
import com.example.recycle.appExample1.model.RecycleItem
import com.example.recycle.appExample1.uicomponents.Detection.DetectionResult
import com.example.recycle.appExample1.uicomponents.Detection.drawBoundingBoxOnBitmap
import com.example.recycle.appExample1.uicomponents.Detection.postBitmapForDetection

@Composable
fun CameraCaptureScreen(
    modifier: Modifier = Modifier.fillMaxWidth(),
    items: MutableState<List<RecycleItem>>,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = ContextCompat.getMainExecutor(context)

    var capturedImage by remember { mutableStateOf<Bitmap?>(null) }
    val imageCapture = remember {
        ImageCapture.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .build()
    }
    var showResultDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var detectionResult by remember { mutableStateOf<DetectionResult?>(null) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Top
    ) {
        // 1. 프리뷰/캡처 이미지 영역 (4:3, overflow 방지)
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(3f / 4f)
                .background(Color.Blue)
        ) {
            if (capturedImage == null) {
                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            scaleType = PreviewView.ScaleType.FIT_CENTER // ★★ 변경
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    update = { previewView ->
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder()
                                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                                .build().also {
                                    it.setSurfaceProvider((previewView as PreviewView).surfaceProvider)
                                }
                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    imageCapture
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }, executor)
                    }
                )
            } else {
                Image(
                    bitmap = capturedImage!!.asImageBitmap(),
                    contentDescription = "Captured Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit // ★★ 변경
                )
            }
        }

        // 2. 버튼 Row (프리뷰 아래 고정)
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            if (capturedImage == null) {
                Button(onClick = {
                    val photoFile = File.createTempFile("IMG_", ".jpg", context.cacheDir)
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                    imageCapture.takePicture(
                        outputOptions,
                        executor,
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                val bmp = BitmapFactory.decodeFile(photoFile.absolutePath)
                                val matrix = Matrix()
                                matrix.postRotate(90f)
                                val rotatedBmp = Bitmap.createBitmap(
                                    bmp, 0, 0, bmp.width, bmp.height, matrix, true
                                )
                                FileOutputStream(photoFile).use { out ->
                                    rotatedBmp.compress(Bitmap.CompressFormat.JPEG, 100, out)
                                }
                                capturedImage = rotatedBmp
                                    // 이미지 캡처 후 바로 분류 요청
                                postBitmapForDetection(context, capturedImage!!) { result ->
                                    if (result != null) {
                                        // 결과 처리
                                        detectionResult = result
                                        showResultDialog = true

                                        // items 업데이트
                                        var tempItems = emptyList<RecycleItem>()
                                        result.detections.forEach { detection ->
                                            // 캡처된 이미지에 바운딩 박스 그리기
                                            if (detection.confidence > 0.5) { // 신뢰도 기준 설정
                                                // 아이템 목록에 추가
                                                tempItems = tempItems + RecycleItem(
                                                    name = detection.label,
                                                    description = detection.description
                                                )
                                                // 바운딩 박스 그리기
                                                capturedImage = drawBoundingBoxOnBitmap(
                                                    capturedImage!!,
                                                    detection.bbox,
                                                    detection.label
                                                )

                                            }
                                        }
                                        items.value = tempItems


                                    } else {
                                        // 에러 처리
                                        showErrorDialog = true
                                        Toast.makeText(context, "postBitmapForDetection error", Toast.LENGTH_LONG).show()
                                    }
                                }

                            }
                            override fun onError(exception: ImageCaptureException) {
                                exception.printStackTrace()
                                showErrorDialog = true
                                Toast.makeText(context, "capturedImage is null", Toast.LENGTH_LONG).show()
                            }

                        }
                    )



                }) {
                    Text("캡처")
                }
            } else {
                Button(onClick = {
                    capturedImage = null
                    items.value = emptyList() // 이미지 캡처 후 다시 촬영 시 아이템 목록 초기화
                }) {
                    Text("다시 촬영")
                }
            }
        }
    }
    if (showResultDialog && detectionResult != null) {
        AlertDialog(
            onDismissRequest = { showResultDialog = false },
            title = { Text("분류 결과") },
            text = {
                Column {
                    Text("처리 시간: ${detectionResult!!.inference_time_s}s")
                    detectionResult!!.detections.forEach { detection ->
                        Text("라벨: ${detection.label}, 신뢰도: ${"%.2f".format(detection.confidence * 100)}%, 위치: ${detection.bbox.joinToString(", ")}")
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showResultDialog = false }) { Text("확인") }
            }
        )
    }

// 에러 팝업
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("오류") },
            text = { Text("분석 중 오류가 발생했습니다. 다시 시도해 주세요.") },
            confirmButton = {
                Button(onClick = { showErrorDialog = false }) { Text("확인") }
            }
        )
    }
}



@Composable
fun MyHorizontalScrollRow(items: List<RecycleItem>) {
    // 팝업 상태
    var selectedItem by remember { mutableStateOf<RecycleItem?>(null) }

    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .fillMaxSize()
    ) {
        items.forEach { item ->
            Card(
                modifier = Modifier
                    .padding(5.dp)
                    .fillMaxHeight(),
                onClick = { selectedItem = item } // 카드 클릭 시 해당 아이템 저장
            ) {
                Box(
                    Modifier
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(item.name)
                }
            }
        }
    }
    // 팝업(다이얼로그)
    if (selectedItem != null) {
        AlertDialog(
            onDismissRequest = { selectedItem = null },
            title = { Text(selectedItem!!.name) },
            text = { Text(selectedItem!!.description) },
            confirmButton = {
                Button(onClick = { selectedItem = null }) {
                    Text("확인")
                }
            }
        )
    }
}

@Composable
fun Recycling(
    userId: String,
    navController: NavHostController
) {
    var items = remember { mutableStateOf<List<RecycleItem>>(emptyList()) }

    CommonScaffold(
        selectedTab = HomeTab.RECYCLING,

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
//            Text("Recycling")
//            CameraScreen(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .aspectRatio(16f / 9f)
//            )
            CameraCaptureScreen(items=items)
            MyHorizontalScrollRow(items.value)

        }
    }
}
