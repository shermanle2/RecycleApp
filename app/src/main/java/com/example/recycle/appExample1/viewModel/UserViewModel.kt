package com.example.recycle.appExample1.viewModel

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

    fun updateStats(userId: String, material: String, success: Boolean) {
        val updates = hashMapOf<String, Any>()

        updates["recycledItemCount.$material"] = FieldValue.increment(1)
        if (success) updates["totalSuccess"] = FieldValue.increment(1)
        else updates["totalFail"] = FieldValue.increment(1)

        db.collection("users").document(userId).set(updates, SetOptions.merge())
    }
}
