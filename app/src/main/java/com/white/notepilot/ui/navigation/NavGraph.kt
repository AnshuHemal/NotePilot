package com.white.notepilot.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.white.notepilot.ui.screens.AccountScreen
import com.white.notepilot.ui.screens.CategoryManagementScreen
import com.white.notepilot.ui.screens.CreateNoteScreen
import com.white.notepilot.ui.screens.HomeScreen
import com.white.notepilot.ui.screens.ImageSyncTestScreen
import com.white.notepilot.ui.screens.LoginScreen
import com.white.notepilot.ui.screens.NoteDetailScreen
import com.white.notepilot.ui.screens.NotificationsScreen
import com.white.notepilot.ui.screens.SearchNoteScreen
import com.white.notepilot.ui.screens.SettingsScreen
import com.white.notepilot.ui.screens.SplashScreen
import com.white.notepilot.ui.screens.UnsyncedNotesScreen

@Composable
fun SetupNavGraph(navController: NavHostController) {

    NavHost(navController = navController, startDestination = Routes.Splash.route) {
        composable(route = Routes.Splash.route) {
            SplashScreen(navController = navController)
        }
        composable(route = Routes.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(route = Routes.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(route = Routes.Notifications.route) {
            NotificationsScreen(navController = navController)
        }
        composable(route = Routes.Account.route) {
            AccountScreen(navController = navController)
        }
        composable(route = Routes.Settings.route) {
            SettingsScreen(navController = navController)
        }
        composable(
            route = Routes.CreateNote.route,
            arguments = listOf(navArgument("noteId") { 
                type = NavType.IntType
                defaultValue = -1
            })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getInt("noteId") ?: -1
            CreateNoteScreen(navController = navController, noteId = if (noteId == -1) null else noteId)
        }
        composable(
            route = Routes.NoteDetail.route,
            arguments = listOf(navArgument("noteId") { type = NavType.IntType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getInt("noteId") ?: 0
            NoteDetailScreen(navController = navController, noteId = noteId)
        }
        composable(route = Routes.SearchNote.route) {
            SearchNoteScreen(navController = navController)
        }
        composable(route = Routes.UnsyncedNotes.route) {
            UnsyncedNotesScreen(navController = navController)
        }
        composable(route = Routes.CategoryManagement.route) {
            CategoryManagementScreen(navController = navController)
        }
        composable(route = Routes.ImageSyncTest.route) {
            ImageSyncTestScreen(navController = navController)
        }
    }
}