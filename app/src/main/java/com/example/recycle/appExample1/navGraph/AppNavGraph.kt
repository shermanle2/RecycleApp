package com.example.recycle.communityExample.navGraph

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.recycle.appExample1.model.Routes
import com.example.recycle.appExample1.uicomponents.auth.AppStart
import com.example.recycle.appExample1.uicomponents.auth.Login
import com.example.recycle.appExample1.uicomponents.auth.Register
import com.example.recycle.appExample1.uicomponents.home.AppMain
import com.example.recycle.appExample1.uicomponents.home.Community
import com.example.recycle.appExample1.uicomponents.home.CreatePostScreen
import com.example.recycle.appExample1.uicomponents.home.PostDetailScreen
import com.example.recycle.appExample1.uicomponents.home.Recycling
import com.example.recycle.appExample1.uicomponents.home.User
import com.example.recycle.appExample1.uicomponents.home.WasteMap
import com.example.recycle.appExample1.viewModel.CommunityViewModel
import com.example.recycle.appExample1.viewModel.UserViewModel


@Composable
fun AppNavGraph(navController: NavHostController) {
    val viewModel: CommunityViewModel = viewModel()
    val userViewModel: UserViewModel = viewModel()

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
                onLoginSuccess = { userId -> navController.navigate("${Routes.Main.route}/$userId") }
            )
        }

        composable(Routes.Register.route) {
            Register(
                onRegisterSuccess = {
                    navController.navigate(Routes.Login.route) {
                        popUpTo(Routes.Register.route) { inclusive = true }
                    }
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
                navController = navController,
                viewModel = viewModel
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
                navController = navController,
                viewModel = userViewModel
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
                navController = navController,
                viewModel = viewModel
            )
        }

        composable(
            route = "${Routes.PostDetail.route}/{postId}/{userID}?infoOnly={infoOnly}",
            arguments = listOf(
                navArgument("postId") { type = NavType.StringType },
                navArgument("userID") { type = NavType.StringType },
                navArgument("infoOnly") {
                    type = NavType.StringType  // ⛳ 바꿔야 함!
                    defaultValue = "false"
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: ""
            val userId = backStackEntry.arguments?.getString("userID") ?: ""
            val infoOnlyString = backStackEntry.arguments?.getString("infoOnly") ?: "false"
            val infoOnly = infoOnlyString.toBooleanStrictOrNull() ?: false  // ⛳ String → Boolean 변환

            PostDetailScreen(
                postId = postId,
                posts = viewModel.posts.collectAsState().value,
                onBackClick = { navController.popBackStack() },
                currentUserId = userId,
                viewModel = viewModel,
                infoOnly = infoOnly
            )
        }

        composable(
            route = "${Routes.CreatePost.route}/{userID}",
            arguments = listOf(navArgument("userID") { type = NavType.StringType })
        ) {
            val userId = it.arguments?.getString("userID") ?: ""
            CreatePostScreen(
                userId = userId,
                navController = navController,
                viewModel = viewModel
            )
        }

        composable(
            route = "${Routes.User.route}/{userID}",
            arguments = listOf(navArgument("userID") {
                type = NavType.StringType
                defaultValue = ""
            })
        ) {
            val userId = it.arguments?.getString("userID") ?: ""
            User(
                userId = userId,
                navController = navController,
                viewModel = userViewModel
            )
        }
    }
}