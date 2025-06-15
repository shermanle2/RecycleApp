<<<<<<<< HEAD:app/src/main/java/com/example/recycle/appExample1/feature/Detection/DetectionResult.kt
package com.example.recycle.appExample1.feature.Detection
========
package com.example.recycle.appExample1.uicomponents.detection
>>>>>>>> feature/community:app/src/main/java/com/example/recycle/appExample1/uicomponents/detection/DetectionResult.kt

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