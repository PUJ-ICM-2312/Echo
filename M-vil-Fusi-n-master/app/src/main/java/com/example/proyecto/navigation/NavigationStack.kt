package com.example.proyecto.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.proyecto.ui.screens.CameraIntentScreen
import com.example.proyecto.ui.screens.EchoScreen
import com.example.proyecto.ui.screens.LogInScreen
import com.example.proyecto.ui.screens.RegisterScreen
import com.example.proyecto.ui.screens.MapScreen
import com.example.proyecto.ui.screens.NewPostScreen
import com.example.proyecto.ui.screens.ProfileScreen
import com.example.proyecto.ui.screens.ProfileSetupScreen

@Composable
fun NavigationStack() {
    val navController = rememberNavController()
    var selectedTab by remember { mutableStateOf(0) }

    // Estado para guardar la ubicación seleccionada
    var selectedLocation by remember { mutableStateOf<String?>(null) }
    var selectedLatitude by remember { mutableStateOf<Double?>(null) }
    var selectedLongitude by remember { mutableStateOf<Double?>(null) }

    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(route = Screen.Login.route) {
            LogInScreen(navController = navController)
        }
        composable(route = Screen.Register.route) {
            RegisterScreen(navController = navController)
        }
        composable(route = Screen.Home.route) {
            when (selectedTab) {
                0 -> MapScreen(
                    onTabSelected = { selectedTab = it },
                    navController = navController,
                    location = selectedLocation,
                    latitude = selectedLatitude,
                    longitude = selectedLongitude
                )
                1 -> EchoScreen(
                    onTabSelected = { selectedTab = it },
                    navController = navController,
                    // Cuando toques un card, cambiamos la ubicación seleccionada
                    onPlaceSelected = { location, latitude, longitude ->
                        selectedLocation = location
                        selectedLatitude = latitude
                        selectedLongitude = longitude
                        selectedTab = 0 // Cambiar a MapTab
                    }
                )
            }
        }
        composable(route = Screen.ProfileSetup.route) {
            ProfileSetupScreen(navController = navController)
        }
        composable(route = Screen.Camera.route) {
            CameraIntentScreen(navController = navController)
        }
        composable(route = Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }
        composable("newPost/{imageUrl}") { backStackEntry ->
            val imageUrl = backStackEntry.arguments?.getString("imageUrl") ?: ""
            NewPostScreen(imageUrl, navController)
        }

    }
}



sealed class Screen(val route: String) {
    object Login : Screen("login_screen")
    object Home : Screen("home_screen")
    object Register : Screen("register_screen")
    object ProfileSetup : Screen("profile_setup_screen")
    object Camera : Screen("camera_screen")
    object Profile: Screen("profile_screen")
    object NewPost : Screen("newPost/{imageUrl}") {
        fun createRoute(imageUrl: String) = "preview/$imageUrl"
    }

}