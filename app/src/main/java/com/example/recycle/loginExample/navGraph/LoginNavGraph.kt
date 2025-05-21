package com.example.recycle.loginExample.navGraph

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.recycle.loginExample.model.Routes
import com.example.recycle.loginExample.uicomponents.LoginScreen
import com.example.recycle.loginExample.uicomponents.WelcomeScreen
import com.example.recycle.loginExample.uicomponents.Register

@Composable
fun LoginNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.Login.route) {
        composable(route = Routes.Login.route) {
            LoginScreen(
                onWelcomeNavigate = { userID ->
                    navController.navigate(Routes.Welcome.route + "/$userID")
                },
                onRegisterNavigate = { userID, userPasswd ->
                    if(userID.isNotEmpty() && userPasswd.isNotEmpty())
                        navController.navigate(Routes.Register.route + "?userID=$userID&passWD=$userPasswd")
                    else
                        navController.navigate(Routes.Register.route)
                }
            )
        }

        composable(
            route = Routes.Welcome.route + "/{userID}",
            arguments = listOf(
                navArgument("userID") {
                    type = NavType.StringType
                }
            )
        ) {
            WelcomeScreen(
                it.arguments?.getString("userID") // Get the userID from the arguments
            )
        }

        composable(
            route = Routes.Register.route + "?userID={userID}&passWD={passWD}",
            arguments = listOf(
                navArgument("userID") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("passWD") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) {
            Register(
                it.arguments?.getString("userID") ?: "",
                it.arguments?.getString("passWD") ?: "",
            )
        }
    }
}