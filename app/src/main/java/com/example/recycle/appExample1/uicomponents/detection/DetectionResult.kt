package com.example.recycle.appExample1.uicomponents.detection

data class Detection(
    val label: String,
    val confidence: Double,
    val bbox: List<Int>,
    val description: String
)

data class DetectionResult(
    val inference_time_s: Double,
    val detections: List<Detection>
)