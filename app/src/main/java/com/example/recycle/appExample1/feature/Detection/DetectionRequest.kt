<<<<<<<< HEAD:app/src/main/java/com/example/recycle/appExample1/feature/Detection/DetectionRequest.kt
package com.example.recycle.appExample1.feature.Detection
========
package com.example.recycle.appExample1.uicomponents.detection
>>>>>>>> feature/community:app/src/main/java/com/example/recycle/appExample1/uicomponents/detection/DetectionRequest.kt

import android.content.Context
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color

fun bitmapToFile(context: Context, bitmap: Bitmap): File {
    val file = File.createTempFile("upload_", ".jpg", context.cacheDir)
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
    }
    return file
}

fun postBitmapForDetection(
    context: Context,
    bitmap: Bitmap,
    onResult: (DetectionResult?) -> Unit
) {
    val file = bitmapToFile(context, bitmap)
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = ApiRepository.uploadImageForDetectionSync(file)
            CoroutineScope(Dispatchers.Main).launch {
                onResult(response)
            }
        } finally {
            file.delete()
        }
    }
}

fun drawBoundingBoxOnBitmap(
    bitmap: Bitmap,
    bbox: List<Int>,
    tag: String? = null,
    strokeWidth: Float = 5f
): Bitmap {
    // 원본 비트맵을 변경하지 않기 위해 복사본 생성
    val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(mutableBitmap)

//    이름을 해쉬값으로 색상 결정
    val color = Color.rgb(
        (tag?.hashCode() ?: 0) and 0xFF,
        (tag?.hashCode()?.shr(8) ?: 0) and 0xFF,
        (tag?.hashCode()?.shr(16) ?: 0) and 0xFF
    )

    val paint = Paint().apply {
        this.color = color
        style = Paint.Style.STROKE
        this.strokeWidth = strokeWidth
    }

    if (bbox.size == 4) {
        val (left, top, right, bottom) = bbox
        canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), paint)
//        태그도 추가
        if (tag != null) {
            paint.textSize = 80f
            paint.color = Color.WHITE
            canvas.drawText(tag, left.toFloat(), top.toFloat() - 10, paint)
        }
    } else {
        throw IllegalArgumentException("Bounding box must contain exactly 4 values: [left, top, right, bottom]")
    }

    return mutableBitmap
}