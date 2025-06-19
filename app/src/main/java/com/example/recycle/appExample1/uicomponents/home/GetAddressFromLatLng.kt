package com.example.recycle.appExample1.uicomponents.home

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

fun getAddressFromLatLng(lat: Double, lng: Double): String? {
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
            val items = (0 until results.length()).map { results.getJSONObject(it) }

// 우선순위: roadaddr → addr → admcode
            val target = items.firstOrNull { it.getString("name") == "roadaddr" }
                ?: items.firstOrNull { it.getString("name") == "addr" }
                ?: items.firstOrNull { it.getString("name") == "admcode" }

            return if (target != null) {
                val region = target.getJSONObject("region")
                val area1 = region.getJSONObject("area1").getString("name")
                val area2 = region.getJSONObject("area2").getString("name")
                val area3 = region.getJSONObject("area3").getString("name")
                // roadaddr나 addr에 추가 정보가 필요하면 target에서 꺼내서 합치면 됩니다.
                "$area1 $area2 $area3"
            } else {
                Log.w("ReverseGeocoding", "⚠️ roadaddr/addr/admcode 항목 없음")
                null
            }
        } else {
            Log.e("ReverseGeocoding", "❌ HTTP Error: ${response.code}")
        }

        return null
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}