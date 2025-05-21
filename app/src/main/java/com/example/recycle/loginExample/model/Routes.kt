package com.example.recycle.loginExample.model

sealed class Routes(val route: String) {
    object Login : Routes("login")
    object Register : Routes("register")
    object Welcome : Routes("welcome")
}