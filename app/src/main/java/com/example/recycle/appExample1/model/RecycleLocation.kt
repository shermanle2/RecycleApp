package com.example.recycle.appExample1.model

import com.google.gson.annotations.SerializedName

data class RecycleLocation(
    @SerializedName("배출장소유형") val locationType: String,
    @SerializedName("배출장소") val location: String,
    @SerializedName("생활쓰레기배출방법") val garbageMethod: String,
    @SerializedName("음식물쓰레기배출방법") val foodMethod: String,
    @SerializedName("재활용품배출방법") val recycleMethod: String,
    @SerializedName("일시적다량폐기물배출방법") val bulkMethod: String,
    @SerializedName("일시적다량폐기물배출장소") val bulkLocation: String,
    @SerializedName("생활쓰레기배출요일") val garbageDays: String,
    @SerializedName("음식물쓰레기배출요일") val foodDays: String,
    @SerializedName("재활용품배출요일") val recycleDays: String,
    @SerializedName("생활쓰레기배출시작시각") val garbageStartTime: String,
    @SerializedName("생활쓰레기배출종료시각") val garbageEndTime: String,
    @SerializedName("음식물쓰레기배출시작시각") val foodStartTime: String,
    @SerializedName("음식물쓰레기배출종료시각") val foodEndTime: String,
    @SerializedName("재활용품배출시작시각") val recycleStartTime: String,
    @SerializedName("재활용품배출종료시각") val recycleEndTime: String,
    @SerializedName("일시적다량폐기물배출시") val bulkStartTime: String,
    @SerializedName("일시적다량폐기물배출종료시") val bulkEndTime: String,
    @SerializedName("미수거일") val notCollectedDays: String,
    @SerializedName("관리부서명") val departmentName: String,
    @SerializedName("관리부서전화번호") val departmentPhone: String,
    @SerializedName("데이터기준일자") val dataDate: String
)
