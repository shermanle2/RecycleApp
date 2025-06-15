package com.example.recycle.appExample1.feature.Detection

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface MyApiService {
    @Multipart
    @POST("/detect/")
    fun uploadImage(
        @Part image: MultipartBody.Part
    ): Call<DetectionResult>
}