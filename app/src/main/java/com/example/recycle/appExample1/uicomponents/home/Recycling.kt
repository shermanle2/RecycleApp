package com.example.recycle.appExample1.uicomponents.home

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.provider.Settings
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.example.recycle.BuildConfig
import com.example.recycle.appExample1.feature.Detection.DetectionResult
import com.example.recycle.appExample1.feature.Detection.drawBoundingBoxOnBitmap
import com.example.recycle.appExample1.feature.Detection.postBitmapForDetection
import com.example.recycle.appExample1.model.RecycleItem
import com.example.recycle.appExample1.model.Routes
import com.example.recycle.appExample1.viewModel.UserViewModel
import com.example.recycle.communityExample.uicomponents.home.layout.CommonScaffold
import com.example.recycle.communityExample.uicomponents.home.layout.HomeTab
import java.io.File

private val DEBUG = BuildConfig.DEBUG

@Composable
fun CameraCaptureScreen(
    modifier: Modifier = Modifier.fillMaxWidth(),
    items: MutableState<List<RecycleItem>>,
    userId: String,
    viewModel: UserViewModel
) {
    val context = LocalContext.current
    val activity = context as? Activity

    // 권한 상태
    var cameraPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    // 거부 횟수 추적
    var deniedCount by remember { mutableStateOf(0) }
    // 설정 버튼 표시 여부: 거부된 횟수와 시스템의 shouldShowRequestPermissionRationale 리턴값을 조합
    val showSettingsButton: Boolean = deniedCount >= 1 &&
            !(activity?.let { ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.CAMERA) } ?: false)
// 권한 요청 런처
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) deniedCount += 1
        cameraPermissionGranted = granted
    }
    activity?.let { !ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.CAMERA) }
        ?: false

    // 권한 요청/설정 전환 UI
    if (!cameraPermissionGranted) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("카메라 권한이 필요합니다.")
            Spacer(Modifier.height(8.dp))
            if (showSettingsButton) {
                Button(onClick = {
                    activity?.let {
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", it.packageName, null)
                        )
                        it.startActivity(intent)
                    }
                }) {
                    Text("설정으로 이동")
                }
            } else {
                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("권한 요청")
                }
            }
        }
        return
    }

    // 이미지 캡처 및 분석 상태
    var isLoading by remember { mutableStateOf(false) }
    var capturedImage by remember { mutableStateOf<Bitmap?>(null) }
    var detectionResult by remember { mutableStateOf<DetectionResult?>(null) }
    var showResultDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = ContextCompat.getMainExecutor(context)
    val imageCapture = remember {
        ImageCapture.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .build()
    }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(3f / 4f)
                .background(Color.Black)
        ) {
            // 프리뷰 또는 결과 이미지
            if (capturedImage == null) {
                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            scaleType = PreviewView.ScaleType.FIT_CENTER
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    update = { previewView ->
                        val cameraProvider = ProcessCameraProvider.getInstance(context).get()
                        val preview = Preview.Builder()
                            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                            .build().also { it.setSurfaceProvider((previewView as PreviewView).surfaceProvider) }
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                imageCapture
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                )
            } else {
                Image(
                    bitmap = capturedImage!!.asImageBitmap(),
                    contentDescription = "Captured",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            // 촬영/재촬영 버튼
            FloatingActionButton(
                onClick = {
                    if (capturedImage == null) {
                        isLoading = true
                        val file = File.createTempFile("IMG_", ".jpg", context.cacheDir)
                        val options = ImageCapture.OutputFileOptions.Builder(file).build()
                        imageCapture.takePicture(
                            options, executor,
                            object : ImageCapture.OnImageSavedCallback {
                                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                    val bmp = BitmapFactory.decodeFile(file.absolutePath)
                                    val rotated = Bitmap.createBitmap(
                                        bmp, 0, 0, bmp.width, bmp.height,
                                        Matrix().apply { postRotate(90f) }, true
                                    )
                                    capturedImage = rotated
                                    postBitmapForDetection(context, rotated) { result ->
                                        isLoading = false
                                        if (result != null) {
                                            detectionResult = result
                                            showResultDialog = true
                                            var list = emptyList<RecycleItem>()
                                            result.detections.forEach { det ->
                                                if (det.confidence > 0.5) {
                                                    list += RecycleItem(det.label, det.description)
                                                    capturedImage = drawBoundingBoxOnBitmap(
                                                        capturedImage!!, det.bbox, det.label
                                                    )
                                                }
                                            }
                                            items.value = list
                                        } else {
                                            showErrorDialog = true
                                        }
                                    }
                                }
                                override fun onError(exception: ImageCaptureException) {
                                    isLoading = false
                                    showErrorDialog = true
                                }
                            }
                        )
                    } else {
                        capturedImage = null
                        items.value = emptyList()
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp),
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
                    .size(64.dp)
            ) {
                Icon(
                    imageVector = if (capturedImage == null) Icons.Filled.Camera else Icons.Filled.Refresh,
                    contentDescription = if (capturedImage == null) "Capture" else "Retake",
                    modifier = Modifier.size(32.dp)
                )
            }

            // 로딩 오버레이
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("분석 중...", color = Color.White)
                    }
                }
            }
        }
        // 감지된 재활용품 표시
        if (!isLoading && items.value.isNotEmpty()) {
            Text(
                text = "감지된 재활용품!\n클릭해서 분리수거 방법을 알아 보세요!",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp, top = 12.dp)
            )
            MyHorizontalScrollRow(userId = userId, viewModel=viewModel, items = items.value)
        }
        else if (!isLoading && items.value.isEmpty() && capturedImage != null) {
            var showNoDetectionDialog by remember { mutableStateOf(true) }
            if (showNoDetectionDialog) {
                AlertDialog(
                    onDismissRequest = { showNoDetectionDialog = false },
                    title = { Text("알림") },
                    text = { Text("감지된 재활용품이 없습니다!\n다시 촬영해 보세요!") },
                    confirmButton = {
                        TextButton(onClick = { showNoDetectionDialog = false }) {
                            Text("확인")
                        }
                    }
                )
            }
        }
    }

    // 디버그용 다이얼로그
    if (DEBUG && showResultDialog && detectionResult != null) {
        AlertDialog(
            onDismissRequest = { showResultDialog = false },
            title = { Text("분류 결과") },
            text = {
                Column {
                    Text("처리 시간: ${detectionResult!!.inference_time_s}s")
                    detectionResult!!.detections.forEach { det ->
                        Text("${det.label} (${"%.2f".format(det.confidence*100)}%)")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showResultDialog = false }) { Text("확인") }
            }
        )
    }
    if (DEBUG && showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("오류") },
            text = { Text("캡처 또는 분석 중 오류가 발생했습니다.") },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) { Text("확인") }
            }
        )
    }
}

