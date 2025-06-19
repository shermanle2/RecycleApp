package com.example.recycle.appExample1.feature.Detection

import android.util.Log
import com.example.recycle.appExample1.model.RecycleLocation
import com.google.gson.reflect.TypeToken
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import java.io.File

object ApiRepository {
    private fun imageFileToMultipart(file: File): MultipartBody.Part {
        val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
        return MultipartBody.Part.createFormData("image", file.name, requestFile)
    }

    fun uploadImageForDetectionSync(
        imageFile: File
    ): DetectionResult? {
        return try {
            val imagePart = imageFileToMultipart(imageFile)
            val response = RetrofitClient.api.uploadImage(imagePart).execute()
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun fetchLocationsSync(address: String): List<RecycleLocation>? {
        return try {
            val call: Call<RecycleLocationResponse> = RetrofitClient.api.getLocations(address)
            val response: Response<RecycleLocationResponse> = call.execute()
            if (response.isSuccessful) {
                response.body()?.result
            } else {
                Log.e("ApiRepository", "fetchLocationsSync HTTP:${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e("ApiRepository", "fetchLocationsSync 실패", e)
            null
        }
    }
}