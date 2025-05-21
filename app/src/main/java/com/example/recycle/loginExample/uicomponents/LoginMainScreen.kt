package com.example.recycle.loginExample.uicomponents

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.recycle.loginExample.navGraph.LoginNavGraph

@Composable
fun LoginMainScreen(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    LoginNavGraph(navController = navController)
}