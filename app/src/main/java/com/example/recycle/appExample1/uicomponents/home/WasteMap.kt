package com.example.recycle.appExample1.uicomponents.home

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
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
    //var searchMarkerPosition by remember { mutableStateOf<LatLng?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var searchTarget by remember { mutableStateOf<LatLng?>(null) }

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

    fun searchLocation(query: String, onResult: (LatLng?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val encoded = URLEncoder.encode(query, "UTF-8")
                val clientId = "r89ds09wik"
                val clientSecret = "VQ0m9n5QxEfpC156ixZmCzYemq1e6wNzMTxBzY2p"
                val url = "https://maps.apigw.ntruss.com/map-geocode/v2/geocode?query=$encoded"

                val request = Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("X-NCP-APIGW-API-KEY-ID", clientId)
                    .addHeader("X-NCP-APIGW-API-KEY", clientSecret)
                    .build()

                val response = OkHttpClient().newCall(request).execute()
                val json = response.body?.string()

                Log.d("GeocodingRaw", "clientId=$clientId, clientSecret=$clientSecret, response=$json")

                val obj = JSONObject(json ?: "")
                if (obj.has("error")) {
                    val error = obj.getJSONObject("error")
                    Log.e("GeocodingError", "code=${error.getString("errorCode")}, msg=${error.getString("message")}")
                    withContext(Dispatchers.Main) { onResult(null) }
                    return@launch
                }

                val addresses = obj.getJSONArray("addresses")
                if (addresses.length() == 0) {
                    withContext(Dispatchers.Main) {
                        onResult(null)
                    }
                    return@launch
                }

                val first = addresses.getJSONObject(0)
                val x = first.getDouble("x")
                val y = first.getDouble("y")
                val latLng = LatLng(y, x)

                withContext(Dispatchers.Main) {
                    onResult(latLng)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onResult(null)
                }
            }
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
                            searchLocation(searchQuery) { latLng ->
                                if (latLng != null) {
                                    searchTarget = latLng
                                } else {
                                    Toast.makeText(context, "검색 결과 없음", Toast.LENGTH_SHORT).show()
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
                                captionText = "현 위치"
                            )
                        }
                        if (searchTarget != null) {
                            Marker(
                                state = searchMarkerState,
                                captionText = "검색 위치"
                            )
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

// preview
@Preview(showBackground = true)
@Composable
fun WasteMapPreview() {
    WasteMap(
        userId = "testUser",
        navController = NavHostController(LocalContext.current)
    )
}