package com.killingpart.killingpoint.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import com.killingpart.killingpoint.ui.component.AppBackground
import com.killingpart.killingpoint.ui.screen.HomeScreen.HelloScreen
import com.killingpart.killingpoint.ui.screen.MainScreen.MainScreen
import com.killingpart.killingpoint.ui.screen.AddMusicScreen.AddMusicScreen
import com.killingpart.killingpoint.ui.screen.WriteDiaryScreen.WriteDiaryScreen
import androidx.navigation.navArgument
import androidx.navigation.NavType
import java.net.URLDecoder

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

        composable(
            route = "write_diary" +
                    "?title={title}" +
                    "&artist={artist}" +
                    "&image={image}" +
                    "&duration={duration}" +
                    "&start={start}" +
                    "&end={end}",
            arguments = listOf(
                navArgument("title") { type = NavType.StringType; defaultValue = "" },
                navArgument("artist") { type = NavType.StringType; defaultValue = "" },
                navArgument("image") { type = NavType.StringType; defaultValue = "" },
                navArgument("duration") { type = NavType.StringType; defaultValue = "" },
                navArgument("start") { type = NavType.StringType; defaultValue = "" },
                navArgument("end") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val title = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("title").orEmpty(), "UTF-8")
            val artist = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("artist").orEmpty(), "UTF-8")
            val image = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("image").orEmpty(), "UTF-8")
            val duration = URLDecoder.decode(backStackEntry.arguments?.getString("duration").orEmpty(), "UTF-8")
            val start = URLDecoder.decode(backStackEntry.arguments?.getString("start").orEmpty(), "UTF-8")
            val end = URLDecoder.decode(backStackEntry.arguments?.getString("end").orEmpty(), "UTF-8")

            WriteDiaryScreen(navController, title, artist, image, duration, start, end)
        }
    }
}
