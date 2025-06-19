package com.example.recycle.appExample1.uicomponents.home

import android.util.Log
import com.naver.maps.geometry.LatLng
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder

suspend fun searchLocation(query: String): LatLng? {
    return try {
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

        Log.d("GeocodingRaw", "response=$json")

        val obj = JSONObject(json ?: "")
        if (obj.has("error")) {
            val error = obj.getJSONObject("error")
            Log.e("GeocodingError", "code=${error.getString("errorCode")}, msg=${error.getString("message")}")
            return null
        }

        val addresses = obj.getJSONArray("addresses")
        if (addresses.length() == 0) return null

        val first = addresses.getJSONObject(0)
        val x = first.getDouble("x")
        val y = first.getDouble("y")
        Log.d("AddressLatLng", "Address: $query → lat=$x, lng=$y")
        LatLng(y, x)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

suspend fun searchKeywordLocation(query: String): LatLng? {
    return try {
        val encoded = URLEncoder.encode(query, "UTF-8")
        val clientId = "wiNHlWjJsYDlfj_S0SWj"
        val clientSecret = "lnIdq7f2g6"
        val url = "https://openapi.naver.com/v1/search/local.json?query=$encoded&display=1"

        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("X-Naver-Client-Id", clientId)
            .addHeader("X-Naver-Client-Secret", clientSecret)
            .build()

        val response = OkHttpClient().newCall(request).execute()

        if (response.isSuccessful) {
            val responseData = response.body?.string()
            val json = JSONObject(responseData)
            val items = json.getJSONArray("items")
            if (items.length() > 0) {
                val item = items.getJSONObject(0)

                Log.d("KeywordLatLng", "원본 item: $item") // ✅ 아이템 전체 구조 확인
                val rawMapx = item.getString("mapx")
                val rawMapy = item.getString("mapy")
                Log.d("KeywordLatLng", "Raw mapx=$rawMapx, mapy=$rawMapy") // ✅ 원시 TM128 좌표 확인

                val lng = rawMapx.toDouble() / 1e7
                val lat = rawMapy.toDouble() / 1e7
                Log.d("KeywordLatLng", "Converted lat=$lat, lng=$lng")

                return LatLng(lat, lng)
            } else {
                null
            }
        } else {
            null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}