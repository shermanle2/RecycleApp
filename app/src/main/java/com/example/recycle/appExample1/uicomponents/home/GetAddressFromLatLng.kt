package com.example.recycle.appExample1.uicomponents.home

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

suspend fun getAddressFromLatLng(lat: Double, lng: Double): String? {
    return withContext(Dispatchers.IO) {
        try {
            val clientId = "r89ds09wik"
            val clientSecret = "VQ0m9n5QxEfpC156ixZmCzYemq1e6wNzMTxBzY2p"

            val coord = "$lng,$lat"
            Log.d("ReverseGeocoding", "Requesting address for: $coord")
            val url =
                "https://maps.apigw.ntruss.com/map-reversegeocode/v2/gc?coords=$coord&output=json&orders=legalcode,admcode,addr,roadaddr"

            val request = Request.Builder()
                .url(url)
                .get()
                .addHeader("X-NCP-APIGW-API-KEY-ID", clientId)
                .addHeader("X-NCP-APIGW-API-KEY", clientSecret)
                .build()

            val response = OkHttpClient().newCall(request).execute()

            if (response.isSuccessful) {
                val responseData = response.body?.string()
                Log.d("ReverseGeocoding", "📝 Raw Response: $responseData")

                val jsonObject = JSONObject(responseData)
                val results = jsonObject.getJSONArray("results")

                // "roadaddr" 항목만 찾아서 사용
                val roadAddrObj = (0 until results.length())
                    .map { results.getJSONObject(it) }
                    .firstOrNull { it.getString("name") == "roadaddr" }

                if (roadAddrObj != null) {
                    val region = roadAddrObj.getJSONObject("region")
                    val area1 = region.getJSONObject("area1").getString("name")
                    val area2 = region.getJSONObject("area2").getString("name")
                    val area3 = region.getJSONObject("area3").getString("name")

                    val land = roadAddrObj.getJSONObject("land")
                    val roadName = land.getString("name")

                    val fullAddress = "$area1 $area2 $area3 $roadName"
                    Log.d("ReverseGeocoding", "✅ Parsed road address: $fullAddress")
                    return@withContext fullAddress
                } else {
                    Log.w("ReverseGeocoding", "⚠️ roadaddr 항목이 응답에 없음")
                }
            } else {
                Log.e("ReverseGeocoding", "❌ HTTP Error: ${response.code}")
            }

            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}