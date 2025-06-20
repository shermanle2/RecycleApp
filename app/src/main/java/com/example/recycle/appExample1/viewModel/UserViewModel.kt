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
        val data = mutableMapOf<String, Float>()
        Log.d(TAG, "Loading stats for user: $userId")
        db.collection("posts")
            .whereEqualTo("author", userId)
            .whereEqualTo("category", "PROOF")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val postCount = querySnapshot.size()
                val currentData = _stats.value.toMutableMap()
                currentData["myProofPosts"] = postCount.toFloat()
                _stats.value = currentData

                var postsWithComments = 0
                var postsWithoutComments = 0
                Log.d(TAG, "총 게시물 수: $postCount")
                if (postCount > 0) {
                    // 각 게시물에 대해 댓글 확인
                    querySnapshot.documents.forEach { document ->
                        val postId = document.id

                        // 게시물 내의 comments 서브컬렉션 확인
                        db.collection("posts").document(postId).collection("comments")
                            .limit(1) // 댓글 존재 여부만 확인하면 되므로 1개만 가져옴
                            .get()
                            .addOnSuccessListener { commentSnapshot ->
                                if (commentSnapshot.isEmpty) {
                                    postsWithoutComments++
                                } else {
                                    postsWithComments++
                                }

                                // 모든 게시물 확인 완료 시 user 문서 업데이트
                                if (postsWithComments + postsWithoutComments == postCount) {
                                    val updates = hashMapOf<String, Any>(
                                        "totalSuccess" to postsWithComments,
                                        "totalFail" to postsWithoutComments
                                    )

                                    db.collection("users").document(userId)
                                        .set(updates, SetOptions.merge())
                                        .addOnSuccessListener {
                                            Log.d(TAG, "성공/실패 카운트 업데이트: $updates")

                                            // 업데이트 완료 후 최신 데이터 가져오기
                                            db.collection("users").document(userId).get()
                                                .addOnSuccessListener { doc ->
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
                                        .addOnFailureListener { e ->
                                            Log.w(TAG, "성공/실패 카운트 업데이트 실패", e)
                                        }
                                }
                            }
                    }
                } else {
                    // 게시물이 없는 경우에도 users 문서 데이터 가져오기
                    db.collection("users").document(userId).get()
                        .addOnSuccessListener { doc ->
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