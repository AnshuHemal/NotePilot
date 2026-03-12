package com.white.notepilot

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.white.notepilot.ui.components.AnimatedBottomBar
import com.white.notepilot.ui.components.ForceUpdateBottomSheet
import com.white.notepilot.ui.components.ads.InterstitialAdManager
import com.white.notepilot.ui.components.ads.NavigationTracker
import com.white.notepilot.ui.navigation.Routes
import com.white.notepilot.ui.navigation.SetupNavGraph
import com.white.notepilot.ui.theme.NotesTheme
import com.white.notepilot.utils.NotificationHelper
import com.white.notepilot.viewmodel.NotificationViewModel
import com.white.notepilot.viewmodel.ThemeViewModel
import com.white.notepilot.viewmodel.UpdateViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "MainActivity"
        var onPermissionResult: ((Boolean) -> Unit)? = null
    }
    
    private lateinit var interstitialAdManager: InterstitialAdManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        interstitialAdManager = InterstitialAdManager(this)
        
        NotificationHelper.getFCMToken { token ->
            Log.d(TAG, "FCM Token received: $token")
        }
        
        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val isDarkMode by themeViewModel.isDarkMode.collectAsState()
            
            NotesTheme(darkTheme = isDarkMode) {
                MainScreen(interstitialAdManager = interstitialAdManager)
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (::interstitialAdManager.isInitialized) {
            interstitialAdManager.destroy()
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NotificationHelper.NOTIFICATION_PERMISSION_REQUEST_CODE) {
            val isGranted = grantResults.isNotEmpty() && 
                           grantResults[0] == PackageManager.PERMISSION_GRANTED
            onPermissionResult?.invoke(isGranted)
        }
    }
}

@Composable
fun MainScreen(interstitialAdManager: InterstitialAdManager) {
    val context = LocalContext.current
    val activity = context as ComponentActivity
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val notificationViewModel: NotificationViewModel = hiltViewModel()
    val unreadCount by notificationViewModel.unreadCount.collectAsState()
    val hasUnreadNotifications = unreadCount > 0
    
    val updateViewModel: UpdateViewModel = hiltViewModel()
    val showUpdateDialog by updateViewModel.showUpdateDialog.collectAsState()
    val isForceUpdate by updateViewModel.isForceUpdate.collectAsState()
    val updateInfo by updateViewModel.updateInfo.collectAsState()

    val shouldShowInterstitial by NavigationTracker.shouldShowInterstitial.collectAsState()
    val isAdReady by interstitialAdManager.isAdReady.collectAsState()
    
    LaunchedEffect(shouldShowInterstitial, isAdReady) {
        Log.d("MainActivity", "LaunchedEffect triggered - shouldShow: $shouldShowInterstitial, isReady: $isAdReady")
        if (shouldShowInterstitial && isAdReady) {
            Log.d("MainActivity", "Attempting to show interstitial ad")
            val adShown = interstitialAdManager.showAd(activity)
            Log.d("MainActivity", "Ad shown result: $adShown")
            if (adShown) {
                NavigationTracker.onAdShown()
            }
        }
    }
    
    LaunchedEffect(unreadCount) {
        Log.d("MainActivity", "Updating app badge with count: $unreadCount")
        NotificationHelper.updateAppBadge(context, unreadCount)
        
        val isSupported = NotificationHelper.isBadgeSupported(context)
        Log.d("MainActivity", "Badge supported on this launcher: $isSupported")
    }

    val bottomBarRoutes = listOf(
        Routes.Home.route,
        Routes.Notifications.route,
        Routes.Account.route,
        Routes.Settings.route
    )
    val showBottomBar = currentRoute in bottomBarRoutes
    
    val onNavigate = remember(navController) {
        { route: String ->
            NavigationTracker.trackNavigation()
            navController.navigate(route) {
                popUpTo(Routes.Home.route) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }
    
    val onFabClick = remember(navController) {
        {
            navController.navigate(Routes.CreateNote.createRoute())
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                AnimatedBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = onNavigate,
                    onFabClick = onFabClick,
                    hasUnreadNotifications = hasUnreadNotifications
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    innerPadding
                )
        ) {
            SetupNavGraph(navController = navController)
        }
    }
    
    // Force Update Bottom Sheet
    if (showUpdateDialog && updateInfo != null) {
        ForceUpdateBottomSheet(
            updateInfo = updateInfo!!,
            isForceUpdate = isForceUpdate,
            onUpdateClick = {
                // User clicked update, keep dialog open until they return
            },
            onExitClick = {
                // Exit the app
                activity.finishAffinity()
            },
            onDismiss = {
                updateViewModel.dismissUpdateDialog()
            }
        )
    }
}