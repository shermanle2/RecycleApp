package com.example.recycle.appExample1.uicomponents.home

import android.util.Log
import com.naver.maps.geometry.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
        LatLng(y, x)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}