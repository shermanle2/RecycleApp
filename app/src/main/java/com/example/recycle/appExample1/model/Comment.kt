package com.example.recycle.appExample1.model

data class Comment(
    val content: String = "",
    val author: String = "",
    val authorEmail: String = "",
    val timestamp: String = "",
    val isPrivate: Boolean = false
)
