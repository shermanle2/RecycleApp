package com.example.recycle.appExample1.uicomponents

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.recycle.appExample1.navGraph.AppNavGraph

@Composable
fun MainScreen1(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    AppNavGraph(navController = navController)
}