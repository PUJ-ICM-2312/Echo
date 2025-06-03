package com.example.proyecto.navigation

import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.proyecto.ui.components.SharedScaffold
import com.example.proyecto.ui.screens.CameraIntentScreen
import com.example.proyecto.ui.screens.EchoScreen
import com.example.proyecto.ui.screens.FriendProfileScreen
import com.example.proyecto.ui.screens.FriendRequestScreen
import com.example.proyecto.ui.screens.LogInScreen
import com.example.proyecto.ui.screens.RegisterScreen
import com.example.proyecto.ui.screens.MapScreen
import com.example.proyecto.ui.screens.NewPostScreen
import com.example.proyecto.ui.screens.ProfileScreen
import com.example.proyecto.ui.screens.ProfileSetupScreen
import com.example.proyecto.ui.screens.SplashScreen
import com.example.proyecto.ui.screens.chat.ChatScreen
import com.example.proyecto.ui.screens.chat.DirectChatScreen
import com.example.proyecto.ui.screens.chat.FriendListScreen

@Composable
fun NavigationStack() {
    val navController = rememberNavController()
    var selectedTab by remember { mutableStateOf(0) }

    // Estado para guardar la ubicación seleccionada
    var selectedLocation by remember { mutableStateOf<String?>(null) }
    var selectedLatitude by remember { mutableStateOf<Double?>(null) }
    var selectedLongitude by remember { mutableStateOf<Double?>(null) }

    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(route = Screen.Splash.route) {
            SplashScreen(navController = navController)
        }
        composable(route = Screen.Login.route) {
            LogInScreen(navController = navController)
        }
        composable(route = Screen.Register.route) {
            RegisterScreen(navController = navController)
        }
        composable(route = Screen.Home.route) {
            Crossfade(targetState = selectedTab) { tab ->
                when (tab) {
                    0 -> key("map_screen") {
                        MapScreen(
                            onTabSelected = { selectedTab = it },
                            navController = navController,
                            location = selectedLocation,
                            latitude = selectedLatitude,
                            longitude = selectedLongitude
                        )
                    }
                    1 -> key("echo_screen") {
                        EchoScreen(
                            onTabSelected = { selectedTab = it },
                            navController = navController,
                            onPlaceSelected = { location, latitude, longitude ->
                                selectedLocation = location
                                selectedLatitude = latitude
                                selectedLongitude = longitude
                                selectedTab = 0
                            }
                        )
                    }
                }
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

        composable(route = Screen.Chat.route) {

            ChatScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(route = Screen.FriendList.route) {

            SharedScaffold(navController, selectedTab = null) {
                FriendListScreen(navController)
            }
        }
        composable(
            route = Screen.FriendProfile.route,
            arguments = listOf(
                navArgument("friendUid") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val friendUid = backStackEntry.arguments?.getString("friendUid") ?: ""
            FriendProfileScreen(friendUid = friendUid, navController = navController)
        }

        composable(
            route = Screen.DirectChat.routeWithArg,
            arguments = listOf(navArgument("friendUid") {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val friendUid = backStackEntry.arguments?.getString("friendUid") ?: ""
            DirectChatScreen(
                friendUid = friendUid,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route= Screen.FriendRequest.route) {
            FriendRequestScreen(navController = navController)
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
    object Chat : Screen("chat_screen")
    object FriendList     : Screen("friend_list_screen")
    object DirectChat     : Screen("direct_chat") {
        const val routeWithArg = "direct_chat/{friendUid}"
        fun createRoute(friendUid: String) = "direct_chat/$friendUid"
    }
    object FriendProfile : Screen("friend_profile/{friendUid}") {
        fun createRoute(friendUid: String) = "friend_profile/$friendUid"
    }
    object Splash : Screen("splash_screen")
    object FriendRequest : Screen("friend_requests")

}