@Composable
fun MyHorizontalScrollRow(items: List<RecycleItem>, userId: String, viewModel: UserViewModel) {
    var selectedItem by remember { mutableStateOf<RecycleItem?>(null) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 8.dp)
    ) {
        items.forEach { item ->
            Card(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .wrapContentSize()
                    .clickable { selectedItem = item },
                colors = CardDefaults.cardColors(containerColor = Color.LightGray),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Black,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
    selectedItem?.let {
        AlertDialog(
            onDismissRequest = { selectedItem = null },
            title = { Text(it.name) },
            text = { Text(it.description) },
            confirmButton = {
                Button(
                    onClick = { selectedItem = null
                        viewModel.updateStats(userId=userId, material= it.name, success= true) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("분리수거 완료", color = MaterialTheme.colorScheme.onPrimary)
                }

            },
            dismissButton = {
                TextButton(onClick = { selectedItem = null }) { Text("확인") }
            }
        )
    }
}

@Composable
fun Recycling(
    userId: String,
    navController: NavHostController,
    viewModel: UserViewModel
) {
    var items by remember { mutableStateOf(emptyList<RecycleItem>()) }
    CommonScaffold(
        selectedTab = HomeTab.RECYCLING,
        onTabSelected = { tab ->
            val route = when (tab) {
                HomeTab.APPMAIN   -> "${Routes.Main.route}/$userId"
                HomeTab.RECYCLING -> "${Routes.Recycling.route}/$userId"
                HomeTab.WASTEMAP  -> "${Routes.WasteMap.route}/$userId"
                HomeTab.COMMUNITY -> "${Routes.Community.route}/$userId"
                HomeTab.USER      -> "${Routes.User.route}/$userId"
            }
            navController.navigate(route) {
                launchSingleTop = true
                popUpTo(Routes.Main.route) { inclusive = false }
            }
        },
        onUserClick = {
            navController.navigate("${Routes.User.route}/$userId") { launchSingleTop = true }
        },
        onDeleteClick = { }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            CameraCaptureScreen(
                items = remember { mutableStateOf(items) },
                userId = userId,
                viewModel = viewModel
            )
        }
    }
}