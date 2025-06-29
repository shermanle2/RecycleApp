package com.example.recycle.appExample1.uicomponents.home

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.recycle.appExample1.feature.Detection.ApiRepository
import com.example.recycle.appExample1.model.RecycleLocation
import com.example.recycle.appExample1.model.Routes
import com.example.recycle.communityExample.uicomponents.home.layout.CommonScaffold
import com.example.recycle.communityExample.uicomponents.home.layout.HomeTab
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import com.naver.maps.map.compose.LocationTrackingMode
import com.naver.maps.map.compose.MapProperties
import com.naver.maps.map.compose.MapUiSettings
import com.naver.maps.map.compose.Marker
import com.naver.maps.map.compose.NaverMap
import com.naver.maps.map.compose.rememberCameraPositionState
import com.naver.maps.map.compose.rememberFusedLocationSource
import com.naver.maps.map.compose.rememberMarkerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLEncoder

@OptIn(ExperimentalNaverMapApi::class, ExperimentalPermissionsApi::class)
@Composable
fun WasteMap(
    userId: String,
    navController: NavHostController
) {
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    LaunchedEffect(Unit) {
        permissionsState.launchMultiplePermissionRequest()
    }

    val granted = permissionsState.permissions.any { it.status.isGranted }


    val context = LocalContext.current
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }


    var currentPosition by remember { mutableStateOf<LatLng?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var searchTarget by remember { mutableStateOf<LatLng?>(null) }
    var isSearchPerformed by remember { mutableStateOf(false) }
    var selectedAddress by remember { mutableStateOf<String?>(null) }
    var selectedLatLng by remember { mutableStateOf<LatLng?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var addressText by remember { mutableStateOf<String?>(null) }
    var recycleLocationList by remember { mutableStateOf<List<RecycleLocation>?>(null) }
    var isDataLoading by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState()
    val locationSource = rememberFusedLocationSource()
    val searchMarkerState = rememberMarkerState()

    LaunchedEffect(granted) {
        if (granted) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        val latLng = LatLng(it.latitude, it.longitude)
                        currentPosition = latLng
                        searchTarget = latLng
                    }
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(searchTarget) {
        searchTarget?.let {
            cameraPositionState.move(
                CameraUpdate.toCameraPosition(CameraPosition(it, 16.0))
            )
            searchMarkerState.position = it
        }
    }

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
        Column(modifier = Modifier
            .padding(innerPadding)
            .padding(16.dp)
            .fillMaxWidth()
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text("검색") }
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (searchQuery.isNotBlank()) {
                            selectedAddress = null
                            CoroutineScope(Dispatchers.IO).launch {
                                var latLng = searchLocation(searchQuery)
                                if (latLng == null) {
                                    latLng = searchKeywordLocation(searchQuery)
                                }
                                withContext(Dispatchers.Main) {
                                    if (latLng != null) {
                                        searchTarget = latLng
                                        isSearchPerformed = true
                                    } else {
                                        Toast.makeText(context, "검색 결과 없음", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6A4FB6),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("검색", color = Color.White)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                if(granted) {
                    NaverMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        locationSource = locationSource,
                        properties = MapProperties(
                            locationTrackingMode = LocationTrackingMode.None
                        ),
                        uiSettings = MapUiSettings(
                            isLocationButtonEnabled = true
                        )
                    ) {
                        currentPosition?.let {
                            Marker(
                                state = rememberMarkerState(position = it),
                                captionText = "현 위치",
                                onClick = {
                                    selectedLatLng = currentPosition
                                    selectedAddress = null      // 초기화
                                    addressText = null          // 지오코딩 결과도 초기화
                                    true
                                }
                            )
                        }
                        if (isSearchPerformed && searchTarget != null) {
                            Marker(
                                state = searchMarkerState,
                                captionText = "검색 위치",
                                onClick = {
                                    selectedLatLng = searchTarget
                                    selectedAddress = searchQuery
                                    true
                                }
                            )
                        }
                    }

                    if (selectedLatLng != null) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 16.dp)
                                .background(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp))
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(16.dp),
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "분리수거 정보",
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "✕",
                                        modifier = Modifier
                                            .clickable {
                                                selectedLatLng = null
                                                selectedAddress = null
                                            }
                                            .padding(4.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                LaunchedEffect(selectedLatLng) {
                                    selectedLatLng?.let { latLng ->
                                        isLoading = true
                                        try {
                                            withContext(Dispatchers.IO) {
                                                val addr = getAddressFromLatLng(latLng.latitude, latLng.longitude)
                                                withContext(Dispatchers.Main) {
                                                    addressText = addr
                                                    selectedAddress = addr
                                                }
                                            }
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                }

                                if (isLoading) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("주소 정보를 가져오는 중...")
                                    Spacer(modifier = Modifier.height(8.dp))
                                } else if(addressText == null){
                                    Text(
                                        text = "주소 정보를 가져올 수 없습니다.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Red
                                    )

                                }
                                else {
                                    val apiFormAddr = addressText.orEmpty()

                                    LaunchedEffect(apiFormAddr) {
                                        if (apiFormAddr.isNotBlank()) {
                                            isDataLoading = true
                                            try {
                                                withContext(Dispatchers.IO) {
                                                    recycleLocationList = ApiRepository.fetchLocationsSync(apiFormAddr)
                                                }
                                            } finally {
                                                isDataLoading = false
                                            }
                                        }
                                    }
                                    Text(
                                        text = "주소: $apiFormAddr",
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    if (isDataLoading) {
                                        Text("분리수거 정보를 가져오는 중...")
                                    } else if (!recycleLocationList.isNullOrEmpty()) {
                                        val location = recycleLocationList!![0]
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(8.dp)
                                        ) {
                                            Text(
                                                text = "배출장소: ${location.location}",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))

                                            WasteInfoRow(
                                                title = "일반쓰레기",
                                                method = location.garbageMethod,
                                                days = location.garbageDays,
                                                start = location.garbageStartTime,
                                                end = location.garbageEndTime
                                            )

                                            WasteInfoRow(
                                                title = "재활용품",
                                                method = location.recycleMethod,
                                                days = location.recycleDays,
                                                start = location.recycleStartTime,
                                                end = location.recycleEndTime
                                            )

                                            WasteInfoRow(
                                                title = "음식물쓰레기",
                                                method = location.foodMethod,
                                                days = location.foodDays,
                                                start = location.foodStartTime,
                                                end = location.foodEndTime
                                            )
                                        }
                                    } else {
                                        Text("해당 위치에 대한 분리수거 정보가 없습니다.")
                                    }
                                }
                            }
                        }
                    }

                }else{
                    NaverMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState
                    )
                }
            }
        }
    }
}
    @Composable
    private fun WasteInfoRow(
        title: String,
        method: String,
        days: String,
        start: String,
        end: String
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            Text(
                text = "$title: $method",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "배출요일: $days ($start~$end)",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }

    // preview
// preview
@Preview(showBackground = true)
@Composable
fun WasteMapPreview() {
    WasteMap(
        userId = "testUser",
        navController = NavHostController(LocalContext.current)
    )
}