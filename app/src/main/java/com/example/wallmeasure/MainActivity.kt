package com.example.wallmeasure

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.wallmeasure.navigation.Screen
import com.example.wallmeasure.ui.screens.AboutScreen
import com.example.wallmeasure.ui.screens.HistoryScreen
import com.example.wallmeasure.ui.screens.HomeScreen
import com.example.wallmeasure.ui.screens.MultipleWallsScreen
import com.example.wallmeasure.ui.screens.OpeningsScreen
import com.example.wallmeasure.ui.screens.SettingsScreen
import com.example.wallmeasure.ui.screens.SplashScreen
import com.example.wallmeasure.ui.theme.WallMeasureTheme
import com.example.wallmeasure.viewmodel.WallMeasureViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: WallMeasureViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()

            WallMeasureTheme(
                themeSetting = uiState.settings.theme,
                dynamicColor = uiState.settings.dynamicColor
            ) {
                WallMeasureApp(
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun WallMeasureApp(
    viewModel: WallMeasureViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier.fillMaxSize()
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                uiState = uiState,
                onNavigateToOpenings = { navController.navigate(Screen.Openings.route) },
                onNavigateToMultipleWalls = { navController.navigate(Screen.MultipleWalls.route) },
                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToAbout = { navController.navigate(Screen.About.route) }
            )
        }

        composable(Screen.Openings.route) {
            OpeningsScreen(
                viewModel = viewModel,
                uiState = uiState,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.MultipleWalls.route) {
            MultipleWallsScreen(
                viewModel = viewModel,
                uiState = uiState,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                viewModel = viewModel,
                uiState = uiState,
                onNavigateBack = { navController.popBackStack() },
                onRestoreToHome = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = viewModel,
                uiState = uiState,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.About.route) {
            AboutScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
