<<<<<<<< HEAD:app/src/main/java/com/example/recycle/appExample1/feature/Detection/MyApiService.kt
package com.example.recycle.appExample1.feature.Detection
========
package com.example.recycle.appExample1.uicomponents.detection
>>>>>>>> feature/community:app/src/main/java/com/example/recycle/appExample1/uicomponents/detection/MyApiService.kt

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