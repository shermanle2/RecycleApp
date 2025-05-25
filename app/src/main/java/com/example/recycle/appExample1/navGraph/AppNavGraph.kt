package com.example.recycle.appExample1.navGraph

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.recycle.appExample1.model.Routes
import com.example.recycle.appExample1.uicomponents.auth.AppStart
import com.example.recycle.appExample1.uicomponents.auth.Login
import com.example.recycle.appExample1.uicomponents.auth.Register
import com.example.recycle.appExample1.uicomponents.auth.GoogleLogin
import com.example.recycle.appExample1.uicomponents.home.AppMain
import com.example.recycle.appExample1.uicomponents.home.Recycling
import com.example.recycle.appExample1.uicomponents.home.WasteMap
import com.example.recycle.appExample1.uicomponents.home.Community
import com.example.recycle.appExample1.uicomponents.home.User
import com.example.recycle.appExample1.uicomponents.home.post.Article


@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.Start.route) {

        composable(Routes.Start.route) {
            AppStart(
                onLogin = { navController.navigate(Routes.Login.route) },
                onRegister = { navController.navigate(Routes.Register.route) },
                onGuest = { navController.navigate(Routes.Main.route + "/") }
            )
        }

        composable(Routes.Login.route) {
            Login(
                onLoginSuccess = { userId -> navController.navigate("${Routes.Main.route}/$userId") },
                onGoogleLogin = { navController.navigate(Routes.GoogleLogin.route) }
            )
        }

        composable(Routes.GoogleLogin.route) {
            GoogleLogin()
        }

        composable(Routes.Register.route) {
            Register(
                onRegisterSuccess = {
                    navController.navigate("${Routes.Login.route}/")
                }
            )
        }

        composable(
            route = "${Routes.Main.route}/{userID}",
            arguments = listOf(navArgument("userID") {
                type = NavType.StringType
                defaultValue = ""
            })
        ) {
            AppMain(
                userId = it.arguments?.getString("userID") ?: "",
                navController = navController
            )
        }

        composable(
            route = "${Routes.Recycling.route}/{userID}",
            arguments = listOf(navArgument("userID") {
                type = NavType.StringType
                defaultValue = ""
            })
        ) {
            Recycling(
                userId = it.arguments?.getString("userID") ?: "",
                navController = navController
            )
        }

        composable(
            route = "${Routes.WasteMap.route}/{userID}",
            arguments = listOf(navArgument("userID") {
                type = NavType.StringType
                defaultValue = ""
            })
        ) {
            WasteMap(
                userId = it.arguments?.getString("userID") ?: "",
                navController = navController
            )
        }

        composable(
            route = "${Routes.Community.route}/{userID}",
            arguments = listOf(navArgument("userID") {
                type = NavType.StringType
                defaultValue = ""
            })
        ) {
            Community(
                userId = it.arguments?.getString("userID") ?: "",
                navController = navController
            )
        }

        composable(
            route = "${Routes.User.route}/{userID}",
            arguments = listOf(navArgument("userID") {
                type = NavType.StringType
                defaultValue = ""
            })
        ) {
            User(
                userId = it.arguments?.getString("userID") ?: "",
                navController = navController
            )
        }

        composable(
            route = "${Routes.Article.route}/{title}/{author}/{date}/{content}",
            arguments = listOf(
                navArgument("title") { type = NavType.StringType },
                navArgument("author") { type = NavType.StringType },
                navArgument("date") { type = NavType.StringType },
                navArgument("content") { type = NavType.StringType }
            )
        ) {
            val title = it.arguments?.getString("title") ?: ""
            val author = it.arguments?.getString("author") ?: ""
            val date = it.arguments?.getString("date") ?: ""
            val content = it.arguments?.getString("content") ?: ""

            Article(
                title = title,
                author = author,
                date = date,
                content = content,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}