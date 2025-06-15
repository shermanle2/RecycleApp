package com.example.recycle.appExample1.viewModel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.recycle.appExample1.model.Comment
import com.example.recycle.appExample1.model.CommunityPost
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class CommunityViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val postsCollection = db.collection("posts")

    private val _posts = MutableStateFlow<List<CommunityPost>>(emptyList())
    val posts: StateFlow<List<CommunityPost>> = _posts

    init {
        loadPosts()
    }

    fun loadPosts() {
        postsCollection.get().addOnSuccessListener { result ->
            val postList = result.mapNotNull { doc ->
                doc.toObject(CommunityPost::class.java).copy(id = doc.id)
            }
            _posts.value = postList
        }
    }

    fun addPost(post: CommunityPost, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val docRef = postsCollection.document()
                val postWithId = post.copy(id = docRef.id)
                docRef.set(postWithId).await()

                // 즉시 반영
                _posts.value = listOf(postWithId) + _posts.value // 최신글이 위


                onComplete()
            } catch (e: Exception) {
                println("Error adding post: ${e.message}")
            }
        }
    }

    fun deletePost(postId: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                postsCollection.document(postId).delete().await()
                _posts.value = _posts.value.filterNot { it.id == postId }
                onComplete()
            } catch (e: Exception) {
                println("Error deleting post: ${e.message}")
            }
        }
    }

    fun getPostById(id: String): CommunityPost? {
        return _posts.value.find { it.id == id }
    }

    fun uploadImagesAndAddPost(
        post: CommunityPost,
        imageUris: List<Uri>,
        context: Context,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val urls = mutableListOf<String>()
                val storage = FirebaseStorage.getInstance()

                for ((index, uri) in imageUris.withIndex()) {
                    val filename = "images/${UUID.randomUUID()}"
                    val ref = storage.reference.child(filename)
                    val stream = context.contentResolver.openInputStream(uri) ?: continue
                    ref.putStream(stream).await()
                    val url = ref.downloadUrl.await().toString()
                    urls.add(url)
                }

                val docRef = postsCollection.document()
                val fullPost = post.copy(id = docRef.id, imageUrls = urls)
                docRef.set(fullPost).await()
                _posts.value = listOf(fullPost) + _posts.value
                onComplete()
            } catch (e: Exception) {
                println("Image upload failed: ${e.message}")
            }
        }
    }


    // comment

    fun loadComments(postId: String, onResult: (List<Comment>) -> Unit) {
        db.collection("posts")
            .document(postId)
            .collection("comments")
            .get()
            .addOnSuccessListener { result ->
                val comments = result.mapNotNull { it.toObject(Comment::class.java) }
                onResult(comments)
            }
    }

    fun addComment(postId: String, comment: Comment, onComplete: () -> Unit) {
        db.collection("posts")
            .document(postId)
            .collection("comments")
            .add(comment)
            .addOnSuccessListener { onComplete() }
            .addOnFailureListener { e -> println("댓글 추가 실패: ${e.message}") }
    }


}
