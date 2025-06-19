package com.example.recycle.appExample1.viewModel

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UserViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _stats = MutableStateFlow<Map<String, Float>>(emptyMap())
    val stats: StateFlow<Map<String, Float>> = _stats

    fun loadStats(userId: String) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                val data = mutableMapOf<String, Float>()
                doc.getLong("totalSuccess")?.toFloat()?.let { data["totalSuccess"] = it }
                doc.getLong("totalFail")?.toFloat()?.let { data["totalFail"] = it }

                val recycledMap = doc.get("recycledItemCount") as? Map<*, *>
                recycledMap?.forEach { (k, v) ->
                    if (k is String && v is Number) {
                        data[k] = v.toFloat()
                    }
                }

                _stats.value = data
            }
    }

//    fun updateStats(userId: String, material: String, success: Boolean) {
//        val updates = hashMapOf<String, Any>()
//
//        updates["recycledItemCount.$material"] = FieldValue.increment(1)
//        if (success) updates["totalSuccess"] = FieldValue.increment(1)
//        else updates["totalFail"] = FieldValue.increment(1)
//
//        db.collection("users").document(userId).set(updates, SetOptions.merge())
//    }

    fun updateStats(userId: String, material: String, success: Boolean) {
        // 1) recycledItemCount 맵 안에 들어갈 단일 엔트리 생성
        val materialEntry = mapOf(material to FieldValue.increment(1))

        // 2) 최종 업데이트할 필드 맵 구성
        val updates = hashMapOf<String, Any>(
            // recycledItemCount 필드에 materialEntry 맵을 병합
            "recycledItemCount" to materialEntry
        )
        // 성공/실패 카운터 추가
        if (success) {
            updates["totalSuccess"] = FieldValue.increment(1)
        } else {
            updates["totalFail"] = FieldValue.increment(1)
        }

        // 3) set + merge 로 한 번에 반영
        db.collection("users")
            .document(userId)
            .set(updates, SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "Stats updated for $userId")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Failed to update stats for $userId", e)
            }
    }
}