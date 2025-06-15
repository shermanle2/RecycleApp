package com.example.recycle.appExample1.model

sealed class Routes(val route: String) {
    object Start : Routes("start")
    object Login : Routes("login")
    object Register : Routes("register")
    object Main : Routes("main")
    object Recycling : Routes("recycling")
    object WasteMap : Routes("waste_map")
    object Community : Routes("community")
    object PostDetail : Routes("post_detail")
    object CreatePost : Routes("create_post")
    object User : Routes("user")
}