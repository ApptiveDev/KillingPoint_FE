package com.killingpart.killingpoint.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import com.killingpart.killingpoint.ui.component.AppBackground
import com.killingpart.killingpoint.ui.screen.HomeScreen.HelloScreen
import com.killingpart.killingpoint.ui.screen.MainScreen.MainScreen
import com.killingpart.killingpoint.ui.screen.AddMusicScreen.AddMusicScreen

@Composable
fun NavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") { HelloScreen(navController) }

        composable("main") {
            MainScreen(navController)
        }

        composable("add_music") {
            AddMusicScreen(navController)
        }
    }
}
