package com.example.recycle.appExample1.uicomponents.Detection

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
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
}