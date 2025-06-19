package com.example.recycle.appExample1.feature.Detection

import com.example.recycle.appExample1.model.RecycleLocation
import com.google.gson.annotations.SerializedName

data class RecycleLocationResponse(
    @SerializedName("result") val result: List<RecycleLocation>
)